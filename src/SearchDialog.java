import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SearchDialog extends JDialog {
    private ArrayList<Document> documentList; // Danh sách tài liệu
    private JTextField searchField; // Ô tìm kiếm
    private JTextArea resultArea; // Kết quả tìm kiếm

    public SearchDialog(JFrame parent, ArrayList<Document> documentList) {
        super(parent, "Tìm kiếm tài liệu", true);
        this.documentList = documentList;

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
        resultArea = new JTextArea(10, 40);
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
        resultArea.setText(""); // Xóa kết quả cũ
        for (Document doc : documentList) {
            if (doc.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                resultArea.append("Tên sách: " + doc.getTitle() + "\nTác giả: " + doc.getAuthor() + "\nThể loại: " + doc.getCategory() + "\nTrạng thái: " + doc.getStatus() + "\nSố lượng: " + doc.getQuantity() + "\n\n");
            }
        }
        if (resultArea.getText().isEmpty()) {
            resultArea.setText("Không tìm thấy tài liệu nào.");
        }
    }
}
