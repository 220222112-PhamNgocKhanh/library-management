package chinhsua;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;

public class CalendarController implements Initializable {

    // Biến lưu trữ ngày hiện tại và ngày được chọn
    ZonedDateTime dateFocus;
    ZonedDateTime today;

    @FXML
    private Text year; // Hiển thị năm

    @FXML
    private Text month; // Hiển thị tháng

    @FXML
    private FlowPane calendar; // Mảng chứa các ô của lịch

    // Phương thức khởi tạo, sẽ được gọi khi view được tạo
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateFocus = ZonedDateTime.now(); // Lấy ngày hiện tại làm ngày được chọn
        today = ZonedDateTime.now(); // Lấy ngày hiện tại
        drawCalendar(); // Vẽ lịch
    }

    // Phương thức quay lại một tháng
    @FXML
    public void backOneMonth(ActionEvent event) {
        dateFocus = dateFocus.minusMonths(1); // Lùi lại một tháng
        calendar.getChildren().clear(); // Xóa các ô cũ của lịch
        drawCalendar(); // Vẽ lại lịch
    }

    // Phương thức tiến tới một tháng
    @FXML
    public void forwardOneMonth(ActionEvent event) {
        dateFocus = dateFocus.plusMonths(1); // Tiến tới một tháng
        calendar.getChildren().clear(); // Xóa các ô cũ của lịch
        drawCalendar(); // Vẽ lại lịch
    }

    // Phương thức vẽ lịch
    public void drawCalendar(){
        year.setText(String.valueOf(dateFocus.getYear())); // Hiển thị năm
        month.setText(String.valueOf(dateFocus.getMonth())); // Hiển thị tháng

        // Lấy chiều rộng và chiều cao của lịch
        double calendarWidth = calendar.getPrefWidth();
        double calendarHeight = calendar.getPrefHeight();
        double strokeWidth = 1; // Độ dày của viền ô
        double spacingH = calendar.getHgap(); // Khoảng cách ngang giữa các ô
        double spacingV = calendar.getVgap(); // Khoảng cách dọc giữa các ô

        // Lấy danh sách các hoạt động trong tháng hiện tại
        Map<Integer, List<CalendarActivity>> calendarActivityMap = getCalendarActivitiesMonth(dateFocus);

        // Số ngày tối đa trong tháng
        int monthMaxDate = dateFocus.getMonth().maxLength();

        // Kiểm tra năm nhuận (nếu có tháng 2 có 29 ngày thì sửa lại thành 28)
        if(dateFocus.getYear() % 4 != 0 && monthMaxDate == 29){
            monthMaxDate = 28;
        }

        // Lấy ngày trong tuần của ngày đầu tháng
        int dateOffset = ZonedDateTime.of(dateFocus.getYear(), dateFocus.getMonthValue(), 1,0,0,0,0,dateFocus.getZone()).getDayOfWeek().getValue();

        // Vẽ các ô cho lịch (6 hàng, 7 cột)
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                StackPane stackPane = new StackPane();

                // Tạo hình chữ nhật cho từng ô trong lịch
                Rectangle rectangle = new Rectangle();
                rectangle.setFill(Color.TRANSPARENT); // Màu nền trong suốt
                rectangle.setStroke(Color.BLACK); // Màu viền là đen
                rectangle.setStrokeWidth(strokeWidth); // Độ dày viền
                double rectangleWidth =(calendarWidth/7) - strokeWidth - spacingH; // Tính chiều rộng ô
                rectangle.setWidth(rectangleWidth);
                double rectangleHeight = (calendarHeight/6) - strokeWidth - spacingV; // Tính chiều cao ô
                rectangle.setHeight(rectangleHeight);
                stackPane.getChildren().add(rectangle); // Thêm hình chữ nhật vào ô

                // Tính toán ngày trong tháng cho ô hiện tại
                int calculatedDate = (j+1) + (7*i);
                if(calculatedDate > dateOffset){
                    int currentDate = calculatedDate - dateOffset; // Ngày hiện tại trong tháng
                    if(currentDate <= monthMaxDate){
                        Text date = new Text(String.valueOf(currentDate)); // Hiển thị số ngày
                        double textTranslationY = - (rectangleHeight / 2) * 0.75; // Dịch chuyển vị trí văn bản
                        date.setTranslateY(textTranslationY);
                        stackPane.getChildren().add(date);

                        // Kiểm tra nếu có hoạt động cho ngày này
                        List<CalendarActivity> calendarActivities = calendarActivityMap.get(currentDate);
                        if(calendarActivities != null){
                            createCalendarActivity(calendarActivities, rectangleHeight, rectangleWidth, stackPane);
                        }
                    }
                    // Đánh dấu ngày hôm nay bằng màu xanh
                    if(today.getYear() == dateFocus.getYear() && today.getMonth() == dateFocus.getMonth() && today.getDayOfMonth() == currentDate){
                        Color colorBlue = Color.rgb(173, 224, 238);
                        rectangle.setFill(colorBlue);
                    }
                }
                calendar.getChildren().add(stackPane); // Thêm ô vào lịch
            }
        }
    }

    // Phương thức tạo các hoạt động cho ngày trong lịch
    private void createCalendarActivity(List<CalendarActivity> calendarActivities, double rectangleHeight, double rectangleWidth, StackPane stackPane) {
        VBox calendarActivityBox = new VBox();
        for (int k = 0; k < calendarActivities.size(); k++) {
            if(k >= 2) {
                // Nếu có quá 2 hoạt động, hiển thị dấu "..."
                Text moreActivities = new Text("...");
                calendarActivityBox.getChildren().add(moreActivities);
                moreActivities.setOnMouseClicked(mouseEvent -> {
                    // Khi click vào "..." sẽ in ra tất cả các hoạt động
                    System.out.println(calendarActivities);
                });
                break;
            }
            // Hiển thị tên khách hàng và giờ hoạt động
            Text text = new Text(calendarActivities.get(k).getClientName() + ", " + calendarActivities.get(k).getDate().toLocalTime());
            calendarActivityBox.getChildren().add(text);
            text.setOnMouseClicked(mouseEvent -> {
                // Khi click vào hoạt động, in ra thông tin chi tiết
                System.out.println(text.getText());
            });
        }
        // Đặt vị trí và kích thước cho ô hoạt động
        calendarActivityBox.setTranslateY((rectangleHeight / 2) * 0.20);
        calendarActivityBox.setMaxWidth(rectangleWidth * 0.8);
        calendarActivityBox.setMaxHeight(rectangleHeight * 0.65);
        calendarActivityBox.setStyle("-fx-background-color:GRAY"); // Màu nền xám
        stackPane.getChildren().add(calendarActivityBox); // Thêm hoạt động vào ô
    }

    // Phương thức tạo bản đồ các hoạt động cho từng ngày trong tháng
    private Map<Integer, List<CalendarActivity>> createCalendarMap(List<CalendarActivity> calendarActivities) {
        Map<Integer, List<CalendarActivity>> calendarActivityMap = new HashMap<>();

        for (CalendarActivity activity: calendarActivities) {
            int activityDate = activity.getDate().getDayOfMonth();
            if(!calendarActivityMap.containsKey(activityDate)){
                calendarActivityMap.put(activityDate, List.of(activity)); // Thêm hoạt động mới vào ngày
            } else {
                List<CalendarActivity> OldListByDate = calendarActivityMap.get(activityDate);

                List<CalendarActivity> newList = new ArrayList<>(OldListByDate);
                newList.add(activity); // Thêm hoạt động vào ngày đã có
                calendarActivityMap.put(activityDate, newList);
            }
        }
        return  calendarActivityMap;
    }

    // Phương thức lấy các hoạt động trong tháng
    private Map<Integer, List<CalendarActivity>> getCalendarActivitiesMonth(ZonedDateTime dateFocus) {
        List<CalendarActivity> calendarActivities = new ArrayList<>();
        int year = dateFocus.getYear();
        int month = dateFocus.getMonth().getValue();

        Random random = new Random();
        /** Tạo ra 50 hoạt động ngẫu nhiên cho tháng
         for (int i = 0; i < 50; i++) {
         ZonedDateTime time = ZonedDateTime.of(year, month, random.nextInt(27)+1, 16,0,0,0,dateFocus.getZone());
         calendarActivities.add(new CalendarActivity(time, "Hans", 111111));
         }
         */

        return createCalendarMap(calendarActivities); // Trả về bản đồ hoạt động
    }
}
