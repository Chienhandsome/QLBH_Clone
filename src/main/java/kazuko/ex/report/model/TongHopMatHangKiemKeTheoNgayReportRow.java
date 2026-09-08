package kazuko.ex.report.model;

public class TongHopMatHangKiemKeTheoNgayReportRow {
    private int stt;
    private String groupNgay;
    private String matHang;
    private String dvt;
    private double slThucTe;
    private double slHeThong;
    private double chenhLechTang;
    private double chenhLechGiam;

    public TongHopMatHangKiemKeTheoNgayReportRow() {}

    public TongHopMatHangKiemKeTheoNgayReportRow(int stt, String groupNgay, String matHang, String dvt,
                                                double slThucTe, double slHeThong,
                                                double chenhLechTang, double chenhLechGiam) {
        this.stt = stt;
        this.groupNgay = groupNgay;
        this.matHang = matHang;
        this.dvt = dvt;
        this.slThucTe = slThucTe;
        this.slHeThong = slHeThong;
        this.chenhLechTang = chenhLechTang;
        this.chenhLechGiam = chenhLechGiam;
    }

    public int getStt() { return stt; }
    public void setStt(int stt) { this.stt = stt; }

    public String getGroupNgay() { return groupNgay; }
    public void setGroupNgay(String groupNgay) { this.groupNgay = groupNgay; }

    public String getMatHang() { return matHang; }
    public void setMatHang(String matHang) { this.matHang = matHang; }

    public String getDvt() { return dvt; }
    public void setDvt(String dvt) { this.dvt = dvt; }

    public double getSlThucTe() { return slThucTe; }
    public void setSlThucTe(double slThucTe) { this.slThucTe = slThucTe; }

    public double getSlHeThong() { return slHeThong; }
    public void setSlHeThong(double slHeThong) { this.slHeThong = slHeThong; }

    public double getChenhLechTang() { return chenhLechTang; }
    public void setChenhLechTang(double chenhLechTang) { this.chenhLechTang = chenhLechTang; }

    public double getChenhLechGiam() { return chenhLechGiam; }
    public void setChenhLechGiam(double chenhLechGiam) { this.chenhLechGiam = chenhLechGiam; }
}
