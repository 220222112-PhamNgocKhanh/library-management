package chinhsua;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BorrowHistoryDialog extends Stage {

  private TableView<BorrowHistory> historyTable;
  private ObservableList<BorrowHistory> historyList;
  private DatePicker borrowDateField;
  private DatePicker returnDateField;
  private ComboBox<String> statusField;
  private Button selectDocumentButton;
  private Label selectedDocumentLabel;

  private int selectedBorrowerId;
  private int selectedDocumentId;

  public BorrowHistoryDialog(int borrowerId,Main mainInstance) {
    this.selectedBorrowerId = borrowerId;

    Stage historyStage = new Stage();
    historyStage.initModality(Modality.APPLICATION_MODAL);
    historyStage.setTitle("Lịch sử mượn");

    // Initialize table
    historyTable = new TableView<>();
    historyList = FXCollections.observableArrayList();

    TableColumn<BorrowHistory, Integer> idDocumentCol = new TableColumn<>("ID Sách");
    idDocumentCol.setCellValueFactory(new PropertyValueFactory<>("idDocument"));

    TableColumn<BorrowHistory, String> titleCol = new TableColumn<>("Tên Sách");
    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

    TableColumn<BorrowHistory, Date> borrowDateCol = new TableColumn<>("Ngày mượn");
    borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));

    TableColumn<BorrowHistory, Date> returnDateCol = new TableColumn<>("Ngày trả");
    returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));

    TableColumn<BorrowHistory, String> statusCol = new TableColumn<>("Trạng thái");
    statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

    historyTable.getColumns()
        .addAll(idDocumentCol, titleCol, borrowDateCol, returnDateCol, statusCol);
    loadHistoryForBorrower();

    // UI để chọn tài liệu
    selectDocumentButton = new Button("Chọn tài liệu");
    selectedDocumentLabel = new Label("Chưa chọn tài liệu");

    selectDocumentButton.setOnAction(e -> showDocumentSelectionDialog());

    borrowDateField = new DatePicker();
    returnDateField = new DatePicker();

    statusField = new ComboBox<>();
    statusField.getItems().addAll("borrowed", "returned", "overdue");

    // Layout for form
    HBox formBox = new HBox(10, selectDocumentButton, selectedDocumentLabel, borrowDateField,
        returnDateField, statusField);
    formBox.setAlignment(Pos.CENTER);

    // Buttons for add, edit, delete
    HBox buttonBox = new HBox(20);
    Button addButton = new Button("Thêm");
    Button editButton = new Button("Sửa");
    Button deleteButton = new Button("Xóa");
    Button cancelButton = new Button("Thoát");
    buttonBox.getChildren().addAll(addButton, editButton, deleteButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER);

    // Main layout
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(10));
    layout.getChildren().addAll(historyTable, formBox, buttonBox);

    // Scene and Stage
    Scene scene = new Scene(layout, 900, 700);
    historyStage.setScene(scene);
    historyStage.show();

   //nut them
    addButton.setOnAction(e -> {
      addNewHistory();
      mainInstance.loadDocumentsFromDatabase();
    });
    //nut sua
    editButton.setOnAction(e -> {
      BorrowHistory selected = historyTable.getSelectionModel().getSelectedItem();
      if(selected == null) {
        showAlert("Lỗi", "Vui lòng chọn lịch sử để xóa!", AlertType.ERROR);
        return;
      }
      editHistory(selected,mainInstance);
    });
    //nut xoa
    deleteButton.setOnAction(e -> {
      BorrowHistory selected = historyTable.getSelectionModel().getSelectedItem();
      if(selected == null) {
        showAlert("Lỗi", "Vui lòng chọn lịch sử để xóa!", AlertType.ERROR);
        return;
      }
      deleteHistory(selected,mainInstance);
    });
    cancelButton.setOnAction(e -> historyStage.close());
  }

  private void showDocumentSelectionDialog() {
    Stage documentStage = new Stage();
    documentStage.initModality(Modality.APPLICATION_MODAL);
    documentStage.setTitle("Chọn tài liệu");

    TableView<Document> documentTable = new TableView<>();
    ObservableList<Document> documentList = FXCollections.observableArrayList();

    TableColumn<Document, Integer> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("idDocument"));

    TableColumn<Document, String> titleCol = new TableColumn<>("Tên tài liệu");
    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

    TableColumn<Document, String> authorCol = new TableColumn<>("Tác giả");
    authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));

    documentTable.getColumns().addAll(idCol, titleCol, authorCol);
    documentTable.setItems(documentList);
    loadDocuments(documentList);

    Button selectButton = new Button("Chọn");
    selectButton.setOnAction(e -> {
      Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();
      selectedDocumentId = selectedDocument.getIdDocument();
      if (selectedDocument != null) {
        selectedDocumentLabel.setText(
            "ID Sách: " + selectedDocument.getIdDocument() + " - " + selectedDocument.getTitle());
        documentStage.close();
      } else {
        showAlert("Lỗi", "Vui lòng chọn tài liệu!", AlertType.ERROR);
      }
    });

    VBox layout = new VBox(10, documentTable, selectButton);
    layout.setPadding(new Insets(10));
    Scene scene = new Scene(layout, 400, 400);
    documentStage.setScene(scene);
    documentStage.show();
  }

  /**
   * cap nhat bang lich su tu database
   */
  private void loadHistoryForBorrower() {
    historyList.clear();
    String query = """
            SELECT bh.idDocument, d.title, bh.borrowDate, bh.returnDate, bh.status
            FROM borrow_history bh
            JOIN document d ON bh.idDocument = d.idDocument
            WHERE bh.idBorrower = ?
        """;

    try (Connection connection = new ApiAndDatabase().getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {

      preparedStatement.setInt(1, selectedBorrowerId);
      ResultSet resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        int idDocument = resultSet.getInt("idDocument");
        String title = resultSet.getString("title");
        Date borrowDate = resultSet.getDate("borrowDate");
        Date returnDate = resultSet.getDate("returnDate");
        String status = resultSet.getString("status");

        BorrowHistory history = new BorrowHistory(idDocument, title, borrowDate, returnDate,
            status);
        historyList.add(history);
      }

      historyTable.setItems(historyList);

    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể tải dữ liệu từ database!", AlertType.ERROR);
    }
  }


  private void loadDocuments(ObservableList<Document> documentList) {
    String query = "SELECT idDocument, title, author FROM document";
    try (Connection connection = new ApiAndDatabase().getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query)) {

      while (resultSet.next()) {
        int id = resultSet.getInt("idDocument");
        String title = resultSet.getString("title");
        String author = resultSet.getString("author");

        documentList.add(new Document(id, title, author));
      }
    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể tải danh sách tài liệu!", AlertType.ERROR);
    }
  }


  /**
   * Add new borrow history entry
   */
  private void addNewHistory() {
    if (borrowDateField.getValue() == null || returnDateField.getValue() == null || statusField.getValue() == null) {
      showAlert("Lỗi", "Vui lòng điền đầy đủ các trường thông tin!", AlertType.ERROR);
      return;
    }

    try (Connection connection = new ApiAndDatabase().getConnection()) {
      connection.setAutoCommit(false); // Bắt đầu giao dịch

      // 1. Thêm dữ liệu mới vào bảng borrow_history
      String insertQuery = "INSERT INTO borrow_history (idDocument, idBorrower, borrowDate, returnDate, status) VALUES (?, ?, ?, ?, ?)";
      try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
        insertStatement.setInt(1, selectedDocumentId);
        insertStatement.setInt(2, selectedBorrowerId);
        insertStatement.setDate(3, Date.valueOf(borrowDateField.getValue()));
        insertStatement.setDate(4, Date.valueOf(returnDateField.getValue()));
        insertStatement.setString(5, statusField.getValue());
        insertStatement.executeUpdate();
      }

      // 2. Trừ số lượng sách trong bảng document
      String updateQuery = "UPDATE document SET quantity = quantity - 1 WHERE idDocument = ? AND quantity > 0";
      try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
        updateStatement.setInt(1, selectedDocumentId);
        int rowsAffected = updateStatement.executeUpdate();

        if (rowsAffected == 0) {
          // Không có sách để mượn
          connection.rollback(); // Hoàn tác thay đổi nếu không trừ được số lượng
          showAlert("Lỗi", "Không còn đủ số lượng sách để mượn!", AlertType.ERROR);
          return;
        }
      }

      connection.commit();
      loadHistoryForBorrower();
    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể thêm dữ liệu vào database!", AlertType.ERROR);
    }
  }


  /**
   * Edit selected borrow history
   */
  /**
   * Edit selected borrow history with a new window
   */
  private void editHistory(BorrowHistory borrowHistory,Main mainInstance) {
    Stage editStage = new Stage();
    editStage.initModality(Modality.APPLICATION_MODAL);
    editStage.setTitle("Chỉnh sửa lịch sử mượn");

    // Fields for editing
    DatePicker editBorrowDateField = new DatePicker(
        borrowHistory.getBorrowDate().toLocalDate());
    DatePicker editReturnDateField = new DatePicker(
        borrowHistory.getReturnDate().toLocalDate());
    ComboBox<String> editStatusField = new ComboBox<>();
    editStatusField.getItems().addAll("borrowed", "returned", "overdue");
    editStatusField.setValue(borrowHistory.getStatus());

    // Layout for edit form
    VBox editForm = new VBox(10);
    editForm.setPadding(new Insets(10));
    editForm.getChildren().addAll(
        new Label("Ngày mượn:"), editBorrowDateField,
        new Label("Ngày trả:"), editReturnDateField,
        new Label("Trạng thái:"), editStatusField
    );

    // Buttons for save and cancel
    Button saveButton = new Button("Lưu");
    Button cancelButton = new Button("Hủy");
    HBox buttonBox = new HBox(10, saveButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER);

    VBox layout = new VBox(10, editForm, buttonBox);
    layout.setPadding(new Insets(10));
    Scene editScene = new Scene(layout, 600, 400);
    editStage.setScene(editScene);
    editStage.show();

    // Handle save action
    saveButton.setOnAction(e -> {
      try (Connection connection = new ApiAndDatabase().getConnection()) {
        connection.setAutoCommit(false);

        String oldStatus = borrowHistory.getStatus();
        String newStatus = editStatusField.getValue();

        // Update the borrow history
        String query = """
                UPDATE borrow_history 
                SET borrowDate = ?, returnDate = ?, status = ? 
                WHERE idDocument = ? AND idBorrower = ?
            """;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setDate(1, Date.valueOf(editBorrowDateField.getValue()));
        preparedStatement.setDate(2, Date.valueOf(editReturnDateField.getValue()));
        preparedStatement.setString(3, newStatus);
        preparedStatement.setInt(4, borrowHistory.getIdDocument());
        preparedStatement.setInt(5, selectedBorrowerId);
        preparedStatement.executeUpdate();

        // Handle book quantity based on status change
        if (!oldStatus.equals(newStatus)) {
          String quantityUpdateQuery = null;

          if (oldStatus.equals("returned") && (newStatus.equals("borrowed") || newStatus.equals("overdue"))) {
            // Decrease quantity when changing from returned to borrowed/overdue
            quantityUpdateQuery = "UPDATE document SET quantity = quantity - 1 WHERE idDocument = ?";
          } else if ((oldStatus.equals("borrowed") || oldStatus.equals("overdue")) && newStatus.equals("returned")) {
            // Increase quantity when changing from borrowed/overdue to returned
            quantityUpdateQuery = "UPDATE document SET quantity = quantity + 1 WHERE idDocument = ?";
          }

          if (quantityUpdateQuery != null) {
            PreparedStatement quantityStatement = connection.prepareStatement(quantityUpdateQuery);
            quantityStatement.setInt(1, borrowHistory.getIdDocument());
            quantityStatement.executeUpdate();
          }
        }

        connection.commit();
        loadHistoryForBorrower();
        showAlert("Thành công", "Chỉnh sửa thành công!", AlertType.INFORMATION);
        mainInstance.loadDocumentsFromDatabase();
        editStage.close();
      } catch (SQLException ex) {
        showAlert("Lỗi", "Không thể sửa dữ liệu trong database!", AlertType.ERROR);
      } catch (NullPointerException ex) {
        showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin!", AlertType.ERROR);
      }
    });

    cancelButton.setOnAction(e -> editStage.close());
  }




  /**
   * Delete selected borrow history
   */
  private void deleteHistory(BorrowHistory borrowHistory, Main mainInstance) {
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Xác nhận xóa");
    confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa?");
    confirmAlert.setContentText("ID Sách: " + borrowHistory.getIdDocument() + "\nID Người mượn: " + selectedBorrowerId);

    confirmAlert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try (Connection connection = new ApiAndDatabase().getConnection()) {
          connection.setAutoCommit(false);

          // Delete the borrow history
          String deleteQuery = "DELETE FROM borrow_history WHERE idDocument = ? AND idBorrower = ?";
          PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
          deleteStatement.setInt(1, borrowHistory.getIdDocument());
          deleteStatement.setInt(2, selectedBorrowerId);
          deleteStatement.executeUpdate();

          // Handle book quantity if status is returned
          if (borrowHistory.getStatus().equals("borrowed") || borrowHistory.getStatus().equals("overdue")) {
            String quantityUpdateQuery = "UPDATE document SET quantity = quantity + 1 WHERE idDocument = ?";
            PreparedStatement quantityStatement = connection.prepareStatement(quantityUpdateQuery);
            quantityStatement.setInt(1, borrowHistory.getIdDocument());
            quantityStatement.executeUpdate();
          }

          connection.commit();
          loadHistoryForBorrower();
          showAlert("Thành công", "Xóa lịch sử mượn thành công!", AlertType.INFORMATION);
          mainInstance.loadDocumentsFromDatabase();
        } catch (SQLException e) {
          showAlert("Lỗi", "Không thể xóa dữ liệu trong database!", AlertType.ERROR);
        }
      }
    });
  }


  /**
   * Show alert dialog
   */
  private void showAlert(String title, String message, AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

}
