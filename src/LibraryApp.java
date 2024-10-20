// LibraryApp.java
import javax.swing.SwingUtilities;

public class LibraryApp {
    public static void main(String[] args) {
        // Khởi chạy màn hình khóa
        SwingUtilities.invokeLater(() -> new LockScreen().setVisible(true));
    }
}
