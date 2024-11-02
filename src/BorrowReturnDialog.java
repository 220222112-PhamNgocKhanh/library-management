import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class BorrowReturnDialog extends JDialog {
    private ArrayList<Document> documentList; // Danh sách tài liệu
    private Document selectedDocument; // Tài liệu được chọn
    private ArrayList<User> userList; // Danh sách người dùng gốc
    private ArrayList<User> filteredUserList; // Danh sách người dùng sau khi lọc
    private User selectedUser; // Người dùng được chọn
    private JList<String> userListDisplay;
    private DefaultListModel<String> userModel;
    private JTextArea userInfoArea; // Khu vực hiển thị thông tin người dùng
    private JTextField searchField; // Ô tìm kiếm thành viên

    public BorrowReturnDialog(JFrame parent, ArrayList<Document> documentList, Document selectedDocument, ArrayList<User> userList) {
        super(parent, "Mượn/Trả tài liệu", true);
        this.documentList = documentList;
        this.selectedDocument = selectedDocument;
        this.userList = userList;
        this.filteredUserList = new ArrayList<>(userList);

        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        JLabel infoLabel = new JLabel("Chọn thành viên thực hiện mượn/trả tài liệu:");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        // Panel chính chứa tìm kiếm, danh sách thành viên và thông tin chi tiết
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // Panel tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Dòng chữ gợi ý tìm kiếm
        JLabel searchLabel = new JLabel("Nhập tên thành viên để tìm kiếm:");

        // Ô tìm kiếm
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(150, 25)); // Kích thước của ô tìm kiếm
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterUserList(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterUserList(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterUserList(searchField.getText());
            }
        });

        // Thêm các thành phần vào panel tìm kiếm
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Panel chứa danh sách thành viên và thông tin chi tiết
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 10, 10)); // Chia thành 2 phần
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Hiển thị danh sách thành viên
        userModel = new DefaultListModel<>();
        userListDisplay = new JList<>(userModel);
        updateUserListDisplay(); // Cập nhật hiển thị danh sách ban đầu
        userListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScrollPane = new JScrollPane(userListDisplay);

        // Panel bên trái chứa tiêu đề và danh sách
        JPanel userListPanel = new JPanel(new BorderLayout());
        JLabel userListLabel = new JLabel("Danh sách thành viên:");
        userListLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userListLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userListPanel.add(userListLabel, BorderLayout.NORTH);
        userListPanel.add(userScrollPane, BorderLayout.CENTER);
        contentPanel.add(userListPanel);

        // Khu vực hiển thị thông tin người dùng
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
        contentPanel.add(userInfoPanel);

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
                filterUserList(searchField.getText());
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
                userList.remove(filteredUserList.get(selectedIndex));
                filterUserList(searchField.getText());
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
                selectedUser = filteredUserList.get(selectedUserIndex);
                if (selectedDocument.getQuantity() > 0) {
                    selectedUser.borrowBook(selectedDocument);
                    JOptionPane.showMessageDialog(this, "Mượn tài liệu thành công!");
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
                selectedUser = filteredUserList.get(selectedUserIndex);
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
                    selectedUser = filteredUserList.get(selectedUserIndex);
                    displayUserInfo(selectedUser);
                }
            }
        });
    }

    // Phương thức để hiển thị thông tin người dùng
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

    // Phương thức lọc danh sách người dùng theo từ khóa
    private void filterUserList(String keyword) {
        filteredUserList = userList.stream()
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
        updateUserListDisplay();
    }

    // Cập nhật hiển thị danh sách người dùng sau khi lọc
    private void updateUserListDisplay() {
        userModel.clear();
        for (User user : filteredUserList) {
            userModel.addElement(user.getName() + " (ID: " + user.getUserId() + ")");
        }
    }
}
