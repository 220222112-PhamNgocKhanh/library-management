import javax.swing.table.TableRowSorter;


public class Document {
    private String title;     // Tên sách
    private String author;    // Tác giả
    private String category;  // Thể loại
    private String status;    // Trạng thái (Còn hoặc Hết)
    private int quantity;     // Số lượng

    // Constructor để khởi tạo một tài liệu mới
    public Document(String title, String author, String category, String status, int quantity) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.status = status;
        this.quantity = quantity;
    }

    // Getter và Setter cho từng thuộc tính
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Phương thức in thông tin tài liệu
    public String toString() {
        return String.format("Tên sách: %s\nTác giả: %s\nThể loại: %s\nTrạng thái: %s\nSố lượng: %d",
                title, author, category, status, quantity);
    }
}
