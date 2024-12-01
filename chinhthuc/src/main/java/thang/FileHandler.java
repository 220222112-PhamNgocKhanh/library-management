package thang;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHandler {
    private static final String INTRO_FILE = "intro.txt";
    private static final String NOTE_FILE = "note.txt";

    public void introduce() {
        Stage introStage = new Stage();
        introStage.setTitle("Giới thiệu");

        TextArea textArea = new TextArea();
        textArea.setEditable(false); // Chỉ đọc, không cho phép chỉnh sửa
        textArea.setWrapText(true); // Tự động xuống dòng
        textArea.setPrefSize(380, 280); // Đặt kích thước ưa thích cho TextArea

        // Đọc nội dung từ file intro.txt trong thư mục resources
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/intro.txt"), StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.setText(content.toString());
        } catch (IOException e) {
            textArea.setText("Không thể đọc file intro.txt");
            e.printStackTrace();
        }

        VBox layout = new VBox(textArea);
        VBox.setVgrow(textArea, Priority.ALWAYS); // Cho phép TextArea mở rộng theo chiều dọc của VBox
        Scene scene = new Scene(layout, 400, 300);
        introStage.setScene(scene);
        introStage.show();
    }

    // Hàm mở note.txt và cho phép chỉnh sửa, lưu lại khi người dùng đóng cửa sổ
    public void note() {
        Stage noteStage = new Stage();
        noteStage.setTitle("Ghi chú");

        TextArea textArea = new TextArea();
        textArea.setWrapText(true); // Tự động xuống dòng
        textArea.setPrefSize(380, 280); // Đặt kích thước ưa thích cho TextArea

        // Đọc nội dung từ file note.txt nếu có
        try (BufferedReader reader = new BufferedReader(new FileReader(NOTE_FILE))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            textArea.setText(content.toString());
        } catch (IOException e) {
            textArea.setText(""); // Nếu file không tồn tại hoặc không đọc được, để trống
            e.printStackTrace();
        }

        // Lưu lại nội dung vào file note.txt khi cửa sổ đóng
        noteStage.setOnCloseRequest(event -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(NOTE_FILE))) {
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
