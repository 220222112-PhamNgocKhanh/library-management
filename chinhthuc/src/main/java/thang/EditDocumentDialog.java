package thang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
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
    private TextField isbn10Field; // Trường để chỉnh sửa ISBN 10
    private TextField isbn13Field; // Trường để chỉnh sửa ISBN 13
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

        // Đặt biểu tượng cho cửa sổ
        Image icon = new Image("/logo.png");
        getIcons().add(icon);

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

        // Trường ISBN 10
        grid.add(new Label("ISBN 10:"), 0, 7);
        isbn10Field = new TextField(document.getIsbn10());
        grid.add(isbn10Field, 1, 7);

        // Trường ISBN 13
        grid.add(new Label("ISBN 13:"), 0, 8);
        isbn13Field = new TextField(document.getIsbn13());
        grid.add(isbn13Field, 1, 8);

        grid.add(new Label("Mô tả:"), 0, 9);
        descriptionArea = new TextArea(document.getDescription());
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true); // Bật chế độ ngắt dòng tự động nếu cần
        grid.add(descriptionArea, 1, 9, 2, 1);

        Button saveButton = new Button("Lưu");
        saveButton.setOnAction(e -> saveDocument());
        grid.add(saveButton, 0, 10);

        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(e -> close());
        grid.add(cancelButton, 1, 10);

        Scene scene = new Scene(grid);
        setScene(scene);
        setResizable(false);
        sizeToScene();
    }

    private void saveDocument() {
        // Đặt biểu tượng cho cửa sổ
        Image icon = new Image("/logo.png");

        try (Connection connection = ApiAndDatabase.getConnection()) {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String status = (quantity >= 1) ? "Còn" : "Hết";
            String publisher = publisherField.getText().trim();
            String publishedDate = publishedDateField.getText().trim();
            String isbn10 = isbn10Field.getText().trim();
            String isbn13 = isbn13Field.getText().trim();
            String description = descriptionArea.getText().trim();

            Document document = documentList.get(selectedRow);

            String updateQuery = """
                UPDATE document
                SET title = ?, author = ?, category = ?, status = ?, quantity = ?, 
                    publisher = ?, publishedDate = ?, isbn10 = ?, isbn13 = ?, description = ?
                WHERE iddocument = ?
            """;

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setString(3, category);
                preparedStatement.setString(4, status);
                preparedStatement.setInt(5, quantity);
                preparedStatement.setString(6, publisher);
                preparedStatement.setString(7, publishedDate);
                preparedStatement.setString(8, isbn10);
                preparedStatement.setString(9, isbn13);
                preparedStatement.setString(10, description);
                preparedStatement.setInt(11, document.getIdDocument());

                int rowsAffected = preparedStatement.executeUpdate();
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

                if (rowsAffected > 0) {
                    mainInstance.loadDocumentsFromDatabase();
                    getIcons().add(icon);
                    showAlert("Thông báo", "Cập nhật thành công!",AlertType.INFORMATION);
                    close();
                } else {
                    getIcons().add(icon);
                    showAlert("Lỗi", "Không thể cập nhật tài liệu!",AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            getIcons().add(icon);
            showAlert("Lỗi", "Nhập đúng định dạng số lượng",AlertType.ERROR);
        } catch (SQLException e) {
            getIcons().add(icon);
            showAlert("Lỗi", "Lỗi kết nối cơ sở dữ liệu: ",AlertType.ERROR);
        }
    }


    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType); // Sử dụng kiểu thông báo được truyền vào
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(this);
        alert.showAndWait();
    }
}