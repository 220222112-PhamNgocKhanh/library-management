import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class UserLoginRegisterDialog extends JDialog {
    public UserLoginRegisterDialog(JFrame parent) {
        super(parent, "Đăng nhập hoặc Đăng ký", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Nội dung chính
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Nhãn thông báo
        JLabel promptLabel = new JLabel("Chọn hành động:", SwingConstants.CENTER);
        promptLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(promptLabel);
        mainPanel.add(Box.createVerticalStrut(20)); // Khoảng cách giữa nhãn và các nút

        // Nút Đăng nhập
        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLoginForm();
            }
        });

        // Nút Tạo tài khoản
        JButton registerButton = new JButton("Tạo tài khoản");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegisterForm();
            }
        });

        // Thêm các nút vào giao diện
        mainPanel.add(loginButton);
        mainPanel.add(Box.createVerticalStrut(10)); // Khoảng cách giữa hai nút
        mainPanel.add(registerButton);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void showLoginForm() {
        // Tạo hộp thoại đăng nhập
        JTextField usernameField = new JTextField(15);
        JTextField idField = new JTextField(15);
        JTextField emailField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Tên đăng nhập:"));
        panel.add(usernameField);
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Đăng nhập", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String id = idField.getText();
            String email = emailField.getText();
            // Kiểm tra thông tin đăng nhập
            if (validateLogin(username, id, email)) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");

                dispose();
                // Lấy danh sách tài liệu từ LibraryManager
                ArrayList<Document> documentList = LibraryManager.getDocumentList();

                // Tạo đối tượng User
                User currentUser = new User(id, username, email);

                // Mở giao diện chính của người dùng với danh sách tài liệu và người dùng hiện tại
                new UserMain(documentList, currentUser).setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập hoặc tài khoản không tồn tại.");
            }
        }
    }

    private void showRegisterForm() {
        // Tạo hộp thoại đăng ký
        JTextField usernameField = new JTextField(15);
        JTextField idField = new JTextField(15);
        JTextField emailField = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Tên đăng nhập:"));
        panel.add(usernameField);
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Tạo tài khoản", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            // Lưu thông tin người dùng mới
            String username = usernameField.getText();
            String id = idField.getText();
            String email = emailField.getText();
            if (registerNewUser(username, id, email)) {
                JOptionPane.showMessageDialog(this, "Đăng ký tài khoản thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký không thành công. Vui lòng thử lại.");
            }
        }
    }

    private boolean validateLogin(String username, String id, String email) {
        // Kiểm tra thông tin đăng nhập trong danh sách người dùng từ LibraryManager
        ArrayList<User> userList = LibraryManager.getUserList();
        for (User user : userList) {
            if (user.getName().equals(username) &&
                    user.getUserId().equals(id) &&
                    user.getEmail().equals(email)) {
                return true; // Đăng nhập thành công
            }
        }
        return false; // Không tìm thấy thông tin phù hợp
    }

    private boolean registerNewUser(String username, String id, String email) {
        // Kiểm tra xem người dùng đã tồn tại chưa trong LibraryManager
        if (LibraryManager.isUserExists(id, email)) {
            return false; // Người dùng đã tồn tại
        }

        // Tạo người dùng mới và thêm vào LibraryManager
        User newUser = new User(id, username, email);
        LibraryManager.addUser(newUser);
        return true; // Đăng ký thành công
    }
}
