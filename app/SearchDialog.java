package org.example.app;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

public class SearchDialog extends Stage {
    private ArrayList<Document> documentList; // Danh sách tài liệu
    private TextField searchField; // Ô tìm kiếm
    private TextArea bookInfoArea; // Kết quả tìm kiếm
    private TableView<Document> table; // Bảng để hiển thị kết quả tìm kiếm
    private Stage parentStage; // Tham chiếu tới frame cha để hiển thị dialog

    public SearchDialog(Stage parent, ArrayList<Document> documentList, TableView<Document> table, TextArea bookInfoArea) {
        this.documentList = documentList;
        this.table = table;
        this.bookInfoArea = bookInfoArea;
        this.parentStage = parent;

        initModality(Modality.APPLICATION_MODAL); // Tạo cửa sổ dạng modal
        initOwner(parent);

        setTitle("Tìm kiếm tài liệu");

        // Tạo thanh tìm kiếm
        FlowPane searchPanel = new FlowPane(10, 10);
        searchPanel.setPadding(new Insets(10));
        searchField = new TextField();
        searchField.setPromptText("Nhập tên sách...");
        searchField.setPrefWidth(200);

        Button searchButton = new Button("Tìm kiếm");
        searchPanel.getChildren().addAll(new Label("Nhập tên sách:"), searchField, searchButton);

        // Kết quả tìm kiếm
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefSize(400, 200);

        // Bố trí các thành phần vào cửa sổ chính
        BorderPane root = new BorderPane();
        root.setTop(searchPanel);
        root.setCenter(new ScrollPane(resultArea));

        // Xử lý sự kiện tìm kiếm
        searchButton.setOnAction(e -> performSearch(searchField.getText().trim()));

        // Thiết lập cảnh và hiển thị
        Scene scene = new Scene(root, 500, 300);
        setScene(scene);
        setX(parent.getX() + 100);
        setY(parent.getY() + 100);
    }

    // Hàm tìm kiếm tài liệu theo tên
    private void performSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập từ khóa tìm kiếm");
            alert.initOwner(parentStage);
            alert.showAndWait();
            return;
        }

        boolean found = false; // Biến kiểm tra xem có tìm thấy tài liệu hay không
        for (int i = 0; i < documentList.size(); i++) {
            Document doc = documentList.get(i);
            if (doc.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                table.getSelectionModel().select(i); // Chọn dòng chứa tài liệu
                table.scrollTo(i); // Cuộn đến dòng chứa tài liệu
                bookInfoArea.setText(doc.toString()); // Cập nhật thông tin tài liệu vào ô "Thông tin"
                found = true;
                break; // Tìm thấy tài liệu và dừng tìm kiếm
            }
        }

        if (!found) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Không tìm thấy tài liệu");
            alert.initOwner(parentStage);
            alert.showAndWait();
            bookInfoArea.clear(); // Xóa nội dung trong ô "Thông tin"
        }
    }

    // Hàm công khai để gọi performSearch từ bên ngoài
    public void search(String searchTerm) {
        performSearch(searchTerm);
    }
}
