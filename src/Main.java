import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Main {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextArea bookInfoArea;
    private JTextField searchField;
    private ArrayList<Document> documentList; // Danh sách các tài liệu
    private ArrayList<User> userList; // Danh sách người dùng

    public Main() {
        documentList = new ArrayList<>();
        userList = new ArrayList<>();
        initialize();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Main window = new Main();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Hàm cập nhật thông tin sách vào ô "Thông tin"
    public void updateBookInfo(Document document) {
        String info = String.format("Tên sách: %s\nTác giả: %s\nThể loại: %s\nTrạng thái: %s\nSố lượng: %d",
                document.getTitle(), document.getAuthor(), document.getCategory(), document.getStatus(), document.getQuantity());
        bookInfoArea.setText(info);  // Cập nhật thông tin sách
    }

    private void initialize() {
        // Tạo cửa sổ chính
        frame = new JFrame("Thư viện");
        frame.setSize(800, 600); // Đặt kích thước khung chính
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        // Tiêu đề ở phía trên với thanh tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        frame.getContentPane().add(searchPanel, BorderLayout.NORTH);

        // Các nút chức năng
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10)); // 4 nút
        frame.getContentPane().add(buttonPanel, BorderLayout.CENTER);

        JButton btnAdd = new JButton("Thêm tài liệu");
        btnAdd.setBackground(new Color(173, 216, 230));
        btnAdd.setFont(new Font("Arial", Font.BOLD, 18));
        buttonPanel.add(btnAdd);

        JButton btnEdit = new JButton("Sửa tài liệu");
        btnEdit.setBackground(new Color(135, 206, 250));
        btnEdit.setFont(new Font("Arial", Font.BOLD, 18));
        buttonPanel.add(btnEdit);

        JButton btnDelete = new JButton("Xóa tài liệu");
        btnDelete.setBackground(new Color(255, 99, 71));
        btnDelete.setFont(new Font("Arial", Font.BOLD, 18));
        buttonPanel.add(btnDelete);

        JButton btnBorrowReturn = new JButton("Mượn/Trả tài liệu");
        btnBorrowReturn.setBackground(new Color(255, 165, 0));
        btnBorrowReturn.setFont(new Font("Arial", Font.BOLD, 18));
        buttonPanel.add(btnBorrowReturn);

        // Panel chính chứa bảng danh sách và khu vực thông tin
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(1, 2, 10, 10)); // Chia thành 2 phần
        frame.getContentPane().add(contentPanel, BorderLayout.SOUTH); // Di chuyển xuống dưới cùng

        // Tạo bảng hiển thị danh sách tài liệu
        String[] columnNames = {"Tên tài liệu", "Trạng thái", "Số lượng"};
        model = new DefaultTableModel(new Object[][]{}, columnNames);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(300, 300)); // Kích thước thu hẹp
        contentPanel.add(scrollPane);

        // Thêm một số tài liệu mẫu
        addSampleDocuments(); // Phương thức để thêm tài liệu mẫu

        // Khu vực thông tin chi tiết tài liệu
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(300, 300)); // Kích thước thu hẹp
        contentPanel.add(infoPanel);

        JLabel lblInfo = new JLabel("Thông tin:");
        lblInfo.setFont(new Font("Arial", Font.BOLD, 16));
        lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
        lblInfo.setBackground(new Color(173, 216, 230));
        lblInfo.setOpaque(true);
        infoPanel.add(lblInfo, BorderLayout.NORTH);

        bookInfoArea = new JTextArea();
        bookInfoArea.setEditable(false);
        bookInfoArea.setBackground(new Color(240, 255, 255));
        infoPanel.add(new JScrollPane(bookInfoArea), BorderLayout.CENTER);

        // Khi chọn một dòng trong bảng, hiển thị thông tin tài liệu
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        Document selectedDocument = documentList.get(selectedRow); // Lấy tài liệu từ danh sách
                        showBookInfo(selectedDocument); // Hiển thị thông tin chi tiết tài liệu
                    }
                }
            }
        });

        // Xử lý sự kiện nút "Thêm tài liệu"
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddDocumentDialog dialog = new AddDocumentDialog(frame, documentList, Main.this);
                dialog.setVisible(true);
            }
        });

        // Xử lý sự kiện nút "Sửa tài liệu"
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    EditDocumentDialog editDialog = new EditDocumentDialog(frame, documentList, selectedRow, Main.this);
                    editDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(frame, "Vui lòng chọn tài liệu để sửa!");
                }
            }
        });

        // Xử lý sự kiện nút "Xóa tài liệu"
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    documentList.remove(selectedRow); // Xóa tài liệu từ danh sách
                    updateTable(); // Cập nhật lại bảng
                    bookInfoArea.setText(""); // Xóa thông tin khi xóa tài liệu
                } else {
                    JOptionPane.showMessageDialog(frame, "Vui lòng chọn tài liệu để xóa!");
                }
            }
        });

        // Xử lý sự kiện nút "Mượn/Trả tài liệu"
        btnBorrowReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    Document selectedDocument = documentList.get(selectedRow); // Lấy tài liệu được chọn
                    BorrowReturnDialog borrowReturnDialog = new BorrowReturnDialog(frame, documentList, selectedDocument, userList);
                    borrowReturnDialog.setVisible(true); // Hiển thị dialog mượn/trả
                    updateTable(); // Cập nhật bảng sau khi mượn/trả sách
                } else {
                    JOptionPane.showMessageDialog(frame, "Vui lòng chọn tài liệu để mượn/trả!");
                }
            }
        });

        // Xử lý sự kiện nút "Tìm kiếm"
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(searchField.getText().trim());
            }
        });
    }

    // Hàm thực hiện tìm kiếm sách
    private void performSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Vui lòng nhập từ khóa tìm kiếm"); // Thông báo khi ô tìm kiếm trống
            return;
        }

        boolean found = false; // Biến kiểm tra xem có tìm thấy sách hay không
        for (int i = 0; i < documentList.size(); i++) {
            Document doc = documentList.get(i);
            if (doc.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                table.setRowSelectionInterval(i, i); // Highlight dòng chứa sách
                table.scrollRectToVisible(table.getCellRect(i, 0, true)); // Cuộn đến dòng chứa sách
                updateBookInfo(doc); // Cập nhật thông tin sách vào ô "Thông tin"
                found = true;
                break; // Tìm thấy sách và dừng tìm kiếm
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(frame, "Không tìm thấy sách"); // Thông báo khi không tìm thấy sách
            bookInfoArea.setText(""); // Xóa nội dung trong ô "Thông tin"
        }
    }

    // Thêm tài liệu mẫu vào danh sách
    private void addSampleDocuments() {
        documentList.add(new Document("Sách 1", "Tác giả A", "Thể loại 1", "Còn", 19));
        documentList.add(new Document("Sách 2", "Tác giả B", "Thể loại 2", "Hết", 20));
        documentList.add(new Document("Sách 3", "Tác giả C", "Thể loại 1", "Còn", 10));
        updateTable(); // Cập nhật bảng sau khi thêm tài liệu
    }

    // Hiển thị thông tin tài liệu
    private void showBookInfo(Document document) {
        bookInfoArea.setText(document.toString()); // Hiển thị chi tiết tài liệu
    }

    // Cập nhật bảng hiển thị danh sách tài liệu từ danh sách documentList
    public void updateTable() {
        model.setRowCount(0); // Xóa dữ liệu cũ trong bảng
        for (Document doc : documentList) {
            model.addRow(new Object[]{doc.getTitle(), doc.getStatus(), doc.getQuantity()});
        }
    }
}
