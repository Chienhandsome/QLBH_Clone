package kazuko.ex.model.nhansu;

public class ChamCongChiTiet {
    private String id;
    private String bangLuongId;
    private String nhanVienId;
    private String caLamViecId;
    private int ngay; // 1 -> 31
    private int trangThai = 0; // 0: Không có lịch, 1: Đi làm (Đỏ), 2: Nghỉ có phép (Xanh lá), 3: Nghỉ không phép (Xanh cyan), 4: Khác (Xanh nhạt)

    public ChamCongChiTiet() {}

    public ChamCongChiTiet(String bangLuongId, String nhanVienId, String caLamViecId, int ngay, int trangThai) {
        this.bangLuongId = bangLuongId;
        this.nhanVienId = nhanVienId;
        this.caLamViecId = caLamViecId;
        this.ngay = ngay;
        this.trangThai = trangThai;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBangLuongId() { return bangLuongId; }
    public void setBangLuongId(String bangLuongId) { this.bangLuongId = bangLuongId; }

    public String getNhanVienId() { return nhanVienId; }
    public void setNhanVienId(String nhanVienId) { this.nhanVienId = nhanVienId; }

    public String getCaLamViecId() { return caLamViecId; }
    public void setCaLamViecId(String caLamViecId) { this.caLamViecId = caLamViecId; }

    public int getNgay() { return ngay; }
    public void setNgay(int ngay) { this.ngay = ngay; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
}
