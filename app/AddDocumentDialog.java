package org.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AddDocumentDialog extends Stage {
    private TextField titleField;
    private TextField isbnField;
    private TextField authorField;
    private TextField categoryField;
    private ComboBox<String> statusComboBox;
    private TextField quantityField;
    private TextField isbn10Field; // Thêm trường ISBN 10
    private TextField isbn13Field; // Thêm trường ISBN 13
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

        // Trường nhập ISBN 10
        grid.add(new Label("ISBN 10:"), 0, 1);
        isbn10Field = new TextField();
        grid.add(isbn10Field, 1, 1);

        // Trường nhập ISBN 13
        grid.add(new Label("ISBN 13:"), 0, 2);
        isbn13Field = new TextField();
        grid.add(isbn13Field, 1, 2);

        // Trường nhập tên tài liệu
        grid.add(new Label("Tên tài liệu:"), 0, 3);
        titleField = new TextField();
        grid.add(titleField, 1, 3);

        // Trường nhập tác giả
        grid.add(new Label("Tác giả:"), 0, 4);
        authorField = new TextField();
        grid.add(authorField, 1, 4);

        // Trường nhập thể loại
        grid.add(new Label("Thể loại:"), 0, 5);
        categoryField = new TextField();
        grid.add(categoryField, 1, 5);

        // ComboBox chọn trạng thái
        grid.add(new Label("Trạng thái:"), 0, 6);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Còn", "Hết");
        grid.add(statusComboBox, 1, 6);

        // Trường nhập số lượng
        grid.add(new Label("Số lượng:"), 0, 7);
        quantityField = new TextField();
        grid.add(quantityField, 1, 7);

        // Trường nhập nhà xuất bản
        grid.add(new Label("Nhà xuất bản:"), 0, 8);
        publisherField = new TextField();
        grid.add(publisherField, 1, 8);

        // Trường nhập ngày xuất bản
        grid.add(new Label("Ngày xuất bản:"), 0, 9);
        publishedDateField = new TextField();
        grid.add(publishedDateField, 1, 9);

        // Trường mô tả
        grid.add(new Label("Mô tả:"), 0, 10);
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, 10, 2, 1);

        // Nút thêm tài liệu
        Button addButton = new Button("Thêm");
        addButton.setOnAction(e -> addDocument());
        grid.add(addButton, 0, 12);

        // Nút đóng
        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());
        grid.add(cancelButton, 1, 12);

        // Tạo và hiển thị Scene
        Scene scene = new Scene(grid);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    // Phương thức gọi API Google Books để lấy thông tin sách theo ISBN
    private void fetchBookDetailsByISBN(String isbn) {
        try {
            // Tạo URL cho API Google Books với ISBN
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

            // Mở kết nối HTTP để gửi yêu cầu GET
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            // Đọc phản hồi từ API
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Phân tích cú pháp JSON
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray items = jsonObject.optJSONArray("items");

            if (items != null && items.length() > 0) {
                JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");

                // Lấy thông tin từ JSON và điền vào các trường giao diện
                titleField.setText(volumeInfo.optString("title", "N/A"));

                JSONArray authorsArray = volumeInfo.optJSONArray("authors");
                String authors = (authorsArray != null) ? authorsArray.join(", ").replace("\"", "") : "N/A";
                authorField.setText(authors);

                JSONArray categoriesArray = volumeInfo.optJSONArray("categories");
                String category = (categoriesArray != null && categoriesArray.length() > 0) ? categoriesArray.getString(0) : "N/A";
                categoryField.setText(category);

                publisherField.setText(volumeInfo.optString("publisher", "N/A"));
                publishedDateField.setText(volumeInfo.optString("publishedDate", "N/A"));
                descriptionArea.setText(volumeInfo.optString("description", "N/A"));

                // Xử lý ISBN 10 và ISBN 13
                JSONArray industryIdentifiers = volumeInfo.optJSONArray("industryIdentifiers");
                if (industryIdentifiers != null) {
                    for (int i = 0; i < industryIdentifiers.length(); i++) {
                        JSONObject identifier = industryIdentifiers.getJSONObject(i);
                        String type = identifier.optString("type");
                        String identifierValue = identifier.optString("identifier");

                        if ("ISBN_10".equals(type)) {
                            isbn10Field.setText(identifierValue);
                        } else if ("ISBN_13".equals(type)) {
                            isbn13Field.setText(identifierValue);
                        }
                    }
                }
            } else {
                showAlert("Thông báo", "Không tìm thấy sách với ISBN đã nhập.");
            }

        } catch (Exception e) {
            e.printStackTrace(); // In chi tiết lỗi cho việc gỡ lỗi
            showAlert("Lỗi", "Không thể lấy thông tin từ API.");
        }
    }


    private void addDocument() {
        try {
            String isbn10 = isbn10Field.getText().trim();
            String isbn13 = isbn13Field.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = statusComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String publisher = publisherField.getText().trim();
            String publishedDate = publishedDateField.getText().trim();
            String description = descriptionArea.getText().trim();

            // Kiểm tra nếu ISBN 10 hoặc ISBN 13 bị để trống
            if (isbn10.isEmpty() || isbn13.isEmpty()) {
                showAlert("Lỗi", "Vui lòng nhập đầy đủ ISBN 10 và ISBN 13.");
                return;
            }

            Document newDocument = new Document(title, author, category, status, quantity, publisher, publishedDate, description, isbn10, isbn13);
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
