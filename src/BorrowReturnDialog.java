import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BorrowReturnDialog extends JDialog {
    private ArrayList<Document> documentList;  // Danh sách tài liệu
    private Document selectedDocument;  // Tài liệu được chọn
    private ArrayList<User> userList;  // Danh sách người dùng
    private User selectedUser;  // Người dùng được chọn
    private JList<String> userListDisplay;
    private DefaultListModel<String> userModel;

    public BorrowReturnDialog(JFrame parent, ArrayList<Document> documentList, Document selectedDocument, ArrayList<User> userList) {
        super(parent, "Mượn/Trả tài liệu", true);
        this.documentList = documentList;
        this.selectedDocument = selectedDocument;
        this.userList = userList;

        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JLabel infoLabel = new JLabel("Chọn thành viên và thực hiện mượn hoặc trả tài liệu:");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        // Hiển thị danh sách thành viên
        userModel = new DefaultListModel<>();
        userListDisplay = new JList<>(userModel);
        for (User user : userList) {
            userModel.addElement(user.getName() + " (ID: " + user.getUserId() + ")");
        }
        userListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userListDisplay);
        add(scrollPane, BorderLayout.CENTER);

        // Panel cho các nút
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));

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
                    dispose();
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

        add(buttonPanel, BorderLayout.SOUTH);
    }
}