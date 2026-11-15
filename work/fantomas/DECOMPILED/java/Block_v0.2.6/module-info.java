module javafx.web {
    requires java.desktop;
    requires javafx.media;
    requires jdk.xml.dom;
    requires jdk.jsobject;
    /* transitive */ requires java.xml;
    /* transitive */ requires javafx.base;
    /* transitive */ requires javafx.controls;
    /* transitive */ requires javafx.graphics;

    exports javafx.scene.web;
    exports com.sun.javafx.fxml.builder.web to javafx.fxml;

}

