package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public final class StatusLineController {

    private final BorderPane rootPane;
    private final LongProperty aircraftCountProperty = new SimpleLongProperty(0L);
    private final LongProperty messageCountProperty = new SimpleLongProperty(0L);

    public StatusLineController() {

        Text aircraftCountText = new Text();
        aircraftCountText.textProperty()
                .bind(Bindings.format("Aéronefs visibles : %d", aircraftCountProperty));

        Text messageCountText = new Text();
        messageCountText.textProperty()
                .bind(Bindings.format("Messages reçus : %d", messageCountProperty));


        rootPane = new BorderPane(null, null,
                messageCountText, null, aircraftCountText);

        rootPane.getStylesheets().add("status.css");
    }

    public BorderPane pane() {
        return rootPane;
    }

    public LongProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }
}