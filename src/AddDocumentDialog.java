import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class AddDocumentDialog extends JDialog {
    private JTextField titleField;
    private JTextField authorField;
    private JTextField categoryField;
    private JComboBox<String> statusComboBox;
    private JTextField quantityField;
    private ArrayList<Document> documentList;
    private LibrarianMain librarianMain;

    public AddDocumentDialog(JFrame parent, ArrayList<Document> documentList, LibrarianMain librarianMain) {
        super(parent, "Thêm tài liệu mới", true);
        this.documentList = documentList;
        this.librarianMain = librarianMain;

        setLayout(new GridLayout(6, 2));

        // Nhập thông tin tài liệu
        add(new JLabel("Tên tài liệu:"));
        titleField = new JTextField();
        add(titleField);

        add(new JLabel("Tác giả:"));
        authorField = new JTextField();
        add(authorField);

        add(new JLabel("Thể loại:"));
        categoryField = new JTextField();
        add(categoryField);

        add(new JLabel("Trạng thái:"));
        statusComboBox = new JComboBox<>(new String[]{"Còn", "Hết"});
        add(statusComboBox);

        add(new JLabel("Số lượng:"));
        quantityField = new JTextField();
        add(quantityField);

        // Nút thêm tài liệu
        JButton addButton = new JButton("Thêm");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDocument();
            }
        });
        add(addButton);

        // Nút đóng
        JButton cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        pack();
        setLocationRelativeTo(parent);
    }

    // Phương thức để thêm tài liệu mới vào documentList
    private void addDocument() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            // Tạo đối tượng tài liệu mới
            Document newDocument = new Document(title, author, category, status, quantity);

            // Thêm tài liệu mới vào danh sách
            documentList.add(newDocument);

            // Cập nhật bảng để hiển thị tài liệu mới
            librarianMain.updateTable();

            // Đóng dialog sau khi thêm tài liệu thành công
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng số lượng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
