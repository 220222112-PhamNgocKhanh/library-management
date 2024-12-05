package thang;

import java.sql.*;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    Main mainInstance;

    public BorrowHistoryDialog(int borrowerId, Main mainInstance) {
        this.selectedBorrowerId = borrowerId;
        this.mainInstance = mainInstance;

        Stage historyStage = new Stage();
        historyStage.initModality(Modality.APPLICATION_MODAL);
        historyStage.setTitle("Lịch sử mượn");

        // Initialize table
        historyTable = new TableView<>();
        historyList = FXCollections.observableArrayList();

        TableColumn<BorrowHistory, Integer> idDocumentCol = new TableColumn<>("ID");
        idDocumentCol.setCellValueFactory(new PropertyValueFactory<>("idDocument"));
        idDocumentCol.setPrefWidth(50);

        TableColumn<BorrowHistory, String> titleCol = new TableColumn<>("Tên Sách");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(530);

        TableColumn<BorrowHistory, Date> borrowDateCol = new TableColumn<>("Ngày mượn");
        borrowDateCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowDateCol.setPrefWidth(100);

        TableColumn<BorrowHistory, Date> returnDateCol = new TableColumn<>("Ngày trả");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        returnDateCol.setPrefWidth(100);

        TableColumn<BorrowHistory, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
//    statusCol.setPrefWidth(100);

        historyTable.getColumns()
            .addAll(idDocumentCol, titleCol, borrowDateCol, returnDateCol, statusCol);
        loadHistoryForBorrower();
        historyTable.setRowFactory(tv -> new TableRow<BorrowHistory>() {
            @Override
            protected void updateItem(BorrowHistory item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    getStyleClass().removeAll("borrowed", "overdue", "returned");
                    setStyle("");  // Đảm bảo reset style
                } else {
                    getStyleClass().removeAll("borrowed", "overdue", "returned");
                    getStyleClass().add(item.getStatus());
                }
            }
        });


        // Buttons for add, edit, delete
        HBox buttonBox = new HBox(20);
        Button returnButton = new Button("Trả");
        returnButton.setPrefWidth(150);
        returnButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

        Button editButton = new Button("Sửa");
        editButton.setPrefWidth(150);
        editButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

        Button deleteButton = new Button("Xóa");
        deleteButton.setPrefWidth(150);
        deleteButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

        Button cancelButton = new Button("Thoát");
        cancelButton.setPrefWidth(150);
        cancelButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

        buttonBox.getChildren().addAll(returnButton, editButton, deleteButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Main layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        layout.getChildren().addAll(historyTable, buttonBox);

        // Scene and Stage
        Scene scene = new Scene(layout, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        historyStage.setScene(scene);
        historyStage.show();


        //Nut tra
        returnButton.setOnAction(e -> {
            BorrowHistory selectedHistory = historyTable.getSelectionModel()
                .getSelectedItem(); // Lấy lịch sử mượn được chọn
            if (selectedHistory != null) {
                returnDocument(selectedHistory, selectedBorrowerId);
            } else {
                showAlert("Lỗi", "Vui lòng chọn một lịch sử mượn để trả!", Alert.AlertType.ERROR);
            }
        });

        //nut sua
        editButton.setOnAction(e -> {
            BorrowHistory selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Lỗi", "Vui lòng chọn lịch sử để sửa!", AlertType.ERROR);
                return;
            }
            editHistory(selected);
            try{
                mainInstance.loadDocumentsFromDatabase();
            }
            catch (NullPointerException ex){

            }
        });
        //nut xoa
        deleteButton.setOnAction(e -> {
            BorrowHistory selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Lỗi", "Vui lòng chọn lịch sử để xóa!", AlertType.ERROR);
                return;
            }
            deleteHistory(selected);
            try{
                mainInstance.loadDocumentsFromDatabase();
            }
            catch (NullPointerException ex){

            }
        });
        cancelButton.setOnAction(e -> historyStage.close());



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

    /**
     * phuong thuc tra sach
     *
     * @param borrowHistory lịch sử trả
     * @param borrowerId    mã người mượn
     */
    private void returnDocument(BorrowHistory borrowHistory, int borrowerId) {
        if (borrowHistory == null) {
            showAlert("Lỗi", "Không có tài liệu nào được chọn!", Alert.AlertType.ERROR);
            return;
        }

        String currentStatus = borrowHistory.getStatus();

        if ("returned".equals(currentStatus)) {
            showAlert("Thông báo", "Tài liệu này đã được trả trước đó!", Alert.AlertType.INFORMATION);
            return;
        }

        try (Connection connection = new ApiAndDatabase().getConnection()) {
            connection.setAutoCommit(false);

            // Cập nhật trạng thái thành 'returned'
            String updateStatusQuery = """
              UPDATE borrow_history
              SET status = ?, returnDate = ?
              WHERE idDocument = ? AND idBorrower = ?
          """;
            PreparedStatement updateStatusStmt = connection.prepareStatement(updateStatusQuery);
            updateStatusStmt.setString(1, "returned");
            updateStatusStmt.setDate(2, Date.valueOf(LocalDate.now())); // Ngày trả là hôm nay
            updateStatusStmt.setInt(3, borrowHistory.getIdDocument());
            updateStatusStmt.setInt(4, borrowerId);
            updateStatusStmt.executeUpdate();

            // Tăng số lượng tài liệu trong bảng document
            String updateQuantityQuery = "UPDATE document SET quantity = quantity + 1 WHERE idDocument = ?";
            PreparedStatement updateQuantityStmt = connection.prepareStatement(updateQuantityQuery);
            updateQuantityStmt.setInt(1, borrowHistory.getIdDocument());
            updateQuantityStmt.executeUpdate();

            connection.commit();

            // Load lại dữ liệu và thông báo thành công
            loadHistoryForBorrower(); // Phương thức load lại danh sách lịch sử
            mainInstance.loadDocumentsFromDatabase(); // Phương thức load lại danh sách tài liệu
            showAlert("Thành công", "Tài liệu đã được trả thành công!", Alert.AlertType.INFORMATION);
        } catch (SQLException ex) {
            showAlert("Lỗi", "Không thể cập nhật dữ liệu trong cơ sở dữ liệu!", Alert.AlertType.ERROR);
        }
        catch (NullPointerException ex) {

        }
    }

    /**
     * Edit selected borrow history
     */
    /**
     * Edit selected borrow history with a new window
     */
    private void editHistory(BorrowHistory borrowHistory) {
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
                LocalDate editBorrowDate = editBorrowDateField.getValue();
                String newStatus = editStatusField.getValue();
                LocalDate editedReturnDate = editReturnDateField.getValue();
                LocalDate currentDate = LocalDate.now();

                // Kiểm tra nếu ngày trả chỉnh sửa xuống dưới ngày hiện tại, cập nhật trạng thái thành "overdue"
                if (editedReturnDate.isBefore(currentDate) && oldStatus == "borrowed") {
                    newStatus = "overdue";  // Cập nhật trạng thái thành "overdue"
                    editStatusField.setValue(newStatus);  // Cập nhật giá trị ComboBox
                }

                // Cập nhật lịch sử mượn
                String query = """
        UPDATE borrow_history 
        SET borrowDate = ?, returnDate = ?, status = ? 
        WHERE idDocument = ? AND idBorrower = ?
        """;
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setDate(1, Date.valueOf(editBorrowDate));
                preparedStatement.setDate(2, Date.valueOf(editReturnDateField.getValue()));
                preparedStatement.setString(3, newStatus);
                preparedStatement.setInt(4, borrowHistory.getIdDocument());
                preparedStatement.setInt(5, selectedBorrowerId);
                preparedStatement.executeUpdate();

                // Xử lý số lượng sách dựa trên thay đổi trạng thái
                if (!oldStatus.equals(newStatus)) {
                    String quantityUpdateQuery = null;

                    if (oldStatus.equals("returned") && (newStatus.equals("borrowed") || newStatus.equals("overdue"))) {
                        // Giảm số lượng khi thay đổi từ "returned" sang "borrowed" hoặc "overdue"
                        quantityUpdateQuery = "UPDATE document SET quantity = quantity - 1 WHERE idDocument = ?";
                    } else if ((oldStatus.equals("borrowed") || oldStatus.equals("overdue"))
                        && newStatus.equals("returned")) {
                        // Tăng số lượng khi thay đổi từ "borrowed" hoặc "overdue" sang "returned"
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
                editStage.close();
            } catch (SQLException ex) {
                showAlert("Lỗi", "Không thể sửa dữ liệu trong database!", AlertType.ERROR);
            }
        });


        cancelButton.setOnAction(e -> editStage.close());
    }


    /**
     * Delete selected borrow history
     */
    /**
     * Delete selected borrow history
     */
    private void deleteHistory(BorrowHistory borrowHistory) {
        // Kiểm tra trạng thái của lịch sử mượn
        if (borrowHistory.getStatus().equals("borrowed") || borrowHistory.getStatus().equals("overdue")) {
            showAlert("Cảnh báo",
                "Không thể xóa lịch sử mượn khi sách vẫn ở trạng thái 'borrowed' hoặc 'overdue'.\n" +
                    "Vui lòng chỉnh trạng thái về 'returned' trước khi xóa.",
                Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa?");
        confirmAlert.setContentText(
            "ID Sách: " + borrowHistory.getIdDocument() + "\nID Người mượn: " + selectedBorrowerId);

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

                    connection.commit();
                    loadHistoryForBorrower();
                    showAlert("Thành công", "Xóa lịch sử mượn thành công!", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    showAlert("Lỗi", "Không thể xóa dữ liệu trong database!", Alert.AlertType.ERROR);
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
