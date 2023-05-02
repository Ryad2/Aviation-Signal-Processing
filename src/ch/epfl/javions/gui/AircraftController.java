package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public final class AircraftController {

    Pane pane;
    ObjectProperty<ObservableAircraftState>  aircraftStateProperty;
    private final MapParameters mapParameters;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {

        this.pane = new Pane();
        //this.aircraftStates = aircraftStates;
        this.aircraftStateProperty = aircraftStateProperty;
        this.mapParameters = mapParameters;


        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");


        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                pane.getChildren().add(addressOACIGroups(change.getElementAdded()));
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

    private Node addressOACIGroups(ObservableAircraftState aircraftState) {

        Group groupeAeronef = new Group(trajectoryGroups(aircraftState), etiquetteIconGroups(aircraftState));

        groupeAeronef.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        groupeAeronef.setId(aircraftState.getIcaoAddress().string());

        return groupeAeronef;
    }

    private Node etiquetteIconGroups(ObservableAircraftState aircraftState) {
        Group etiquetteIcon = new Group(etiquetteGroups(aircraftState), iconGroups(aircraftState));

        etiquetteIcon.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) - mapParameters.getminX(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()));

        etiquetteIcon.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) - mapParameters.getminY(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()));

        return etiquetteIcon;
    }

    private Node iconGroups(ObservableAircraftState aircraftState) {
        SVGPath aircraftIcon = new SVGPath();

        AircraftTypeDesignator typeDesignator = aircraftState.getAircraftData().typeDesignator() != null ?
                aircraftState.getAircraftData().typeDesignator(): new AircraftTypeDesignator("");

        AircraftDescription aircraftDescription = aircraftState.getAircraftData().description() != null ?
                aircraftState.getAircraftData().description() : new AircraftDescription("");

        WakeTurbulenceCategory wakeTurbulenceCategory = aircraftState.getAircraftData().wakeTurbulenceCategory() != null ?
                aircraftState.getAircraftData().wakeTurbulenceCategory() : WakeTurbulenceCategory.UNKNOWN;

        var icon = aircraftState.categoryProperty().map( c ->
                AircraftIcon.iconFor(
                        typeDesignator,
                        aircraftDescription,
                        c.intValue(),
                        wakeTurbulenceCategory));


        aircraftIcon.getStyleClass().add("aircraft");

        aircraftIcon.contentProperty().bind(icon.map(AircraftIcon::svgPath));

        aircraftIcon.rotateProperty().bind(
                Bindings.createDoubleBinding (()->
                        icon.getValue().canRotate()
                        ? Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE)
                                : 0, icon,
                aircraftState.trackOrHeadingProperty() ));


        //aircraftIcon.setOnMouseClicked(e -> aircraftStateProperty.set(aircraftState));
        //aircraftIcon.visibleProperty(e -> aircraftStateProperty.set(aircraftState));

        //aircraftIcon.visibleProperty().bind();

        return aircraftIcon;
    }

    private Node etiquetteGroups(ObservableAircraftState aircraftState) {

        Group label = new Group();
        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + 4));


        text.textProperty().bind(Bindings.format("%s \n %s km/h %s m", getAircraftIdentifier(aircraftState),
        Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR), aircraftState.getAltitude()));


        label.getStyleClass().add("label");

        label.visibleProperty().bind(aircraftStateProperty.isEqualTo(aircraftState));
        label.visibleProperty().bind(Bindings.lessThanOrEqual(11, mapParameters.zoomProperty()));


        return new Group(rectangle, text);
    }

    private Node trajectoryGroups(ObservableAircraftState aircraftState) {
        pane.getStyleClass().add("trajectory");
        return new Group(new Line(), new Line(), new Line());
    }

    private String getAircraftIdentifier (ObservableAircraftState aircraftState) {
        if (aircraftState.getAircraftData().registration() != null) {
            return aircraftState.getAircraftData().registration().string();
        } else if (aircraftState.getAircraftData().typeDesignator() != null) {
            return aircraftState.getAircraftData().typeDesignator().string();
        } else {
            return aircraftState.getIcaoAddress().string();
        }
    }
}