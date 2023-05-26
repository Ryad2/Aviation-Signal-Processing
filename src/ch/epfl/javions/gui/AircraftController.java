package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.adsb.CallSign;
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
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;
import javafx.beans.property.SimpleStringProperty;



/**
 * Cette classe gère la vue des aéronefs.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class AircraftController {
    public static final AircraftDescription AIRCRAFT_DESCRIPTION = new AircraftDescription("");
    public static final AircraftTypeDesignator AIRCRAFT_TYPE_DESIGNATOR = new AircraftTypeDesignator("");
    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftStateProperty;
    private final MapParameters mapParameters;
    private static final int MAX_ALTITUDE_FLIGHT_LEVEL = 12000;
    private static final double POWER_ALTITUDE = 1d/3d;

    /**
     * Constructeur de la classe AircraftController qui initialise la vue des aéronefs et
     * appelle les méthodes privées qui permettent d'ajouter et de supprimer les aéronefs de la vue
     * et de mettre en forme les aéronefs.
     *
     * @param mapParameters les paramètres de la portion visible de la carte
     * @param aircraftStates l'ensemble des états des aéronefs qui doivent apparaitre sur la vue
     * @param selectedAircraft l'état de l'aéronef sélectionné. Le contenu peut être nul lorsque
     *                         aucun aéronef n'est sélectionné.
     */
    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraft) {
        this.pane = new Pane();
        this.selectedAircraftStateProperty = selectedAircraft;
        this.mapParameters = mapParameters;
        pane.setPickOnBounds(false);
        setupAircraftView();
        addAndRemoveAircraft(aircraftStates);
    }

    public Pane pane() {
        return pane;
    }


    /**
     * Méthode privée qui met en forme dans un bon style les avions et qui permet à la vue des
     * aéronefs de recevoir les événements souris lorsque l'utilisateur clique sur une partie
     * transparente de l'avion de la vue des aéronefs.
     */
    private void setupAircraftView() {
        pane.setPickOnBounds(false);
        pane.getStylesheets().add("aircraft.css");
    }

    /**
     * Méthode privée qui permet d'ajouter et de supprimer les aéronefs de la vue des aéronefs.
     * Cette méthode est directement appelé dans le constructeur de la classe AircraftController.
     *
     * @param aircraftStates l'ensemble des états des aéronefs qui doivent apparaitre sur la vue
     */
    private void addAndRemoveAircraft (ObservableSet<ObservableAircraftState> aircraftStates) {
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {

            if (change.wasAdded()) {
                pane.getChildren().add(ICAOAddressGroup(change.getElementAdded()));
            }
            if (change.wasRemoved())
                pane.getChildren().removeIf(a ->
                        a.getId().equals(change.getElementRemoved().getIcaoAddress().string()));
        });
    }

    /**
     * Méthode privée qui permet de créer un groupe des adresses OACI d'un aéronef.
     *
     * @param aircraftState l'état de l'aéronef
     * @return le groupe annoté des aéronefs
     */
    private Group ICAOAddressGroup(ObservableAircraftState aircraftState) {
        Group annotatedAircraftGroup = new Group(trajectoryGroup(aircraftState), labelIconGroups(aircraftState));
        annotatedAircraftGroup.setId(aircraftState.getIcaoAddress().string());
        annotatedAircraftGroup.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return annotatedAircraftGroup;
    }


    /**
     * Méthode privée qui permet de créer un groupe contenant l'icône et l'étiquette des aéronefs.
     * @param aircraftState l'état de l'aéronef
     * @return le groupe des étiquettes et des Icons
     */
    private Group labelIconGroups(ObservableAircraftState aircraftState) {

        Group labelIconGroup = new Group(labelGroup(aircraftState), iconGroup(aircraftState));

        labelIconGroup.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                                WebMercator.x(mapParameters.getZoom(),
                                aircraftState.getPosition().longitude()) - mapParameters.getminX(),
                                aircraftState.positionProperty(),
                                mapParameters.zoomProperty(),
                                mapParameters.minXProperty()));

        labelIconGroup.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                                      WebMercator.y(mapParameters.getZoom(),
                                      aircraftState.getPosition().latitude()) - mapParameters.getminY(),
                                      aircraftState.positionProperty(),
                                      mapParameters.zoomProperty(),
                                      mapParameters.minYProperty()));
        return labelIconGroup;
    }

    /**
     * Méthode privée qui permet de créer un groupe contenant l'icône de l'aéronef.
     *
     * @param aircraftState l'état de l'aéronef
     * @return le groupe de l'icône des aéronefs
     */
    private SVGPath iconGroup(ObservableAircraftState aircraftState) {

        AircraftData aircraftData = aircraftState.getAircraftData();

        AircraftTypeDesignator aircraftTypeDesignator = (aircraftData != null)
                ? aircraftData.typeDesignator()
                : AIRCRAFT_TYPE_DESIGNATOR;

        AircraftDescription aircraftDescription = (aircraftData != null)
                ? aircraftData.description()
                : AIRCRAFT_DESCRIPTION;

        WakeTurbulenceCategory wakeTurbulenceCategory = (aircraftData != null)
                ? aircraftData.wakeTurbulenceCategory()
                : WakeTurbulenceCategory.UNKNOWN;

        ObservableValue<AircraftIcon> icon = aircraftState.categoryProperty().map(c ->
                AircraftIcon.iconFor(
                        aircraftTypeDesignator,
                        aircraftDescription,
                        c.intValue(),
                        wakeTurbulenceCategory));

        SVGPath aircraftIcon = new SVGPath();
        aircraftIcon.getStyleClass().add("aircraft");
        aircraftIcon.contentProperty().bind(icon.map(AircraftIcon::svgPath));

        aircraftIcon.rotateProperty().bind(
                Bindings.createDoubleBinding (()->
                                icon.getValue().canRotate() ?
                                Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE)
                                : 0, icon,
                                aircraftState.trackOrHeadingProperty()));

        aircraftIcon.fillProperty().bind(aircraftState.altitudeProperty()
                                         .map(v -> ColorRamp.PLASMA
                                                 .at(getColorForAltitude((v.doubleValue())))));

        aircraftIcon.setOnMouseClicked(e -> selectedAircraftStateProperty.set(aircraftState));

        return aircraftIcon;
    }


    /**
     * Méthode privée qui permet de créer un groupe contenant l'étiquette de l'aéronef.
     * On crée un groupe content le texte le rectangle, on les bind afin que le group s'affiche si
     * le niveau de zoom est supérieur ou égal à 11, ou que l'aéronef sélectionné est celui auquel
     * l'étiquette correspond.
     *
     * @param aircraftState l'état de l'aéronef
     * @return le groupe de l'étiquette des aéronefs
     */
    private Group labelGroup(ObservableAircraftState aircraftState) {
        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        text.textProperty().bind(Bindings.format("%s \n%s km/h\u2002%s m",
                getAircraftIdentifier(aircraftState),
                getVelocityString(aircraftState),
                getAltitudeString(aircraftState)));

        Group labelGroup = new Group(rectangle, text);
        labelGroup.getStyleClass().add("label");

        labelGroup.visibleProperty().bind(
                selectedAircraftStateProperty.isEqualTo(aircraftState)
                        .or(mapParameters.zoomProperty().greaterThanOrEqualTo(11))
        );
        return labelGroup;
    }


    private Group trajectoryGroup(ObservableAircraftState aircraftState) {
        Group trajectoryGroup = new Group();

        trajectoryGroup.getStyleClass().add("trajectory");

        trajectoryGroup.visibleProperty().bind(Bindings.equal(aircraftState, selectedAircraftStateProperty));
        InvalidationListener redrawTrajectoryIfNeeded = z -> drawTrajectory(aircraftState.getTrajectory(), trajectoryGroup);

        trajectoryGroup.layoutXProperty().bind(mapParameters.minXProperty().negate());
        trajectoryGroup.layoutYProperty().bind(mapParameters.minYProperty().negate());


        trajectoryGroup.visibleProperty().addListener((object, oldVisible, newVisible) -> {
            if (newVisible) {
                drawTrajectory(aircraftState.getTrajectory(), trajectoryGroup);
                mapParameters.zoomProperty().addListener(redrawTrajectoryIfNeeded);
                aircraftState.getTrajectory().addListener(redrawTrajectoryIfNeeded);
            }
            else{
                trajectoryGroup.getChildren().clear();
                mapParameters.zoomProperty().removeListener(redrawTrajectoryIfNeeded);
                aircraftState.getTrajectory().removeListener(redrawTrajectoryIfNeeded);
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
        Point2D previousPoint = actualPosition(mapParameters.getZoom(), trajectoryList.get(0).position());

        for (int i = 1; i < trajectoryList.size(); ++i) {

            Point2D actualPoint = actualPosition(mapParameters.getZoom(), trajectoryList.get(i).position());
            Line line = new Line(previousPoint.getX(), previousPoint.getY(), actualPoint.getX(), actualPoint.getY());

            Stop s1 = new Stop(0, ColorRamp.PLASMA
                    .at(getColorForAltitude(trajectoryList.get(i - 1).altitude())));
            Stop s2 = new Stop(1, ColorRamp.PLASMA
                    .at(getColorForAltitude(trajectoryList.get(i).altitude())));

            line.setStroke(new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2));

            lines.add(line);
            previousPoint = actualPoint;
        }
        trajectoryGroup.getChildren().setAll(lines);
    }
    private Point2D actualPosition(int zoom, GeoPos position) {
        return new Point2D(WebMercator.x(zoom, position.longitude()), WebMercator.y(zoom, position.latitude()));
    }

    /**
     * Méthode privée qui permet de retourner le bon identifant de l'aéronef.
     *
     * @param aircraftState l'état de l'aéronef
     * @return l'immatriculation si elle est connue, sinon son indicatif s'il est connu, sinon son
     * adresse OACI
     */
    private ObservableValue<String> getAircraftIdentifier(ObservableAircraftState aircraftState) {
        AircraftData aircraftData = aircraftState.getAircraftData();

        if (Objects.nonNull(aircraftData) ) return new SimpleStringProperty( aircraftData.registration().string() );

        else{
            return Bindings.when(aircraftState.callSignProperty().isNotNull())
                    .then(Bindings.convert(aircraftState.callSignProperty().map(CallSign::string)))
                    .otherwise(aircraftState.getIcaoAddress().string());
        }
    }


    /**
     * Méthode privée qui permet de retourner la vitesse de l'aéronef dans son bon format et la
     * convertie en kilomètre par heure.
     *
     * @param aircraftState l'état de l'aéronef
     * @return la vitesse si elle est différente de 0, sinon "?"
     */
    private ObservableValue<String> getVelocityString(ObservableAircraftState aircraftState) {
        return aircraftState.velocityProperty()
                .map(v -> Double.isNaN(v.doubleValue())
                        ? "?"
                        : String.format("%.0f",Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR)));
    }

    /**
     * Méthode privée qui permet de retourner l'altitude de l'aéronef dans son bon format.
     *
     * @param aircraftState l'état de l'aéronef
     * @return la vitesse si elle est différente de 0, sinon "?"
     */
    private ObservableValue<String> getAltitudeString(ObservableAircraftState aircraftState) {
        return aircraftState.altitudeProperty()
                .map(v -> Double.isNaN(v.doubleValue())
                        ? "?"
                        : String.format("%.0f", v.doubleValue()));

    }

    /**
     * Retourner la couleur de l'aéronef en fonction de son altitude. Le rôle de la racine cubique
     * est de distinguer plus finement les altitudes basses, qui sont les plus importantes.
     *
     * @param altitude l'altitude de l'aéronef
     * @return la couleur de l'aéronef
     */
    private static double getColorForAltitude(double altitude) {
        return Math.pow(altitude / MAX_ALTITUDE_FLIGHT_LEVEL, POWER_ALTITUDE);
    }
}