package chinhsua;


import java.time.ZonedDateTime;

public class CalendarActivity {
    private ZonedDateTime date; // Biến lưu trữ thời gian của hoạt động
    private String clientName;  // Biến lưu trữ tên khách hàng
    private Integer serviceNo;  // Biến lưu trữ số dịch vụ

    // Constructor để tạo một đối tượng CalendarActivity với các thông tin cần thiết
    public CalendarActivity(ZonedDateTime date, String clientName, Integer serviceNo) {
        this.date = date; // Khởi tạo thời gian hoạt động
        this.clientName = clientName; // Khởi tạo tên khách hàng
        this.serviceNo = serviceNo; // Khởi tạo số dịch vụ
    }

    // Getter cho thời gian hoạt động
    public ZonedDateTime getDate() {
        return date;
    }

    // Setter cho thời gian hoạt động
    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    // Getter cho tên khách hàng
    public String getClientName() {
        return clientName;
    }

    // Setter cho tên khách hàng
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // Getter cho số dịch vụ
    public Integer getServiceNo() {
        return serviceNo;
    }

    // Setter cho số dịch vụ
    public void setServiceNo(Integer serviceNo) {
        this.serviceNo = serviceNo;
    }

    // Phương thức toString() để chuyển đối tượng thành chuỗi (dùng để in ra thông tin đối tượng)
    @Override
    public String toString() {
        return "CalenderActivity{" +
                "date=" + date + // In ra thời gian hoạt động
                ", clientName='" + clientName + '\'' + // In ra tên khách hàng
                ", serviceNo=" + serviceNo + // In ra số dịch vụ
                '}';
    }
}
