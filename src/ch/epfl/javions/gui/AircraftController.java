package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import javax.swing.text.GapContent;

public final class AircraftController {

    Pane pane;
    ObservableSet<ObservableAircraftState> aircraftStates;
    //ObservableSet<ObservableAircraftState> unmodifiableAircraftStates;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {

        this.pane = new Pane();
        this.aircraftStates = aircraftStates;

        pane.getStylesheets().add("aircraft.css");
        pane.setId("adr.OACI");
        pane.getStyleClass().add("trajectory");

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {

                if (mapParameters.getZoom() >= 11) {
                    pane.getChildren().add(etiquetteGroups(aircraftStateProperty.get()));
                }
                pane.setPickOnBounds(false);

                iconGroups().setOnMouseClicked(event -> {
                        pane.getChildren().addAll(etiquetteGroups(aircraftStateProperty.get()), trajectoryGroups());
                });
            }

            if (change.wasRemoved()) {
                aircraftStateProperty.set(null);
            }
        });
    }

    public Pane pane() {
        return pane;
    }

    private Group trajectoryGroups() {
        return new Group(new Line(), new Line(), new Line());
    }

    private Group iconGroups() {

        SVGPath aircraftIcon = new SVGPath();

        aircraftIcon.contentProperty().bind(Bindings.createStringBinding(() -> {
            return aircraftIcon.contentProperty().get();
        }));

        aircraftIcon.getStyleClass().add("aircraft");
        return new Group(aircraftIcon);
    }

    private Group etiquetteGroups(ObservableAircraftState aircraftState) {

        Group label = new Group();

        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(Bindings.createDoubleBinding(() -> {
            return text.getLayoutBounds().getWidth() + 4;
        }, text.layoutBoundsProperty()));

        rectangle.heightProperty().bind(Bindings.createDoubleBinding(() -> {
            return text.getLayoutBounds().getHeight() + 4;
        }, text.layoutBoundsProperty()));


        label.getStyleClass().add("label");
        return new Group(rectangle, text);
    }

    private Group etiquetteIconGroups() {
        return new Group(iconGroups());
    }

    private Group adresseOACIGroups() {
        return new Group(iconGroups(), trajectoryGroups());
    }
}