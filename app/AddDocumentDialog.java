package org.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

public class AddDocumentDialog extends Stage {
    private TextField titleField;
    private TextField authorField;
    private TextField categoryField;
    private ComboBox<String> statusComboBox;
    private TextField quantityField;
    private ArrayList<Document> documentList;
    private Main mainInstance; // Đối tượng Main để gọi hàm updateTable()

    public AddDocumentDialog(Stage parent, ArrayList<Document> documentList, Main mainInstance) {
        this.documentList = documentList;
        this.mainInstance = mainInstance;

        initModality(Modality.APPLICATION_MODAL); // Tạo cửa sổ dạng modal
        initOwner(parent);
        setTitle("Thêm tài liệu mới");

        // Tạo layout dạng lưới
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Trường nhập tên tài liệu
        grid.add(new Label("Tên tài liệu:"), 0, 0);
        titleField = new TextField();
        grid.add(titleField, 1, 0);

        // Trường nhập tác giả
        grid.add(new Label("Tác giả:"), 0, 1);
        authorField = new TextField();
        grid.add(authorField, 1, 1);

        // Trường nhập thể loại
        grid.add(new Label("Thể loại:"), 0, 2);
        categoryField = new TextField();
        grid.add(categoryField, 1, 2);

        // ComboBox chọn trạng thái
        grid.add(new Label("Trạng thái:"), 0, 3);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Còn", "Hết");
        grid.add(statusComboBox, 1, 3);

        // Trường nhập số lượng
        grid.add(new Label("Số lượng:"), 0, 4);
        quantityField = new TextField();
        grid.add(quantityField, 1, 4);

        // Nút thêm tài liệu
        Button addButton = new Button("Thêm");
        addButton.setOnAction(e -> addDocument());

        // Nút đóng
        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());

        // Thêm các nút vào GridPane
        grid.add(addButton, 0, 5);
        grid.add(cancelButton, 1, 5);

        // Tạo và hiển thị Scene
        Scene scene = new Scene(grid);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    // Phương thức thêm tài liệu mới vào documentList
    private void addDocument() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = statusComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            // Tạo tài liệu mới và thêm vào danh sách
            Document newDocument = new Document(title, author, category, status, quantity);
            documentList.add(newDocument);

            // Cập nhật bảng hiển thị
            mainInstance.updateTable();

            // Đóng cửa sổ sau khi thêm thành công
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
