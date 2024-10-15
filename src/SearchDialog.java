import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SearchDialog extends JDialog {
    private ArrayList<Document> documentList; // Danh sách tài liệu
    private JTextField searchField; // Ô tìm kiếm
    private JTextArea bookInfoArea; // Kết quả tìm kiếm
    private JTable table; // Bảng để hiển thị kết quả tìm kiếm
    private JFrame parentFrame; // Tham chiếu tới frame cha để hiển thị dialog

    public SearchDialog(JFrame parent, ArrayList<Document> documentList, JTable table, JTextArea bookInfoArea) {
        super(parent, "Tìm kiếm tài liệu", true);
        this.documentList = documentList;
        this.table = table;
        this.bookInfoArea = bookInfoArea;
        this.parentFrame = parent;

        setLayout(new BorderLayout());

        // Tạo thanh tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Tìm kiếm");
        searchPanel.add(new JLabel("Nhập tên sách:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Kết quả tìm kiếm
        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        // Xử lý sự kiện tìm kiếm
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch(searchField.getText().trim());
            }
        });

        setSize(500, 300);
        setLocationRelativeTo(parent);
    }

    // Hàm tìm kiếm tài liệu theo tên
    private void performSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Vui lòng nhập từ khóa tìm kiếm"); // Thông báo khi ô tìm kiếm trống
            return;
        }

        boolean found = false; // Biến kiểm tra xem có tìm thấy tài liệu hay không
        for (int i = 0; i < documentList.size(); i++) {
            Document doc = documentList.get(i);
            if (doc.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                table.setRowSelectionInterval(i, i); // Highlight dòng chứa tài liệu
                table.scrollRectToVisible(table.getCellRect(i, 0, true)); // Cuộn đến dòng chứa tài liệu
                bookInfoArea.setText(doc.toString()); // Cập nhật thông tin tài liệu vào ô "Thông tin"
                found = true;
                break; // Tìm thấy tài liệu và dừng tìm kiếm
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(parentFrame, "Không tìm thấy tài liệu"); // Thông báo khi không tìm thấy tài liệu
            bookInfoArea.setText(""); // Xóa nội dung trong ô "Thông tin"
        }
    }

    // Hàm công khai để gọi performSearch từ bên ngoài
    public void search(String searchTerm) {
        performSearch(searchTerm);
    }
}
