package thang;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemberManagement extends Stage {
    private ArrayList<User> userList; // Danh sách người dùng
    private ArrayList<User> filteredUserList; // Danh sách người dùng đã lọc
    private ListView<String> userListDisplay; // Hiển thị danh sách thành viên
    private ObservableList<String> userModel; // Dữ liệu cho danh sách
    private TextArea userInfoArea; // Hiển thị thông tin chi tiết người dùng
    private TextField searchField; // Ô tìm kiếm
    private User selectedUser; // Người dùng đang được chọn
    private ObservableList<Document> observableDocumentList; // Nhận từ Main

    public MemberManagement(ArrayList<User> userList, ObservableList<Document> observableDocumentList) {
        setTitle("Quản lý độc giả");
        this.observableDocumentList = observableDocumentList; // Nhận trực tiếp từ Main
        this.userList = userList;

        filteredUserList = new ArrayList<>(userList);
        if (userList.isEmpty()) {
            initializeSampleData(); // Chỉ thêm dữ liệu mẫu nếu danh sách trống
        }

        // Tiêu đề chính
        Label titleLabel = new Label("Quản lý độc giả");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: linear-gradient(to bottom, #00aaff, #0055ff);");
        titleLabel.setAlignment(Pos.CENTER);

        // Panel tìm kiếm
        Label searchLabel = new Label("Tìm kiếm (Tên/ID):");
        searchField = new TextField();
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUserList(newValue));

        HBox searchPanel = new HBox(10, searchLabel, searchField);
        searchPanel.setPadding(new Insets(10));
        searchPanel.setAlignment(Pos.CENTER);

        // Danh sách thành viên
        userModel = FXCollections.observableArrayList();
        userListDisplay = new ListView<>(userModel);
        updateUserListDisplay(); // Cập nhật hiển thị danh sách ban đầu

        // Thêm sự kiện khi click vào danh sách
        userListDisplay.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = userListDisplay.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                selectedUser = filteredUserList.get(selectedIndex); // Cập nhật người dùng được chọn
                displayUserInfo(selectedUser); // Hiển thị thông tin chi tiết
            }
        });

        // Tăng chiều rộng của ô danh sách thành viên
        VBox userListPanel = new VBox(10, new Label("Danh sách thành viên:"), userListDisplay);
        userListPanel.setPadding(new Insets(10));
        userListPanel.setPrefWidth(350); // Tăng chiều rộng cho danh sách thành viên

        // Ô thông tin chi tiết
        userInfoArea = new TextArea();
        userInfoArea.setEditable(false);
        userInfoArea.setPrefHeight(300);

        VBox userInfoPanel = new VBox(10, new Label("Thông tin thành viên:"), userInfoArea);
        userInfoPanel.setPadding(new Insets(10));
        userInfoPanel.setPrefWidth(300);

        // Hai bảng xếp ngang
        HBox contentPanel = new HBox(20, userListPanel, userInfoPanel);
        contentPanel.setAlignment(Pos.CENTER);

        // Các nút chức năng
        Button addUserButton = new Button("Thêm thành viên");
        Button editUserButton = new Button("Sửa thông tin");
        Button deleteUserButton = new Button("Xóa thành viên");
        Button borrowReturnButton = new Button("Quản lý mượn/trả");
        Button checkOverdueButton = new Button("Kiểm tra quá hạn");

        // Sự kiện nút Thêm thành viên
        addUserButton.setOnAction(e -> addUser());
        // Sự kiện nút Sửa thông tin
        editUserButton.setOnAction(e -> editUser());
        // Sự kiện nút Xóa thành viên
        deleteUserButton.setOnAction(e -> removeUser());
        // Sự kiện nút Quản lý mượn/trả
        borrowReturnButton.setOnAction(e -> manageBorrowReturn());
        // Sự kiện cho nút "Kiểm tra quá hạn"
        Label dateLabel = new Label("Chọn ngày hiện tại:");
        DatePicker datePicker = new DatePicker(LocalDate.now()); // Mặc định là ngày hiện tại
        checkOverdueButton.setOnAction(e -> checkOverdueForSelectedUser(datePicker.getValue()));

        HBox buttonPanel = new HBox(20, addUserButton, editUserButton, deleteUserButton, borrowReturnButton, checkOverdueButton);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setSpacing(20);

        // Đặt kích thước cho các nút
        addUserButton.setPrefWidth(150);
        editUserButton.setPrefWidth(150);
        deleteUserButton.setPrefWidth(150);
        borrowReturnButton.setPrefWidth(150);
        checkOverdueButton.setPrefWidth(150);

        // Đặt lại bố cục với DatePicker và nút
        HBox datePanel = new HBox(10, dateLabel, datePicker, checkOverdueButton);
        datePanel.setPadding(new Insets(10));
        datePanel.setAlignment(Pos.CENTER);

        // Bố cục chính
        VBox layout = new VBox(10, titleLabel, searchPanel, contentPanel, buttonPanel, datePanel);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        // Thiết lập giao diện
        Scene scene = new Scene(layout, 700, 500);
        setScene(scene);
    }

    // Thêm thành viên mới
    private void addUser() {
        Stage addUserStage = new Stage();
        addUserStage.setTitle("Thêm thành viên mới");

        // Các trường nhập liệu
        Label nameLabel = new Label("Tên thành viên:");
        TextField nameField = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        // Nút Lưu
        Button saveButton = new Button("Lưu");
        saveButton.setOnAction(event -> {
            String userName = nameField.getText().trim();
            String emailInput = emailField.getText().trim();

            if (userName == null || userName.isEmpty()) {
                showAlert("Thông báo", "Vui lòng nhập tên thành viên!");
                return;
            }

            String email = emailInput.contains("@") ? emailInput : emailInput + "@gmail.com";
            String userId = generateUniqueId();
            User newUser = new User(userId, userName, email);

            userList.add(newUser);
            filterUserList(searchField.getText()); // Cập nhật danh sách
            addUserStage.close();
        });

        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(event -> addUserStage.close());

        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, nameLabel, nameField, emailLabel, emailField, buttonBox);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 350, 250);
        addUserStage.setScene(scene);
        addUserStage.initOwner(this);
        addUserStage.initModality(Modality.APPLICATION_MODAL);
        addUserStage.show();
    }

    // Sửa thông tin người dùng
    private void editUser() {
        if (selectedUser == null) {
            showAlert("Thông báo", "Vui lòng chọn thành viên để sửa!");
            return;
        }

        Stage editUserStage = new Stage();
        editUserStage.setTitle("Sửa thông tin thành viên");

        // Các trường nhập liệu
        Label nameLabel = new Label("Tên thành viên:");
        TextField nameField = new TextField(selectedUser.getName());

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField(selectedUser.getEmail());

        // Nút Lưu
        Button saveButton = new Button("Lưu");
        saveButton.setOnAction(event -> {
            String newName = nameField.getText();
            String newEmail = emailField.getText();
            if (newName == null || newName.isEmpty()) {
                showAlert("Thông báo", "Tên không được để trống!");
                return;
            }
            if (newEmail == null || newEmail.isEmpty()) {
                showAlert("Thông báo", "Email không được để trống!");
                return;
            }

            selectedUser.setName(newName);
            selectedUser.setEmail(newEmail);
            filterUserList(searchField.getText()); // Cập nhật danh sách
            displayUserInfo(selectedUser); // Hiển thị lại thông tin người dùng
            editUserStage.close();
        });

        // Nút Hủy
        Button cancelButton = new Button("Hủy");
        cancelButton.setOnAction(event -> editUserStage.close());

        // Bố cục
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, nameLabel, nameField, emailLabel, emailField, buttonBox);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 250);
        editUserStage.setScene(scene);
        editUserStage.initOwner(this);
        editUserStage.initModality(Modality.APPLICATION_MODAL);
        editUserStage.show();
    }

    // Xóa thành viên được chọn
    private void removeUser() {
        if (selectedUser != null) {
            userList.remove(selectedUser);
            filterUserList(searchField.getText()); // Cập nhật danh sách
            userInfoArea.clear(); // Xóa thông tin hiển thị
        } else {
            showAlert("Thông báo", "Vui lòng chọn thành viên để xóa!");
        }
    }

    // Quản lý mượn/trả tài liệu
    private void manageBorrowReturn() {
        if (selectedUser != null) {
            BorrowReturnWindow dialog = new BorrowReturnWindow(observableDocumentList, selectedUser); // Truyền selectedUser
            dialog.showAndWait();
            displayUserInfo(selectedUser); // Cập nhật thông tin sau khi mượn/trả
        } else {
            showAlert("Thông báo", "Vui lòng chọn thành viên để quản lý mượn/trả!");
        }
    }

    // Hàm kiểm tra sách quá hạn
    private boolean isDocumentOverdue(Document doc) {
        if (doc.getBorrowDate() != null) {
            long monthsBorrowed = ChronoUnit.MONTHS.between(doc.getBorrowDate(), LocalDate.now());
            return monthsBorrowed > 3;
        }
        return false;
    }

    // Hiển thị thông tin chi tiết của người dùng
    private void displayUserInfo(User user) {
        StringBuilder info = new StringBuilder();
        info.append("ID: ").append(user.getUserId()).append("\n");
        info.append("Tên: ").append(user.getName()).append("\n");
        info.append("Email: ").append(user.getEmail()).append("\n");
        info.append("Sách đã mượn:\n");

        // Hiển thị danh sách sách đã mượn kèm ngày mượn
        for (Document doc : user.getBorrowedBooks()) {
            doc.setBorrowDate(LocalDate.now()); // Gán ngày mượn là ngày hiện tại
            info.append("- ").append(doc.getTitle())
                    .append("\n(Ngày mượn: ").append(doc.getBorrowDate()).append(")");
            if (isDocumentOverdue(doc)) {
                info.append("\n[QUÁ HẠN!]");
            }
            else {
                info.append("\n[Chưa quá hạn]");
            }
            info.append("\n");
        }
        userInfoArea.setText(info.toString());
    }

    // Lọc danh sách người dùng theo từ khóa
    private void filterUserList(String keyword) {
        filteredUserList = userList.stream()
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()) || user.getUserId().contains(keyword))
                .collect(Collectors.toCollection(ArrayList::new));
        updateUserListDisplay();
    }

    // Cập nhật danh sách hiển thị sau khi lọc
    private void updateUserListDisplay() {
        userModel.clear();
        for (User user : filteredUserList) {
            userModel.add(user.getName() + " (ID: " + user.getUserId() + ", Email: " + user.getEmail() + ")");
        }
    }

    // Hiển thị cảnh báo
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.initOwner(this);
        alert.showAndWait();
    }

    // Tạo dữ liệu mẫu
    private void initializeSampleData() {
        userList.add(new User("0001", "Nguyễn Văn A", "uyenvana@gmail.com"));
        userList.add(new User("0002", "Trần Thị B", "tranthib@gmail.com"));
        userList.add(new User("0003", "Lê Văn C", "levanc@gmail.com"));
        userList.add(new User("0004", "Phạm Thị D", "phamthid@gmail.com"));
        filteredUserList.addAll(userList);
    }

    // Sinh ID ngẫu nhiên 4 chữ số
    private String generateUniqueId() {
        while (true) {
            String id = String.format("%04d", (int) (Math.random() * 10000));
            if (userList.stream().noneMatch(user -> user.getUserId().equals(id))) {
                return id;
            }
        }
    }

    // Hàm kiểm tra quá hạn sử dụng ngày đã chọn
    private void checkOverdueForSelectedUser(LocalDate selectedDate) {
        if (selectedUser != null) {
            StringBuilder overdueInfo = new StringBuilder();
            overdueInfo.append("ID: ").append(selectedUser.getUserId()).append("\n");
            overdueInfo.append("Tên: ").append(selectedUser.getName()).append("\n");
            overdueInfo.append("Email: ").append(selectedUser.getEmail()).append("\n");
            overdueInfo.append("Sách đã mượn:\n");

            boolean hasOverdueBooks = false;
            for (Document doc : selectedUser.getBorrowedBooks()) {
                if (doc.getBorrowDate() != null) {
                    long monthsBorrowed = ChronoUnit.MONTHS.between(doc.getBorrowDate(), selectedDate);
                    overdueInfo.append("- ").append(doc.getTitle())
                            .append("\n(Ngày mượn: ").append(doc.getBorrowDate()).append(")");

                    if (monthsBorrowed > 3) {
                        overdueInfo.append("\n[QUÁ HẠN!]\n");
                        hasOverdueBooks = true;
                    } else {
                        overdueInfo.append("\n[Chưa quá hạn]\n");
                    }
                }
            }

            // Hiển thị thông tin trong một cửa sổ Alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Kiểm tra quá hạn");
            alert.setHeaderText("Kết quả kiểm tra tài liệu mượn");
            if (hasOverdueBooks) {
                alert.setContentText(overdueInfo.toString());
            } else {
                alert.setContentText("Không có tài liệu nào mượn quá hạn.");
            }
            alert.initOwner(this);
            alert.showAndWait();
        } else {
            showAlert("Thông báo", "Vui lòng chọn thành viên để kiểm tra quá hạn!");
        }
    }
}
