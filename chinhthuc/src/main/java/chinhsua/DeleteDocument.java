package chinhsua;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteDocument {
  public boolean deleteDocument(int idDocument) {
    String query = "DELETE FROM documents WHERE idDocuments = ?";
    try (Connection conn = ApiAndDatabase.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setInt(1, idDocument);
      int rowsAffected = stmt.executeUpdate();
      return rowsAffected > 0; // Trả về true nếu xóa thành công, ngược lại trả về false
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
    }
}

