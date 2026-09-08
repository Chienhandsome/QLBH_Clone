package kazuko.ex.report.model;

public class BangTongHopThiPhanReportRow {
    private String danhMuc;
    private Double t1;
    private Double t2;
    private Double t3;
    private Double t4;
    private Double t5;
    private Double t6;
    private Double t7;
    private Double t8;
    private Double t9;
    private Double t10;
    private Double t11;
    private Double t12;
    private Double ttNam;
    private Double tongChiTieu;
    private Double thayDoi;
    private String tyLe;
    private Integer rowType; // 0: Header Tỉnh, 1: Detail, 2: Tổng cộng nhóm, 3: TỔNG CỘNG, 4: QUÍ, 5: CT Doanh thu, 6: Tăng giảm so với CT, 7: ĐẠT %
    private String bgColor; // Hex backcolor

    public BangTongHopThiPhanReportRow() {}

    public BangTongHopThiPhanReportRow(String danhMuc, Double t1, Double t2, Double t3, Double t4, Double t5, Double t6, Double t7, Double t8, Double t9, Double t10, Double t11, Double t12, Double ttNam, Double tongChiTieu, Double thayDoi, String tyLe, Integer rowType, String bgColor) {
        this.danhMuc = danhMuc;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
        this.t5 = t5;
        this.t6 = t6;
        this.t7 = t7;
        this.t8 = t8;
        this.t9 = t9;
        this.t10 = t10;
        this.t11 = t11;
        this.t12 = t12;
        this.ttNam = ttNam;
        this.tongChiTieu = tongChiTieu;
        this.thayDoi = thayDoi;
        this.tyLe = tyLe;
        this.rowType = rowType;
        this.bgColor = bgColor;
    }

    public String getDanhMuc() { return danhMuc; }
    public void setDanhMuc(String danhMuc) { this.danhMuc = danhMuc; }

    public Double getT1() { return t1; }
    public void setT1(Double t1) { this.t1 = t1; }

    public Double getT2() { return t2; }
    public void setT2(Double t2) { this.t2 = t2; }

    public Double getT3() { return t3; }
    public void setT3(Double t3) { this.t3 = t3; }

    public Double getT4() { return t4; }
    public void setT4(Double t4) { this.t4 = t4; }

    public Double getT5() { return t5; }
    public void setT5(Double t5) { this.t5 = t5; }

    public Double getT6() { return t6; }
    public void setT6(Double t6) { this.t6 = t6; }

    public Double getT7() { return t7; }
    public void setT7(Double t7) { this.t7 = t7; }

    public Double getT8() { return t8; }
    public void setT8(Double t8) { this.t8 = t8; }

    public Double getT9() { return t9; }
    public void setT9(Double t9) { this.t9 = t9; }

    public Double getT10() { return t10; }
    public void setT10(Double t10) { this.t10 = t10; }

    public Double getT11() { return t11; }
    public void setT11(Double t11) { this.t11 = t11; }

    public Double getT12() { return t12; }
    public void setT12(Double t12) { this.t12 = t12; }

    public Double getTtNam() { return ttNam; }
    public void setTtNam(Double ttNam) { this.ttNam = ttNam; }

    public Double getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(Double tongChiTieu) { this.tongChiTieu = tongChiTieu; }

    public Double getThayDoi() { return thayDoi; }
    public void setThayDoi(Double thayDoi) { this.thayDoi = thayDoi; }

    public String getTyLe() { return tyLe; }
    public void setTyLe(String tyLe) { this.tyLe = tyLe; }

    public Integer getRowType() { return rowType; }
    public void setRowType(Integer rowType) { this.rowType = rowType; }

    public String getBgColor() { return bgColor; }
    public void setBgColor(String bgColor) { this.bgColor = bgColor; }
}
