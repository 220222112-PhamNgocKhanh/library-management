package thang;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.*;
import javax.mail.internet.*;
import javafx.scene.control.Alert;

public class OverdueReminder {

  private final String emailUsername = "libraryoop5@gmail.com";
  private final String emailPassword = "ekcy cqdj shmj kqfg";

  private final Set<String> sentEmails = ConcurrentHashMap.newKeySet();

  public void sendReminders() {
    List<Borrower> overdueBorrowers = getOverdueBorrowers();
    List<Borrower> upcomingDueBorrowers = getUpcomingDueBorrowers();

    StringBuilder successMessage = new StringBuilder("Danh sách email đã được gửi:\n");

    for (Borrower borrower : overdueBorrowers) {
      if (!sentEmails.contains(borrower.getEmail())) {
        String emailContent = generateEmailContent(borrower, true);
        if (sendEmail(borrower, emailContent)) {
          successMessage.append(String.format("%s - %s", borrower.getName(), borrower.getEmail()));
          successMessage.append("\n");
          sentEmails.add(borrower.getEmail());
        }
      }
    }

    for (Borrower borrower : upcomingDueBorrowers) {
      if (!sentEmails.contains(borrower.getEmail())) {
        String emailContent = generateEmailContent(borrower, false);
        if (sendEmail(borrower, emailContent)) {
          successMessage.append(borrower.getEmail()).append("\n");
          sentEmails.add(borrower.getEmail());
        }
      }
    }

    showAlertArea("Kết quả", successMessage.toString(), Alert.AlertType.INFORMATION);
  }

  private boolean sendEmail(Borrower borrower, String emailContent) {
    String recipient = borrower.getEmail();
    String subject = "Nhắc nhở tài liệu mượn";

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    Session session = Session.getInstance(props, new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(emailUsername, emailPassword);
      }
    });

    try {
      Message message = new MimeMessage(session);
      message.setFrom(new InternetAddress(emailUsername));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
      message.setSubject(subject);
      message.setText(emailContent);

      Transport.send(message);
      return true;
    } catch (MessagingException e) {
      return false;
    }
  }

  private List<Borrower> getOverdueBorrowers() {
    List<Borrower> overdueBorrowers = new ArrayList<>();
    String query = """
        SELECT DISTINCT b.idBorrower, b.name, b.email
        FROM borrower b
        JOIN borrow_history bh ON b.idBorrower = bh.idBorrower
        WHERE bh.status = 'overdue'
        """;

    ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
    try (Connection conn = apiAndDatabase.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        int id = rs.getInt("idBorrower");
        String name = rs.getString("name");
        String email = rs.getString("email");
        overdueBorrowers.add(new Borrower(id, name, email));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return overdueBorrowers;
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

  private String generateEmailContent(Borrower borrower, boolean isOverdue) {
    StringBuilder emailContent = new StringBuilder("Xin chào, ").append(borrower.getName())
        .append(",\n");
    emailContent.append(
        "Bạn có tài liệu sắp hết hạn hoặc đã quá hạn. Xin vui lòng đến thư viện hoàn trả sớm nhất.\n\n");
    return emailContent.toString();
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
