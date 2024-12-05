package thang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DeleteDocument {
    public void deleteDocument(int idDocument) {
        String checkQuery = """
            SELECT  b.name, bh.status,b.phone
            FROM borrow_history bh
            JOIN borrower b ON bh.idBorrower = b.idBorrower
            WHERE bh.idDocument = ? AND (bh.status = 'overdue' OR bh.status = 'borrowed')
        """;

        String deleteQuery = "DELETE FROM document WHERE idDocument = ?";

        try (Connection conn = ApiAndDatabase.getConnection();
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {

            // Kiểm tra trạng thái mượn
            checkStmt.setInt(1, idDocument);
            ResultSet rs = checkStmt.executeQuery();

            StringBuilder message = new StringBuilder();
            message.append(String.format("Danh sách người mượn còn nợ:\n"));

            while (rs.next()) {
                String borrowerName = rs.getString("name");
                String status = rs.getString("status");
                String phone = rs.getString("phone");
                message.append(String.format("Tên: %s, Trạng thái: %s, Sđt: %s\n", borrowerName,status,phone));
            }

            System.out.println(message.length());
            // Nếu có trạng thái mượn hoặc quá hạn
            if (message.length() > 35) {
                showAlertarea("Không thể xóa tài liệu", "Tài liệu đang được mượn hoặc quá hạn:\n" + message, AlertType.WARNING);
                return; // Dừng thực hiện xóa
            }

            // Thực hiện xóa tài liệu
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, idDocument);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert("Thông báo", "Tài liệu đã được xóa thành công.", AlertType.INFORMATION);
                } else {
                    showAlert("Thông báo", "Không tìm thấy tài liệu để xóa.", AlertType.WARNING);
                }
            }

        } catch (SQLException e) {
            showAlert("Lỗi", "Xóa tài liệu thất bại: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void showAlertarea(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Sử dụng TextArea để hiển thị nội dung nhiều dòng
        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        // Thiết lập kích thước TextArea
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        // Thêm TextArea vào hộp thoại
        alert.getDialogPane().setContent(textArea);

        alert.showAndWait();
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
