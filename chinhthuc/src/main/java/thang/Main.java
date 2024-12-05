package thang;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

  private TableView<Document> table;
  private TextArea bookInfoArea;
  private TextField searchField;
  private ArrayList<Document> documentList;
  private ArrayList<User> userList;
  private ObservableList<Document> observableDocumentList;

  // Constructor nhận các tham số truyền vào
  public Main(Stage primaryStage, ArrayList<User> userList,
      ObservableList<Document> observableDocumentList) {
    this.userList = userList;
    this.observableDocumentList = observableDocumentList;
    this.documentList = new ArrayList<>(
        observableDocumentList); // Đồng bộ dữ liệu từ observableDocumentList

    // Gọi phương thức để tải tài liệu từ API khi khởi chạy
    new Thread(() -> {
      ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
      apiAndDatabase.loadDocumentsFromAPI();
      loadDocumentsFromDatabase();
    }).start();
    // Thiết lập giao diện
    setupUI(primaryStage);
  }

  // Tạo giao diện
  private void setupUI(Stage primaryStage) {
    primaryStage.setTitle("Thư viện");

    // Tạo thanh tìm kiếm
    HBox searchPanel = createSearchPanel();

    // Tạo các nút chức năng
    HBox buttonPanel = createButtonPanel(primaryStage);

    // Tạo bảng và khu vực thông tin chi tiết
    createTable();
    VBox infoPanel = createInfoPanel();

    // Bố trí bảng và khu vực thông tin chi tiết vào SplitPane
    SplitPane contentPanel = new SplitPane();
    contentPanel.getItems().addAll(new ScrollPane(table), infoPanel);
    contentPanel.setDividerPositions(0.5);
    contentPanel.setStyle("-fx-divider-width: 2px; -fx-background-color: transparent;");

    // Bố trí tất cả các thành phần vào BorderPane
    BorderPane root = new BorderPane();
    root.setTop(searchPanel);
    root.setCenter(buttonPanel);
    root.setBottom(contentPanel);

    // Tạo Scene và gắn vào Stage
    Scene scene = new Scene(root, 800, 600);
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  // Tạo thanh tìm kiếm
  private HBox createSearchPanel() {
    HBox searchPanel = new HBox(10);
    searchPanel.setAlignment(Pos.CENTER_LEFT);
    searchPanel.setPadding(new Insets(10, 10, 10, 10));

    searchField = new TextField();
    searchField.setPromptText("Search...");
    searchField.setPrefWidth(400);
    searchField.setStyle("-fx-background-radius: 15;");

    Button searchButton = new Button("Search");
    searchButton.setStyle(
        "-fx-background-color: linear-gradient(to bottom right, #00FFD1, #0099FF);"
            + "-fx-font-size: 11px;"
            + "-fx-text-fill: white;"
            + "-fx-font-weight: bold;"
            + "-fx-background-radius: 15;");

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
          table.getItems()
              .setAll(documentList); // Hiển thị lại toàn bộ danh sách nếu không có kết quả
        }
      } else {
        showAlert("Vui lòng nhập từ khóa tìm kiếm!");
        table.getItems().setAll(documentList); // Hiển thị toàn bộ danh sách khi không nhập từ khóa
      }
    });

    searchPanel.getChildren().addAll(searchField, searchButton);
    return searchPanel;
  }

  // Tạo các nút chức năng
  private HBox createButtonPanel(Stage primaryStage) {
    HBox buttonPanel = new HBox(20);
    buttonPanel.setAlignment(Pos.CENTER);
    buttonPanel.setPadding(new Insets(10, 10, 10, 6));

    // Nút Thêm
    Button btnAdd = new Button("Thêm tài liệu");
    btnAdd.setMinWidth(180);
    btnAdd.setMinHeight(100);
    btnAdd.setStyle("-fx-background-color: linear-gradient(to right, #8A2BE2, #00BFFF);"
        + "-fx-font-size: 17px;"
        + "-fx-text-fill: white;"
        + "-fx-font-weight: bold;"
        + "-fx-background-radius: 15;");
    btnAdd.setOnAction(e -> openAddDocumentDialog(primaryStage));

    // Nút Sửa
    Button btnEdit = new Button("Sửa tài liệu");
    btnEdit.setMinWidth(180);
    btnEdit.setMinHeight(100);
    btnEdit.setStyle("-fx-background-color: linear-gradient(to right, #0000FF, #FF00FF);"
        + "-fx-font-size: 17px;"
        + "-fx-text-fill: white;"
        + "-fx-font-weight: bold;"
        + "-fx-background-radius: 15;");
    btnEdit.setOnAction(e -> openEditDocumentDialog(primaryStage));

    // Nút Xóa
    Button btnDelete = new Button("Xóa tài liệu");
    btnDelete.setMinWidth(180);
    btnDelete.setMinHeight(100);
    btnDelete.setStyle("-fx-background-color: linear-gradient(to right, #FF4500, #FFA07A);"
        + "-fx-font-size: 17px;"
        + "-fx-text-fill: white;"
        + "-fx-font-weight: bold;"
        + "-fx-background-radius: 15;");

    // Nut nhac nho
    Button remindButton = new Button("Nhắc nhở");
    remindButton.setMinWidth(180);
    remindButton.setMinHeight(100);
    remindButton.setStyle("-fx-background-color: linear-gradient(to right, #FFD700, #FF8C00);"
        + "-fx-font-size: 17px;"
        + "-fx-text-fill: white;"
        + "-fx-font-weight: bold;"
        + "-fx-background-radius: 15;"
        + "-fx-border-radius: 15;"
    );

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
            deleteDocument.deleteDocument(idDocument);
          }
          loadDocumentsFromDatabase();
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

    remindButton.setOnAction(e -> {
      new Thread(() -> {
        OverdueReminder overdueReminder = new OverdueReminder();
        overdueReminder.sendReminders();
      }).start();

    });

    buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, remindButton);
    return buttonPanel;
  }


  // Tạo bảng hiển thị danh sách tài liệu
  private void createTable() {
    table = new TableView<>();
    table.setPrefWidth(395);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    table.setStyle("-fx-background-radius: 10px; -fx-border-radius: 10px;");

    TableColumn<Document, String> titleColumn = new TableColumn<>("Tên tài liệu");
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    titleColumn.setPrefWidth(170);

    TableColumn<Document, String> statusColumn = new TableColumn<>("Trạng thái");
    statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    statusColumn.setPrefWidth(50);

    TableColumn<Document, Integer> quantityColumn = new TableColumn<>("Số lượng");
    quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    quantityColumn.setPrefWidth(50);

    table.getColumns().addAll(titleColumn, statusColumn, quantityColumn);
    table.setItems(observableDocumentList);

    table.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue != null) {
            showBookInfo(newValue);
          }
        });
  }

  // Tạo khu vực thông tin chi tiết
  private VBox createInfoPanel() {
    VBox infoPanel = new VBox();
    infoPanel.setPadding(new Insets(10));
    infoPanel.setAlignment(Pos.TOP_CENTER);
    infoPanel.setSpacing(10);
    infoPanel.setPrefWidth(300);

    infoPanel.setStyle("-fx-background-color: #E0F7FA; "
        + "-fx-background-radius: 10px; "
        + "-fx-border-radius: 10px; "
        + "-fx-border-color: #B0BEC5;");

    Label lblInfo = new Label("Thông tin:");
    lblInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ADD8E6;"
        + "-fx-background-radius: 10px;");
    lblInfo.setMaxWidth(Double.MAX_VALUE);
    lblInfo.setAlignment(Pos.CENTER);
    lblInfo.setPadding(new Insets(5));

    bookInfoArea = new TextArea();
    bookInfoArea.setEditable(false);
    bookInfoArea.setStyle("-fx-control-inner-background: #F0FFFF; -fx-background-radius: 10px;");
    bookInfoArea.setPrefWidth(280);
    bookInfoArea.setPrefHeight(200);
    bookInfoArea.setWrapText(true);

    VBox.setVgrow(bookInfoArea, Priority.ALWAYS);
    infoPanel.getChildren().addAll(lblInfo, bookInfoArea);

    return infoPanel;
  }

  // Hiển thị thông tin tài liệu
  private void showBookInfo(Document document) {
    bookInfoArea.setText(document.toString());
  }

  // Thêm tài liệu
  private void openAddDocumentDialog(Stage primaryStage) {
    AddDocumentDialog dialog = new AddDocumentDialog(primaryStage, documentList, this);
    dialog.showAndWait();
    loadDocumentsFromDatabase();
  }

  // Sửa tài liệu
  private void openEditDocumentDialog(Stage primaryStage) {
    Document selectedDocument = table.getSelectionModel().getSelectedItem();
    if (selectedDocument != null) {
      EditDocumentDialog editDialog = new EditDocumentDialog(primaryStage, documentList,
          documentList.indexOf(selectedDocument), this);
      editDialog.showAndWait();
    } else {
      showAlert("Vui lòng chọn tài liệu để sửa!");
    }
  }

  // Lấy dữ liệu từ database và thêm vào documentList
  public void loadDocumentsFromDatabase() {
    String query = "SELECT idDocument, title, author, category, status, quantity, publisher, publishedDate, description, isbn13, isbn10 FROM document";
    try (Connection connection = ApiAndDatabase.getConnection();

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

  // Cập nhật bảng
  public void updateTable() {
    observableDocumentList.setAll(documentList);
  }

  // Hiển thị thông báo
  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Thông báo");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
