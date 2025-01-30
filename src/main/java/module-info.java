module fr.marieteam.pdf.marieteampdf {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens fr.marieteam.pdf.marieteampdf to javafx.fxml;
    exports fr.marieteam.pdf.marieteampdf;
}