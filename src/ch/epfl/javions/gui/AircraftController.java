package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
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


        Group annotatedAircraft = new Group(trajectoryGroups(aircraftState), labelIconGroups(aircraftState));

        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());
        annotatedAircraft.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return annotatedAircraft;
    }

    private Node labelIconGroups(ObservableAircraftState aircraftState) {

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

        aircraftIcon.fillProperty().bind(
                Bindings.createObjectBinding(() -> ColorRamp.PLASMA
                                .at(getColorForAltitude(aircraftState.altitudeProperty().get())),
                        aircraftState.altitudeProperty())
        );

        return aircraftIcon;
    }

    private Node labelGroup(ObservableAircraftState aircraftState) {

        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        //TODO : je peux laisser un espace entre le demi cadratin ou il y aura un espace en trop?
        text.textProperty().bind(Bindings.format("%s \n %1.2f km/h \u2002 %1.2f m",
                getAircraftIdentifier(aircraftState),
                velocityString1(aircraftState),
                aircraftState.altitudeProperty()));


        Group label = new Group(rectangle, text);

        label.getStyleClass().add("label");

        label.visibleProperty().bind(
                aircraftStateProperty.isEqualTo(aircraftState)
                        .or(mapParameters.zoomProperty().greaterThanOrEqualTo(11))
        );


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

        //TODO : vérifier que c'est le bonne ordre
        if (aircraftData == null) return aircraftState.getIcaoAddress().string();


        if (aircraftData.registration() != null) {
            return aircraftData.registration().string();
        } else {
            return aircraftData.typeDesignator().string();
        }
    }

    //TODO : mettre ça en km/h et laisser ça observable
    private Object velocityString(ObservableAircraftState aircraftState) {
        return (aircraftState.velocityProperty() != null)
                //? Units.convertTo(aircraftState.velocityProperty().doubleValue(), Units.Speed.KILOMETER_PER_HOUR)
                ? aircraftState.velocityProperty()
                : "?";
    }

    //TODO : le double ? est t'il utile?
    private Object velocityString1(ObservableAircraftState aircraftState) {
        return aircraftState.velocityProperty()
                .map(v -> v != null ? Math.rint(Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR)) : "?")
                .orElse("?");
    }

    private static double getColorForAltitude(double altitude) {
        return Math.pow(altitude/ 12000, 1d/3d);
    }
}