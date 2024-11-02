package org.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

public class EditDocumentDialog extends Stage {
    private TextField titleField;
    private TextField authorField;
    private TextField categoryField;
    private ComboBox<String> statusComboBox;
    private TextField quantityField;
    private int selectedRow; // Vị trí của tài liệu được chọn trong danh sách
    private ArrayList<Document> documentList;
    private Main mainInstance; // Đối tượng Main để gọi hàm updateTable()

    public EditDocumentDialog(Stage parent, ArrayList<Document> documentList, int selectedRow, Main mainInstance) {
        this.documentList = documentList;
        this.selectedRow = selectedRow;
        this.mainInstance = mainInstance;

        initModality(Modality.APPLICATION_MODAL); // Tạo cửa sổ modal
        initOwner(parent);
        setTitle("Sửa tài liệu");

        Document document = documentList.get(selectedRow);

        // Tạo lưới chứa các trường nhập liệu
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Trường nhập tên tài liệu
        grid.add(new Label("Tên tài liệu:"), 0, 0);
        titleField = new TextField(document.getTitle());
        grid.add(titleField, 1, 0);

        // Trường nhập tác giả
        grid.add(new Label("Tác giả:"), 0, 1);
        authorField = new TextField(document.getAuthor());
        grid.add(authorField, 1, 1);

        // Trường nhập thể loại
        grid.add(new Label("Thể loại:"), 0, 2);
        categoryField = new TextField(document.getCategory());
        grid.add(categoryField, 1, 2);

        // ComboBox chọn trạng thái
        grid.add(new Label("Trạng thái:"), 0, 3);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Còn", "Hết");
        statusComboBox.setValue(document.getStatus());
        grid.add(statusComboBox, 1, 3);

        // Trường nhập số lượng
        grid.add(new Label("Số lượng:"), 0, 4);
        quantityField = new TextField(String.valueOf(document.getQuantity()));
        grid.add(quantityField, 1, 4);

        // Nút lưu thay đổi
        Button saveButton = new Button("Lưu");
        saveButton.setOnAction(e -> saveDocument());

        // Nút hủy
        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());

        // Thêm các nút vào GridPane
        grid.add(saveButton, 0, 5);
        grid.add(cancelButton, 1, 5);

        // Tạo và hiển thị cảnh
        Scene scene = new Scene(grid);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    // Phương thức lưu thay đổi vào danh sách tài liệu
    private void saveDocument() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = statusComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            // Cập nhật thông tin tài liệu trong danh sách
            Document document = documentList.get(selectedRow);
            document.setTitle(title);
            document.setAuthor(author);
            document.setCategory(category);
            document.setStatus(status);
            document.setQuantity(quantity);

            // Cập nhật bảng để hiển thị thông tin đã chỉnh sửa
            mainInstance.updateTable();

            // Đóng cửa sổ sau khi lưu
            close();
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập đúng số lượng!");
        }
    }

    // Phương thức hiển thị thông báo lỗi
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }
}
