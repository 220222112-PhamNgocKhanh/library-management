// LockScreen.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LockScreen extends JFrame {
    public LockScreen() {
        setTitle("Thư viện");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Lời chào
        JLabel welcomeLabel = new JLabel("Chào mừng đến với thư viện", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 24)); // Tùy chỉnh font chữ
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10)); // Khoảng cách viền
        add(welcomeLabel, BorderLayout.NORTH);

        // Nội dung chính
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Nhãn đăng nhập
        JLabel loginLabel = new JLabel("Đăng nhập với quyền", SwingConstants.CENTER);
        loginLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(loginLabel);
        mainPanel.add(Box.createVerticalStrut(20)); // Khoảng cách giữa nhãn và các nút

        // Các nút
        JButton librarianButton = new JButton("Thủ thư");
        librarianButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton userButton = new JButton("Người dùng");
        userButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(librarianButton);
        mainPanel.add(Box.createVerticalStrut(10)); // Khoảng cách giữa hai nút
        mainPanel.add(userButton);

        add(mainPanel, BorderLayout.CENTER);

        // Xử lý sự kiện
        librarianButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Mở giao diện quản lý cho thủ thư
                new LibrarianMain().setVisible(true);
                dispose(); // Đóng màn hình khóa
            }
        });

        userButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Hiển thị thông báo cho người dùng
                JOptionPane.showMessageDialog(LockScreen.this, "Chức năng này chưa được triển khai");
            }
        });
    }
}
