package thang;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginApp extends Application {

  private static final String FIXED_USERNAME = "admin";
  private static final String FIXED_PASSWORD = "admin";

  @Override
  public void start(Stage primaryStage) {
    // Tải ảnh nền
    Image backgroundImage = new Image("/background.jpg"); // Đảm bảo đường dẫn đúng
    ImageView backgroundImageView = new ImageView(backgroundImage);
    Image windowIcon = new Image(
        getClass().getResourceAsStream("/logo.png")); // Đảm bảo đường dẫn đúng
    primaryStage.getIcons().add(windowIcon);

    // Thiết lập ảnh nền sao cho nó phủ đầy cửa sổ
    backgroundImageView.setFitWidth(350); // Đặt chiều rộng của ảnh nền theo kích thước cửa sổ
    backgroundImageView.setFitHeight(400); // Đặt chiều cao của ảnh nền theo kích thước cửa sổ

    // Tạo StackPane và thêm ảnh nền
    StackPane rightRoot = new StackPane();
    rightRoot.getChildren().add(backgroundImageView);

    // Giao diện bên trái (VBox)
    VBox leftPane = new VBox();
    leftPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #FF7E5F, #FD3A84);");
    leftPane.setAlignment(Pos.CENTER);
    leftPane.setPrefWidth(250);

    Label title = new Label("Library\nManagement\nSystem");
    title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
    leftPane.getChildren().add(title);

    // Giao diện bên phải (VBox) với nền trong suốt
    VBox rightPane = new VBox(15);
    rightPane.setPadding(new Insets(20));
    rightPane.setAlignment(Pos.CENTER);
    rightPane.setPrefWidth(350);
    rightPane.setStyle("-fx-background-color: transparent;"); // Đặt nền trong suốt

    Label loginLabel = new Label("Đăng nhập");
    loginLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");

    // Sử dụng GridPane để xếp nhãn và ô nhập liệu
    GridPane loginForm = new GridPane();
    loginForm.setHgap(10); // Khoảng cách giữa các cột
    loginForm.setVgap(15); // Khoảng cách giữa các hàng
    loginForm.setAlignment(Pos.CENTER);
    loginForm.setPadding(new Insets(10));

    Label usernameLabel = new Label("Tên đăng nhập:");
    TextField usernameField = new TextField();
    usernameField.setPromptText("Tên đăng nhập");

    Label passwordLabel = new Label("Mật khẩu:");
    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Mật khẩu");

    loginForm.add(usernameLabel, 0, 0); // Thêm nhãn "Tên đăng nhập:" vào cột 0, hàng 0
    loginForm.add(usernameField, 1, 0); // Thêm ô nhập liệu vào cột 1, hàng 0
    loginForm.add(passwordLabel, 0, 1); // Thêm nhãn "Mật khẩu:" vào cột 0, hàng 1
    loginForm.add(passwordField, 1, 1); // Thêm ô nhập liệu vào cột 1, hàng 1

    // Nút Đăng nhập
    Button loginButton = new Button("Đăng nhập");
    loginButton.setStyle(
        "-fx-background-color: #00AAFF; -fx-text-fill: white; -fx-font-weight: bold;"
            + "-fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 10;");
    loginButton.setPrefWidth(100);

    // Nút Thoát
    Button exitButton = new Button("Thoát");
    exitButton.setStyle(
        "-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; "
            + "-fx-border-radius: 50; -fx-background-radius: 50; -fx-padding: 10;");
    exitButton.setPrefWidth(100);

    // Sự kiện khi nhấn nút đăng nhập
    loginButton.setOnAction(e -> {
      String username = usernameField.getText().trim();
      String password = passwordField.getText().trim();

      if (FIXED_USERNAME.equals(username) && FIXED_PASSWORD.equals(password)) {
        // Thay đổi sang màn hình chính hoặc logic khác nếu cần
        new Intro().start(primaryStage);
      } else {
        showAlert("Sai tên đăng nhập hoặc mật khẩu. Vui lòng thử lại.");
      }
    });

    // Sự kiện khi nhấn nút Thoát
    exitButton.setOnAction(e -> {
      primaryStage.close(); // Thoát chương trình
    });

    // Thêm các nút vào giao diện
    rightPane.getChildren().addAll(loginLabel, loginForm, loginButton, exitButton);

    // Kết hợp bên trái và bên phải
    rightRoot.getChildren().add(rightPane);
    HBox root = new HBox(leftPane, rightRoot);
    Scene scene = new Scene(root, 600, 400);

    primaryStage.setTitle("Đăng nhập");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  // Hiển thị cảnh báo
  private void showAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
    alert.setTitle("Thông báo");
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
