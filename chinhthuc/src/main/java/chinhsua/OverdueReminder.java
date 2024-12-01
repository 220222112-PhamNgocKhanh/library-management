package chinhsua;

import eu.hansolo.fx.countries.tools.Api;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.mail.*;
import javax.mail.internet.*;

public class OverdueReminder {


  private final String emailUsername = "phamngockhanh210204@gmail.com";
  private final String emailPassword = "vkkd ghnu rnej tkqu";

  public void sendOverdueReminders() {
    List<Borrower> overdueBorrowers = getOverdueBorrowers();

    if (overdueBorrowers.isEmpty()) {
      showAlert("Thông báo", "Không có người mượn quá hạn.", AlertType.INFORMATION);
      return;
    }

    int successfulEmails = 0;
    for (Borrower borrower : overdueBorrowers) {
      boolean success = sendEmail(borrower);
      if (success) {
        successfulEmails++;
      }
    }

    showAlert("Kết quả",
        successfulEmails + " email nhắc nhở đã được gửi thành công.",
        AlertType.INFORMATION);
  }

  private List<Borrower> getOverdueBorrowers() {
    List<Borrower> overdueBorrowers = new ArrayList<>();
    String query = """
            SELECT DISTINCT b.idBorrower, b.name, b.email
                        FROM borrower b
                        JOIN borrow_history bh ON b.idBorrower = bh.idBorrower
                        where bh.status = "overdue"
        """;

    ApiAndDatabase apiAndDatabase = new ApiAndDatabase();
    try (Connection conn = apiAndDatabase.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          int id = rs.getInt("idBorrower");
          String name = rs.getString("name");
          String email = rs.getString("email");

          overdueBorrowers.add(new Borrower(id, name, email));
        }
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage()
      );
    }

    return overdueBorrowers;
  }

  private boolean sendEmail(Borrower borrower) {
    String recipient = borrower.getEmail();
    String subject = "Nhắc nhở trả sách quá hạn";
    String content = """
            Xin chào %s,
        
            Bạn hiện đang có sách mượn quá hạn. Vui lòng đến thư viện để hoàn trả sớm nhất.
        
            Cảm ơn!
        """.formatted(borrower.getName());

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
      message.setText(content);

      Transport.send(message);
      return true;
    } catch (MessagingException e) {
      showAlert("Lỗi", "Không thể gửi email đến: " + recipient + "\n" + e.getMessage(),
          AlertType.ERROR);
      return false;
    }
  }

  private void showAlert(String title, String message, AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
