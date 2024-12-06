package thang;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
  private DocumentManagement documentManagementInstance; // Đối tượng Main để gọi hàm updateTable()

  /**
   * Constructor.
   */
  public AddDocumentDialog(Stage parent, ArrayList<Document> documentList, DocumentManagement documentManagementInstance) {
    initModality(Modality.APPLICATION_MODAL);
    initOwner(parent);
    setTitle("Thêm tài liệu mới");

    // Đặt biểu tượng cho cửa sổ
    Image icon = new Image("/logo.png");
    getIcons().add(icon);

    // Tiêu đề
    Label titleLabel = new Label("Thêm tài liệu mới");
    titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    titleLabel.setStyle("-fx-text-fill: linear-gradient(to bottom, #00aaff, #0055ff);");
    titleLabel.setTextAlignment(TextAlignment.CENTER);

    // Layout chính
    GridPane grid = new GridPane();
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);

    // Thanh tìm sách
    TextField searchField = new TextField();
    Button searchButton = new Button("Tìm kiếm");
    searchButton.setOnAction(e -> searchBooks(searchField.getText().trim()));
    HBox searchBox = new HBox(10, new Label("Tìm sách:"), searchField, searchButton);
    searchBox.setAlignment(Pos.CENTER_LEFT);
    grid.add(searchBox, 0, 0, 3, 1);

    // Trường nhập tên tài liệu
    Label titleLabelField = createFixedLabel("Tên tài liệu:", 100);
    titleField = new TextField();
    grid.add(titleLabelField, 0, 1);
    grid.add(titleField, 1, 1, 2, 1);

    // ISBN 10, ISBN 13 và Số lượng
    Label isbn10Label = createFixedLabel("ISBN 10:", 100);
    isbn10Field = createFixedTextField(120);

    Label isbn13Label = createFixedLabel("ISBN 13:", 100);
    isbn13Field = createFixedTextField(120);

    Label quantityLabel = createFixedLabel("Số lượng:", 100);
    quantityField = createFixedTextField(120);

    HBox isbnBox = new HBox(10, isbn10Label, isbn10Field, isbn13Label, isbn13Field, quantityLabel,
        quantityField);
    isbnBox.setAlignment(Pos.CENTER_LEFT);
    grid.add(isbnBox, 0, 2, 3, 1);

    // Tác giả và Thể loại
    Label authorLabel = createFixedLabel("Tác giả:", 100);
    authorField = new TextField();
    Label categoryLabel = createFixedLabel("Thể loại:", 100);
    categoryField = new TextField();

    HBox authorCategoryBox = new HBox(10, authorLabel, authorField, categoryLabel, categoryField);
    authorCategoryBox.setAlignment(Pos.CENTER_LEFT);
    grid.add(authorCategoryBox, 0, 3, 3, 1);

    // Nhà xuất bản và Ngày xuất bản
    Label publisherLabel = createFixedLabel("Nhà xuất bản:", 100);
    publisherField = new TextField();
    Label publishedDateLabel = createFixedLabel("Ngày xuất bản:", 100);
    publishedDateField = new TextField();

    HBox publisherDateBox = new HBox(10, publisherLabel, publisherField, publishedDateLabel,
        publishedDateField);
    publisherDateBox.setAlignment(Pos.CENTER_LEFT);
    grid.add(publisherDateBox, 0, 4, 3, 1);

    // Trạng thái
    Label statusLabel = createFixedLabel("Trạng thái:", 100);
    statusComboBox = new ComboBox<>();
    statusComboBox.getItems().addAll("Còn", "Hết");
    grid.add(statusLabel, 0, 5);
    grid.add(statusComboBox, 1, 5);

    // Mô tả
    Label descriptionLabel = createFixedLabel("Mô tả:", 100);
    descriptionArea = new TextArea();
    descriptionArea.setPrefRowCount(3);
    descriptionArea.setWrapText(true);
    grid.add(descriptionLabel, 0, 6);
    grid.add(descriptionArea, 1, 6, 2, 1);

    // Nút thêm tài liệu và hủy
    Button addButton = new Button("Thêm");
    addButton.setOnAction(e -> addDocument());
    Button cancelButton = new Button("Hủy");
    cancelButton.setOnAction(e -> close());

    HBox buttonBox = new HBox(10, addButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    grid.add(buttonBox, 1, 7, 2, 1);

    // Đưa tất cả vào VBox
    VBox root = new VBox(10, titleLabel, grid);
    root.setPadding(new Insets(10));
    root.setAlignment(Pos.CENTER);

    // Tạo Scene
    Scene scene = new Scene(root, 700, 500);  // Giới hạn kích thước Scene
    scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    setScene(scene);
    setResizable(false);
  }

  /**
   * Thiết lập cấu trúc bảng.
   */
  private Label createFixedLabel(String text, int width) {
    Label label = new Label(text);
    label.setPrefWidth(width);
    label.setAlignment(Pos.CENTER_LEFT);
    return label;
  }

  /**
   * Thiết lập cấu trúc bảng.
   */
  private TextField createFixedTextField(int width) {
    TextField textField = new TextField();
    textField.setPrefWidth(width);
    return textField;
  }

  /**
   * Tìm kiếm.
   */
  private void searchBooks(String query) {
    // Hiển thị trạng thái đang tải
    Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
    loadingAlert.setTitle("Đang tải");
    loadingAlert.setHeaderText(null);
    loadingAlert.setContentText("Đang tìm kiếm sách, vui lòng chờ...");
    loadingAlert.initOwner(this.getOwner());
    Stage alertStage = (Stage) loadingAlert.getDialogPane().getScene().getWindow();
    alertStage.getIcons().add(new Image("/logo.png"));
    loadingAlert.show();

      Task<JSONArray> searchTask = new Task<>() {
      @Override
      protected JSONArray call() throws Exception {
        String url =
            "https://www.googleapis.com/books/v1/volumes?q=" + URLEncoder.encode(query, "UTF-8");
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream()))) {
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
        alertStage.getIcons().add(new Image("/logo.png"));
        showAlert("Thông báo", "Không tìm thấy sách.", Alert.AlertType.INFORMATION);
      }
    });

    // Xử lý khi có lỗi
    searchTask.setOnFailed(event -> {
      loadingAlert.close(); // Đóng trạng thái "Đang tải"
      Throwable error = searchTask.getException();
      error.printStackTrace();
      alertStage.getIcons().add(new Image("/logo.png"));
      showAlert("Lỗi", "Không thể tìm sách từ API.", Alert.AlertType.ERROR);
    });

    // Chạy Task trên một luồng nền
    Thread searchThread = new Thread(searchTask);
    searchThread.setDaemon(true);
    searchThread.start();
  }

  /**
   * Hiển thị kết quả tìm kiếm.
   */
  private void showSearchResults(JSONArray items) {
    // Tạo Stage mới để hiển thị kết quả tìm kiếm
    Stage resultsStage = new Stage();
    resultsStage.initModality(Modality.APPLICATION_MODAL);
    resultsStage.setTitle("Kết quả tìm kiếm");
    // Đặt biểu tượng cho cửa sổ
    Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
    resultsStage.getIcons().add(icon);

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
        resultsStage.getIcons().add(icon);
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

  /**
   * Điền thông tin sách.
   */
  private void fillBookDetails(JSONObject book) {
    JSONObject volumeInfo = book.getJSONObject("volumeInfo");

    if (volumeInfo != null) {
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
    }
  }

  /**
   * nhập thông tin vào bảng thêm tài liệu và kiểm tra ngoại lệ.
   */
  private void addDocument() {
    Image icon = new Image("/logo.png");
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
        getIcons().add(icon);
        showAlert("Lỗi", "Tên tài liệu không được để trống!", Alert.AlertType.ERROR);
        return;
      }

      if (quantity < 0) {
        getIcons().add(icon);
        showAlert("Lỗi", "Số lượng tài liệu không được nhỏ hơn 0!", Alert.AlertType.ERROR);
        return;
      }
      Document document = new Document(title, author, category, status, quantity, publisher,
          publishedDate, description, isbn13, isbn10);
      try (Connection connection = ApiAndDatabase.getConnection()) {
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
      getIcons().add(icon);
      showAlert("Lỗi", "Vui lòng nhập số lượng sách!", Alert.AlertType.ERROR);
    } catch (SQLException e) {
      getIcons().add(icon);
      showAlert("Lỗi", "Không thể kiểm tra tài liệu trong cơ sở dữ liệu.", Alert.AlertType.ERROR);
    }
  }

  /**
   * hiển thị danh sách những tài liệu trùng tên lên một cửa sổ khác và những trường xử lý.
   */
  private void showDuplicateDocuments(ResultSet resultSet, Connection connection, Document document)
      throws SQLException {
    Stage duplicateStage = new Stage();
    duplicateStage.initModality(Modality.APPLICATION_MODAL);
    duplicateStage.setTitle("Tài liệu trùng tên");

    // Đặt biểu tượng cho cửa sổ
    Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
    duplicateStage.getIcons().add(icon);

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

    tableView.getColumns()
        .addAll(idDocumentCol, titleCol, authorCol, categoryCol, quantityCol, isbn13Col, isbn10Col);

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
        documentManagementInstance.loadDocumentsFromDatabase();
      } catch (SQLException ex) {
        duplicateStage.getIcons().add(icon);
        showAlert("Lỗi", "Không thể thêm tài liệu mới.", Alert.AlertType.ERROR);
        ex.printStackTrace();
      } catch (NullPointerException ex) {

      }
    });

    // Nút "Chỉnh sửa"
    Button editButton = new Button("Chỉnh sửa");
    editButton.setOnAction(e -> {
      Document selectedDoc = tableView.getSelectionModel().getSelectedItem();
      if (selectedDoc == null) {
        duplicateStage.getIcons().add(icon);
        showAlert("Lỗi", "Vui lòng chọn tài liệu để chỉnh sửa.", Alert.AlertType.ERROR);
        return;
      }
      duplicateStage.close();
      updateDocumentInDatabase(document, selectedDoc.getIdDocument());
      try {
        documentManagementInstance.loadDocumentsFromDatabase();
      } catch (NullPointerException ex) {

      }
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

  /**
   * thêm mới tài liệu.
   */
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
      Image icon = new Image("/logo.png");
      getIcons().add(icon);
      showAlert("Thành công", "Tài liệu đã được thêm vào cơ sở dữ liệu.",
          Alert.AlertType.INFORMATION);
      close();

    }
  }

  /**
   *  cap nhat tai lieu dua vao thong tin nhap va id.
   */
  public void updateDocumentInDatabase(Document document, int id) {
    String updateQuery = "UPDATE document SET title = ?, author = ?, category = ?, status = ?, " +
        "quantity = ?, publisher = ?, publishedDate = ?, description = ?, " +
        "isbn13 = ?, isbn10 = ? WHERE idDocument = ?";
    Image icon = new Image("/logo.png");

    try (Connection connection = ApiAndDatabase.getConnection();
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
      preparedStatement.setInt(11, id);

      // Thực hiện cập nhật
      int rowsAffected = preparedStatement.executeUpdate();
      if (rowsAffected > 0) {
        getIcons().add(icon);
        showAlert("Thành công", "Cập nhật tài liệu thành công.", Alert.AlertType.INFORMATION);
      } else {
        getIcons().add(icon);
        showAlert("Thông báo", "Không tìm thấy tài liệu để cập nhật.", Alert.AlertType.WARNING);
      }
      close();
    } catch (SQLException e) {
      e.printStackTrace();
      getIcons().add(icon);
      showAlert("Lỗi", "Không thể cập nhật tài liệu.", Alert.AlertType.ERROR);
    }
  }

  private void showAlert(String title, String message, Alert.AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.initOwner(this);
    alert.showAndWait();
  }
}
