package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    }

    public Pane pane() {
        return pane;
    }

    private Node adressOACIGroups(ObservableAircraftState aircraftState) {

        Group groupeAeronef = new Group(trajectoryGroups(aircraftState), etiquetteIconGroups(aircraftState));

        groupeAeronef.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        groupeAeronef.setId(aircraftState.getIcaoAddress().string());

        return groupeAeronef;
    }

    private Node trajectoryGroups(ObservableAircraftState aircraftState) {
        return new Group(new Line(), new Line(), new Line());
    }

    private Node iconGroups(ObservableAircraftState aircraftState) {

        SVGPath aircraftIcon = new SVGPath();

        AircraftTypeDesignator typeDesignator = aircraftState.getAircraftData().typeDesignator() != null ?
                aircraftState.getAircraftData().typeDesignator(): new AircraftTypeDesignator("");
        AircraftDescription aircraftDescription = aircraftState.getAircraftData().description() != null ?
                aircraftState.getAircraftData().description() : new AircraftDescription("");
        WakeTurbulenceCategory wakeTurbulenceCategory = aircraftState.getAircraftData().wakeTurbulenceCategory() != null ?
                aircraftState.getAircraftData().wakeTurbulenceCategory() : WakeTurbulenceCategory.UNKNOWN;


        var icon = aircraftState.categoryProperty().map( c -> AircraftIcon.iconFor(typeDesignator, aircraftDescription, c.intValue(),
                wakeTurbulenceCategory));

        aircraftIcon.contentProperty().bind(icon.map(AircraftIcon::svgPath));

        aircraftIcon.rotateProperty().bind(
                Bindings.createDoubleBinding (()->
                        icon.getValue().canRotate()
                        ? Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE)
                                : 0, icon,
                aircraftState.trackOrHeadingProperty() ));

        aircraftIcon.getStyleClass().add("aircraft");

        return aircraftIcon;
    }

    private Node etiquetteIconGroups(ObservableAircraftState aircraftState) {

        Group etiquetteIcon = new Group(etiquetteGroups(aircraftState), iconGroups(aircraftState));

        etiquetteIcon.layoutXProperty();

        return etiquetteIcon;
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