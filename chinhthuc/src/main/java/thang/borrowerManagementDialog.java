package thang;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * quan li nguoi muon sach
 */
public class borrowerManagementDialog extends Stage {

  TextField searchField = new TextField();
  private TextField idBorrowerField;
  private TextField nameField;
  private ComboBox<String> sexField;
  private TextField ageField;
  private TextField emailField;
  private TextField phoneField;
  private TextField addressField;
  private TableView<Borrower> leftTable; // Bảng hiển thị danh sách người mượn
  private ObservableList<Borrower> borrowerList;
  TextArea detailArea;
  DocumentManagement documentManagementInstance;
  Stage editBorrowerStage;

  public borrowerManagementDialog(DocumentManagement documentManagementInstance) {
    this.documentManagementInstance = documentManagementInstance;
    Stage borrowerStage = new Stage();
    borrowerStage.initModality(Modality.APPLICATION_MODAL);
    borrowerStage.setTitle("Quản lý người mượn");

    //o tim kiem
    searchField = new TextField();
    searchField.setPromptText("Nhập tên hoặc số điện thoại...");
    searchField.setPrefWidth(400);

    Button filterButton = new Button("Lọc");
    filterButton.setPrefWidth(150);
    filterButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

// Bố cục cho thanh tìm kiếm
    HBox searchBox = new HBox(10, searchField, filterButton);
    searchBox.setAlignment(Pos.CENTER);
    searchBox.setPadding(new Insets(10));

    // Bảng bên trái (ID, Tên, Số điện thoại)
    leftTable = new TableView<>();
    leftTable.setPrefWidth(700);
    leftTable.setEditable(true);

    TableColumn<Borrower, String> idBorrowerCol = new TableColumn<>("ID");
    idBorrowerCol.setCellValueFactory(new PropertyValueFactory<>("idBorrower"));
    idBorrowerCol.setPrefWidth(40);

    TableColumn<Borrower, String> nameCol = new TableColumn<>("Tên");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameCol.setPrefWidth(250);

    TableColumn<Borrower, String> phoneCol = new TableColumn<>("Số điện thoại");
    phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
    phoneCol.setPrefWidth(150);

    leftTable.getColumns().addAll(idBorrowerCol, nameCol, phoneCol);
    new Thread(() -> {
      loadBorrowerFromDatabase(); // Hàm tải dữ liệu
    }).start();

    // Text area bên phải hiển thị thông tin chi tiết
    detailArea = new TextArea();
    detailArea.setEditable(false);
    detailArea.setPrefWidth(600);
    detailArea.setPromptText("Thông tin chi tiết sẽ hiển thị tại đây...");

    // Xử lý khi chọn dòng trong bảng
    leftTable.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldSelection, newSelection) -> {
          if (newSelection != null) {
            Borrower selectedBorrower = newSelection;
            detailArea.setText(
                "ID: " + selectedBorrower.getIdBorrower() + "\n" +
                    "Tên: " + selectedBorrower.getName() + "\n" +
                    "Giới tính: " + selectedBorrower.getSex() + "\n" +
                    "Tuổi: " + selectedBorrower.getAge() + "\n" +
                    "Email: " + selectedBorrower.getEmail() + "\n" +
                    "Số điện thoại: " + selectedBorrower.getPhone() + "\n" +
                    "Địa chỉ: " + selectedBorrower.getAddress()
            );
          }
        });

    // Bố cục bảng và text area
    HBox tableAndDetail = new HBox(10);
    tableAndDetail.getChildren().addAll(leftTable, detailArea);
    HBox.setHgrow(detailArea, Priority.ALWAYS);

    // Hàng nút đầu tiên
    HBox row1 = new HBox(20);
    row1.setAlignment(Pos.CENTER);

    Button addButton = new Button("Thêm người mượn");
    addButton.setPrefWidth(160);
    addButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    Button editButton = new Button("Sửa người mượn");
    editButton.setPrefWidth(160);
    editButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    Button deleteButton = new Button("Xóa người mượn");
    deleteButton.setPrefWidth(160);
    deleteButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    row1.getChildren().addAll(addButton, editButton, deleteButton);

