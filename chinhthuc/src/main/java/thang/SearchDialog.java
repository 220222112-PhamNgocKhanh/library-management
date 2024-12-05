package thang;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class SearchDialog extends Stage {

  private ArrayList<Document> documentList;
  private TextField searchField;
  private TextArea bookInfoArea;
  private TableView<Document> table;
  private Stage parentStage;

  public SearchDialog(Stage parent, ArrayList<Document> documentList, TableView<Document> table,
      TextArea bookInfoArea) {
    this.documentList = documentList;
    this.table = table;
    this.bookInfoArea = bookInfoArea;
    this.parentStage = parent;

    initModality(Modality.APPLICATION_MODAL);
    initOwner(parent);
    setTitle("Tìm kiếm tài liệu");

    BorderPane root = new BorderPane();
    FlowPane searchPanel = new FlowPane(10, 10);
    searchPanel.setPadding(new Insets(10));

    Label searchLabel = new Label("Nhập tên sách hoặc ISBN:");
    searchField = new TextField();
    searchField.setPrefWidth(200);
    Button searchButton = new Button("Tìm kiếm");
    searchButton.setOnAction(e -> performSearch(searchField.getText().trim()));

    searchPanel.getChildren().addAll(searchLabel, searchField, searchButton);
    root.setTop(searchPanel);

    bookInfoArea = new TextArea();
    bookInfoArea.setEditable(false);
    bookInfoArea.setPrefHeight(200);
    root.setCenter(new ScrollPane(bookInfoArea));

    Scene scene = new Scene(root, 500, 300);
    setScene(scene);
  }

  // Phương thức tìm kiếm tài liệu từ API Google Books
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

      // Kiểm tra từ khóa trong cả tên sách và ISBN
      if (doc.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
          doc.getIsbn13().equalsIgnoreCase(searchTerm) ||
          doc.getIsbn10().equalsIgnoreCase(searchTerm)) {

        // Chọn và highlight dòng chứa tài liệu
        table.getSelectionModel()
            .clearAndSelect(i); // Chọn dòng chứa tài liệu và bỏ chọn các dòng khác
        table.scrollTo(i); // Cuộn đến dòng chứa tài liệu

        // Hiển thị thông tin tài liệu trong ô "Thông tin"
        bookInfoArea.setText(doc.toString());

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

  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.initOwner(this);
    alert.showAndWait();
  }
}
