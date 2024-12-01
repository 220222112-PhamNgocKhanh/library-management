package thang;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.*;
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

    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/library";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "08092004";

    public AddDocumentDialog(Stage parent, ArrayList<Document> documentList, Main mainInstance) {
        /* this.documentList = documentList;
        this.mainInstance = mainInstance; */

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle("Thêm tài liệu mới");

        // Tạo tiêu đề và cấu hình kiểu dáng
        Label titleLabel = new Label("Thêm tài liệu mới");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: linear-gradient(to bottom, #00aaff, #0055ff);"); // Màu xanh
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Tạo layout dạng lưới
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

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
        descriptionArea.setWrapText(true); // Bật chế độ ngắt dòng tự động nếu cần
        grid.add(descriptionArea, 1, 10, 2, 1);

        // Nút thêm tài liệu
        Button addButton = new Button("Thêm");
        addButton.setOnAction(e -> addDocument());
        grid.add(addButton, 0, 12);

        // Nút đóng
        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());
        grid.add(cancelButton, 1, 12);

        grid.add(new Label("Tìm sách (ISBN hoặc Tên):"), 0, 0);
        TextField searchField = new TextField();
        grid.add(searchField, 1, 0);

        Button searchButton = new Button("Tìm kiếm");
        searchButton.setOnAction(e -> searchBooks(searchField.getText().trim()));
        grid.add(searchButton, 2, 0);


        // Đưa tiêu đề và lưới vào VBox để căn giữa tiêu đề ở đầu trang
        VBox root = new VBox(10, titleLabel, grid);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER); // Căn giữa tất cả nội dung

        // Tạo và hiển thị Scene
        Scene scene = new Scene(root);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    private void searchBooks(String query) {
        // Kiểm tra nếu truy vấn có thể là ISBN
        String formattedQuery = query.matches("\\d{10}|\\d{13}") ? "isbn:" + query : query;

        // Hiển thị trạng thái đang tải
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Đang tải");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("Đang tìm kiếm sách, vui lòng chờ...");
        loadingAlert.initOwner(this.getOwner());
        loadingAlert.show();

        Task<JSONArray> searchTask = new Task<>() {
            @Override
            protected JSONArray call() throws Exception {
                String url = "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(query, "UTF-8");
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    return jsonObject.optJSONArray("items");
                }
            }
        };

        // Xử lý khi hoàn thành Task
        searchTask.setOnSucceeded(event -> {
            JSONArray items = searchTask.getValue();
            loadingAlert.close(); // Đóng trạng thái "Đang tải"
            if (items != null && items.length() > 0) {
                showSearchResults(items);
            } else {
                showAlert("Thông báo", "Không tìm thấy sách.", Alert.AlertType.INFORMATION);
            }
        });

        // Xử lý khi có lỗi
        searchTask.setOnFailed(event -> {
            loadingAlert.close(); // Đóng trạng thái "Đang tải"
            Throwable error = searchTask.getException();
            error.printStackTrace();
            showAlert("Lỗi", "Không thể tìm sách từ API.", Alert.AlertType.ERROR);
        });

        // Chạy Task trên một luồng nền
        Thread searchThread = new Thread(searchTask);
        searchThread.setDaemon(true);
        searchThread.start();
    }

    private void showSearchResults(JSONArray items) {
        // Tạo Stage mới để hiển thị kết quả tìm kiếm
        Stage resultsStage = new Stage();
        resultsStage.initModality(Modality.APPLICATION_MODAL);
        resultsStage.setTitle("Kết quả tìm kiếm");

        // Tạo TableView để hiển thị danh sách kết quả
        TableView<JSONObject> tableView = new TableView<>();

        // Cột hiển thị tên sách
        TableColumn<JSONObject, String> titleCol = new TableColumn<>("Tên sách");
        titleCol.setCellValueFactory(data -> {
            JSONObject volumeInfo = data.getValue().optJSONObject("volumeInfo");
            String title = (volumeInfo != null) ? volumeInfo.optString("title", "N/A") : "N/A";
            return new SimpleStringProperty(title);
        });

        // Cột hiển thị tác giả
        TableColumn<JSONObject, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(data -> {
            JSONObject volumeInfo = data.getValue().optJSONObject("volumeInfo");
            if (volumeInfo != null) {
                JSONArray authors = volumeInfo.optJSONArray("authors");
                String authorList = (authors != null) ? authors.join(", ").replace("\"", "") : "N/A";
                return new SimpleStringProperty(authorList);
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        // Thêm các cột vào TableView
        tableView.getColumns().addAll(titleCol, authorCol);

        // Nạp dữ liệu từ JSON vào TableView
        for (int i = 0; i < items.length(); i++) {
            try {
                JSONObject item = items.getJSONObject(i);
                tableView.getItems().add(item); // Thêm từng đối tượng JSON vào bảng
            } catch (JSONException e) {
                e.printStackTrace(); // Ghi log nếu có lỗi khi xử lý JSON
            }
        }

        // Nút để chọn sách từ danh sách kết quả
        Button selectButton = new Button("Chọn");
        selectButton.setOnAction(e -> {
            JSONObject selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                fillBookDetails(selectedItem); // Gọi phương thức điền thông tin sách
                resultsStage.close(); // Đóng cửa sổ kết quả
            } else {
                showAlert("Lỗi", "Vui lòng chọn một sách trong danh sách.", Alert.AlertType.ERROR);
            }
        });

        // Bố cục giao diện hiển thị kết quả tìm kiếm
        VBox layout = new VBox(10, tableView, selectButton);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 600, 400);

        // Hiển thị Stage kết quả tìm kiếm
        resultsStage.setScene(scene);
        resultsStage.showAndWait();
    }


    private void fillBookDetails(JSONObject book) {
        JSONObject volumeInfo = book.getJSONObject("volumeInfo");

        if (volumeInfo != null) {
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

            // Xử lý ISBN
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
        }
    }

    /* // Phương thức gọi API Google Books để lấy thông tin sách theo ISBN
    private void fetchBookDetailsByISBN(String isbn) {
        try {
            // Tạo URL cho API Google Books với ISBN
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

            // Mở kết nối HTTP để gửi yêu cầu GET
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            // Đọc và xây dựng chuỗi phản hồi JSON từ API
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
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
                String category =
                        (categoriesArray != null && categoriesArray.length() > 0) ? categoriesArray.getString(0)
                                : "N/A";
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
                showAlert("Thông báo", "Không tìm thấy sách với ISBN đã nhập.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace(); // In chi tiết lỗi cho việc gỡ lỗi
            showAlert("Lỗi", "Không thể lấy thông tin từ API.", Alert.AlertType.ERROR);
        }
    } */

    //nhập thông tin vào bảng thêm tài liệu và kiểm tra ngoại lệ
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
            String isbn13 = isbn13Field.getText().trim();
            String isbn10 = isbn10Field.getText().trim();
            if (title.isEmpty()) {
                showAlert("Lỗi", "Tên tài liệu không được để trống!", Alert.AlertType.ERROR);
                return;
            }

            if (quantity < 0) {
                showAlert("Lỗi", "Số lượng tài liệu không được nhỏ hơn 0!", Alert.AlertType.ERROR);
                return;
            }
            Document document = new Document(title, author, category, status, quantity, publisher,
                    publishedDate, description, isbn13, isbn10);
            try (Connection connection = ApiToDatabase.getConnection()) {
                // Tìm kiếm các tài liệu trùng tên
                String query = "SELECT * FROM document WHERE title = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, title);
                    ResultSet resultSet = preparedStatement.executeQuery();

                    // Nếu có tài liệu trùng tên
                    if (resultSet.next()) {
                        // Hiển thị cửa sổ chứa danh sách tài liệu trùng tên
                        showDuplicateDocuments(resultSet, connection, document);
                        return;
                    }
                }

                // Nếu không có tài liệu trùng, thực hiện thêm mới
                insertNewDocument(connection, document);
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập số lượng sách!", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Lỗi", "Không thể kiểm tra tài liệu trong cơ sở dữ liệu.", Alert.AlertType.ERROR);
        }
    }

    //hiển thị danh sách những tài liệu trùng tên lên một cửa sổ khác và những trường xử lý
    private void showDuplicateDocuments(ResultSet resultSet, Connection connection, Document document)
            throws SQLException {
        Stage duplicateStage = new Stage();
        duplicateStage.initModality(Modality.APPLICATION_MODAL);
        duplicateStage.setTitle("Tài liệu trùng tên");

        // Tạo TableView
        TableView<Document> tableView = new TableView<>();

        TableColumn<Document, Integer> idDocumentCol = new TableColumn<>("ID Tài liệu");
        idDocumentCol.setCellValueFactory(new PropertyValueFactory<>("idDocument"));

        TableColumn<Document, String> titleCol = new TableColumn<>("Tên tài liệu");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Document, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

        TableColumn<Document, Integer> quantityCol = new TableColumn<>("Số lượng");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Document, String> categoryCol = new TableColumn<>("Thể loại");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Document, String> isbn13Col = new TableColumn<>("isbn13");
        isbn13Col.setCellValueFactory(new PropertyValueFactory<>("isbn13"));

        TableColumn<Document, String> isbn10Col = new TableColumn<>("isbn10");
        isbn10Col.setCellValueFactory(new PropertyValueFactory<>("isbn10"));

        tableView.getColumns().addAll(idDocumentCol, titleCol, authorCol, categoryCol, quantityCol,isbn13Col, isbn10Col);

        // Nạp dữ liệu từ ResultSet
        ArrayList<Document> duplicateList = new ArrayList<>();
        do {
            Document doc = new Document();
            doc.setIdDocument(resultSet.getInt("idDocument")); // Lấy ID
            doc.setTitle(resultSet.getString("title"));
            doc.setCategory(resultSet.getString("category"));
            doc.setIsbn10(resultSet.getString("isbn10"));
            doc.setIsbn13(resultSet.getString("isbn13"));
            doc.setAuthor(resultSet.getString("author"));
            doc.setQuantity(resultSet.getInt("quantity"));
            duplicateList.add(doc);
        } while (resultSet.next());
        tableView.getItems().addAll(duplicateList);

        // Nút "Thêm mới"
        Button addNewButton = new Button("Thêm mới");
        addNewButton.setOnAction(e -> {
            try {
                duplicateStage.close();
                insertNewDocument(connection, document);
            } catch (SQLException ex) {
                showAlert("Lỗi", "Không thể thêm tài liệu mới.", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
            mainInstance.loadDocumentsFromDatabase();
        });

        // Nút "Chỉnh sửa"
        Button editButton = new Button("Chỉnh sửa");
        editButton.setOnAction(e -> {
            Document selectedDoc = tableView.getSelectionModel().getSelectedItem();
            if (selectedDoc == null) {
                showAlert("Lỗi", "Vui lòng chọn tài liệu để chỉnh sửa.", Alert.AlertType.ERROR);
                return;
            }
            duplicateStage.close();
            updateDocumentInDatabase(document,selectedDoc.getIdDocument());
            mainInstance.loadDocumentsFromDatabase();
        });

        Button closeButton = new Button("Hủy");
        closeButton.setOnAction(e -> duplicateStage.close());

        VBox layout = new VBox(10, new Label("Danh sách tài liệu trùng tên:"), tableView,
                addNewButton, editButton, closeButton);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 800, 600);
        duplicateStage.setScene(scene);
        duplicateStage.showAndWait();
    }

    //thêm mới tài liệu
    private void insertNewDocument(Connection connection, Document document) throws SQLException {
        String insertQuery =
                "INSERT INTO document (title, author, category, status, quantity, publisher, publishedDate, description, isbn13, isbn10) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, document.getTitle());
            preparedStatement.setString(2, document.getAuthor());
            preparedStatement.setString(3, document.getCategory());
            preparedStatement.setString(4, document.getStatus());
            preparedStatement.setInt(5, document.getQuantity());
            preparedStatement.setString(6, document.getPublisher());
            preparedStatement.setString(7, document.getPublishedDate());
            preparedStatement.setString(8, document.getDescription());
            preparedStatement.setString(9, document.getIsbn13());
            preparedStatement.setString(10, document.getIsbn10());

            preparedStatement.executeUpdate();
            showAlert("Thành công", "Tài liệu đã được thêm vào cơ sở dữ liệu.", Alert.AlertType.INFORMATION);
            close();

        }
    }

    // cap nhat tai lieu dua vao thong tin nhap va id
    public void updateDocumentInDatabase(Document document,int id) {


        String updateQuery = "UPDATE document SET title = ?, author = ?, category = ?, status = ?, " +
                "quantity = ?, publisher = ?, publishedDate = ?, description = ?, " +
                "isbn13 = ?, isbn10 = ? WHERE idDocument = ?";

        try (Connection connection = ApiToDatabase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            // Gán giá trị cho các tham số
            preparedStatement.setString(1, document.getTitle());
            preparedStatement.setString(2, document.getAuthor());
            preparedStatement.setString(3, document.getCategory());
            preparedStatement.setString(4, document.getStatus());
            preparedStatement.setInt(5, document.getQuantity());
            preparedStatement.setString(6, document.getPublisher());
            preparedStatement.setString(7, document.getPublishedDate());
            preparedStatement.setString(8, document.getDescription());
            preparedStatement.setString(9, document.getIsbn13());
            preparedStatement.setString(10, document.getIsbn10());
            preparedStatement.setInt(11,id);

            // Thực hiện cập nhật
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Thành công", "Cập nhật tài liệu thành công.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Thông báo", "Không tìm thấy tài liệu để cập nhật.", Alert.AlertType.WARNING);
            }
            close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật tài liệu.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }
}
