package chinhsua;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;

public  class ApiAndDatabase {

  private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn";
  private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/library";
  private static final String DB_USERNAME = "root";
  private static final String DB_PASSWORD = "khanhkhanh123";

 public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
  }

  // Kiểm tra xem trong database đã có tài liệu hay chưa
  private boolean isDataInDatabase() {
    try (Connection connection = getConnection()) {
      String checkQuery = "SELECT COUNT(*) FROM documents";
      try (PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
          ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          int count = resultSet.getInt(1);
          return count > 0; // Nếu có ít nhất 1 tài liệu, trả về true
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false; // Nếu không có dữ liệu
  }

  // Hàm để tải dữ liệu từ API và lưu vào cơ sở dữ liệu
  // cap nhat database documents

  public void loadDocumentsFromAPI() {
    new Thread(() -> {
      try {
        // Kiểm tra nếu database đã có dữ liệu
        if (!isDataInDatabase()) {
          int startIndex = 0;
          int totalItems = 537; // Tổng số tài liệu cần lấy
          int maxResults = 40;

          while (startIndex < totalItems) {
            try {
              // Xây dựng URL với startIndex và maxResults
              String url = API_URL + "&startIndex=" + startIndex + "&maxResults=" + maxResults;
              HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
              connection.setRequestMethod("GET");

              // Đọc phản hồi từ API
              BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              StringBuilder response = new StringBuilder();
              String line;
              while ((line = reader.readLine()) != null) {
                response.append(line);
              }
              reader.close();

              // Kiểm tra và phân tích dữ liệu từ API
              if (response.length() > 0) {
                parseBookDataFromAPI(response.toString());
              }

              // Tăng startIndex để lấy trang tiếp theo
              startIndex += maxResults;

              // Kiểm tra số lượng tài liệu thực tế trả về từ API
              if (response.length() == 0 || startIndex >= totalItems) {
                break; // Dừng nếu không còn dữ liệu
              }

            } catch (Exception e) {
              e.printStackTrace();
              showAlert("Không thể lấy dữ liệu từ API.");
              break; // Dừng nếu có lỗi
            }
          }
        } else {
          System.out.println("Database already contains data. No need to fetch from API.");
        }
      } catch (Exception e) {
        e.printStackTrace();
        showAlert("Đã xảy ra lỗi trong quá trình kiểm tra cơ sở dữ liệu.");
      }
    }).start();
  }

  // Hàm phân tích dữ liệu từ API và lưu vào cơ sở dữ liệu
  private void parseBookDataFromAPI(String apiResponse) {
    try {
      JSONObject jsonResponse = new JSONObject(apiResponse);
      JSONArray items = jsonResponse.getJSONArray("items");

      try (Connection connection = getConnection()) {
        String insertQuery = "INSERT INTO documents (title, author, category, status, quantity, publisher, publishedDate, description, isbn13, isbn10) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
          for (int i = 0; i < items.length(); i++) {
            JSONObject book = items.getJSONObject(i).getJSONObject("volumeInfo");

            // Lấy dữ liệu từ JSON object
            String title = book.optString("title");
            String author = book.optString("authors", "Unknown");
            String category = book.optString("categories", "Unknown");
            String status = "Available"; // Status mặc định
            int quantity = 1; // Số lượng mặc định
            String publisher = book.optString("publisher", "Unknown");
            String publishedDate = book.optString("publishedDate", "Unknown");
            String description = book.optString("description", "No description");
            String isbn13 = getIsbn(book, "ISBN_13");
            String isbn10 = getIsbn(book, "ISBN_10");

            // Gán dữ liệu vào PreparedStatement
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, author);
            preparedStatement.setString(3, category);
            preparedStatement.setString(4, status);
            preparedStatement.setInt(5, quantity);
            preparedStatement.setString(6, publisher);
            preparedStatement.setString(7, publishedDate);
            preparedStatement.setString(8, description);
            preparedStatement.setString(9, isbn13);
            preparedStatement.setString(10, isbn10);

            // Thực thi lệnh INSERT
            preparedStatement.executeUpdate();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Lấy ISBN từ volumeInfo nếu có
  private String getIsbn(JSONObject book, String isbnType) {
    JSONArray isbnArray = book.optJSONArray("industryIdentifiers");
    if (isbnArray != null) {
      for (int i = 0; i < isbnArray.length(); i++) {
        JSONObject identifier = isbnArray.getJSONObject(i);
        if (isbnType.equals(identifier.optString("type"))) {
          return identifier.optString("identifier");
        }
      }
    }
    return "";
  }

  // Hiển thị thông báo lỗi
  private void showAlert(String message) {
    // Đây là nơi bạn có thể thêm mã để hiển thị cảnh báo lỗi
    System.out.println(message);
  }
}
