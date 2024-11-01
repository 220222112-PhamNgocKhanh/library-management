package org.example.app;

import java.util.ArrayList;

public class User {
    private String userId;
    private String name;
    private String email;
    private ArrayList<Document> borrowedBooks;  // Danh sách sách đã mượn

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.borrowedBooks = new ArrayList<>();  // Khởi tạo danh sách sách đã mượn
    }

    // Getter và Setter cho các thuộc tính
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Phương thức để mượn sách
    public void borrowBook(Document book) {
        if (!borrowedBooks.contains(book)) {
            borrowedBooks.add(book);  // Thêm sách vào danh sách mượn
            book.setQuantity(book.getQuantity() - 1);  // Giảm số lượng sách
        }
    }

    // Phương thức để trả sách
    public void returnBook(Document book) {
        if (borrowedBooks.contains(book)) {
            borrowedBooks.remove(book);  // Xóa sách khỏi danh sách mượn
            book.setQuantity(book.getQuantity() + 1);  // Tăng số lượng sách trở lại
        }
    }

    public ArrayList<Document> getBorrowedBooks() {
        return borrowedBooks;
    }

    @Override
    public String toString() {
        return "User ID: " + userId + ", Name: " + name + ", Email: " + email;
    }
}
