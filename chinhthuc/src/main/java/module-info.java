module code.chinhthuc {
  requires javafx.fxml;

    requires com.dlsc.formsfx;
    requires org.json;
  requires java.sql;
  requires mysql.connector.j;
  requires java.mail;
  requires eu.hansolo.fx.countries;
  requires javafx.controls;

  exports code.chinhthuc;
  exports thang;
    exports chinhsua;
}