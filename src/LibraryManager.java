import java.util.ArrayList;

public class LibraryManager { // Class này để quản lý danh sách người dùng
    private static ArrayList<User> userList = new ArrayList<>(); // Danh sách người dùng
    private static ArrayList<Document> documentList = new ArrayList<>(); // Danh sách tài liệu
    private static ArrayList<String> requestList = new ArrayList<>(); // Danh sách yêu cầu từ người dùng

    public static ArrayList<User> getUserList() {
        return userList;
    }

    public static void addUser(User user) {
        userList.add(user);
    }

    public static boolean isUserExists(String userId, String email) {
        for (User user : userList) {
            if (user.getUserId().equals(userId) && user.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    // Phương thức để lấy danh sách tài liệu
    public static ArrayList<Document> getDocumentList() {
        return documentList;
    }

    // Phương thức để thêm tài liệu mới vào danh sách
    public static void addDocument(Document document) {
        documentList.add(document);
    }

    // Kiểm tra xem tài liệu có tồn tại hay không
    public static boolean isDocumentExists(String title) {
        for (Document doc : documentList) {
            if (doc.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    // Phương thức thêm yêu cầu vào danh sách
    public static void addRequest(String request) {
        requestList.add(request);
    }

    // Phương thức lấy danh sách yêu cầu
    public static ArrayList<String> getRequestList() {
        return requestList;
    }
}
