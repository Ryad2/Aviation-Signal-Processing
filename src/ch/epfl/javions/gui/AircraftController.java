package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

public final class AircraftController {

    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftStateProperty;
    private final MapParameters mapParameters;
    private static final int MAX_ALTITUDE_FLIGHT_LEVEL = 12000;
    private static final double POWER_ALTITUDE = 1d/3d;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraft) {

        this.pane = new Pane();
        this.selectedAircraftStateProperty = selectedAircraft;
        this.mapParameters = mapParameters;

        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");

        addAndRemoveAircraft(aircraftStates);
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
                Bindings.createObjectBinding(() ->
                                ColorRamp.PLASMA.at(getColorForAltitude(aircraftState.altitudeProperty().get())),
                        aircraftState.altitudeProperty())
        );

        aircraftIcon.setOnMouseClicked(e -> selectedAircraftStateProperty.set(aircraftState));

        return aircraftIcon;
    }



    private Node labelGroup(ObservableAircraftState aircraftState) {
        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + 4));


        text.textProperty().bind(Bindings.format("%s \n%s km/h\u2002%1.0f m",
                getAircraftIdentifier(aircraftState),
                velocityString(aircraftState),
                altitudeString(aircraftState)));


        Group label = new Group(rectangle, text);
        label.getStyleClass().add("label");

        label.visibleProperty().bind(
                selectedAircraftStateProperty.isEqualTo(aircraftState)
                        .or(mapParameters.zoomProperty().greaterThanOrEqualTo(11))
        );


        return label;
    }


    private Node trajectoryGroups(ObservableAircraftState aircraftState) {
        Group trajectoryGroup = new Group();
        trajectoryGroup.getStyleClass().add("trajectory");


        trajectoryGroup.visibleProperty().bind(Bindings.equal(aircraftState, selectedAircraftStateProperty));
        //trajectoryGroup.setVisible(true);
        InvalidationListener listener = z -> drawTrajectory(aircraftState.getTrajectory(), trajectoryGroup);

        trajectoryGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());


        trajectoryGroup.visibleProperty().addListener((object, oldVisible, newVisible) -> {
            if (newVisible) {
                drawTrajectory(aircraftState.getTrajectory(), trajectoryGroup);
                mapParameters.zoomProperty().addListener(listener);
                aircraftState.getTrajectory().addListener(listener);//TODO  ask if should be
            }
        });


        return trajectoryGroup;
    }

    private void drawTrajectory(List<ObservableAircraftState.AirbornePos> trajectoryList, Group trajectoryGroup) {

        if (trajectoryList.size() < 2 ) {
            trajectoryGroup.getChildren().clear();
            return;
        }

        ArrayList<Line> lines = new ArrayList<>(trajectoryList.size() - 1);

        double previousX = WebMercator.x(mapParameters.getZoom(), trajectoryList.get(0).position().longitude());
        double previousY = WebMercator.y(mapParameters.getZoom(), trajectoryList.get(0).position().latitude());

        for (int i = 0; i < trajectoryList.size()-1; ++i) {
            if(trajectoryList.get(i).position() == null) continue;

            double x = WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i+1).position().longitude());
            double y = WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i+1).position().latitude());

            Line line = new Line(previousX, previousY, x, y);
            Stop s1 = new Stop(0, ColorRamp.PLASMA.at(getColorForAltitude(trajectoryList.get(i).altitude())));
            Stop s2 = new Stop(1, ColorRamp.PLASMA.at(getColorForAltitude(trajectoryList.get(i+1).altitude())));
            line.setStroke(new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2));

            lines.add(line);
            previousX = x;
            previousY = y;
        }




        trajectoryGroup.getChildren().setAll(lines);
    }


    private String getAircraftIdentifier (ObservableAircraftState aircraftState) {

        AircraftData aircraftData = aircraftState.getAircraftData();

        if (aircraftData == null) return aircraftState.getIcaoAddress().string();

        if (aircraftData.registration() != null) {
            return aircraftData.registration().string();
        } else {
            return aircraftState.getCallSign().string();
        }
    }

    private Object velocityString(ObservableAircraftState aircraftState) {
        return aircraftState.velocityProperty()
                .map(v -> (v.doubleValue() != 0 || Double.isNaN(v.doubleValue()))
                        ? (int) Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR)
                        : "?");
    }

    //TODO : demander si c'est utilise car une altitude null est impossible selon moi
    private Object altitudeString(ObservableAircraftState aircraftState){
        return aircraftState.altitudeProperty()
                .map(v -> (v.doubleValue() != 0 || Double.isNaN(v.doubleValue()))
                        ? v.doubleValue()
                        : "?");
    }

    private static double getColorForAltitude(double altitude) {
        return Math.pow(altitude / MAX_ALTITUDE_FLIGHT_LEVEL, POWER_ALTITUDE);
    }
}