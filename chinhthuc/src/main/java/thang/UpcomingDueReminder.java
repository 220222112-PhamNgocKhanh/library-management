package thang;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UpcomingDueReminder extends EmailReminder {

  @Override
  public void sendReminders() {
    List<Borrower> upcomingDueBorrowers = getUpcomingDueBorrowers();
    StringBuilder successMessage = new StringBuilder("Danh sách email( sắp đến hạn) đã được gửi:\n");

    for (Borrower borrower : upcomingDueBorrowers) {
      if (!sentEmails.contains(borrower.getEmail())) {
        String emailContent = generateEmailContent(borrower.getName(), false);
        if (sendEmail(borrower.getEmail(), "Nhắc nhở tài liệu sắp hết hạn", emailContent)) {
          successMessage.append(String.format("%s - %s", borrower.getName(), borrower.getEmail())).append("\n");
          sentEmails.add(borrower.getEmail());
        }
      }
    }

    Platform.runLater(() -> showAlertArea("Kết quả", successMessage.toString(), Alert.AlertType.INFORMATION));
  }

  private List<Borrower> getUpcomingDueBorrowers() {
    List<Borrower> borrowers = new ArrayList<>();
    String query = """
            SELECT DISTINCT b.idBorrower, b.name, b.email
            FROM borrower b
            JOIN borrow_history bh ON b.idBorrower = bh.idBorrower
            WHERE DATEDIFF(bh.returnDate, CURDATE()) <= 3 AND bh.status = 'borrowed'
            """;

    ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
    try (Connection conn = apiAndDatabase.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int id = rs.getInt("idBorrower");
        String name = rs.getString("name");
        String email = rs.getString("email");
        borrowers.add(new Borrower(id, name, email));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return borrowers;
  }

  private void showAlertArea(String title, String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);

    javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(message);
    textArea.setEditable(false);
    textArea.setWrapText(true);
    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);

    alert.getDialogPane().setContent(textArea);
    alert.showAndWait();
  }
}
