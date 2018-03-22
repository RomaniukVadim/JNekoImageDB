package ui.simplepanel;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.Objects;

public class Panel extends HBox {
    public Panel(String style, Node ... nodes) {
        super(4);
        setAlignment(Pos.CENTER);
        getStyleClass().addAll("max_width", style);
        Arrays.asList(nodes).stream()
                .filter(e -> Objects.nonNull(e))
                .forEach(e -> getChildren().add(e));
    }

    public Panel(Node ... nodes) {
        super(4);
        getStyleClass().addAll("panel_pane", "max_width", "height_48px");
        Arrays.asList(nodes).stream()
                .filter(e -> Objects.nonNull(e))
                .forEach(e -> getChildren().add(e));
    }

    public static Node getSpacer() {
        final HBox vBox = new HBox();
        vBox.getStyleClass().addAll("panel_separator", "max_width", "max_height");
        return vBox;
    }

    public static Node getFixedSpacer() {
        final HBox vBox = new HBox();
        vBox.getStyleClass().addAll("panel_separator", "width_48px");
        return vBox;
    }
}
