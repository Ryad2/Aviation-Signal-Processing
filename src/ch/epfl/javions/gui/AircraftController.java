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
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.awt.*;
import java.io.Serializable;

public final class AircraftController {

    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState>  aircraftStateProperty;
    private final MapParameters mapParameters;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {

        this.pane = new Pane();
        this.aircraftStateProperty = aircraftStateProperty;
        this.mapParameters = mapParameters;

        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");

        addAndRemoveAircraft(aircraftStates);
    }

    public Pane pane() {
        return pane;
    }

    private Node addressOACIGroups(ObservableAircraftState aircraftState) {

        Group annotatedAircraft = new Group(trajectoryGroups(aircraftState), etiquetteIconGroups(aircraftState));

        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());
        annotatedAircraft.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return annotatedAircraft;
    }

    private Node etiquetteIconGroups(ObservableAircraftState aircraftState) {

        Group labelIconGroup = new Group(labelGroup(aircraftState), iconGroup(aircraftState));

        labelIconGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.x(mapParameters.getZoom(), aircraftState.getPosition().longitude()) - mapParameters.getminX(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty()));

        labelIconGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                WebMercator.y(mapParameters.getZoom(), aircraftState.getPosition().latitude()) - mapParameters.getminY(),
                aircraftState.positionProperty(),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty()));

        return labelIconGroup;
    }

    private Node iconGroup(ObservableAircraftState aircraftState) {


        AircraftData aircraftData = aircraftState.getAircraftData();

        AircraftTypeDesignator typeDesignator = (aircraftData != null)
                ? aircraftData.typeDesignator()
                : new AircraftTypeDesignator("");

        AircraftDescription aircraftDescription = (aircraftData != null)
                ? aircraftData.description()
                : new AircraftDescription("");

        WakeTurbulenceCategory wakeTurbulenceCategory = (aircraftData != null)
                ? aircraftData.wakeTurbulenceCategory()
                : WakeTurbulenceCategory.UNKNOWN;

        ObservableValue<AircraftIcon> icon = aircraftState.categoryProperty().map(c ->
                AircraftIcon.iconFor(
                        typeDesignator,
                        aircraftDescription,
                        c.intValue(),
                        wakeTurbulenceCategory));

        SVGPath aircraftIcon = new SVGPath();

        aircraftIcon.getStyleClass().add("aircraft");

        aircraftIcon.contentProperty().bind(icon.map(AircraftIcon::svgPath));

        aircraftIcon.rotateProperty().bind(
                Bindings.createDoubleBinding (()->
                        icon.getValue().canRotate()
                        ? Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE)
                                : 0, icon,
                aircraftState.trackOrHeadingProperty()));

        //Todo : ou est le problème ?
        //aircraftIcon.fillProperty().bind(icon.map(getColorForAltitude(aircraftState)));

        return aircraftIcon;
    }

    private Node labelGroup(ObservableAircraftState aircraftState) {

        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        text.textProperty().bind(Bindings.format("%s \n %5f km/h %5f m",
                getAircraftIdentifier(aircraftState),
                velocityString(aircraftState),
                aircraftState.getAltitude()));

        Group label = new Group();

        label.getStyleClass().add("label");

        //Todo : il y a un moyen plus simple de faire ça ?
        label.visibleProperty().bind(aircraftStateProperty.isEqualTo(aircraftState)
                .or(Bindings.lessThanOrEqual(11, mapParameters.zoomProperty())));

        return label;
    }

    private Node trajectoryGroups(ObservableAircraftState aircraftState) {

        for (ObservableAircraftState.AirbornePos el : aircraftState.getTrajectory()){

        }
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

    private void addAndRemoveAircraft (ObservableSet<ObservableAircraftState> aircraftStates) {

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                pane.getChildren().add(addressOACIGroups(change.getElementAdded()));
            }

            if (change.wasRemoved())
                pane.getChildren().removeIf(a ->
                        a.getId().equals(change.getElementRemoved().getIcaoAddress().string()));
        });
    }

    private String getAircraftIdentifier (ObservableAircraftState aircraftState) {

        AircraftData aircraftData = aircraftState.getAircraftData();

        if (aircraftData == null) return aircraftState.getIcaoAddress().string();

        //TODO : aircraftData.registration() doit être différent de null ou que aircraftData?
        if (aircraftData.registration() != null) {
            return aircraftData.registration().string();
        } else if (aircraftData.typeDesignator() != null) {
            return aircraftData.typeDesignator().string();
        } else {
            return aircraftState.getIcaoAddress().string();
        }
    }

    //TODO : vérifier que ça retourne la bonne valeur
    private Serializable velocityString(ObservableAircraftState aircraftState) {
        return (aircraftState.velocityProperty() != null)
                ? Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)
                : "?";
    }

    public static Color getColorForAltitude(ObservableAircraftState aircraftState) {
        return ColorRamp.PLASMA.at(aircraftState.getAltitude());
    }
}