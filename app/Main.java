package org.example.app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main extends Application {
    private TableView<Document> table;
    private TextArea bookInfoArea;
    private TextField searchField;
    private ArrayList<Document> documentList; // Danh sách các tài liệu
    private ArrayList<User> userList; // Danh sách người dùng
    private ObservableList<Document> observableDocumentList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        documentList = new ArrayList<>();
        userList = new ArrayList<>();
        observableDocumentList = FXCollections.observableArrayList(documentList);

        primaryStage.setTitle("Thư viện");

        // Gọi phương thức để tải tài liệu từ API khi khởi chạy
        loadDocumentsFromAPI();

        // Tiêu đề ở phía trên với thanh tìm kiếm
        HBox searchPanel = new HBox(10);
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        searchPanel.setPadding(new Insets(10, 10, 10, 10));

        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm...");
        Button searchButton = new Button("Tìm kiếm");
        searchPanel.getChildren().addAll(searchField, searchButton);

        // Tạo HBox với khoảng cách giữa các nút là 20
        HBox buttonPanel = new HBox(20);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10, 10, 10, 10));

        // Tạo các nút với kích thước lớn hơn và thêm chúng vào buttonPanel
        Button btnAdd = new Button("Thêm tài liệu");
        btnAdd.setMinWidth(180);
        btnAdd.setMinHeight(100);
        btnAdd.setStyle("-fx-background-color: #ADD8E6; -fx-font-size: 14px;");

        Button btnEdit = new Button("Sửa tài liệu");
        btnEdit.setMinWidth(180);
        btnEdit.setMinHeight(100);
        btnEdit.setStyle("-fx-background-color: #87CEFA; -fx-font-size: 14px;");

        Button btnDelete = new Button("Xóa tài liệu");
        btnDelete.setMinWidth(180);
        btnDelete.setMinHeight(100);
        btnDelete.setStyle("-fx-background-color: #FF6347; -fx-font-size: 14px;");

        Button btnBorrowReturn = new Button("Mượn/Trả tài liệu");
        btnBorrowReturn.setMinWidth(180);
        btnBorrowReturn.setMinHeight(100);
        btnBorrowReturn.setStyle("-fx-background-color: #FFA500; -fx-font-size: 14px;");

        // Thêm các nút vào buttonPanel và dàn đều chúng trong HBox
        buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnBorrowReturn);
        HBox.setHgrow(btnAdd, Priority.ALWAYS);
        HBox.setHgrow(btnEdit, Priority.ALWAYS);
        HBox.setHgrow(btnDelete, Priority.ALWAYS);
        HBox.setHgrow(btnBorrowReturn, Priority.ALWAYS);


        // Tạo bảng hiển thị danh sách tài liệu
        table = new TableView<>();
        table.setPrefWidth(395); // Đặt chiều rộng cố định cho bảng
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Tự động dàn đều các cột

        // Cột "Tên tài liệu"
        TableColumn<Document, String> titleColumn = new TableColumn<>("Tên tài liệu");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(90); // Đặt chiều rộng cho cột "Tên tài liệu"

        // Cột "Trạng thái"
        TableColumn<Document, String> statusColumn = new TableColumn<>("Trạng thái");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(90); // Đặt chiều rộng cho cột "Trạng thái"

        // Cột "Số lượng"
        TableColumn<Document, Integer> quantityColumn = new TableColumn<>("Số lượng");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(90); // Đặt chiều rộng cho cột "Số lượng"

        // Thêm các cột vào bảng và thiết lập dữ liệu
        table.getColumns().addAll(titleColumn, statusColumn, quantityColumn);
        table.setItems(observableDocumentList);


        // Khu vực thông tin chi tiết tài liệu
        VBox infoPanel = new VBox();
        infoPanel.setPadding(new Insets(10));
        infoPanel.setAlignment(Pos.TOP_CENTER);
        infoPanel.setSpacing(10);
        infoPanel.setPrefWidth(300); // Đặt chiều rộng cho infoPanel

        Label lblInfo = new Label("Thông tin:");
        lblInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ADD8E6;");
        lblInfo.setMaxWidth(Double.MAX_VALUE);
        lblInfo.setAlignment(Pos.CENTER);
        lblInfo.setPadding(new Insets(5)); // Thêm khoảng cách xung quanh

        bookInfoArea = new TextArea();
        bookInfoArea.setEditable(false);
        bookInfoArea.setStyle("-fx-control-inner-background: #F0FFFF;");
        bookInfoArea.setPrefWidth(280);  // Đặt chiều rộng cố định cho TextArea
        bookInfoArea.setPrefHeight(200); // Đặt chiều cao mong muốn cho TextArea
        bookInfoArea.setWrapText(true);  // Bật chế độ ngắt dòng tự động nếu cần

        VBox.setVgrow(bookInfoArea, Priority.ALWAYS); // Để TextArea giãn nở theo chiều dọc

        // Thêm label và TextArea vào infoPanel
        infoPanel.getChildren().addAll(lblInfo, bookInfoArea);

        // Bố trí bảng và khu vực thông tin chi tiết vào một ngăn chia tỷ lệ
        SplitPane contentPanel = new SplitPane();
        contentPanel.getItems().addAll(new ScrollPane(table), infoPanel);
        contentPanel.setDividerPositions(0.5);

        // Xử lý sự kiện chọn tài liệu trong bảng để hiển thị chi tiết
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showBookInfo(newValue); // Hiển thị thông tin chi tiết tài liệu
            }
        });

        // Xử lý sự kiện nút "Thêm tài liệu"
        btnAdd.setOnAction(e -> {
            // Mở hộp thoại thêm tài liệu và cập nhật danh sách sau khi thêm
            AddDocumentDialog dialog = new AddDocumentDialog(primaryStage, documentList, this);
            dialog.showAndWait();
        });

        // Xử lý sự kiện nút "Sửa tài liệu"
        btnEdit.setOnAction(e -> {
            Document selectedDocument = table.getSelectionModel().getSelectedItem();
            if (selectedDocument != null) {
                EditDocumentDialog editDialog = new EditDocumentDialog(primaryStage, documentList, documentList.indexOf(selectedDocument), this);
                editDialog.showAndWait();
            } else {
                showAlert("Vui lòng chọn tài liệu để sửa!");
            }
        });

        // Xử lý sự kiện nút "Xóa tài liệu"
        btnDelete.setOnAction(e -> {
            Document selectedDocument = table.getSelectionModel().getSelectedItem();
            if (selectedDocument != null) {
                documentList.remove(selectedDocument); // Xóa tài liệu từ danh sách
                updateTable(); // Cập nhật lại bảng
                bookInfoArea.clear(); // Xóa thông tin khi xóa tài liệu
            } else {
                showAlert("Vui lòng chọn tài liệu để xóa!");
            }
        });

        // Xử lý sự kiện nút "Mượn/Trả tài liệu"
        btnBorrowReturn.setOnAction(e -> {
            Document selectedDocument = table.getSelectionModel().getSelectedItem();
            if (selectedDocument != null) {
                BorrowReturnDialog borrowReturnDialog = new BorrowReturnDialog(primaryStage, documentList, selectedDocument, userList);
                borrowReturnDialog.showAndWait();
                updateTable(); // Cập nhật bảng sau khi mượn/trả sách
            } else {
                showAlert("Vui lòng chọn tài liệu để mượn/trả!");
            }
        });

        // Xử lý sự kiện nút "Tìm kiếm"
        searchButton.setOnAction(e -> {
            SearchDialog searchDialog = new SearchDialog(primaryStage, documentList, table, bookInfoArea);
            searchDialog.search(searchField.getText().trim()); // Gọi phương thức search công khai
        });

        // Bố trí tất cả các thành phần vào BorderPane chính
        BorderPane root = new BorderPane();
        root.setTop(searchPanel);
        root.setCenter(buttonPanel);
        root.setBottom(contentPanel);

        // Tạo Scene và hiển thị Stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    // Hiển thị thông tin tài liệu
    private void showBookInfo(Document document) {
        bookInfoArea.setText(document.toString()); // Hiển thị chi tiết tài liệu
    }

    // Phương thức tải dữ liệu tài liệu từ Google Books API
    private void loadDocumentsFromAPI() {
        int startIndex = 0;
        int totalItems = 537; // Tổng số tài liệu cần lấy
        int maxResults = 40;

        while (startIndex < totalItems) {
            try {
                String url = "https://www.googleapis.com/books/v1/volumes?q=isbn%30&startIndex=" + startIndex + "&maxResults=" + maxResults;
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                parseBookDataFromAPI(response.toString());

                startIndex += maxResults; // Tăng startIndex để lấy trang tiếp theo
            } catch (Exception e) {
                showAlert("Không thể lấy dữ liệu từ API.");
                break; // Dừng nếu có lỗi
            }
        }
    }


    // Phân tích cú pháp JSON và thêm dữ liệu vào documentList
    private void parseBookDataFromAPI(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray items = jsonObject.optJSONArray("items");

        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");

                String title = volumeInfo.optString("title", "N/A");
                String author = volumeInfo.optJSONArray("authors") != null ?
                        volumeInfo.optJSONArray("authors").join(", ") : "N/A";
                String category = volumeInfo.optJSONArray("categories") != null ?
                        volumeInfo.optJSONArray("categories").getString(0) : "N/A";
                String publisher = volumeInfo.optString("publisher", "N/A");
                String publishedDate = volumeInfo.optString("publishedDate", "N/A");
                String description = volumeInfo.optString("description", "N/A");
                String isbn13 = "N/A";
                String isbn10 = "N/A";
                JSONArray industryIdentifiers = volumeInfo.optJSONArray("industryIdentifiers");

                if (industryIdentifiers != null) {
                    for (int j = 0; j < industryIdentifiers.length(); j++) {
                        JSONObject identifier = industryIdentifiers.getJSONObject(j);
                        if ("ISBN_13".equals(identifier.optString("type"))) {
                            isbn13 = identifier.optString("identifier");
                        } else if ("ISBN_10".equals(identifier.optString("type"))) {
                            isbn10 = identifier.optString("identifier");
                        }
                    }
                }

                // Sử dụng số lượng mặc định và trạng thái từ lớp Document
                Document document = new Document(title, author, category, "Còn", 100, publisher, publishedDate, description, isbn13, isbn10);
                documentList.add(document);
            }
            updateTable();
        }
    }


    // Cập nhật bảng hiển thị danh sách tài liệu từ documentList
    public void updateTable() {
        observableDocumentList.setAll(documentList);
    }



    // Phương thức tiện ích để hiển thị thông báo
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
