package org.example.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class BorrowReturnDialog extends Stage {
    private ArrayList<Document> documentList; // Danh sách tài liệu
    private Document selectedDocument; // Tài liệu được chọn
    private ArrayList<User> userList; // Danh sách người dùng gốc
    private ArrayList<User> filteredUserList; // Danh sách người dùng sau khi lọc
    private User selectedUser; // Người dùng được chọn
    private ListView<String> userListDisplay;
    private ObservableList<String> userModel;
    private TextArea userInfoArea; // Khu vực hiển thị thông tin người dùng
    private TextField searchField; // Ô tìm kiếm thành viên
    private Stage parentStage;

    public BorrowReturnDialog(Stage parent, ArrayList<Document> documentList, Document selectedDocument, ArrayList<User> userList) {
        this.documentList = documentList;
        this.selectedDocument = selectedDocument;
        this.userList = userList;
        this.filteredUserList = new ArrayList<>(userList);
        this.parentStage = parent;

        initModality(Modality.APPLICATION_MODAL); // Tạo cửa sổ dạng modal
        initOwner(parent);
        setTitle("Mượn/Trả tài liệu");

        // Panel tìm kiếm
        Label searchLabel = new Label("Nhập tên thành viên để tìm kiếm:");
        searchField = new TextField();
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterUserList(newValue));

        HBox searchPanel = new HBox(10, searchLabel, searchField);
        searchPanel.setPadding(new Insets(10));

        // Hiển thị danh sách thành viên
        userModel = FXCollections.observableArrayList();
        userListDisplay = new ListView<>(userModel);
        updateUserListDisplay(); // Cập nhật hiển thị danh sách ban đầu

        // Panel bên trái chứa tiêu đề và danh sách
        VBox userListPanel = new VBox(10, new Label("Danh sách thành viên:"), userListDisplay);
        userListPanel.setPadding(new Insets(10));
        userListPanel.setPrefWidth(300); // Đặt chiều rộng cho bảng danh sách thành viên

        // Khu vực hiển thị thông tin người dùng
        userInfoArea = new TextArea();
        userInfoArea.setEditable(false);
        userInfoArea.setPrefHeight(250);

        VBox userInfoPanel = new VBox(10, new Label("Thông tin:"), userInfoArea);
        userInfoPanel.setPadding(new Insets(10));
        userInfoPanel.setPrefWidth(300); // Đặt chiều rộng cho bảng thông tin người dùng

        // Panel chính để chứa hai bảng
        HBox contentPanel = new HBox(20, userListPanel, userInfoPanel);
        HBox.setHgrow(userListPanel, Priority.ALWAYS); // Cho phép giãn đều
        HBox.setHgrow(userInfoPanel, Priority.ALWAYS); // Cho phép giãn đều
        contentPanel.setPrefWidth(600); // Đặt chiều rộng tổng cộng của HBox

        // Panel cho các nút chức năng
        Button addUserButton = new Button("Thêm thành viên");
        addUserButton.setOnAction(e -> addUser());

        Button removeUserButton = new Button("Xóa thành viên");
        removeUserButton.setOnAction(e -> removeUser());

        Button borrowButton = new Button("Mượn tài liệu");
        borrowButton.setOnAction(e -> borrowDocument());

        Button returnButton = new Button("Trả tài liệu");
        returnButton.setOnAction(e -> returnDocument());

        HBox buttonPanel = new HBox(10, addUserButton, removeUserButton, borrowButton, returnButton);
        buttonPanel.setPadding(new Insets(10));
        buttonPanel.setSpacing(15);

        // Xử lý hiển thị thông tin người dùng khi click vào danh sách
        userListDisplay.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = userListDisplay.getSelectionModel().getSelectedIndex();
            if (selectedIndex != -1) {
                selectedUser = filteredUserList.get(selectedIndex);
                displayUserInfo(selectedUser);
            }
        });

        // Thiết lập cảnh và hiển thị
        VBox root = new VBox(10, searchPanel, contentPanel, buttonPanel);
        Scene scene = new Scene(root, 500, 400);
        setScene(scene);
    }

    // Phương thức thêm thành viên mới
    private void addUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Nhập tên thành viên:");
        dialog.initOwner(this);

        String userName = dialog.showAndWait().orElse(null);
        if (userName != null && !userName.isEmpty()) {
            TextInputDialog idDialog = new TextInputDialog();
            idDialog.setHeaderText("Nhập mã thành viên:");
            idDialog.initOwner(this);

            String userId = idDialog.showAndWait().orElse(null);
            if (userId != null && !userId.isEmpty()) {
                User newUser = new User(userId, userName, "example@example.com");
                userList.add(newUser);
                filterUserList(searchField.getText());
            } else {
                showAlert("Thông báo", "Vui lòng nhập mã thành viên!");
            }
        } else {
            showAlert("Thông báo", "Vui lòng nhập tên thành viên!");
        }
    }

    // Phương thức xóa thành viên
    private void removeUser() {
        int selectedIndex = userListDisplay.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            userList.remove(filteredUserList.get(selectedIndex));
            filterUserList(searchField.getText());
        } else {
            showAlert("Thông báo", "Vui lòng chọn thành viên để xóa!");
        }
    }

    // Phương thức mượn tài liệu
    private void borrowDocument() {
        int selectedUserIndex = userListDisplay.getSelectionModel().getSelectedIndex();
        if (selectedUserIndex != -1) {
            selectedUser = filteredUserList.get(selectedUserIndex);
            if (selectedDocument.getQuantity() > 0) {
                selectedUser.borrowBook(selectedDocument);
                showAlert("Thông báo", "Mượn tài liệu thành công!");
                displayUserInfo(selectedUser);
            } else {
                showAlert("Thông báo", "Tài liệu đã hết!");
            }
        } else {
            showAlert("Thông báo", "Vui lòng chọn thành viên để mượn!");
        }
    }

    // Phương thức trả tài liệu
    private void returnDocument() {
        int selectedUserIndex = userListDisplay.getSelectionModel().getSelectedIndex();
        if (selectedUserIndex != -1) {
            selectedUser = filteredUserList.get(selectedUserIndex);
            selectedUser.returnBook(selectedDocument);
            showAlert("Thông báo", "Trả tài liệu thành công!");
            displayUserInfo(selectedUser);
        } else {
            showAlert("Thông báo", "Vui lòng chọn thành viên để trả!");
        }
    }

    // Phương thức hiển thị thông tin người dùng
    private void displayUserInfo(User user) {
        StringBuilder info = new StringBuilder();
        info.append("ID: ").append(user.getUserId()).append("\n");
        info.append("Tên: ").append(user.getName()).append("\n");
        info.append("Email: ").append(user.getEmail()).append("\n");
        info.append("Sách đã mượn:\n");
        for (Document doc : user.getBorrowedBooks()) {
            info.append("- ").append(doc.getTitle()).append("\n");
        }
        userInfoArea.setText(info.toString());
    }

    // Phương thức lọc danh sách người dùng theo từ khóa
    private void filterUserList(String keyword) {
        filteredUserList = userList.stream()
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
        updateUserListDisplay();
    }

    // Cập nhật hiển thị danh sách người dùng sau khi lọc
    private void updateUserListDisplay() {
        userModel.clear();
        for (User user : filteredUserList) {
            userModel.add(user.getName() + " (ID: " + user.getUserId() + ")");
        }
    }

    // Hiển thị cảnh báo
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.initOwner(this);
        alert.showAndWait();
    }
}
