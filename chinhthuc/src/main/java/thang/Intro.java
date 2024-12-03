package thang;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Intro extends Application {
    private BorderPane root; // Dùng để thay đổi nội dung chính
    private ObservableList<Document> observableDocumentList;
    private ArrayList<User> userList;
    private ArrayList<Document> documentList;
    Main mainInstance;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Khởi tạo dữ liệu
        documentList = new ArrayList<>();
        userList = new ArrayList<>();
        observableDocumentList = FXCollections.observableArrayList(documentList);

        primaryStage.setTitle("Thư viện - Dashboard");
        // Đặt biểu tượng cho cửa sổ
        Image icon = new Image(getClass().getResourceAsStream("/logo.png"));
        primaryStage.getIcons().add(icon);

        // Sidebar: Thanh bên trái
        VBox sidebar = createSidebar();

        // Nội dung chính ban đầu
        VBox mainContent = createMainContent();

        // Root layout
        root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(mainContent);

        // Scene và hiển thị giao diện
        Scene scene = new Scene(root, 1000, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Tạo sidebar với các nút chức năng
     */
    // Biến để lưu trữ nút hiện tại được chọn
    private Button selectedButton;

    /**
     * Tạo sidebar với các nút chức năng
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #FF7E5F, #FD3A84);");

        Label title = new Label("Library\nManagement");
        title.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button homeButton = new Button("Home");
        Button helpButton = new Button("Trợ giúp");
        Button bookButton = new Button("Quản lý sách");
        Button noteButton = new Button("Ghi chú");
        Button memberButton = new Button("Thành viên");

        for (Button btn : new Button[]{homeButton, bookButton, memberButton, noteButton, helpButton}) {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: CENTER_LEFT;");
            btn.setMaxWidth(Double.MAX_VALUE);

            // Gán sự kiện để highlight nút
            btn.setOnAction(e -> {
                highlightButton(btn); // Highlight nút được nhấn
                // Gọi phương thức tương ứng
                if (btn == homeButton) switchToHome();
                else if (btn == helpButton) new FileHandler().introduce();
                else if (btn == bookButton) switchToBookManagement();
                else if (btn == noteButton) new FileHandler().note();
                else if (btn == memberButton) switchToMemberManagement();
            });
        }

        // Tạo nút Đăng xuất
        Button logoutButton = new Button("Đăng xuất");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-alignment: CENTER_LEFT;");
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        // Xử lý sự kiện cho nút Đăng xuất
        logoutButton.setOnAction(e -> {
            highlightButton(logoutButton);
            showLogoutConfirmation();
        });

        // Thêm các nút vào sidebar
        sidebar.getChildren().addAll(title, homeButton, bookButton, memberButton, noteButton, helpButton);

        // Thêm khoảng trống giữa các nút và nút Đăng xuất
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Thêm nút Đăng xuất vào cuối sidebar
        sidebar.getChildren().addAll(spacer, logoutButton);

        // Tạo ImageView cho từng nút chức năng
        ImageView homeIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/home.png")));
        homeIcon.setFitWidth(20);
        homeIcon.setFitHeight(20);

        ImageView bookIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/book.png")));
        bookIcon.setFitWidth(20);
        bookIcon.setFitHeight(20);

        ImageView memberIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/member.png")));
        memberIcon.setFitWidth(20);
        memberIcon.setFitHeight(20);

        ImageView noteIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/note.png")));
        noteIcon.setFitWidth(20);
        noteIcon.setFitHeight(20);

        ImageView helpIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/help.png")));
        helpIcon.setFitWidth(20);
        helpIcon.setFitHeight(20);

        ImageView logoutIcon = new ImageView(new Image(getClass().getResourceAsStream("/icon/logout.png")));
        logoutIcon.setFitWidth(20);
        logoutIcon.setFitHeight(20);

        // Thêm logo vào nút
        homeButton.setGraphic(homeIcon);
        bookButton.setGraphic(bookIcon);
        memberButton.setGraphic(memberIcon);
        noteButton.setGraphic(noteIcon);
        helpButton.setGraphic(helpIcon);
        logoutButton.setGraphic(logoutIcon);

        return sidebar;
    }

    /**
     * Highlight nút được chọn
     */
    private void highlightButton(Button button) {
        // Bỏ highlight nút trước đó
        if (selectedButton != null) {
            selectedButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT;");
            ImageView previousIcon = (ImageView) selectedButton.getGraphic();
            previousIcon.setEffect(null); // Gỡ bỏ hiệu ứng màu cho icon trước đó
        }

        // Tạo hiệu ứng đổi màu chính xác thành #FF7E5F
        ImageView currentIcon = (ImageView) button.getGraphic();
        Blend blendEffect = new Blend();
        blendEffect.setMode(BlendMode.SRC_ATOP);

        // Áp dụng lớp phủ màu lên icon
        ColorInput colorInput = new ColorInput(
                0, 0, // Vị trí
                currentIcon.getImage().getWidth(), currentIcon.getImage().getHeight(), // Kích thước ảnh
                Color.web("#FF7E5F") // Màu overlay
        );
        blendEffect.setTopInput(colorInput);

        currentIcon.setEffect(blendEffect); // Áp dụng hiệu ứng cho icon

        // Highlight nút hiện tại
        button.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #FF7E5F; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT;");
        selectedButton = button;
    }

    /**
     * Chuyển sang giao diện Home (mặc định).
     */
    private void switchToHome() {
        VBox mainContent = createMainContent(); // Tạo lại giao diện chính
        root.setCenter(mainContent); // Đặt vào phần trung tâm
    }

    /**
     * Chuyển sang nội dung "Quản lý sách" mà không tạo ra cửa sổ bất chợt.
     */
    private void switchToBookManagement() {
        try {
            // Tạo một Stage ẩn (không hiển thị)
            Stage hiddenStage = new Stage();
            hiddenStage.initStyle(javafx.stage.StageStyle.UTILITY); // Giảm thiểu sự hiển thị
            hiddenStage.setOpacity(0); // Đặt cửa sổ vô hình

            // Tạo một đối tượng Main
            Main mainInstance = new Main(hiddenStage, userList, observableDocumentList);

            // Lấy giao diện chính từ Scene của Main
            Scene mainScene = hiddenStage.getScene();
            if (mainScene != null) {
                Parent mainContent = mainScene.getRoot();

                // Đặt giao diện chính vào trung tâm của root
                root.setCenter(mainContent);
            }

            // Đảm bảo hiddenStage không bao giờ hiển thị
            hiddenStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Chuyển sang nội dung "Thành viên"
     */
    private void switchToMemberManagement() {
        try {
            // Tạo một đối tượng MemberManagement
            borrowerManagementDialog borrowerManagementDialog = new borrowerManagementDialog(mainInstance);

            // Lấy nội dung giao diện từ MemberManagement
            Scene memberScene = borrowerManagementDialog.getScene(); // Lấy scene từ class MemberManagement
            Parent memberContent = memberScene.getRoot();   // Truy cập root của scene

            // Đặt giao diện của thành viên vào nội dung chính
            root.setCenter(memberContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Đăng xuất.
     */
    private void showLogoutConfirmation() {
        // Tạo hộp thoại xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận Đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất không?");

        // Xử lý lựa chọn của người dùng
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Quay lại màn hình đăng nhập
                Stage currentStage = (Stage) root.getScene().getWindow();
                currentStage.close(); // Đóng cửa sổ hiện tại

                // Tạo và hiển thị lại cửa sổ đăng nhập
                Stage loginStage = new Stage();
                new LoginApp().start(loginStage);
            }
        });
    }


    /**
     * Tạo nội dung chính ban đầu
     */
    private VBox createMainContent() {
        // Tạo box tiêu đề ở trên cùng
        HBox titleBox = new HBox();
        titleBox.setStyle("-fx-background-color: linear-gradient(to right, #8ac4d0, #00c9a7);"
                + "-fx-padding: 30;" // Tăng padding
                + "-fx-alignment: center;"
                + "-fx-border-radius: 20;"
                + "-fx-background-radius: 20;");
        titleBox.setSpacing(30); // Tăng khoảng cách giữa ảnh và chữ

        // Hình ảnh cho box tiêu đề
        ImageView imageView = new ImageView(new Image("library.png")); // Đường dẫn tới file ảnh
        imageView.setFitHeight(150); // Tăng chiều cao ảnh
        imageView.setFitWidth(200);  // Tăng chiều rộng ảnh

        // Chữ tiêu đề
        Label titleLabel = new Label("LIBRARY\nMANAGEMENT");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36)); // Tăng kích thước chữ và làm chữ đậm
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE); // Đặt màu chữ trắng

        // Thêm hình ảnh và chữ vào box
        titleBox.getChildren().addAll(imageView, titleLabel);

        // Box 1: Văn bản bên trái, hình ảnh bên phải
        HBox box1 = new HBox(20);
        box1.setAlignment(Pos.CENTER);
        box1.setPadding(new Insets(20));
        box1.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; "
                + "-fx-border-width: 1px; -fx-border-radius: 20; -fx-background-radius: 20;");

        // Tiêu đề cho Box 1
        VBox textBox = new VBox(10); // Chứa tiêu đề và nội dung văn bản
        Label box1Title = new Label("Library Management System");
        box1Title.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Làm tiêu đề to, đậm
        box1Title.setTextFill(javafx.scene.paint.Color.DARKBLUE); // Màu chữ xanh đậm
        box1Title.setAlignment(Pos.TOP_LEFT);

        // Nội dung văn bản của Box 1
        Label box1Content = new Label("Chào mừng bạn đến với Library Management System, một ứng dụng đơn giản giúp "
                + "quản lý tài liệu và sách trong thư viện của bạn một cách hiệu quả. "
                + "Với giao diện thân thiện và nhiều tính năng mạnh mẽ, hệ thống cho phép bạn quản lý danh mục sách, "
                + "theo dõi hoạt động mượn trả, và cung cấp trải nghiệm tốt nhất cho độc giả. "
                + "Hãy bắt đầu hành trình của bạn với chúng tôi ngay hôm nay!");
        box1Content.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        box1Content.setWrapText(true);
        box1Content.setTextFill(javafx.scene.paint.Color.BLACK);

        // Thêm tiêu đề và nội dung vào textBox
        textBox.getChildren().addAll(box1Title, box1Content);
        textBox.setAlignment(Pos.CENTER_LEFT);

        // Hình ảnh của Box 1
        ImageView introImage = new ImageView(new Image("kesach.jpg")); // Đường dẫn ảnh từ file tải lên
        introImage.setFitWidth(200);
        introImage.setPreserveRatio(true);
        introImage.setStyle("-fx-border-color: #ccc; -fx-border-width: 2px; -fx-border-radius: 15px;");

        // Thêm văn bản và hình ảnh vào Box 1
        box1.getChildren().addAll(textBox, introImage);


        // Box 2: Hình ảnh bên trái, văn bản bên phải
        HBox box2 = new HBox(20);
        box2.setAlignment(Pos.CENTER_LEFT); // Căn chỉnh các phần tử về bên trái
        box2.setPadding(new Insets(20));
        box2.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ddd; "
                + "-fx-border-width: 1px; -fx-border-radius: 20; -fx-background-radius: 20;");

        // Hình ảnh bên trái
        ImageView featureImage2 = new ImageView(new Image("laptop.jpg")); // Đường dẫn ảnh từ file tải lên
        featureImage2.setFitWidth(180); // Kích thước hình ảnh
        featureImage2.setPreserveRatio(true);
        featureImage2.setStyle("-fx-border-color: #ccc; -fx-border-width: 2px; -fx-border-radius: 15px;");

        // Văn bản bên phải (gồm tiêu đề và nội dung chức năng)
        VBox textBox2 = new VBox(10); // Chứa tiêu đề và danh sách chức năng
        textBox2.setAlignment(Pos.CENTER_LEFT);

        // Tiêu đề
        Label titleLabel2 = new Label("Giới thiệu các chức năng");
        titleLabel2.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel2.setTextFill(javafx.scene.paint.Color.DARKBLUE); // Màu xanh đậm cho tiêu đề

        // Nội dung chức năng
        Label featureLabel2 = new Label("- Quản lý sách: Thêm, xóa, và cập nhật thông tin sách trong thư viện.\n"
                + "- Quản lý độc giả: Theo dõi thông tin và lịch sử mượn trả của độc giả.\n"
                + "- Tìm kiếm tài liệu: Dễ dàng tìm kiếm tài liệu theo tên, tác giả hoặc mã ISBN.\n"
                + "- Mượn và trả sách: Hỗ trợ quản lý hoạt động mượn trả sách hiệu quả.\n"
                + "- Báo cáo và thống kê: Tổng hợp dữ liệu chi tiết về hoạt động thư viện.");
        featureLabel2.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        featureLabel2.setWrapText(true);

        // Thêm tiêu đề và nội dung vào textBox2
        textBox2.getChildren().addAll(titleLabel2, featureLabel2);

        // Thêm hình ảnh và văn bản vào Box 2
        box2.getChildren().addAll(featureImage2, textBox2);


        // Thêm tất cả các Box vào VBox
        VBox contentVBox = new VBox(20, titleBox, box1, box2);
        contentVBox.setAlignment(Pos.CENTER);
        contentVBox.setPadding(new Insets(20));

        // Tạo ScrollPane để cuộn khi nội dung quá dài
        ScrollPane scrollPane = new ScrollPane(contentVBox);
        scrollPane.setFitToWidth(true); // Đảm bảo nội dung tự động điều chỉnh chiều ngang
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background-color: transparent;"); // Nền trong suốt cho ScrollPane

        // Đặt ScrollPane vào VBox chính
        VBox mainContent = new VBox(scrollPane);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(10));
        return mainContent;
    }
}
