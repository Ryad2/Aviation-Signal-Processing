package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import static ch.epfl.javions.gui.AircraftIcon.iconFor;

public final class AircraftController {

    Pane pane;
    ObservableSet<ObservableAircraftState> aircraftStates;
    ObjectProperty<ObservableAircraftState>  aircraftStateProperty;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {

        this.pane = new Pane();
        //this.aircraftStates = aircraftStates;
        this.aircraftStateProperty=aircraftStateProperty;

        pane.getStylesheets().add("aircraft.css");
        pane.setPickOnBounds(false);


        pane.getStyleClass().add("trajectory");

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {

                pane.getChildren().add(adressOACIGroups(change.getElementAdded()));
            }

            if (change.wasRemoved()) {
                pane.getChildren().removeIf( a->
                    a.getId().equals(change.getElementRemoved().getIcaoAddress().string())
                );
            }


        });

        for(ObservableAircraftState aircraftState : aircraftStates) {
            adressOACIGroups(aircraftState);
        }
    }

    public Pane pane() {
        return pane;
    }

    private Group adressOACIGroups(ObservableAircraftState aircraftState) {
        return new Group(trajectoryGroups(aircraftState), etiquetteGroups(aircraftState));
    }

    private Node trajectoryGroups(ObservableAircraftState aircraftState) {
        return new Group(new Line(), new Line(), new Line());
    }

    private Group iconGroups(ObservableAircraftState aircraftState) {

        SVGPath aircraftIcon = new SVGPath();


        aircraftIcon.contentProperty().bind(AircraftIcon.getIcon(aircraftState.getAircraftData().typeDesignator()).svgPath);


        /*iconFor(aircraftState.getAircraftData().typeDesignator(),
                            aircraftState.getAircraftData().description(), aircraftState.getCategory(),
                         aircraftState.getAircraftData().wakeTurbulenceCategory()).*/

        aircraftIcon.contentProperty().bind(aircraftStates);

        aircraftIcon.getStyleClass().add("aircraft");

        return new Group(aircraftIcon);
    }

    private Group etiquetteIconGroups(ObservableAircraftState aircraftState) {

        return new Group(etiquetteGroups(aircraftState), iconGroups(aircraftState));
    }

    private Node etiquetteGroups(ObservableAircraftState aircraftState) {

        Group label = new Group();

        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(Bindings.createDoubleBinding(() ->
                text.getLayoutBounds().getWidth() + 4, text.layoutBoundsProperty()));

        rectangle.heightProperty().bind(Bindings.createDoubleBinding(() ->
                text.getLayoutBounds().getHeight() + 4, text.layoutBoundsProperty()));


        label.getStyleClass().add("label");

        return new Group(rectangle, text);
    }
}