module fr.marieteam.pdf.marieteampdf {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires kernel;
    requires layout;

    opens fr.marieteam.pdf.marieteampdf to javafx.fxml;
    exports fr.marieteam.pdf.marieteampdf;
}