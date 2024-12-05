package thang;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHandler {

  private static final String INTRO_FILE = "help.txt";
  private static final String NOTE_FILE = "note.txt";

  public void help() {
    Stage helpStage = new Stage();
    helpStage.setTitle("Trợ giúp");
    // Đặt biểu tượng cho cửa sổ
    Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
    helpStage.getIcons().add(icon);

    TextArea textArea = new TextArea();
    textArea.setEditable(false); // Chỉ đọc, không cho phép chỉnh sửa
    textArea.setWrapText(true); // Tự động xuống dòng
    textArea.setPrefSize(380, 280); // Đặt kích thước ưa thích cho TextArea

    // Đọc nội dung từ file help.txt trong thư mục resources
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        getClass().getResourceAsStream("/help.txt"), StandardCharsets.UTF_8))) {

      StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
      textArea.setText(content.toString());
    } catch (IOException e) {
      textArea.setText("Không thể đọc file help.txt");
      e.printStackTrace();
    }

    VBox layout = new VBox(textArea);
    VBox.setVgrow(textArea, Priority.ALWAYS); // Cho phép TextArea mở rộng theo chiều dọc của VBox
    Scene scene = new Scene(layout, 400, 300);
    helpStage.setScene(scene);
    helpStage.show();
  }

  // Hàm mở note.txt và cho phép chỉnh sửa, lưu lại khi người dùng đóng cửa sổ
  public void note() {
    Stage noteStage = new Stage();
    noteStage.setTitle("Ghi chú");
    // Đặt biểu tượng cho cửa sổ
    Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
    noteStage.getIcons().add(icon);

    TextArea textArea = new TextArea();
    textArea.setWrapText(true); // Tự động xuống dòng
    textArea.setPrefSize(380, 280); // Đặt kích thước ưa thích cho TextArea

    // Đọc nội dung từ file note1.txt nếu có
    File noteFile = new File(NOTE_FILE); // Đọc file từ hệ thống file
    if (noteFile.exists()) {
      try (BufferedReader reader = new BufferedReader(
          new FileReader(noteFile, StandardCharsets.UTF_8))) {
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line).append("\n");
        }
        textArea.setText(content.toString());
      } catch (IOException e) {
        textArea.setText("Không thể đọc file ghi chú.");
        e.printStackTrace();
      }
    } else {
      textArea.setText("File ghi chú chưa tồn tại. Bạn có thể tạo ghi chú mới.");
    }

    // Lưu lại nội dung vào file note1.txt khi cửa sổ đóng
    noteStage.setOnCloseRequest(event -> {
      try (BufferedWriter writer = new BufferedWriter(
          new FileWriter(noteFile, StandardCharsets.UTF_8))) {
        writer.write(textArea.getText());
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    VBox layout = new VBox(textArea);
    VBox.setVgrow(textArea, Priority.ALWAYS); // Cho phép TextArea mở rộng theo chiều dọc của VBox
    Scene scene = new Scene(layout, 400, 300);
    noteStage.setScene(scene);
    noteStage.show();
  }

}
