package thang;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

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
        // Đặt biểu tượng cho cửa sổ
        Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
        primaryStage.getIcons().add(icon);

        // Gọi phương thức để tải tài liệu từ API khi khởi chạy
        ApiToDatabase apiToDatabase = new ApiToDatabase();
        apiToDatabase.loadDocumentsFromAPI();
        loadDocumentsFromDatabase();

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

        ListView<String> suggestionList = new ListView<>();
        suggestionList.setVisible(false); // Ban đầu ẩn danh sách
        suggestionList.setPrefHeight(200); // Chiều cao tối đa của danh sách
        suggestionList.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 5;");

        // Tạo StackPane để chứa thanh tìm kiếm và các nút chức năng
        StackPane searchOverlay = new StackPane();

        searchField = new TextField();
        searchField.setPromptText("Search...");
        //searchField.setPrefWidth(400); // Chiều rộng của ô tìm kiếm
        searchField.setStyle("-fx-background-radius: 15;"); // Bo tròn góc

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: linear-gradient(to bottom right, #00FFD1, #0099FF);"
                + "-fx-font-size: 11px;"
                + "-fx-text-fill: white;" // Màu chữ trắng
                + "-fx-font-weight: bold;" // Chữ in đậm
                + "-fx-background-radius: 15;" // Bo tròn góc
        );

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                suggestionList.setVisible(false);
            } else {
                ObservableList<String> filteredResults = FXCollections.observableArrayList();
                for (Document doc : documentList) {
                    if (doc.getTitle().toLowerCase().contains(newValue.toLowerCase()) ||
                            doc.getAuthor().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredResults.add(doc.getTitle() + " - " + doc.getAuthor());
                    }
                }
                suggestionList.setItems(filteredResults);
                suggestionList.setVisible(!filteredResults.isEmpty());
            }
        });
        suggestionList.setOnMouseClicked(event -> {
            String selectedItem = suggestionList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                searchField.setText(selectedItem.split(" - ")[0]); // Đặt lại tiêu đề vào ô tìm kiếm
                suggestionList.setVisible(false); // Ẩn danh sách gợi ý
            }
        });

        /* searchField.setOnAction(e -> {
            searchButton.fire(); // Kích hoạt sự kiện cho nút search
        }); */

        // Tạo ImageView để hiển thị logo
        ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/logo.png")));
        logoView.setFitWidth(100); // Đặt chiều rộng ban đầu cho logo
        logoView.setFitHeight(100); // Đặt chiều cao ban đầu cho logo
        logoView.setPreserveRatio(true); // Bảo toàn tỷ lệ ảnh

        // Thêm logo vào HBox với ô tìm kiếm và nút search
        //searchPanel.getChildren().add(0, logoView); // Thêm logo vào vị trí đầu tiên của HBox
        //searchPanel.getChildren().addAll(searchField, searchButton); // Thêm ô tìm kiếm và nút tìm kiếm vào searchPanel

        // Tạo các nút thêm, sửa, xóa, mượn/trả tài liệu
        HBox buttonPanel = new HBox(20); // Sử dụng HBox cho layout ngang
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10, 10, 10, 6));

        // Đảm bảo logo không co giãn, còn thanh tìm kiếm sẽ giãn nở khi phóng to
        HBox.setHgrow(searchField, Priority.ALWAYS);
        HBox.setHgrow(searchButton, Priority.NEVER);
        searchPanel.getChildren().addAll(logoView, searchField, searchButton);

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

        // Đặt thanh tìm kiếm, danh sách gợi ý và các nút vào StackPane
        searchOverlay.getChildren().addAll(buttonPanel, suggestionList);
// Đặt vị trí cho ListView để nó đè lên thanh tìm kiếm và nút
        StackPane.setAlignment(suggestionList, Pos.TOP_CENTER);
        suggestionList.setTranslateY(-43); // Điều chỉnh để gợi ý xuất hiện ngay dưới thanh tìm kiếm
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
        //VBox searchContainer = new VBox(5, searchPanel, suggestionList);
        //searchContainer.setPadding(new Insets(10));

