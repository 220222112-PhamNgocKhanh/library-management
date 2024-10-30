import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class EditDocumentDialog extends JDialog {
    private JTextField titleField;
    private JTextField authorField;
    private JTextField categoryField;
    private JComboBox<String> statusComboBox;
    private JTextField quantityField;
    private int selectedRow; // Vị trí của tài liệu được chọn trong danh sách
    private ArrayList<Document> documentList;
    private Main mainInstance; // Đối tượng Main để gọi hàm updateTable()

    public EditDocumentDialog(JFrame parent, ArrayList<Document> documentList, int selectedRow, Main mainInstance) {
        super(parent, "Sửa tài liệu", true);
        this.documentList = documentList;
        this.selectedRow = selectedRow;
        this.mainInstance = mainInstance;

        setLayout(new GridLayout(6, 2));

        // Lấy tài liệu hiện tại để sửa
        Document document = documentList.get(selectedRow);

        // Hiển thị thông tin hiện tại của tài liệu trong các trường nhập liệu
        add(new JLabel("Tên tài liệu:"));
        titleField = new JTextField(document.getTitle());
        add(titleField);

        add(new JLabel("Tác giả:"));
        authorField = new JTextField(document.getAuthor());
        add(authorField);

        add(new JLabel("Thể loại:"));
        categoryField = new JTextField(document.getCategory());
        add(categoryField);

        add(new JLabel("Trạng thái:"));
        statusComboBox = new JComboBox<>(new String[]{"Còn", "Hết"});
        statusComboBox.setSelectedItem(document.getStatus());
        add(statusComboBox);

        add(new JLabel("Số lượng:"));
        quantityField = new JTextField(String.valueOf(document.getQuantity()));
        add(quantityField);

        // Nút lưu thay đổi
        JButton saveButton = new JButton("Lưu");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDocument();
            }
        });
        add(saveButton);

        // Nút hủy bỏ
        JButton cancelButton = new JButton("Hủy");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        pack();
        setLocationRelativeTo(parent);
    }

    // Phương thức lưu thay đổi vào danh sách tài liệu
    private void saveDocument() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String category = categoryField.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();
            int quantity = Integer.parseInt(quantityField.getText().trim());

            // Cập nhật thông tin tài liệu trong danh sách
            Document document = documentList.get(selectedRow);
            document.setTitle(title);
            document.setAuthor(author);
            document.setCategory(category);
            document.setStatus(status);
            document.setQuantity(quantity);

            // Cập nhật bảng để hiển thị thông tin đã chỉnh sửa
            mainInstance.updateTable();

            // Đóng dialog sau khi lưu
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng số lượng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
