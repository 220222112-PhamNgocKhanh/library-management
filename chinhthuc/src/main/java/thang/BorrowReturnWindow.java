package thang;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class BorrowReturnWindow extends Stage {
    private TextField searchField;
    private TableView<Document> documentTable;
    private ObservableList<Document> observableDocumentList;
    private User selectedUser; // Thêm User để cập nhật sách mượn của người dùng

    public BorrowReturnWindow(ObservableList<Document> documentList, User selectedUser) {
        this.observableDocumentList = documentList;
        this.selectedUser = selectedUser; // Gán selectedUser để quản lý sách mượn

        setTitle("Quản lý mượn/trả tài liệu");

        // Ô tìm kiếm tài liệu
        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm tài liệu theo tên hoặc ISBN");

        // Tạo bảng hiển thị danh sách tài liệu
        documentTable = new TableView<>();
        documentTable.setPrefWidth(395); // Đặt chiều rộng cố định cho bảng
        documentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Tự động dàn đều các cột
        documentTable.setStyle("-fx-background-radius: 10px; -fx-border-radius: 10px;");

        // Cột "Tên tài liệu"
        TableColumn<Document, String> titleColumn = new TableColumn<>("Tên tài liệu");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Cột "Trạng thái"
        TableColumn<Document, String> statusColumn = new TableColumn<>("Trạng thái");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cột "Số lượng"
        TableColumn<Document, Integer> quantityColumn = new TableColumn<>("Số lượng");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Thêm các cột vào bảng và thiết lập dữ liệu
        documentTable.getColumns().addAll(titleColumn, statusColumn, quantityColumn);
        documentTable.setItems(observableDocumentList);

        // Các nút chức năng mượn và trả tài liệu
        Button borrowButton = new Button("Mượn tài liệu");
        Button returnButton = new Button("Trả tài liệu");

        // Bố cục nút chức năng
        HBox buttonBox = new HBox(10, borrowButton, returnButton);
        buttonBox.setStyle("-fx-alignment: center;");

        VBox layout = new VBox(10, searchField, documentTable, buttonBox);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 600, 400);
        setScene(scene);

        // Xử lý sự kiện tìm kiếm
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDocumentList(newValue);
        });

        // Xử lý mượn/trả tài liệu
        borrowButton.setOnAction(e -> borrowDocument());
        returnButton.setOnAction(e -> returnDocument());
    }

    private void filterDocumentList(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            documentTable.setItems(observableDocumentList); // Hiển thị toàn bộ danh sách khi ô tìm kiếm trống
        } else {
            ObservableList<Document> filteredList = FXCollections.observableArrayList();
            for (Document doc : observableDocumentList) {
                if (doc.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || doc.getIsbn10().toLowerCase().contains(keyword.toLowerCase())
                        || doc.getIsbn13().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredList.add(doc);
                }
            }
            documentTable.setItems(filteredList);
        }
    }

    private void borrowDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();

        if (selectedDocument != null) {
            if (selectedDocument.getQuantity() > 0) {
                selectedUser.borrowBook(selectedDocument); // Cập nhật danh sách sách mượn của người dùng
                selectedDocument.setQuantity(selectedDocument.getQuantity() - 1); // Giảm số lượng sách
                documentTable.refresh();

                showAlert("Thông báo", "Mượn tài liệu thành công!");
            } else {
                showAlert("Thông báo", "Tài liệu đã hết!");
            }
        } else {
            showAlert("Thông báo", "Vui lòng chọn tài liệu để mượn!");
        }
    }

    private void returnDocument() {
        Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();

        if (selectedDocument != null && selectedUser.getBorrowedBooks().contains(selectedDocument)) {
            selectedUser.returnBook(selectedDocument); // Cập nhật danh sách sách mượn của người dùng
            selectedDocument.setQuantity(selectedDocument.getQuantity() + 1); // Tăng số lượng sách
            documentTable.refresh();

            showAlert("Thông báo", "Trả tài liệu thành công!");
        } else {
            showAlert("Thông báo", "Thành viên không mượn tài liệu này!");
        }
    }

    // Phương thức hiển thị thông báo
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
