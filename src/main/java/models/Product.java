package models;

public class Product {
    private String stt;
    private String prodId;
    private String sanpham;
    private String moTa;
    private String soLuong;
    private String giaSP;

    public Product(String stt, String sanpham, String moTa, String soLuong, String giaSP, String prodId) {
        this.stt = stt;
        this.sanpham = sanpham;
        this.moTa = moTa;
        this.soLuong = soLuong;
        this.giaSP = giaSP;
        this.prodId = prodId;
    }

    @Override
    public String toString() {
        return  "\nstt: " + this.stt + "tên SP: " + this.sanpham +
                "| mô tả: " + this.moTa +
                "| số Lượng: " + this.soLuong +
                "| giá SP: " + this.giaSP;
    }

    public String getSanpham() {
        return sanpham;
    }

    public String getGiaSP() {
        return giaSP;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getSoLuong() {
        return soLuong;
    }
}