// Hàng nút thứ hai
    HBox row2 = new HBox(20);
    row2.setAlignment(Pos.CENTER);

    Button borrowButton = new Button("Mượn tài liệu");
    borrowButton.setPrefWidth(160);
    borrowButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    Button returnButton = new Button("Trả tài liệu");
    returnButton.setPrefWidth(160);
    returnButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    row2.getChildren().addAll(borrowButton, returnButton);

// Đưa hai hàng nút vào VBox
    VBox buttonBox = new VBox(10);
    buttonBox.getChildren().addAll(row1, row2);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");

    // Layout chính
    VBox layout = new VBox(10);
    layout.getChildren().addAll(searchBox, tableAndDetail, buttonBox);
    layout.setPadding(new Insets(10));

    // Scene và Stage
    Scene scene = new Scene(layout, 1000, 600);
    setScene(scene);

    //chuc nang kiem tra qua han
    filterButton.setOnAction(e -> {
      filterBorrowersWithOverdueBooks();
    });

    //xử lý khi nhập vào ô tìm kiếm
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      filterBorrowers(newValue.trim(), leftTable); // Gọi hàm lọc khi nội dung thay đổi
    });

    // Xử lý chức năng thêm, sửa, xóa
    addButton.setOnAction(e -> addNewBorrower());
    editButton.setOnAction(e -> {
      Borrower selectedBorrower = leftTable.getSelectionModel().getSelectedItem();
      if (selectedBorrower != null) {
        editBorrower(selectedBorrower);
      } else {
        showAlert("Lỗi", "Vui lòng chọn người mượn để sửa!", AlertType.ERROR);
      }
    });
    deleteButton.setOnAction(e -> {
      Borrower selectedBorrower = leftTable.getSelectionModel().getSelectedItem();
      if (selectedBorrower != null) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa?");
        confirmAlert.setContentText(
            "Mã: " + selectedBorrower.getIdBorrower() + "\nTên: " + selectedBorrower.getName()
        );

        confirmAlert.showAndWait().ifPresent(response -> {
          if (response == ButtonType.OK) {
            deleteBorrower(selectedBorrower);
            loadBorrowerFromDatabase(); // Tải lại dữ liệu sau khi xóa
          }
        });
      } else {
        showAlert("Lỗi", "Vui lòng chọn người mượn để xóa!", AlertType.ERROR);
      }
    });

    //xu ly chuc nang muon sach
    borrowButton.setOnAction(e -> {
      Borrower selectedBorrower = leftTable.getSelectionModel().getSelectedItem();
      if (selectedBorrower == null) {
        showAlert("Lỗi", "Vui lòng chọn người mượn để thao tác", AlertType.ERROR);
      } else {
        borrowDocument(selectedBorrower);
        try {
          documentManagementInstance.loadDocumentsFromDatabase();
        } catch (NullPointerException ex) {

        }
      }
    });
    //xử lý chức năng trả sách
    returnButton.setOnAction(e -> {
      Borrower selectedBorrower = leftTable.getSelectionModel().getSelectedItem();
      if (selectedBorrower == null) {
        showAlert("Lỗi", "Vui lòng chọn người mượn để thao tác", AlertType.ERROR);
      } else {
        int id = selectedBorrower.getIdBorrower();
        BorrowHistoryDialog borrowHistoryDialog = new BorrowHistoryDialog(id,
            documentManagementInstance);
      }
    });
  }


  /**
   * cap nhat thong tin nguoi muon tu database len bang hien thi
   */
  // Cập nhật loadBorrowerFromDatabase để đồng bộ với bảng leftTable
  private void loadBorrowerFromDatabase() {
    if (borrowerList == null) {
      borrowerList = FXCollections.observableArrayList();
    } else {
      borrowerList.clear();
    }

    ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
    String query = "SELECT * FROM borrower";

    try (Connection connection = apiAndDatabase.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery()) {

      while (resultSet.next()) {
        int idBorrower = resultSet.getInt("idBorrower");
        String name = resultSet.getString("name");
        String sex = resultSet.getString("sex");
        int age = resultSet.getInt("age");
        String email = resultSet.getString("email");
        String phone = resultSet.getString("phone");
        String address = resultSet.getString("address");

        Borrower borrower = new Borrower(idBorrower, name, sex, age, email, phone, address);
        borrowerList.add(borrower);
      }

      leftTable.setItems(borrowerList); // Cập nhật đúng bảng

      if (borrowerList.isEmpty()) {
        showAlert("Thông báo", "Không có dữ liệu người mượn trong cơ sở dữ liệu.",
            AlertType.INFORMATION);
      }

      leftTable.getSelectionModel().selectedItemProperty()
          .addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
              detailArea.setText(
                  "ID: " + newValue.getIdBorrower() + "\n" +
                      "Tên: " + newValue.getName() + "\n" +
                      "Giới tính: " + newValue.getSex() + "\n" +
                      "Tuổi: " + newValue.getAge() + "\n" +
                      "Email: " + newValue.getEmail() + "\n" +
                      "Số điện thoại: " + newValue.getPhone() + "\n" +
                      "Địa chỉ: " + newValue.getAddress()
              );
            } else {
              detailArea.clear(); // Xóa nội dung nếu không có gì được chọn
            }
          });

    } catch (SQLException e) {
      e.printStackTrace();
      showAlert("Lỗi", "Không thể tải dữ liệu từ database!\n" + e.getMessage(), AlertType.ERROR);
    }
  }


  /**
   * them nguoi muon( nhap du lieu)
   */
  private void addNewBorrower() {
    Stage addBorrowerStage = new Stage();
    addBorrowerStage.initModality(Modality.APPLICATION_MODAL);
    addBorrowerStage.setTitle("Thêm người mượn");

    GridPane grid = new GridPane();
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Tên"), 0, 0);
    nameField = new TextField();
    grid.add(nameField, 1, 0);

    grid.add(new Label("Giới tính"), 0, 1);
    sexField = new ComboBox<>();
    sexField.getItems().addAll("nam", "nữ", "khác");
    grid.add(sexField, 1, 1);

    grid.add(new Label("Tuổi"), 0, 2);
    ageField = new TextField();
    grid.add(ageField, 1, 2);

    grid.add(new Label("Email"), 0, 3);
    emailField = new TextField();
    grid.add(emailField, 1, 3);

    grid.add(new Label("SĐT"), 0, 4);
    phoneField = new TextField();
    grid.add(phoneField, 1, 4);

    grid.add(new Label("Địa chỉ"), 0, 5);
    addressField = new TextField();
    grid.add(addressField, 1, 5);

    HBox addButtonBox = new HBox(10);
    addButtonBox.setAlignment(Pos.CENTER);
    Button add = new Button("Thêm");
    Button cancel = new Button("Hủy");
    addButtonBox.getChildren().addAll(add, cancel);

    VBox addLayout = new VBox(10, grid, addButtonBox);
    addLayout.setPadding(new Insets(10));
    addLayout.setAlignment(Pos.CENTER);

    add.setOnAction(e -> {
      try {
        String name = nameField.getText().trim();
        String sex = sexField.getValue();
        String ageText = ageField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        // Biểu thức chính quy kiểm tra email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        String phoneRegex = "^(0[3|5|7|8|9])[0-9]{8}$";

        if (phone.isEmpty()) {
          showAlert("Lỗi", "Số điện thoại không được để trống!", AlertType.ERROR);
          return;
        }

        if (name.isEmpty()) {
          showAlert("Lỗi", "Tên không được để trống!", AlertType.ERROR);
          return;
        }

        if (sex == null) {
          showAlert("Lỗi", "Giới tính không được để trống!", AlertType.ERROR);
          return;
        }

        if (ageText.isEmpty()) {
          showAlert("Lỗi", "Tuổi không được để trống!", AlertType.ERROR);
          return;
        }

        if (!ageText.matches("\\d+")) {
          showAlert("Lỗi", "Tuổi phải là một số nguyên hợp lệ!", AlertType.ERROR);
          return;
        }

        int age = Integer.parseInt(ageText);
        if (age < 0) {
          showAlert("Lỗi", "Tuổi không thể là số âm!", AlertType.ERROR);
          return;
        }

        if (email.isEmpty()) {
          showAlert("Lỗi", "Email không được để trống!", AlertType.ERROR);
          return;
        }

        if (!email.matches(emailRegex)) {
          showAlert("Lỗi", "Email không đúng định dạng!", AlertType.ERROR);
          return;
        }
        if (!phone.matches(phoneRegex)) {
          showAlert("Lỗi", "Số điện thoại không đúng định dạng!", AlertType.ERROR);
          return;
        }
        if (isEmailExist(email)) {
          showAlert("Lỗi", "Email đã tồn tại!", AlertType.ERROR);
          return;
        }
        if (isPhoneNumberExist(phone)) {
          showAlert("Lỗi", "SĐT đã tồn tại!", AlertType.ERROR);
          return;
        }

        Borrower borrower = new Borrower(name, sex, age, email, phone, address);
        insertNewBorrower(borrower);
        addBorrowerStage.close();

      } catch (NumberFormatException ex) {
        showAlert("Lỗi", "Tuổi không hợp lệ!", AlertType.ERROR);
      }
    });

    cancel.setOnAction(e -> addBorrowerStage.close());

    Scene addScene = new Scene(addLayout, 500, 400);
    addBorrowerStage.setScene(addScene);
    addBorrowerStage.showAndWait();
    loadBorrowerFromDatabase();
  }

  // Kiểm tra số điện thoại đã tồn tại trong cơ sở dữ liệu hay chưa
  private boolean isPhoneNumberExist(String phoneNumber) {
    String query = "SELECT COUNT(*) FROM borrower WHERE phone = ?";
    try (Connection conn = ApiAndDatabase.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, phoneNumber);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0; // Trả về true nếu số điện thoại đã tồn tại
        }
      }
    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể kiểm tra số điện thoại!", AlertType.ERROR);
    }
    return false; // Trả về false nếu có lỗi hoặc không tồn tại
  }

  // Kiểm tra email đã tồn tại trong cơ sở dữ liệu hay chưa
  private boolean isEmailExist(String email) {
    String query = "SELECT COUNT(*) FROM borrower WHERE email = ?";
    try (Connection conn = ApiAndDatabase.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, email);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0; // Trả về true nếu email đã tồn tại
        }
      }
    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể kiểm tra email!", AlertType.ERROR);
    }
    return false; // Trả về false nếu có lỗi hoặc không tồn tại
  }


  /**
   * them nguoi dung vao database
   *
   * @param borrower thong tin ng dung
   */
  private void insertNewBorrower(Borrower borrower) {
    ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
    try (Connection connection = apiAndDatabase.getConnection();) {

      String insertQuery = "INSERT INTO borrower (name, sex, age, email, phone, address) "
          + "VALUES (?, ?, ?, ?, ?, ?)";

      PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
      preparedStatement.setString(1, borrower.getName());
      preparedStatement.setString(2, borrower.getSex());
      preparedStatement.setInt(3, borrower.getAge());
      preparedStatement.setString(4, borrower.getEmail());
      preparedStatement.setString(5, borrower.getPhone());
      preparedStatement.setString(6, borrower.getAddress());

      preparedStatement.executeUpdate();
      showAlert("Thông báo", "Đã thêm thành công!", AlertType.INFORMATION);

    } catch (SQLException e) {
      showAlert("Lỗi", "Không thêm thành công", AlertType.ERROR);
    }
  }

  /**
   * chinh sua thong tin nguoi dung
   *
   * @param borrower nguoi dung can chinh sua
   */
  private void editBorrower(Borrower borrower) {
    Stage editBorrowerStage = new Stage();
    this.editBorrowerStage = editBorrowerStage;
    editBorrowerStage.initModality(Modality.APPLICATION_MODAL);
    editBorrowerStage.setTitle("Sửa thông tin người mượn");

    GridPane grid = new GridPane();
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Tên người mượn:"), 0, 0);
    nameField = new TextField(borrower.getName());
    grid.add(nameField, 1, 0);

    grid.add(new Label("Giới tính:"), 0, 1);
    sexField = new ComboBox<>();
    sexField.getItems().addAll("Nam", "Nữ", "Khác");
    sexField.setValue(borrower.getSex());
    grid.add(sexField, 1, 1);

    grid.add(new Label("Tuổi:"), 0, 2);
    ageField = new TextField(String.valueOf(borrower.getAge()));
    grid.add(ageField, 1, 2);

    grid.add(new Label("Email:"), 0, 3);
    emailField = new TextField(borrower.getEmail());
    grid.add(emailField, 1, 3);

    grid.add(new Label("SĐT:"), 0, 4);
    phoneField = new TextField(borrower.getPhone());
    grid.add(phoneField, 1, 4);

    grid.add(new Label("Địa chỉ:"), 0, 5);
    addressField = new TextField(borrower.getAddress());
    grid.add(addressField, 1, 5);

    HBox buttonBox = new HBox(10);
    Button saveButton = new Button("Lưu");
    Button cancelButton = new Button("Hủy");
    buttonBox.getChildren().addAll(saveButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER);
    grid.add(buttonBox, 0, 6, 2, 1);

    saveButton.setOnAction(e -> {
      saveBorrower(borrower.getIdBorrower());
    });

    cancelButton.setOnAction(e -> {
      editBorrowerStage.close();
    });
    Scene editScene = new Scene(grid, 500, 400);
    editBorrowerStage.setScene(editScene);
    editBorrowerStage.show();
  }

  /**
   * luu thong tin nguoi dung
   *
   * @param idBorrower id
   */
  private void saveBorrower(int idBorrower) {
    try (Connection connection = ApiAndDatabase.getConnection()) {
      String name = nameField.getText().trim();
      String email = emailField.getText().trim();
      String phone = phoneField.getText().trim();
      int age = Integer.parseInt(ageField.getText().trim());
      String sex = sexField.getValue();
      String address = addressField.getText().trim();
      String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
      if (name.isEmpty()) {
        showAlert("Lỗi", "Tên không được để trống!", AlertType.ERROR);
        return;
      }

      if (sex == null) {
        showAlert("Lỗi", "Giới tính không được để trống!", AlertType.ERROR);
        return;
      }
      if (age < 0) {
        showAlert("Lỗi", "Tuổi phải lớn hơn 0!", AlertType.ERROR);
        return;
      }

      if (email.isEmpty()) {
        showAlert("Lỗi", "Email không được để trống!", AlertType.ERROR);
        return;
      }

      if (!email.matches(emailRegex)) {
        showAlert("Lỗi", "Email không đúng định dạng!", AlertType.ERROR);
        return;
      }

      String updateQuery = """
              UPDATE borrower
              SET name = ?, sex = ?, age = ?, email = ?, phone = ?, 
                  address = ?
              WHERE idBorrower = ?
          """;

      try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, sex);
        preparedStatement.setInt(3, age);
        preparedStatement.setString(4, email);
        preparedStatement.setString(5, phone);
        preparedStatement.setString(6, address);
        preparedStatement.setInt(7, idBorrower);

        int rowsAffected = preparedStatement.executeUpdate();

        if (rowsAffected > 0) {
          loadBorrowerFromDatabase();
          showAlert("Thông báo", "Cập nhật thành công!", AlertType.INFORMATION);
          close();
          editBorrowerStage.close();
        } else {
          showAlert("Lỗi", "Không thể cập nhật người mượn!", AlertType.ERROR);
        }
      }
    } catch (NumberFormatException e) {
      showAlert("Lỗi", "Vui lòng nhập tuổi!", AlertType.ERROR);
    } catch (SQLException e) {
      showAlert("Lỗi", "Lỗi kết nối cơ sở dữ liệu: ", AlertType.ERROR);
    }
  }

  /**
   * xoa nguoi dung
   *
   * @param borrower nguoi dung can xoa
   */
  private void deleteBorrower(Borrower borrower) {
    String checkQuery = "SELECT COUNT(*) AS bookCount FROM borrow_history WHERE idBorrower = ? AND status IN ('borrowed', 'overdue')";
    String deleteQuery = "DELETE FROM borrower WHERE idBorrower = ?";

    try (Connection conn = ApiAndDatabase.getConnection();
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {

      // Kiểm tra người mượn còn sách chưa trả hay không
      checkStmt.setInt(1, borrower.getIdBorrower());
      ResultSet rs = checkStmt.executeQuery();

      if (rs.next() && rs.getInt("bookCount") > 0) {
        showAlert("Thông báo", "Người mượn vẫn còn sách chưa trả!", AlertType.WARNING);
        return; // Dừng việc xóa nếu người mượn còn sách
      }

      // Thực hiện xóa nếu không còn sách chưa trả
      deleteStmt.setInt(1, borrower.getIdBorrower());
      int rowsAffected = deleteStmt.executeUpdate();

      if (rowsAffected > 0) {
        showAlert("Thông báo", "Xóa thành công", AlertType.INFORMATION);
      } else {
        showAlert("Thông báo", "Không tìm thấy người mượn để xóa", AlertType.WARNING);
      }
    } catch (SQLException e) {
      showAlert("Lỗi", "Xóa thất bại!", AlertType.ERROR);
    }
  }


  /**
   * chức năng tìm kiếm
   *
   * @param searchTerm nội dung
   * @param table      cập nhật bảng
   */
  private void filterBorrowers(String searchTerm, TableView<Borrower> table) {
    if (searchTerm == null || searchTerm.trim().isEmpty()) {
      table.setItems(borrowerList); // Hiển thị lại toàn bộ danh sách nếu từ khóa rỗng
      return;
    }

    String lowerCaseTerm = searchTerm.toLowerCase();
    ObservableList<Borrower> filteredList = FXCollections.observableArrayList();

    for (Borrower borrower : borrowerList) {
      if (borrower.getName().toLowerCase().contains(lowerCaseTerm) ||
          borrower.getPhone().contains(lowerCaseTerm)) {
        filteredList.add(borrower);
      }
    }

    table.setItems(filteredList); // Cập nhật bảng với danh sách lọc

    if (filteredList.isEmpty()) {
      showAlert("Kết quả tìm kiếm", "Không tìm thấy người mượn nào phù hợp.",
          AlertType.INFORMATION);
    }
  }

  private void borrowDocument(Borrower borrower) {
    Stage borrowStage = new Stage();
    borrowStage.initModality(Modality.APPLICATION_MODAL);
    borrowStage.setTitle("Mượn tài liệu");

    // Thanh tìm kiếm
    TextField searchField = new TextField();
    searchField.setPromptText("Tìm kiếm theo tên tài liệu hoặc tác giả...");
    searchField.setPrefWidth(400);

    HBox searchBox = new HBox(10, searchField);
    searchBox.setAlignment(Pos.CENTER);
    searchBox.setPadding(new Insets(10));

    // Bảng hiển thị tài liệu
    TableView<Document> documentTable = new TableView<>();
    documentTable.setPrefSize(600, 300);

    TableColumn<Document, String> titleCol = new TableColumn<>("Tên tài liệu");
    titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
    titleCol.setPrefWidth(300);

    TableColumn<Document, String> authorCol = new TableColumn<>("Tác giả");
    authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
    authorCol.setPrefWidth(200);

    documentTable.getColumns().addAll(titleCol, authorCol);
    ObservableList<Document> documentList = FXCollections.observableArrayList();
    documentTable.setItems(documentList);
    loadDocuments(documentList); // Hàm tải tài liệu từ database hoặc danh sách sẵn có

    // Lọc tài liệu khi nhập từ khóa tìm kiếm
    searchField.textProperty().addListener((observable, oldValue, newValue) -> {
      filterDocument(documentList, documentTable, newValue);
    });

    // Chọn ngày trả
    Label returnDateLabel = new Label("Ngày trả:");
    DatePicker returnDatePicker = new DatePicker();
    returnDatePicker.setValue(
        LocalDate.now().plusMonths(3)); // Mặc định là 3 tháng từ ngày hiện tại

    HBox returnDateBox = new HBox(10, returnDateLabel, returnDatePicker);
    returnDateBox.setAlignment(Pos.CENTER);
    returnDateBox.setPadding(new Insets(10));

    // Nút mượn và thoát
    Button borrowButton = new Button("Mượn");
    borrowButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    Button cancelButton = new Button("Thoát");
    cancelButton.setStyle(
        "-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold;");

    HBox buttonBox = new HBox(20, borrowButton, cancelButton);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setPadding(new Insets(10));

    // Xử lý nút mượn
    borrowButton.setOnAction(e -> {
      Document selectedDocument = documentTable.getSelectionModel().getSelectedItem();

      if (selectedDocument == null) {
        showAlert("Lỗi", "Vui lòng chọn tài liệu để mượn!", Alert.AlertType.ERROR);
        return;
      }

      LocalDate returnDate = returnDatePicker.getValue();
      if (returnDate == null) {
        showAlert("Lỗi", "Vui lòng chọn ngày trả hợp lệ!", Alert.AlertType.ERROR);
        return;
      }

      borrowDocumentForBorrower(borrower, selectedDocument, returnDate); // Gọi hàm mượn tài liệu
      try {
        documentManagementInstance.loadDocumentsFromDatabase();
      } catch (NullPointerException ex) {

      }
      borrowStage.close();
    });

    // Xử lý nút thoát
    cancelButton.setOnAction(e -> borrowStage.close());

    // Layout chính
    VBox layout = new VBox(10, searchBox, documentTable, returnDateBox, buttonBox);
    layout.setPadding(new Insets(15));
    layout.setAlignment(Pos.CENTER);

    // Hiển thị
    Scene scene = new Scene(layout, 650, 500);
    borrowStage.setScene(scene);
    borrowStage.show();
  }

  private void borrowDocumentForBorrower(Borrower borrower, Document document,
      LocalDate returnDate) {
    String checkQuery = "SELECT status FROM borrow_history " +
        "WHERE idBorrower = ? AND idDocument = ? ORDER BY returnDate DESC LIMIT 1";
    String updateStatusQuery = "UPDATE borrow_history " +
        "SET borrowDate = ?, returnDate = ?, status = ? " +
        "WHERE idBorrower = ? AND idDocument = ? AND status = 'returned'";
    String insertQuery =
        "INSERT INTO borrow_history (idBorrower, idDocument, borrowDate, returnDate, status) " +
            "VALUES (?, ?, ?, ?, ?)";
    String updateDocumentQuantityQuery = "UPDATE document SET quantity = quantity - 1 WHERE idDocument = ?";
    String checkQuantityQuery = "SELECT quantity FROM document WHERE idDocument = ?";

    try (
        Connection conn = ApiAndDatabase.getConnection();
        PreparedStatement checkQuantity = conn.prepareStatement(checkQuantityQuery);
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        PreparedStatement updateStatusStmt = conn.prepareStatement(updateStatusQuery);
        PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
        PreparedStatement updateQuantityStmt = conn.prepareStatement(updateDocumentQuantityQuery)) {

      checkQuantity.setInt(1, document.getIdDocument());
      ResultSet quantity = checkQuantity.executeQuery();
      if (quantity.next() && quantity.getInt("quantity") <= 0) {
        showAlert("Lỗi", "Không đủ sách để mượn!", Alert.AlertType.ERROR);
        return;
      }

      // Kiểm tra trạng thái mượn gần nhất
      checkStmt.setInt(1, borrower.getIdBorrower());
      checkStmt.setInt(2, document.getIdDocument());
      ResultSet rs = checkStmt.executeQuery();

      if (rs.next()) {
        String status = rs.getString("status");

        if ("overdue".equalsIgnoreCase(status)) {
          showAlert("Lỗi", "Người mượn đã mượn tài liệu này và đang ở trạng thái quá hạn!",
              Alert.AlertType.ERROR);
          return;
        } else if ("borrowed".equalsIgnoreCase(status)) {
          showAlert("Lỗi", "Người mượn hiện đang giữ tài liệu này!", Alert.AlertType.ERROR);
          return;
        } else if ("returned".equalsIgnoreCase(status)) {
          // Nếu trạng thái là 'returned', chỉ cập nhật thành 'borrowed'
          updateStatusStmt.setDate(1, Date.valueOf(LocalDate.now())); // Ngày mượn mới
          updateStatusStmt.setDate(2, Date.valueOf(returnDate));      // Ngày trả mới

          // Kiểm tra nếu ngày trả nhỏ hơn ngày hiện tại thì đổi trạng thái thành "overdue"
          if (returnDate.isBefore(LocalDate.now())) {
            updateStatusStmt.setString(3, "overdue");
          } else {
            updateStatusStmt.setString(3, "borrowed");
          }

          updateStatusStmt.setInt(4, borrower.getIdBorrower());
          updateStatusStmt.setInt(5, document.getIdDocument());
          updateStatusStmt.executeUpdate();

          // Giảm số lượng tài liệu
          updateQuantityStmt.setInt(1, document.getIdDocument());
          updateQuantityStmt.executeUpdate();

          showAlert("Thành công", "Mượn tài liệu thành công!", Alert.AlertType.INFORMATION);
          return;
        }
      }

      // Kiểm tra số lượng tài liệu còn lại

      // Thêm bản ghi mới vào borrow_history
      String status = returnDate.isBefore(LocalDate.now()) ? "overdue"
          : "borrowed"; // Chọn trạng thái "overdue" nếu ngày trả nhỏ hơn ngày hiện tại
      insertStmt.setInt(1, borrower.getIdBorrower());
      insertStmt.setInt(2, document.getIdDocument());
      insertStmt.setDate(3, Date.valueOf(LocalDate.now()));
      insertStmt.setDate(4, Date.valueOf(returnDate));
      insertStmt.setString(5, status);
      insertStmt.executeUpdate();

      // Giảm số lượng tài liệu
      updateQuantityStmt.setInt(1, document.getIdDocument());
      updateQuantityStmt.executeUpdate();

      showAlert("Thành công", "Mượn tài liệu thành công!", Alert.AlertType.INFORMATION);
    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể mượn tài liệu: " + e.getMessage(), Alert.AlertType.ERROR);
    }
  }

  private void loadDocuments(ObservableList<Document> documentList) {
    String query = "SELECT idDocument, title, author FROM document";
    try (Connection connection = new ApiAndDatabase().getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query)) {

      while (resultSet.next()) {
        int id = resultSet.getInt("idDocument");
        String title = resultSet.getString("title");
        String author = resultSet.getString("author");

        documentList.add(new Document(id, title, author));
      }
    } catch (SQLException e) {
      showAlert("Lỗi", "Không thể tải danh sách tài liệu!", AlertType.ERROR);
    }
  }

  /**
   * tim kiem tai lieu
   *
   * @param documentList  ds tai lieu
   * @param documentTable bang tai lieu
   * @param keyword       tu khoa da nhap
   */
  private void filterDocument(ObservableList<Document> documentList,
      TableView<Document> documentTable, String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      documentTable.setItems(documentList);
      return;
    }

    String lowerCaseKeyword = keyword.toLowerCase();

    // Tạo danh sách đã lọc
    ObservableList<Document> filteredList = documentList.filtered(doc ->
        doc.getTitle().toLowerCase().contains(lowerCaseKeyword) ||
            doc.getAuthor().toLowerCase().contains(lowerCaseKeyword)
    );

    documentTable.setItems(filteredList);
  }

  private void filterBorrowersWithOverdueBooks() {
    ObservableList<Borrower> allBorrowers = leftTable.getItems();
    ObservableList<Borrower> borrowersWithOverdueBooks = FXCollections.observableArrayList();

    // Duyệt qua tất cả người mượn và kiểm tra trạng thái sách mượn của họ
    for (Borrower borrower : allBorrowers) {
      if (hasOverdueBooks(borrower)) {
        borrowersWithOverdueBooks.add(borrower); // Thêm người mượn có sách quá hạn vào danh sách
      }
    }

    // Cập nhật lại bảng với những người mượn có sách quá hạn
    leftTable.setItems(borrowersWithOverdueBooks);
  }

  // Phương thức kiểm tra xem người mượn có sách quá hạn hay không
  private boolean hasOverdueBooks(Borrower borrower) {
    ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
    // Giả định rằng bạn sẽ truy vấn cơ sở dữ liệu để kiểm tra các bản ghi mượn của người mượn này
    String query = "SELECT * FROM borrow_history WHERE idBorrower = ? AND status = 'overdue'";
    try {
      Connection connection = apiAndDatabase.getConnection();
      PreparedStatement stmt = connection.prepareStatement(query);
      stmt.setInt(1, borrower.getIdBorrower());
      ResultSet rs = stmt.executeQuery();

      return rs.next(); // Nếu có ít nhất một sách quá hạn, kết quả sẽ có dòng dữ liệu
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private void showAlert(String title, String message, AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}