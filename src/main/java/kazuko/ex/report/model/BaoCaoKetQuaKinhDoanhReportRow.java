package kazuko.ex.report.model;

public class BaoCaoKetQuaKinhDoanhReportRow {
    private String stt;
    private String tenMuc;
    private Double tyLeDT;
    private Double giaTri;
    private Double tyLe;
    private Double tyLeCP;
    private Double tangGiam;
    private Double kqkdThangTruoc;
    private Boolean isBold;
    private Boolean isItalic;
    private Integer level;

    public BaoCaoKetQuaKinhDoanhReportRow() {}

    public BaoCaoKetQuaKinhDoanhReportRow(String stt, String tenMuc, Double tyLeDT, Double giaTri, Double tyLe, Double tyLeCP, Double tangGiam, Double kqkdThangTruoc, Boolean isBold, Boolean isItalic, Integer level) {
        this.stt = stt;
        this.tenMuc = tenMuc;
        this.tyLeDT = tyLeDT;
        this.giaTri = giaTri;
        this.tyLe = tyLe;
        this.tyLeCP = tyLeCP;
        this.tangGiam = tangGiam;
        this.kqkdThangTruoc = kqkdThangTruoc;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.level = level;
    }

    public String getStt() { return stt; }
    public void setStt(String stt) { this.stt = stt; }

    public String getTenMuc() { return tenMuc; }
    public void setTenMuc(String tenMuc) { this.tenMuc = tenMuc; }

    public Double getTyLeDT() { return tyLeDT; }
    public void setTyLeDT(Double tyLeDT) { this.tyLeDT = tyLeDT; }

    public Double getGiaTri() { return giaTri; }
    public void setGiaTri(Double giaTri) { this.giaTri = giaTri; }

    public Double getTyLe() { return tyLe; }
    public void setTyLe(Double tyLe) { this.tyLe = tyLe; }

    public Double getTyLeCP() { return tyLeCP; }
    public void setTyLeCP(Double tyLeCP) { this.tyLeCP = tyLeCP; }

    public Double getTangGiam() { return tangGiam; }
    public void setTangGiam(Double tangGiam) { this.tangGiam = tangGiam; }

    public Double getKqkdThangTruoc() { return kqkdThangTruoc; }
    public void setKqkdThangTruoc(Double kqkdThangTruoc) { this.kqkdThangTruoc = kqkdThangTruoc; }

    public Boolean getIsBold() { return isBold != null ? isBold : Boolean.FALSE; }
    public void setIsBold(Boolean bold) { isBold = bold; }

    public Boolean getIsItalic() { return isItalic != null ? isItalic : Boolean.FALSE; }
    public void setIsItalic(Boolean italic) { isItalic = italic; }

    public Integer getLevel() { return level != null ? level : 0; }
    public void setLevel(Integer level) { this.level = level; }
}
