package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
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

public final class AircraftController {

    private final Pane pane;
    ObjectProperty<ObservableAircraftState>  aircraftStateProperty;
    private final MapParameters mapParameters;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {

        this.pane = new Pane();
        this.aircraftStateProperty = aircraftStateProperty;
        this.mapParameters = mapParameters;


        pane.setPickOnBounds(false);
        pane.getStylesheets().add("/aircraft.css");


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


        groupeAeronef.setId(aircraftState.getIcaoAddress().string());
        groupeAeronef.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return groupeAeronef;
    }

    private Node etiquetteIconGroups(ObservableAircraftState aircraftState) {
        Group etiquetteIconGroup = new Group(etiquetteGroups(aircraftState), iconGroups(aircraftState));

        etiquetteIconGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) - mapParameters.getminX(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()));

        etiquetteIconGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) - mapParameters.getminY(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()));

        return etiquetteIconGroup;
    }

    private Node iconGroups(ObservableAircraftState aircraftState) {
        SVGPath aircraftIcon = new SVGPath();

        AircraftData data = aircraftState.getAircraftData();

        AircraftTypeDesignator typeDesignator = (data.typeDesignator() != null) ?
                data.typeDesignator(): new AircraftTypeDesignator("");

        AircraftDescription aircraftDescription = (data.description() != null) ?
                data.description() : new AircraftDescription("");

        WakeTurbulenceCategory wakeTurbulenceCategory = (data.wakeTurbulenceCategory() != null) ?
                data.wakeTurbulenceCategory() : WakeTurbulenceCategory.UNKNOWN;

        var icon = aircraftState.categoryProperty().map(c ->
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
                aircraftState.trackOrHeadingProperty()));


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


        text.textProperty().bind(Bindings.format("%s \n %s km/h %s m",
                getAircraftIdentifier(aircraftState),
        Math.round(Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)),
                Math.round(aircraftState.getAltitude())));


        label.getStyleClass().add("label");

        label.visibleProperty().bind(aircraftStateProperty.isEqualTo(aircraftState));
        label.visibleProperty().bind(Bindings.lessThanOrEqual(11, mapParameters.zoomProperty()));

        return new Group(rectangle, text);
    }

    private Node trajectoryGroups(ObservableAircraftState aircraftState) {

       Group trajectoryGroup = new Group(new Line(), new Line(), new Line());

        trajectoryGroup.visibleProperty().bind(aircraftStateProperty.isEqualTo(aircraftState));

        trajectoryGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) - mapParameters.getminX(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()));

        trajectoryGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                        WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) - mapParameters.getminY(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()));

        pane.getStyleClass().add("trajectory");
        return new Group(new Line(), new Line(), new Line());
    }

    private String getAircraftIdentifier (ObservableAircraftState aircraftState) {

        AircraftData data = aircraftState.getAircraftData();

        if (data.registration() != null) {
            return data.registration().string();
        } else if (data.typeDesignator() != null) {
            return data.typeDesignator().string();
        } else {
            return aircraftState.getIcaoAddress().string();
        }
    }
}