module org.mp.frontend26 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires static lombok;

    opens org.mp.frontend26 to javafx.fxml;
    exports org.mp.frontend26;
    exports org.mp.frontend26.dto;
}