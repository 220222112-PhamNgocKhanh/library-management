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
    private TextField publisherField;
    private TextField publishedDateField;
    private TextArea descriptionArea;
    private int selectedRow;
    private ArrayList<Document> documentList;
    private Main mainInstance;

    public EditDocumentDialog(Stage parent, ArrayList<Document> documentList, int selectedRow, Main mainInstance) {
        this.documentList = documentList;
        this.selectedRow = selectedRow;
        this.mainInstance = mainInstance;

        initModality(Modality.APPLICATION_MODAL);
        initOwner(parent);
        setTitle("Sửa tài liệu");

        Document document = documentList.get(selectedRow);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // Điền các trường thông tin có sẵn từ tài liệu đã chọn
        grid.add(new Label("Tên tài liệu:"), 0, 0);
        titleField = new TextField(document.getTitle());
        grid.add(titleField, 1, 0);

        grid.add(new Label("Tác giả:"), 0, 1);
        authorField = new TextField(document.getAuthor());
        grid.add(authorField, 1, 1);

        grid.add(new Label("Thể loại:"), 0, 2);
        categoryField = new TextField(document.getCategory());
        grid.add(categoryField, 1, 2);

        grid.add(new Label("Trạng thái:"), 0, 3);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Còn", "Hết");
        statusComboBox.setValue(document.getStatus());
        grid.add(statusComboBox, 1, 3);

        grid.add(new Label("Số lượng:"), 0, 4);
        quantityField = new TextField(String.valueOf(document.getQuantity()));
        grid.add(quantityField, 1, 4);

        grid.add(new Label("Nhà xuất bản:"), 0, 5);
        publisherField = new TextField(document.getPublisher());
        grid.add(publisherField, 1, 5);

        grid.add(new Label("Ngày xuất bản:"), 0, 6);
        publishedDateField = new TextField(document.getPublishedDate());
        grid.add(publishedDateField, 1, 6);

        grid.add(new Label("Mô tả:"), 0, 7);
        descriptionArea = new TextArea(document.getDescription());
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, 7, 2, 1);

        Button saveButton = new Button("Lưu");
        saveButton.setOnAction(e -> saveDocument());
        grid.add(saveButton, 0, 8);

        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());
        grid.add(cancelButton, 1, 8);

        Scene scene = new Scene(grid);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    private void saveDocument() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = statusComboBox.getValue();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String publisher = publisherField.getText().trim();
            String publishedDate = publishedDateField.getText().trim();
            String description = descriptionArea.getText().trim();

            Document document = documentList.get(selectedRow);
            document.setTitle(title);
            document.setAuthor(author);
            document.setCategory(category);
            document.setStatus(status);
            document.setQuantity(quantity);
            document.setPublisher(publisher);
            document.setPublishedDate(publishedDate);
            document.setDescription(description);

            mainInstance.updateTable();
            close();
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Vui lòng nhập đúng số lượng!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }
}
