import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BorrowReturnDialog extends JDialog {
    private ArrayList<Document> documentList;  // Danh sách tài liệu
    private Document selectedDocument;  // Tài liệu được chọn
    private ArrayList<User> userList;  // Danh sách người dùng
    private User selectedUser;  // Người dùng được chọn
    private JList<String> userListDisplay;
    private DefaultListModel<String> userModel;
    private JTextArea userInfoArea;  // Khu vực hiển thị thông tin người dùng

    public BorrowReturnDialog(JFrame parent, ArrayList<Document> documentList, Document selectedDocument, ArrayList<User> userList) {
        super(parent, "Mượn/Trả tài liệu", true);
        this.documentList = documentList;
        this.selectedDocument = selectedDocument;
        this.userList = userList;

        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JLabel infoLabel = new JLabel("Chọn thành viên thực hiện mượn/trả tài liệu:");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        // Panel chứa hai khu vực: danh sách thành viên và thông tin chi tiết
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // Chia thành 2 phần
        add(mainPanel, BorderLayout.CENTER);

        // Hiển thị danh sách thành viên
        userModel = new DefaultListModel<>();
        userListDisplay = new JList<>(userModel);
        for (User user : userList) {
            userModel.addElement(user.getName() + " (ID: " + user.getUserId() + ")");
        }
        userListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScrollPane = new JScrollPane(userListDisplay);
        mainPanel.add(userScrollPane);

        // Thêm tiêu đề cho ô bên trái
        JPanel userListPanel = new JPanel(new BorderLayout());
        JLabel userListLabel = new JLabel("Danh sách thành viên:");
        userListLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userListLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userListPanel.add(userListLabel, BorderLayout.NORTH);
        userListPanel.add(userScrollPane, BorderLayout.CENTER);
        mainPanel.add(userListPanel);

        // Khu vực hiển thị thông tin người dùng và danh sách sách đã mượn
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        JLabel userInfoLabel = new JLabel("Thông tin:");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userInfoPanel.add(userInfoLabel, BorderLayout.NORTH);

        userInfoArea = new JTextArea();
        userInfoArea.setEditable(false);
        userInfoArea.setBackground(new Color(240, 255, 255));
        JScrollPane infoScrollPane = new JScrollPane(userInfoArea);
        userInfoPanel.add(infoScrollPane, BorderLayout.CENTER);
        mainPanel.add(userInfoPanel);

        // Panel cho các nút chức năng
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10)); // Thêm các nút bên dưới
        add(buttonPanel, BorderLayout.SOUTH);

        // Nút Thêm thành viên
        JButton addUserButton = new JButton("Thêm thành viên");
        addUserButton.addActionListener(e -> {
            String userName = JOptionPane.showInputDialog(this, "Nhập tên thành viên:");
            String userId = JOptionPane.showInputDialog(this, "Nhập mã thành viên:");
            if (userName != null && userId != null && !userName.isEmpty() && !userId.isEmpty()) {
                User newUser = new User(userId, userName, "example@example.com");
                userList.add(newUser);
                userModel.addElement(userName + " (ID: " + userId + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            }
        });
        buttonPanel.add(addUserButton);

        // Nút Xóa thành viên
        JButton removeUserButton = new JButton("Xóa thành viên");
        removeUserButton.addActionListener(e -> {
            int selectedIndex = userListDisplay.getSelectedIndex();
            if (selectedIndex != -1) {
                userList.remove(selectedIndex);
                userModel.remove(selectedIndex);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn thành viên để xóa!");
            }
        });
        buttonPanel.add(removeUserButton);

        // Nút Mượn tài liệu
        JButton borrowButton = new JButton("Mượn tài liệu");
        borrowButton.addActionListener(e -> {
            int selectedUserIndex = userListDisplay.getSelectedIndex();
            if (selectedUserIndex != -1) {
                selectedUser = userList.get(selectedUserIndex);
                if (selectedDocument.getQuantity() > 0) {
                    selectedUser.borrowBook(selectedDocument);
                    JOptionPane.showMessageDialog(this, "Mượn tài liệu thành công!");
                    // Cập nhật lại thông tin hiển thị của người dùng sau khi mượn sách
                    displayUserInfo(selectedUser);
                } else {
                    JOptionPane.showMessageDialog(this, "Tài liệu đã hết!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn thành viên để mượn!");
            }
        });
        buttonPanel.add(borrowButton);

        // Nút Trả tài liệu
        JButton returnButton = new JButton("Trả tài liệu");
        returnButton.addActionListener(e -> {
            int selectedUserIndex = userListDisplay.getSelectedIndex();
            if (selectedUserIndex != -1) {
                selectedUser = userList.get(selectedUserIndex);
                selectedUser.returnBook(selectedDocument);
                JOptionPane.showMessageDialog(this, "Trả tài liệu thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn thành viên để trả!");
            }
        });
        buttonPanel.add(returnButton);

        // Hiển thị thông tin người dùng khi click vào danh sách
        userListDisplay.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedUserIndex = userListDisplay.getSelectedIndex();
                if (selectedUserIndex != -1) {
                    selectedUser = userList.get(selectedUserIndex);
                    displayUserInfo(selectedUser);
                }
            }
        });

        refreshUserList(); // Cập nhật danh sách người dùng khi mở BorrowReturnDialog
    }

    public void refreshUserList() {
        userModel.clear();
        for (User user : userList) {
            userModel.addElement(user.getName() + " (ID: " + user.getUserId() + ")");
        }
    }

    // Phương thức để hiển thị thông tin người dùng và sách đã mượn
    private void displayUserInfo(User user) {
        StringBuilder info = new StringBuilder();
        info.append("ID: ").append(user.getUserId()).append("\n");
        info.append("Tên: ").append(user.getName()).append("\n");
        info.append("Email: ").append(user.getEmail()).append("\n");
        info.append("Sách đã mượn:\n");
        for (Document doc : user.getBorrowedBooks()) {
            info.append("- ").append(doc.getTitle()).append("\n");
        }
        userInfoArea.setText(info.toString());
    }
}
