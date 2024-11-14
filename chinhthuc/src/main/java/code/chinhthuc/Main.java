package code.chinhthuc;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Main extends Application {
    private TableView<Document> table;
    private TextArea bookInfoArea;
    private TextField searchField;
    private ArrayList<Document> documentList;
    private ArrayList<User> userList;
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

        // Tạo sidebar với các nút chức năng
        VBox sidebar = new VBox(20); // Sử dụng VBox để tạo layout dọc, với khoảng cách giữa các phần tử là 20
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200); // Chiều rộng của sidebar
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #FF7E5F, #FD3A84);"); // Màu nền dạng gradient

        // Tiêu đề trên cùng của sidebar
        Label title = new Label("Library\nManagement");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;"); // Định dạng tiêu đề

        // Tạo các nút chức năng trong sidebar
        Button introButton = new Button("Giới thiệu");
        Button noteButton = new Button("Ghi chú");
        Button calendarButton = new Button("Lịch");
        Button btnThanhVien = new Button("Thành viên");

        // Định dạng cho các nút chức năng trong sidebar
        for (Button btn : new Button[]{introButton, noteButton, calendarButton, btnThanhVien}) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: CENTER_LEFT;");
            btn.setMaxWidth(Double.MAX_VALUE); // Đặt chiều rộng của nút là tối đa, để vừa với chiều rộng của sidebar
            btn.setOnAction(e -> System.out.println(btn.getText() + " clicked")); // Xử lý sự kiện khi nhấn nút
        }

        // Thêm tiêu đề và các nút vào sidebar
        sidebar.getChildren().addAll(title, introButton, noteButton, calendarButton, btnThanhVien);

        // Tạo phần nội dung chính - bao gồm thanh tìm kiếm và các nút chức năng
        HBox searchPanel = new HBox(10); // Sử dụng HBox để tạo thanh tìm kiếm và nút
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        searchPanel.setPadding(new Insets(10, 10, 10, 10));

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(400); // Chiều rộng của ô tìm kiếm

        Button searchButton = new Button("Search");
        searchField.setOnAction(e -> {
            searchButton.fire(); // Kích hoạt sự kiện cho nút search
        });

        searchPanel.getChildren().addAll(searchField, searchButton); // Thêm ô tìm kiếm và nút tìm kiếm vào searchPanel

        // Tạo các nút thêm, sửa, xóa, mượn/trả tài liệu
        HBox buttonPanel = new HBox(20); // Sử dụng HBox cho layout ngang
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10, 10, 10, 6));

        // Tạo các nút với kích thước lớn hơn và thêm chúng vào buttonPanel
        Button btnAdd = new Button("Thêm tài liệu");
        btnAdd.setMinWidth(180);
        btnAdd.setMinHeight(100);
        btnAdd.setStyle("-fx-background-color: linear-gradient(to right, #8A2BE2, #00BFFF);"
                + "-fx-font-size: 17px;"
                + "-fx-text-fill: white;" // Màu chữ trắng
                + "-fx-font-weight: bold;" // Chữ in đậm
                + "-fx-background-radius: 15;" // Bo tròn góc
                + "-fx-border-radius: 15;" // Bo tròn góc cho viền (nếu có)
        );

        Button btnEdit = new Button("Sửa tài liệu");
        btnEdit.setMinWidth(180);
        btnEdit.setMinHeight(100);
        btnEdit.setStyle("-fx-background-color: linear-gradient(to right, #0000FF, #FF00FF);"
                + "-fx-font-size: 17px;"
                + "-fx-text-fill: white;" // Màu chữ trắng
                + "-fx-font-weight: bold;" // Chữ in đậm
                + "-fx-background-radius: 15;" // Bo tròn góc
                + "-fx-border-radius: 15;" // Bo tròn góc cho viền (nếu có)
        );

        Button btnDelete = new Button("Xóa tài liệu");
        btnDelete.setMinWidth(180);
        btnDelete.setMinHeight(100);
        btnDelete.setStyle("-fx-background-color: linear-gradient(to right, #FF4500, #FFA07A);"
                + "-fx-font-size: 17px;"
                + "-fx-text-fill: white;" // Màu chữ trắng
                + "-fx-font-weight: bold;" // Chữ in đậm
                + "-fx-background-radius: 15;" // Bo tròn góc
                + "-fx-border-radius: 15;" // Bo tròn góc cho viền (nếu có)
        );

        Button btnBorrowReturn = new Button("Mượn/Trả tài liệu");
        btnBorrowReturn.setMinWidth(180);
        btnBorrowReturn.setMinHeight(100);
        btnBorrowReturn.setStyle("-fx-background-color: linear-gradient(to right, #FFD700, #FF8C00);"
                + "-fx-font-size: 17px;"
                + "-fx-text-fill: white;" // Màu chữ trắng
                + "-fx-font-weight: bold;" // Chữ in đậm
                + "-fx-background-radius: 15;" // Bo tròn góc
                + "-fx-border-radius: 15;" // Bo tròn góc cho viền (nếu có)
        );

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
        table.setStyle("-fx-background-radius: 10px; -fx-border-radius: 10px;");

        // Cột "Tên tài liệu"
        TableColumn<Document, String> titleColumn = new TableColumn<>("Tên tài liệu");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(170); // Đặt chiều rộng cho cột "Tên tài liệu"

        // Cột "Trạng thái"
        TableColumn<Document, String> statusColumn = new TableColumn<>("Trạng thái");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(50); // Đặt chiều rộng cho cột "Trạng thái"

        // Cột "Số lượng"
        TableColumn<Document, Integer> quantityColumn = new TableColumn<>("Số lượng");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setPrefWidth(50); // Đặt chiều rộng cho cột "Số lượng"

        // Thêm các cột vào bảng và thiết lập dữ liệu
        table.getColumns().addAll(titleColumn, statusColumn, quantityColumn);
        table.setItems(observableDocumentList);

        // Khu vực thông tin chi tiết tài liệu
        VBox infoPanel = new VBox();
        infoPanel.setPadding(new Insets(10));
        infoPanel.setAlignment(Pos.TOP_CENTER);
        infoPanel.setSpacing(10);
        infoPanel.setPrefWidth(300); // Đặt chiều rộng cho infoPanel

        // Thiết lập bo tròn góc cho toàn bộ infoPanel
        infoPanel.setStyle("-fx-background-color: #E0F7FA; "
                + "-fx-background-radius: 10px; "
                + "-fx-border-radius: 10px; "
                + "-fx-border-color: #B0BEC5;"); // Thêm màu viền nhẹ

        Label lblInfo = new Label("Thông tin:");
        lblInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ADD8E6;"
                + "-fx-background-radius: 10px;");
        lblInfo.setMaxWidth(Double.MAX_VALUE);
        lblInfo.setAlignment(Pos.CENTER);
        lblInfo.setPadding(new Insets(5)); // Thêm khoảng cách xung quanh

        bookInfoArea = new TextArea();
        bookInfoArea.setEditable(false);
        bookInfoArea.setStyle("-fx-control-inner-background: #F0FFFF; -fx-background-radius: 10px;");
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
        contentPanel.setStyle("-fx-divider-width: 2px; -fx-background-color: transparent;");

        // Kết hợp các thành phần chính của nội dung vào một VBox
        VBox mainContent = new VBox(10, searchPanel, buttonPanel, contentPanel);
        mainContent.setPadding(new Insets(10));

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

        // Xử lý sự kiện nút calendarButton
        calendarButton.setOnAction(e -> {
            try {
                // Tạo một cửa sổ mới cho lịch
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Calendar.fxml"));
                Scene calendarScene = new Scene(fxmlLoader.load());  // Tạo Scene từ calendar.fxml

                // Tạo một Stage (cửa sổ) mới
                Stage calendarStage = new Stage();
                calendarStage.setTitle("Calendar");  // Đặt tiêu đề cho cửa sổ
                calendarStage.setScene(calendarScene);  // Gán scene vào stage
                calendarStage.show();  // Hiển thị cửa sổ
            } catch (IOException ex) {
                ex.printStackTrace();  // In ra lỗi nếu có vấn đề khi load FXML
            }
        });

        FileHandler fileHandler = new FileHandler();

        // Gán các hành động cho nút sử dụng các phương thức trong FileHandler
        introButton.setOnAction(e -> fileHandler.introduce());
        noteButton.setOnAction(e -> fileHandler.note());

        // Sắp xếp tổng thể - kết hợp sidebar và nội dung chính
        BorderPane root = new BorderPane();
        root.setLeft(sidebar); // Đặt sidebar ở bên trái
        root.setCenter(mainContent); // Đặt nội dung chính ở giữa

        // Cài đặt Scene và hiển thị Stage
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Hiển thị thông tin tài liệu
    private void showBookInfo(Document document) {
        bookInfoArea.setText(document.toString()); // Hiển thị chi tiết tài liệu
    }

    // Phương thức tải dữ liệu tài liệu từ Google Books API
    private void loadDocumentsFromAPI() {
        new Thread(() -> {
            int startIndex = 0;
            int totalItems = 537; // Tổng số tài liệu cần lấy
            int maxResults = 40;

            while (startIndex < totalItems) {
                try {
                    String url = "https://www.googleapis.com/books/v1/volumes?q=isbn%30&startIndex=" + startIndex + "&maxResults=" + maxResults;
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); // Dùng để đọc phản hồi từ API theo dòng.
                    StringBuilder response = new StringBuilder(); // Dùng để xây dựng chuỗi phản hồi JSON
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
        }).start();
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
