package org.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;

public class AddDocumentDialog extends Stage {
    private TextField titleField;
    private TextField authorField;
    private TextField categoryField;
    private ComboBox<String> statusComboBox;
    private TextField quantityField;
    private TextField isbnField;
    private TextField publisherField;
    private TextField publishedDateField;
    private TextArea descriptionArea;
    private ArrayList<Document> documentList;
    private Main mainInstance; // Đối tượng Main để gọi hàm updateTable()

    public AddDocumentDialog(Stage parent, ArrayList<Document> documentList, Main mainInstance) {
        this.documentList = documentList;
        this.mainInstance = mainInstance;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle("Thêm tài liệu mới");

        // Tạo layout dạng lưới
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Trường nhập ISBN
        grid.add(new Label("ISBN:"), 0, 0);
        isbnField = new TextField();
        grid.add(isbnField, 1, 0);

        Button fetchButton = new Button("Tìm sách");
        fetchButton.setOnAction(e -> fetchBookDetailsByISBN(isbnField.getText().trim()));
        grid.add(fetchButton, 2, 0);

        // Trường nhập tên tài liệu
        grid.add(new Label("Tên tài liệu:"), 0, 1);
        titleField = new TextField();
        grid.add(titleField, 1, 1);

        // Trường nhập tác giả
        grid.add(new Label("Tác giả:"), 0, 2);
        authorField = new TextField();
        grid.add(authorField, 1, 2);

        // Trường nhập thể loại
        grid.add(new Label("Thể loại:"), 0, 3);
        categoryField = new TextField();
        grid.add(categoryField, 1, 3);

        // ComboBox chọn trạng thái
        grid.add(new Label("Trạng thái:"), 0, 4);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Còn", "Hết");
        grid.add(statusComboBox, 1, 4);

        // Trường nhập số lượng
        grid.add(new Label("Số lượng:"), 0, 5);
        quantityField = new TextField();
        grid.add(quantityField, 1, 5);

        // Trường nhập nhà xuất bản
        grid.add(new Label("Nhà xuất bản:"), 0, 6);
        publisherField = new TextField();
        grid.add(publisherField, 1, 6);

        // Trường nhập ngày xuất bản
        grid.add(new Label("Ngày xuất bản:"), 0, 7);
        publishedDateField = new TextField();
        grid.add(publishedDateField, 1, 7);

        // Trường mô tả
        grid.add(new Label("Mô tả:"), 0, 8);
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, 8, 2, 1);

        // Nút thêm tài liệu
        Button addButton = new Button("Thêm");
        addButton.setOnAction(e -> addDocument());
        grid.add(addButton, 0, 9);

        // Nút đóng
        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());
        grid.add(cancelButton, 1, 9);

        // Tạo và hiển thị Scene
        Scene scene = new Scene(grid);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    // Phương thức gọi API Google Books để lấy thông tin sách theo ISBN
    private void fetchBookDetailsByISBN(String isbn) {
        try {
            // Thêm logic gọi API, xử lý JSON và điền thông tin vào các trường tương ứng
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
            // Gọi API và phân tích JSON (dùng HttpClient hoặc các thư viện khác)

            // Ví dụ: Điền thông tin vào các trường (sau khi lấy dữ liệu thành công)
            titleField.setText("Book Title from API");
            authorField.setText("Author from API");
            categoryField.setText("Category from API");
            publisherField.setText("Publisher from API");
            publishedDateField.setText("Published Date from API");
            descriptionArea.setText("Description from API");

        } catch (Exception e) {
            showAlert("Lỗi", "Không thể lấy thông tin từ API.");
        }
    }

    private void addDocument() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = statusComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String publisher = publisherField.getText().trim();
            String publishedDate = publishedDateField.getText().trim();
            String description = descriptionArea.getText().trim();

            Document newDocument = new Document(title, author, category, status, quantity, publisher, publishedDate, description, "", "");
            documentList.add(newDocument);

            mainInstance.updateTable();
            close();
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập đúng số lượng!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }
}
