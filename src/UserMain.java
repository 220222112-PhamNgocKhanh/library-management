import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class UserMain extends JFrame {
    private ArrayList<Document> documentList;
    private JTextField searchField;
    private JPanel documentPanel;
    private User currentUser;

    public UserMain(ArrayList<Document> documentList, User currentUser) {
        this.documentList = documentList;
        this.currentUser = currentUser;
        setTitle("Thư viện - Giao diện Người dùng");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Thêm một số tài liệu mẫu
        addSampleDocuments(); // Phương thức để thêm tài liệu mẫu

        // Ô rộng hiển thị các tài liệu
        documentPanel = new JPanel();
        documentPanel.setLayout(new GridLayout(0, 4, 10, 10)); // Sắp xếp theo ma trận 4 cột
        JScrollPane scrollPane = new JScrollPane(documentPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Panel chứa hai nút "Thư viện của tôi" và "Thông tin tài khoản"
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton myLibraryButton = new JButton("Thư viện của tôi");
        JButton accountInfoButton = new JButton("Thông tin tài khoản");

        myLibraryButton.addActionListener(e -> showMyLibrary());
        accountInfoButton.addActionListener(e -> showAccountInfo());

        bottomPanel.add(myLibraryButton);
        bottomPanel.add(accountInfoButton);
        add(bottomPanel, BorderLayout.SOUTH);


        // Thêm sự kiện cho nút tìm kiếm
        searchButton.addActionListener(e -> searchDocuments());

        // Hiển thị danh sách tài liệu ban đầu
        displayDocuments(documentList);
    }

    private void displayDocuments(ArrayList<Document> documents) {
        documentPanel.removeAll();
        for (Document doc : documents) {
            JPanel docPanel = new JPanel(new BorderLayout());
            JLabel titleLabel = new JLabel("<html><center>" + doc.getTitle() + "</center></html>", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            docPanel.add(titleLabel, BorderLayout.SOUTH);
            docPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            docPanel.setBackground(new Color(220, 220, 220));
            docPanel.setPreferredSize(new Dimension(150, 150));

            // Sự kiện click để xem chi tiết tài liệu
            docPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showDocumentDetails(doc);
                }
            });

            documentPanel.add(docPanel);
        }
        documentPanel.revalidate();
        documentPanel.repaint();
    }


    private void searchDocuments() {
        // Thực hiện tìm kiếm tài liệu dựa trên từ khóa trong searchField
        String keyword = searchField.getText().trim().toLowerCase();
        ArrayList<Document> filteredDocuments = new ArrayList<>();
        for (Document doc : documentList) {
            if (doc.getTitle().toLowerCase().contains(keyword) ||
                    doc.getAuthor().toLowerCase().contains(keyword)){
                filteredDocuments.add(doc);
            }
        }
        displayDocuments(filteredDocuments);
    }

    private void showDocumentDetails(Document document) {
        // Hiển thị hộp thoại thông tin chi tiết tài liệu
        JDialog detailsDialog = new JDialog(this, "Thông tin tài liệu", true);
        detailsDialog.setSize(400, 300);
        detailsDialog.setLayout(new BorderLayout());
        detailsDialog.setLocationRelativeTo(this);

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setText("Tên: " + document.getTitle() + "\n" +
                "Tác giả: " + document.getAuthor() + "\n" +
                "Thể loại: " + document.getCategory() + "\n" +
                "Trạng thái: " + document.getStatus() + "\n" +
                "Số lượng còn: " + document.getQuantity());
        detailsDialog.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton requestBorrowButton = new JButton("Yêu cầu mượn tài liệu");
        JButton requestReturnButton = new JButton("Yêu cầu trả tài liệu");
        buttonPanel.add(requestBorrowButton);
        buttonPanel.add(requestReturnButton);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Xử lý sự kiện yêu cầu mượn tài liệu
        requestBorrowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String request = "Mượn tài liệu: " + document.getTitle() + " bởi " + currentUser.getName();
                LibraryManager.addRequest(request); // Thêm yêu cầu vào LibraryManager
                JOptionPane.showMessageDialog(detailsDialog, "Yêu cầu mượn tài liệu đã được gửi đến thủ thư.");
                detailsDialog.dispose();
            }
        });

        // Xử lý sự kiện yêu cầu trả tài liệu
        requestReturnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String request = "Trả tài liệu: " + document.getTitle() + " bởi " + currentUser.getName();
                LibraryManager.addRequest(request); // Thêm yêu cầu vào LibraryManager
                JOptionPane.showMessageDialog(detailsDialog, "Yêu cầu trả tài liệu đã được gửi đến thủ thư.");
                detailsDialog.dispose();
            }
        });

        detailsDialog.setVisible(true);
    }

    // Thêm tài liệu mẫu vào danh sách
    private void addSampleDocuments() {
        documentList.add(new Document("Sách 1", "Tác giả A", "Thể loại 1", "Còn", 19));
        documentList.add(new Document("Sách 2", "Tác giả B", "Thể loại 2", "Hết", 20));
        documentList.add(new Document("Sách 3", "Tác giả C", "Thể loại 1", "Còn", 10));

    }

    private void showMyLibrary() {
        // Hiển thị danh sách các tác phẩm đã mượn của người dùng hiện tại
        JDialog myLibraryDialog = new JDialog(this, "Thư viện của tôi", true);
        myLibraryDialog.setSize(400, 300);
        myLibraryDialog.setLayout(new BorderLayout());
        myLibraryDialog.setLocationRelativeTo(this);

        JTextArea libraryArea = new JTextArea();
        libraryArea.setEditable(false);
        StringBuilder borrowedBooks = new StringBuilder("Các tác phẩm đã mượn:\n");
        for (Document doc : currentUser.getBorrowedBooks()) {
            borrowedBooks.append("- ").append(doc.getTitle()).append("\n");
        }
        libraryArea.setText(borrowedBooks.toString());
        myLibraryDialog.add(new JScrollPane(libraryArea), BorderLayout.CENTER);

        myLibraryDialog.setVisible(true);
    }

    private void showAccountInfo() {
        // Hiển thị thông tin tài khoản của người dùng
        JDialog accountInfoDialog = new JDialog(this, "Thông tin tài khoản", true);
        accountInfoDialog.setSize(300, 200);
        accountInfoDialog.setLayout(new BorderLayout());
        accountInfoDialog.setLocationRelativeTo(this);

        JTextArea accountInfoArea = new JTextArea();
        accountInfoArea.setEditable(false);
        accountInfoArea.setText("Tên: " + currentUser.getName() + "\n" +
                "ID: " + currentUser.getUserId() + "\n" +
                "Email: " + currentUser.getEmail());
        accountInfoDialog.add(new JScrollPane(accountInfoArea), BorderLayout.CENTER);

        accountInfoDialog.setVisible(true);
    }
}
