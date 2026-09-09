import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { JasperEditorComponent } from '@florianrauscha/ngx-jrxml-editor';

type BindingKind = 'P' | 'F';
interface BindingItem { kind: BindingKind; name: string; label: string; }
interface TemplatePayload {
  name: string;
  paperWidth: number;
  jrxml: string;
  hasLiveData: boolean;
  draftVersion: number;
  publishedVersion: number;
  hasUnpublishedChanges: boolean;
}
interface StatePayload {
  message: string;
  jrxml: string;
  draftVersion: number;
  publishedVersion: number;
  hasUnpublishedChanges: boolean;
}
interface VersionItem { versionNo: number; publishedAt: string; publishedBy?: string; }

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [JasperEditorComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header>
      <div class="identity">
        <strong>THIẾT KẾ BILL</strong>
        <span>{{ templateName() }} · {{ paperWidth() }} mm</span>
        <span class="version" [class.dirty]="dirty()">
          Nháp v{{ draftVersion() || 'mới' }} · Đã xuất bản v{{ publishedVersion() || 'chưa có' }}
        </span>
        <details class="dictionary">
          <summary>Từ điển dữ liệu</summary>
          <div class="dictionary-panel">
            <p>Kéo dữ liệu vào band phù hợp. Tham số dùng cho đầu/cuối bill; field dùng trong detail.</p>
            <h4>Thông tin hóa đơn</h4>
            <div class="binding-grid">
              @for (item of parameterBindings; track item.name) {
                <div draggable="true" (dragstart)="dragBinding($event, item)" [title]="'$P{' + item.name + '}'">
                  <b>{{ item.label }}</b><code>{{ bindingExpression(item) }}</code>
                </div>
              }
            </div>
            <h4>Chi tiết mặt hàng</h4>
            <div class="binding-grid">
              @for (item of fieldBindings; track item.name) {
                <div draggable="true" (dragstart)="dragBinding($event, item)" [title]="'$F{' + item.name + '}'">
                  <b>{{ item.label }}</b><code>{{ bindingExpression(item) }}</code>
                </div>
              }
            </div>
          </div>
        </details>
      </div>
      <div class="actions">
        <span class="status" [class.error]="statusError()">{{ status() }}</span>
        <button type="button" (click)="preview()" [disabled]="busy() || !jrxml()">
          {{ hasLiveData() ? 'Preview hóa đơn' : 'Preview mẫu' }}
        </button>
        <button type="button" (click)="openVersions()" [disabled]="busy()">Phiên bản</button>
        <button type="button" (click)="reload()" [disabled]="busy()">Tải lại</button>
        <button type="button" (click)="saveDraft()" [disabled]="busy() || !jrxml()">Lưu nháp</button>
        <button type="button" class="primary" (click)="publish()" [disabled]="busy() || !jrxml()">
          Xuất bản
        </button>
      </div>
    </header>
    <div class="compat-note">
      Bill an toàn hỗ trợ: Text, Field, Image, Line, Rectangle, Ellipse, Frame và Break.
      Table, Barcode, Chart, Crosstab và Subreport đang bị khóa.
    </div>
    <main>
      @if (loading()) {
        <div class="message">Đang tải JRXML từ ứng dụng Java…</div>
      } @else if (loadError()) {
        <div class="message error">{{ loadError() }}</div>
      } @else {
        <lib-jasper-editor [jrxml]="jrxml()" (jrxmlChange)="onJrxmlChange($event)" />
      }
    </main>

    @if (previewUrl()) {
      <div class="modal-backdrop">
        <section class="preview-modal">
          <div><strong>Preview {{ hasLiveData() ? 'hóa đơn đang chọn' : 'dữ liệu mẫu' }}</strong>
            <button type="button" (click)="closePreview()">Đóng</button></div>
          <iframe [src]="previewUrl()" title="Preview bill PDF"></iframe>
        </section>
      </div>
    }

    @if (showVersions()) {
      <div class="modal-backdrop">
        <section class="versions-modal">
          <div class="modal-title"><strong>Lịch sử xuất bản</strong>
            <button type="button" (click)="showVersions.set(false)">Đóng</button></div>
          @if (versions().length === 0) {
            <p>Chưa có phiên bản nào được xuất bản.</p>
          } @else {
            <table><thead><tr><th>Phiên bản</th><th>Thời gian</th><th></th></tr></thead><tbody>
              @for (version of versions(); track version.versionNo) {
                <tr><td>v{{ version.versionNo }}</td><td>{{ formatDate(version.publishedAt) }}</td>
                  <td><button type="button" (click)="restore(version.versionNo)">Khôi phục thành nháp</button></td></tr>
              }
            </tbody></table>
          }
        </section>
      </div>
    }
  `,
  styles: [`
    :host { display:flex; flex-direction:column; height:100vh; min-width:1280px; font-family:"Segoe UI",Arial,sans-serif; }
    header { min-height:58px; box-sizing:border-box; display:flex; align-items:center; justify-content:space-between; padding:7px 14px; background:#202632; color:#fff; gap:12px; }
    .identity,.actions { display:flex; align-items:center; gap:10px; }
    .identity > span { color:#cbd5e1; font-size:12px; }
    .version { padding:4px 7px; border-radius:12px; background:#166534; color:#dcfce7!important; }
    .version.dirty { background:#92400e; color:#fef3c7!important; }
    .actions { margin-left:auto; }
    .status { color:#cbd5e1; font-size:12px; max-width:260px; text-align:right; }
    .status.error { color:#fca5a5; }
    button { border:1px solid #64748b; border-radius:4px; background:#fff; color:#172033; padding:6px 10px; cursor:pointer; }
    button.primary { color:#fff; background:#2563eb; border-color:#2563eb; font-weight:600; }
    button:disabled { opacity:.55; cursor:default; }
    .compat-note { padding:5px 14px; font-size:12px; color:#7c2d12; background:#ffedd5; border-bottom:1px solid #fdba74; }
    main { flex:1; min-height:0; overflow:hidden; }
    lib-jasper-editor { display:block; height:100%; }
    .message { margin:40px; padding:20px; background:#fff; border-radius:6px; }
    .message.error { color:#b91c1c; }
    details.dictionary { position:relative; }
    details.dictionary summary { cursor:pointer; color:#bfdbfe; font-size:12px; }
    .dictionary-panel { position:absolute; z-index:20; left:0; top:30px; width:660px; max-height:70vh; overflow:auto; padding:12px; color:#172033; background:#fff; border:1px solid #94a3b8; border-radius:6px; box-shadow:0 8px 24px #0005; }
    .dictionary-panel p { margin:0 0 10px; color:#475569; }
    .dictionary-panel h4 { margin:10px 0 5px; }
    .binding-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:5px; }
    .binding-grid div { display:flex; flex-direction:column; padding:6px; border:1px solid #cbd5e1; border-radius:4px; cursor:grab; }
    .binding-grid div:hover { border-color:#2563eb; background:#eff6ff; }
    .binding-grid code { color:#64748b; font-size:10px; }
    .modal-backdrop { position:fixed; inset:0; z-index:50; display:flex; align-items:center; justify-content:center; background:#0f172acc; }
    .preview-modal { width:min(1000px,92vw); height:92vh; background:#fff; border-radius:7px; overflow:hidden; display:flex; flex-direction:column; }
    .preview-modal > div,.modal-title { display:flex; align-items:center; justify-content:space-between; padding:10px 14px; border-bottom:1px solid #ddd; }
    .preview-modal iframe { flex:1; border:0; }
    .versions-modal { width:620px; max-height:75vh; overflow:auto; padding-bottom:14px; background:#fff; border-radius:7px; }
    .versions-modal p { padding:0 14px; }
    table { width:calc(100% - 28px); margin:12px 14px; border-collapse:collapse; }
    th,td { padding:8px; text-align:left; border-bottom:1px solid #e2e8f0; }
  `]
})
export class AppComponent implements OnInit, OnDestroy {
  readonly jrxml = signal('');
  readonly templateName = signal('');
  readonly paperWidth = signal(80);
  readonly hasLiveData = signal(false);
  readonly draftVersion = signal(0);
  readonly publishedVersion = signal(0);
  readonly dirty = signal(true);
  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly loadError = signal('');
  readonly status = signal('');
  readonly statusError = signal(false);
  readonly previewUrl = signal<SafeResourceUrl | null>(null);
  readonly showVersions = signal(false);
  readonly versions = signal<VersionItem[]>([]);
  private readonly sanitizer = inject(DomSanitizer);
  private previewObjectUrl = '';
  private readonly token = typeof window === 'undefined' ? '' : new URLSearchParams(window.location.search).get('token') ?? '';

  readonly parameterBindings: BindingItem[] = [
    ['companyName','Tên cửa hàng'],['companyAddress','Địa chỉ cửa hàng'],['companyPhone','Điện thoại cửa hàng'],
    ['invoiceNumber','Số hóa đơn'],['invoiceDate','Ngày hóa đơn'],['customerName','Tên khách hàng'],
    ['customerAddress','Địa chỉ khách'],['customerPhone','Điện thoại khách'],['cashier','Thu ngân'],['note','Ghi chú'],
    ['subtotal','Tiền hàng'],['discountRate','Tỷ lệ giảm'],['discount','Tiền giảm'],['taxRate','Tỷ lệ VAT'],
    ['tax','Tiền VAT'],['shipping','Phí vận chuyển'],['returnAmount','Tiền đổi trả'],['total','Tổng cộng'],
    ['paid','Đã thanh toán'],['change','Tiền trả lại'],['debt','Còn nợ'],['cash','Tiền mặt'],
    ['bankTransfer','Chuyển khoản'],['card','Tiền thẻ'],['voucher','Voucher']
  ].map(([name,label]) => ({ kind:'P' as const, name, label }));
  readonly fieldBindings: BindingItem[] = [
    ['itemCode','Mã hàng'],['itemName','Tên mặt hàng'],['unit','Đơn vị tính'],['quantity','Số lượng'],
    ['unitPrice','Đơn giá'],['discountRate','Tỷ lệ giảm'],['discountAmount','Tiền giảm'],
    ['amount','Thành tiền'],['note','Ghi chú mặt hàng']
  ].map(([name,label]) => ({ kind:'F' as const, name, label }));

  ngOnInit(): void { void this.reload(); }
  ngOnDestroy(): void { this.closePreview(); }

  async reload(): Promise<void> {
    this.loading.set(true); this.loadError.set('');
    try {
      const response = await fetch(this.api('/api/template'), { cache:'no-store' });
      if (!response.ok) throw new Error(await response.text());
      const payload = await response.json() as TemplatePayload;
      this.templateName.set(payload.name); this.paperWidth.set(payload.paperWidth);
      this.hasLiveData.set(payload.hasLiveData); this.jrxml.set(payload.jrxml);
      this.applyVersionState(payload); this.setStatus('Đã tải mẫu', false);
    } catch (error) { this.loadError.set(this.errorMessage(error)); }
    finally { this.loading.set(false); }
  }

  onJrxmlChange(value: string): void {
    this.jrxml.set(value); this.dirty.set(true); this.setStatus('Có thay đổi chưa lưu', false);
  }

  async saveDraft(): Promise<void> { await this.persist('/api/template/draft', 'PUT'); }
  async publish(): Promise<void> {
    if (!confirm('Xuất bản mẫu này để các lần in tiếp theo sử dụng?')) return;
    await this.persist('/api/template/publish', 'POST');
  }

  async preview(): Promise<void> {
    this.busy.set(true); this.setStatus('Đang render PDF preview…', false);
    try {
      const response = await fetch(this.api('/api/template/preview'), {
        method:'POST', headers:{'Content-Type':'application/xml; charset=UTF-8'}, body:this.jrxml()
      });
      if (!response.ok) throw new Error(await response.text());
      this.closePreview();
      this.previewObjectUrl = URL.createObjectURL(await response.blob());
      this.previewUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(this.previewObjectUrl));
      this.setStatus('Preview đã sẵn sàng', false);
    } catch (error) { this.setStatus(this.errorMessage(error), true); }
    finally { this.busy.set(false); }
  }

  closePreview(): void {
    if (this.previewObjectUrl) URL.revokeObjectURL(this.previewObjectUrl);
    this.previewObjectUrl = '';
    this.previewUrl.set(null);
  }

  async openVersions(): Promise<void> {
    this.busy.set(true);
    try {
      const response = await fetch(this.api('/api/template/versions'), { cache:'no-store' });
      if (!response.ok) throw new Error(await response.text());
      this.versions.set(await response.json() as VersionItem[]);
      this.showVersions.set(true);
    } catch (error) { this.setStatus(this.errorMessage(error), true); }
    finally { this.busy.set(false); }
  }

  async restore(versionNo: number): Promise<void> {
    if (!confirm(`Khôi phục phiên bản v${versionNo} thành bản nháp mới?`)) return;
    this.busy.set(true);
    try {
      const response = await fetch(this.api(`/api/template/restore?version=${versionNo}`), { method:'POST' });
      if (!response.ok) throw new Error(await response.text());
      const payload = await response.json() as StatePayload;
      this.jrxml.set(payload.jrxml); this.applyVersionState(payload);
      this.showVersions.set(false); this.setStatus(payload.message, false);
    } catch (error) { this.setStatus(this.errorMessage(error), true); }
    finally { this.busy.set(false); }
  }

  dragBinding(event: DragEvent, item: BindingItem): void {
    if (!event.dataTransfer) return;
    event.dataTransfer.setData('application/x-jasper-data-binding', JSON.stringify({kind:item.kind,name:item.name}));
    event.dataTransfer.setData('text/plain', `$${item.kind}{${item.name}}`);
    event.dataTransfer.effectAllowed = 'copy';
  }

  bindingExpression(item: BindingItem): string { return `$${item.kind}{${item.name}}`; }

  formatDate(value: string): string { return value ? new Date(value).toLocaleString('vi-VN') : ''; }

  private async persist(path: string, method: string): Promise<void> {
    this.busy.set(true); this.setStatus('Đang compile và kiểm tra dữ liệu…', false);
    try {
      const response = await fetch(this.api(path), {
        method, headers:{'Content-Type':'application/xml; charset=UTF-8'}, body:this.jrxml()
      });
      if (!response.ok) throw new Error(await response.text());
      const payload = await response.json() as StatePayload;
      this.jrxml.set(payload.jrxml); this.applyVersionState(payload); this.setStatus(payload.message, false);
    } catch (error) { this.setStatus(this.errorMessage(error), true); }
    finally { this.busy.set(false); }
  }

  private applyVersionState(payload: Pick<TemplatePayload,'draftVersion'|'publishedVersion'|'hasUnpublishedChanges'>): void {
    this.draftVersion.set(payload.draftVersion); this.publishedVersion.set(payload.publishedVersion);
    this.dirty.set(payload.hasUnpublishedChanges);
  }

  private api(path: string): string {
    return `${path}${path.includes('?') ? '&' : '?'}token=${encodeURIComponent(this.token)}`;
  }
  private setStatus(message: string, error: boolean): void { this.status.set(message); this.statusError.set(error); }
  private errorMessage(error: unknown): string { return error instanceof Error ? error.message : String(error); }
}
