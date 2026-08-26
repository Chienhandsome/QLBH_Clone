package kazuko.ex.model.danhmuc;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DotKhuyenMai {
    private String id;
    private String name;
    private String note;
    private int status = 30;
    private String dLoaiHinhKhuyenMaiId;
    private String tenLoaiHinhKhuyenMai;
    private Timestamp tuNgay;
    private Timestamp denNgay;
    private boolean ngungApDung = false;
    private double tiLeGiamGia = 0;
    private Timestamp tuGio;
    private Timestamp denGio;
    private Timestamp timeCreated;
    private String userCreatedId;
    private Timestamp timeModified;
    private String userModifiedId;
    private String tenNguoiTao = "Administrator";
    private String tenNguoiSua;

    private List<String> listCuaHangApDungIds = new ArrayList<>();

    public DotKhuyenMai() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getdLoaiHinhKhuyenMaiId() {
        return dLoaiHinhKhuyenMaiId;
    }

    public void setdLoaiHinhKhuyenMaiId(String dLoaiHinhKhuyenMaiId) {
        this.dLoaiHinhKhuyenMaiId = dLoaiHinhKhuyenMaiId;
    }

    public String getTenLoaiHinhKhuyenMai() {
        return tenLoaiHinhKhuyenMai;
    }

    public void setTenLoaiHinhKhuyenMai(String tenLoaiHinhKhuyenMai) {
        this.tenLoaiHinhKhuyenMai = tenLoaiHinhKhuyenMai;
    }

    public Timestamp getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(Timestamp tuNgay) {
        this.tuNgay = tuNgay;
    }

    public Timestamp getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(Timestamp denNgay) {
        this.denNgay = denNgay;
    }

    public boolean isNgungApDung() {
        return ngungApDung;
    }

    public void setNgungApDung(boolean ngungApDung) {
        this.ngungApDung = ngungApDung;
    }

    public double getTiLeGiamGia() {
        return tiLeGiamGia;
    }

    public void setTiLeGiamGia(double tiLeGiamGia) {
        this.tiLeGiamGia = tiLeGiamGia;
    }

    public Timestamp getTuGio() {
        return tuGio;
    }

    public void setTuGio(Timestamp tuGio) {
        this.tuGio = tuGio;
    }

    public Timestamp getDenGio() {
        return denGio;
    }

    public void setDenGio(Timestamp denGio) {
        this.denGio = denGio;
    }

    public Timestamp getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Timestamp timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getUserCreatedId() {
        return userCreatedId;
    }

    public void setUserCreatedId(String userCreatedId) {
        this.userCreatedId = userCreatedId;
    }

    public Timestamp getTimeModified() {
        return timeModified;
    }

    public void setTimeModified(Timestamp timeModified) {
        this.timeModified = timeModified;
    }

    public String getUserModifiedId() {
        return userModifiedId;
    }

    public void setUserModifiedId(String userModifiedId) {
        this.userModifiedId = userModifiedId;
    }

    public String getTenNguoiTao() {
        return tenNguoiTao;
    }

    public void setTenNguoiTao(String tenNguoiTao) {
        this.tenNguoiTao = tenNguoiTao;
    }

    public String getTenNguoiSua() {
        return tenNguoiSua;
    }

    public void setTenNguoiSua(String tenNguoiSua) {
        this.tenNguoiSua = tenNguoiSua;
    }

    public List<String> getListCuaHangApDungIds() {
        return listCuaHangApDungIds;
    }

    public void setListCuaHangApDungIds(List<String> listCuaHangApDungIds) {
        this.listCuaHangApDungIds = listCuaHangApDungIds;
    }
}
