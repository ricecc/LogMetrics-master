module grafo.view {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.opencsv;
    requires OpenXES;
    requires gs.core;

    opens grafo.view to javafx.fxml;
    exports grafo.view;
}