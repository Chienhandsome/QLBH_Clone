import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { JasperEditorComponent } from '@florianrauscha/ngx-jrxml-editor';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [JasperEditorComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header>
      <div>
        <strong>QLBH – JRXML Canvas (PoC)</strong>
        <span>{{ templateName() }}</span>
      </div>
      <div class="actions">
        <span [class.error]="statusError()">{{ status() }}</span>
        <button type="button" (click)="reload()">Tải lại</button>
        <button type="button" class="primary" (click)="save()" [disabled]="saving() || !jrxml()">
          {{ saving() ? 'Đang lưu…' : 'Lưu và kiểm tra' }}
        </button>
      </div>
    </header>
    <main>
      @if (loading()) {
        <div class="message">Đang tải JRXML từ ứng dụng Java…</div>
      } @else if (loadError()) {
        <div class="message error">{{ loadError() }}</div>
      } @else {
        <lib-jasper-editor [jrxml]="jrxml()" (jrxmlChange)="onJrxmlChange($event)" />
      }
    </main>
  `,
  styles: [`
    :host { display: flex; flex-direction: column; height: 100vh; min-width: 1100px; font-family: "Segoe UI", Arial, sans-serif; }
    header { height: 52px; box-sizing: border-box; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; background: #202632; color: #fff; }
    header > div { display: flex; align-items: center; gap: 12px; }
    header span { color: #cbd5e1; font-size: 13px; }
    .actions span { min-width: 220px; text-align: right; }
    .actions span.error { color: #fca5a5; }
    button { border: 1px solid #64748b; border-radius: 4px; background: #fff; color: #172033; padding: 7px 13px; cursor: pointer; }
    button.primary { color: #fff; background: #2563eb; border-color: #2563eb; }
    button:disabled { opacity: .55; cursor: default; }
    main { flex: 1; min-height: 0; overflow: hidden; }
    lib-jasper-editor { display: block; height: 100%; }
    .message { margin: 40px; padding: 20px; background: #fff; border-radius: 6px; }
    .message.error { color: #b91c1c; }
  `]
})
export class AppComponent implements OnInit {
  readonly jrxml = signal('');
  readonly templateName = signal('');
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly loadError = signal('');
  readonly status = signal('');
  readonly statusError = signal(false);

  ngOnInit(): void {
    void this.reload();
  }

  async reload(): Promise<void> {
    this.loading.set(true);
    this.loadError.set('');
    try {
      const response = await fetch('/api/template', { cache: 'no-store' });
      if (!response.ok) throw new Error(await response.text());
      const payload = await response.json() as { name: string; jrxml: string };
      this.templateName.set(payload.name);
      this.jrxml.set(payload.jrxml);
      this.setStatus('Đã tải mẫu', false);
    } catch (error) {
      this.loadError.set(this.errorMessage(error));
    } finally {
      this.loading.set(false);
    }
  }

  onJrxmlChange(value: string): void {
    this.jrxml.set(value);
    this.setStatus('Có thay đổi chưa lưu', false);
  }

  async save(): Promise<void> {
    this.saving.set(true);
    this.setStatus('Đang gửi JasperReports kiểm tra…', false);
    try {
      const response = await fetch('/api/template', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/xml; charset=UTF-8' },
        body: this.jrxml()
      });
      const message = await response.text();
      if (!response.ok) throw new Error(message);
      this.setStatus(message || 'Đã lưu', false);
    } catch (error) {
      this.setStatus(this.errorMessage(error), true);
    } finally {
      this.saving.set(false);
    }
  }

  private setStatus(message: string, error: boolean): void {
    this.status.set(message);
    this.statusError.set(error);
  }

  private errorMessage(error: unknown): string {
    return error instanceof Error ? error.message : String(error);
  }
}
