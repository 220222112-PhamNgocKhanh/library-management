module code.chinhthuc {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.json;
  requires java.sql;
  requires java.desktop;
  requires mysql.connector.j;

  exports code.chinhthuc;
    exports chinhsua;
}