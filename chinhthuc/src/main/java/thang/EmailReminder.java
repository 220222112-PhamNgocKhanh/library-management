package thang;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.*;
import javax.mail.internet.*;

public abstract class EmailReminder {

  protected final String emailUsername = "libraryoop5@gmail.com";
  protected final String emailPassword = "ekcy cqdj shmj kqfg";
  protected final Set<String> sentEmails = ConcurrentHashMap.newKeySet();

  protected boolean sendEmail(String recipient, String subject, String emailContent) {
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
      e.printStackTrace();
      return false;
    }
  }

  protected abstract void sendReminders();

  protected String generateEmailContent(String name, boolean isOverdue) {
    StringBuilder emailContent = new StringBuilder("Xin chào, ").append(name).append(",\n");
    emailContent.append(isOverdue
        ? "Bạn có tài liệu đã quá hạn. Xin vui lòng đến thư viện hoàn trả sớm nhất.\n\n"
        : "Bạn có tài liệu sắp hết hạn. Xin vui lòng đến thư viện hoàn trả trước ngày hết hạn.\n\n");
    return emailContent.toString();
  }
}
