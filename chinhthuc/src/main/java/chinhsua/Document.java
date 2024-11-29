package chinhsua;

public class Document {

  private String title;
  private String author;
  private String category;
  private String status;
  private int quantity;
  private String publisher;
  private String publishedDate;
  private String description;
  private String isbn13;
  private String isbn10;
  private int idDocument;

  // Constructor cơ bản
  public Document() {

  }
  public Document(String title, String author, String category, String status, int quantity,
      int idDocument) {
    this.title = title;
    this.author = author;
    this.category = category;
    this.status = status;
    this.quantity = quantity;
    this.idDocument = idDocument;
  }
  public Document(int idDocument, String title, String author) {
    this.idDocument = idDocument;
    this.title = title;
    this.author = author;
  }

  public Document(int idDocument,String title, String author, String category, String status, int quantity,
      String publisher, String publishedDate, String description, String isbn13, String isbn10) {
    this.title = title;
    this.author = author;
    this.category = category;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
    this.description = description;
    this.isbn13 = isbn13;
    this.isbn10 = isbn10;
    this.quantity = quantity; // Đặt số lượng mặc định là 100
    this.status = (this.quantity >= 1) ? "Còn" : "Hết"; // Đặt trạng thái
    this.idDocument = idDocument;
  }


  // Constructor mở rộng để khởi tạo với dữ liệu từ API
  public Document(String title, String author, String category, String status, int quantity,
      String publisher, String publishedDate, String description, String isbn13, String isbn10) {
    this.title = title;
    this.author = author;
    this.category = category;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
    this.description = description;
    this.isbn13 = isbn13;
    this.isbn10 = isbn10;
    this.quantity = quantity; // Đặt số lượng mặc định là 100
    this.status = (this.quantity >= 1) ? "Còn" : "Hết"; // Đặt trạng thái
  }

  // Getters và setters cho tất cả các thuộc tính
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

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getPublishedDate() {
    return publishedDate;
  }

  public void setPublishedDate(String publishedDate) {
    this.publishedDate = publishedDate;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getIsbn13() {
    return isbn13;
  }

  public void setIsbn13(String isbn13) {
    this.isbn13 = isbn13;
  }

  public String getIsbn10() {
    return isbn10;
  }

  public void setIsbn10(String isbn10) {
    this.isbn10 = isbn10;
  }

  public int getIdDocument() {
    return idDocument;
  }

  public void setIdDocument(int idDocument) {
    this.idDocument = idDocument;
  }

  @Override
  public String toString() {
    return String.format(
        "ID: %d\nTitle: %s\nAuthor: %s\nCategory: %s\nStatus: %s\nQuantity: %d\nPublisher: %s\nPublished Date: %s\nDescription: %s\nISBN-13: %s\nISBN-10: %s",
        idDocument, title, author, category, status, quantity, publisher, publishedDate,
        description, isbn13, isbn10);
  }
}