// Thêm searchContainer vào phần nội dung chính
        //VBox mainContent = new VBox(10, searchContainer, buttonPanel, contentPanel);
        //mainContent.setPadding(new Insets(10));

        VBox mainContent = new VBox(10, searchPanel, searchOverlay, contentPanel);
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
            loadDocumentsFromDatabase();
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
                int idDocument = selectedDocument.getIdDocument();
                DeleteDocument deleteDocument = new DeleteDocument();

                // Hiển thị xác nhận trước khi xóa
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Xác nhận xóa");
                confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa tài liệu này?");
                confirmAlert.setContentText(
                        "Mã:" + selectedDocument.getIdDocument() + "\nTên: " + selectedDocument.getTitle());

                // Xử lý kết quả xác nhận
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean success = deleteDocument.deleteDocument(idDocument);

                        if (success) {
                            // Hiển thị thông báo xóa thành công
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Thành công");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Tài liệu đã được xóa!");
                            successAlert.showAndWait();

                            // Làm mới bảng dữ liệu
                            loadDocumentsFromDatabase();
                        } else {
                            // Hiển thị thông báo xóa thất bại
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Lỗi");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Không thể xóa tài liệu. Vui lòng thử lại.");
                            errorAlert.showAndWait();
                        }
                    }
                });
            } else {
                // Hiển thị thông báo khi chưa chọn tài liệu
                Alert warningAlert = new Alert(Alert.AlertType.WARNING);
                warningAlert.setTitle("Cảnh báo");
                warningAlert.setHeaderText(null);
                warningAlert.setContentText("Vui lòng chọn một tài liệu để xóa.");
                warningAlert.showAndWait();
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

        // Xử lý thanh "Tìm kiếm"
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchTerm = newValue.trim().toLowerCase();
            if (!searchTerm.isEmpty()) {
                // Lọc danh sách tài liệu theo từ khóa
                List<Document> filteredDocuments = documentList.stream()
                        .filter(doc -> doc.getTitle().toLowerCase().contains(searchTerm) ||
                                doc.getAuthor().toLowerCase().contains(searchTerm) ||
                                doc.getCategory().toLowerCase().contains(searchTerm))
                        .collect(Collectors.toList());

                // Cập nhật bảng với danh sách đã lọc
                table.getItems().setAll(filteredDocuments);
            } else {
                // Hiển thị lại toàn bộ danh sách nếu không có từ khóa
                table.getItems().setAll(documentList);
            }
        });

        // Xử lý sự kiện nút "Tìm kiếm"
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().trim().toLowerCase(); // Lấy từ khóa tìm kiếm
            if (!searchTerm.isEmpty()) {
                // Lọc danh sách tài liệu theo từ khóa
                List<Document> filteredDocuments = documentList.stream()
                        .filter(doc -> doc.getTitle().toLowerCase().contains(searchTerm) ||
                                doc.getAuthor().toLowerCase().contains(searchTerm) ||
                                doc.getCategory().toLowerCase().contains(searchTerm))
                        .collect(Collectors.toList());

                // Kiểm tra kết quả và cập nhật bảng
                if (!filteredDocuments.isEmpty()) {
                    table.getItems().setAll(filteredDocuments);
                } else {
                    showAlert("Không tìm thấy tài liệu nào phù hợp với từ khóa!");
                    table.getItems().setAll(documentList); // Hiển thị lại toàn bộ danh sách nếu không có kết quả
                }
            } else {
                showAlert("Vui lòng nhập từ khóa tìm kiếm!");
                table.getItems().setAll(documentList); // Hiển thị toàn bộ danh sách khi không nhập từ khóa
            }
        });



        // Xử lý sự kiện nút "Thành viên"
        btnThanhVien.setOnAction(e -> {
            //borrowerManagementDialog borrowerManagementDialog = new borrowerManagementDialog(this);
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

        // Tạo ImageView cho từng nút chức năng
        ImageView introIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/intro.png")));
        introIcon.setFitWidth(20); // Đặt chiều rộng logo
        introIcon.setFitHeight(20); // Đặt chiều cao logo

        ImageView noteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/note.png")));
        noteIcon.setFitWidth(20);
        noteIcon.setFitHeight(20);

        ImageView calendarIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/calendar.png")));
        calendarIcon.setFitWidth(20);
        calendarIcon.setFitHeight(20);

        ImageView memberIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/member.png")));
        memberIcon.setFitWidth(20);
        memberIcon.setFitHeight(20);

// Thêm logo vào nút
        introButton.setGraphic(introIcon);
        noteButton.setGraphic(noteIcon);
        calendarButton.setGraphic(calendarIcon);
        btnThanhVien.setGraphic(memberIcon);

// Định dạng căn chỉnh nội dung và khoảng cách giữa biểu tượng và chữ
        for (Button btn : new Button[]{introButton, noteButton, calendarButton, btnThanhVien}) {
            btn.setContentDisplay(ContentDisplay.LEFT); // Đặt biểu tượng bên trái văn bản
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: CENTER_LEFT; -fx-padding: 5 10 5 5;");
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        // Sắp xếp tổng thể - kết hợp sidebar và nội dung chính
        BorderPane root = new BorderPane();
        root.setLeft(sidebar); // Đặt sidebar ở bên trái
        root.setCenter(mainContent); // Đặt nội dung chính ở giữa

        // Cài đặt Scene và hiển thị Stage
        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Hiển thị thông tin tài liệu
    private void showBookInfo(Document document) {
        bookInfoArea.setText(document.toString()); // Hiển thị chi tiết tài liệu
    }

    // Lấy dữ liệu từ database và thêm vào documentList
    public void loadDocumentsFromDatabase() {
        String query = "SELECT idDocument, title, author, category, status, quantity, publisher, publishedDate, description, isbn13, isbn10 FROM document";
        try (Connection connection = ApiToDatabase.getConnection();

             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            documentList.clear(); // Xóa danh sách cũ để đồng bộ

            while (resultSet.next()) {
                int idDocument = resultSet.getInt("idDocument");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String category = resultSet.getString("category");
                String status = resultSet.getString("status");
                int quantity = resultSet.getInt("quantity");
                String publisher = resultSet.getString("publisher");
                String publishedDate = resultSet.getString("publishedDate");
                String description = resultSet.getString("description");
                String isbn13 = resultSet.getString("isbn13");
                String isbn10 = resultSet.getString("isbn10");

                Document document = new Document(idDocument, title, author, category, status, quantity,
                        publisher, publishedDate, description, isbn13, isbn10);
                documentList.add(document);
            }

            updateTable(); // Cập nhật lại giao diện bảng
        } catch (SQLException e) {
            showAlert("lỗi kết nối");
            ; // Hoặc xử lý lỗi theo cách của bạn
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
