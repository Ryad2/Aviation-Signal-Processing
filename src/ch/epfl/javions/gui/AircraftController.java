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


/**
 * Cette classe gère la vue des aéronefs.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class AircraftController {
    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState> selectedAircraftStateProperty;
    private final MapParameters mapParameters;
    private static final int MAX_ALTITUDE_FLIGHT_LEVEL = 12000;
    private static final double POWER_ALTITUDE = 1d/3d;

    /**
     * Constructeur de la classe AircraftController.
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
     * @param aircraftStates l'ensemble des états des aéronefs qui doivent apparaitre sur la vue
     */
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

    /**
     * Méthode privée qui permet de créer un groupe des adresses OACI d'un aéronef.
     * @param aircraftState l'état de l'aéronef
     * @return le groupe annoté des aéronefs
     */
    //TODO : voir s'il y a moyen de donner un meilleur nom aux variables
    private Node addressOACIGroups(ObservableAircraftState aircraftState) {
        Group annotatedAircraft = new Group(trajectoryGroups(aircraftState), labelIconGroups(aircraftState));
        annotatedAircraft.setId(aircraftState.getIcaoAddress().string());
        annotatedAircraft.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());

        return annotatedAircraft;
    }


    /**
     * Méthode privée qui permet de créer un groupe contenant l'icône et l'étiquette des aéronefs.
     * @param aircraftState l'état de l'aéronef
     * @return le groupe des étiquettes et des Icons
     */
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

    // TODO : comment on dit typeDesignator en français ?
    /**
     * Méthode privée qui permet de créer un groupe contenant l'icône de l'aéronef.
     * Tout d'abord avant d'ajouter les données dans la méthode "iconFor", on vérifie si les
     * désignations de type, les descriptions et les catégories de turbulence de l'aéronef sont nuls.
     * Si c'est le cas, on retourne un string vide. Ensuite, on crée un SVGPath qui contient une
     * feuille de classe qu'on ajoute pour lui donner un certain style
     * On ajoute ensuite à ce SVGPath trois propriétés afin de garantir qu'elle a la bonne apparence.
     * Premièrement, on lui ajoute la propriété "content" qui contient le SVGPath et représentant le
     * dessin de l'aéronef. Deuxièmement, on lui ajoute la propriété "rotate" qui contient l'angle
     * et permet de pivoter l'icône de l'avion dans la bonne direction en fonction de la direction
     * de l'aéronef. Troisièmement, on lui ajoute la propriété "fill" qui contient la couleur de
     * remplissage de l'icône de l'aéronef.
     *
     * @param aircraftState l'état de l'aéronef
     * @return le groupe de l'icône des aéronefs
     */
    private Node iconGroup(ObservableAircraftState aircraftState) {

        AircraftData aircraftData = aircraftState.getAircraftData();

        AircraftTypeDesignator aircraftTypeDesignator = (aircraftData != null)
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
                        aircraftTypeDesignator,
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


    /**
     * Méthode privée qui permet de créer un groupe contenant l'étiquette de l'aéronef.
     * Tout d'abord, on crée l'étiquette, ensuite la remplie. Sur la première ligne, on affiche
     * son immatriculation si elle est connue, sinon son indicatif s'il est connue, sinon son
     * adresse OACI. (Ces conditions se font dans une autre méthode privée à part) Sur la deuxième
     * ligne, on affiche sa vitesse en kilomètre par heure et son altitude en mètre. Ces deux valeurs
     * sont séparées d'un espace demi-cadratin. Ensuite, on crée un groupe content le text et
     * le rectangle, on leur ajoute une classe de style pour les mettre en forme et puis, on les bind
     * afin que le group s'affiche si le niveau de zoom est supérieur ou égal à 11, ou que l'aéronef
     * sélectionné est celui auquel l'étiquette correspond.
     *
     * @param aircraftState l'état de l'aéronef
     * @return le groupe de l'étiquette des aéronefs
     */
    private Node labelGroup(ObservableAircraftState aircraftState) {
        Text text = new Text();
        Rectangle rectangle = new Rectangle();

        rectangle.widthProperty().bind(text.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        rectangle.heightProperty().bind(text.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        text.textProperty().bind(Bindings.format("%s \n%s km/h\u2002%1.0f m",
                getAircraftIdentifier(aircraftState),
                velocityString(aircraftState),
                aircraftState.altitudeProperty()));

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
        //trajectoryGroup.setVisible(true);TODo check it
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

        double previousX = WebMercator//todo comment eviter la redendance de code
                .x(mapParameters.getZoom(), trajectoryList.get(0).position().longitude());
        double previousY = WebMercator
                .y(mapParameters.getZoom(), trajectoryList.get(0).position().latitude());

        for (int i = 1; i < trajectoryList.size(); ++i) {
            if(trajectoryList.get(i).position() == null) continue;//todo ask if useful

            double x = WebMercator
                    .x(mapParameters.getZoom(), trajectoryList.get(i).position().longitude());
            double y = WebMercator
                    .y(mapParameters.getZoom(), trajectoryList.get(i).position().latitude());

            Line line = new Line(previousX, previousY, x, y);

            Stop s1 = new Stop(0, ColorRamp.PLASMA
                    .at(getColorForAltitude(trajectoryList.get(i).altitude())));
            Stop s2 = new Stop(1, ColorRamp.PLASMA
                    .at(getColorForAltitude(trajectoryList.get(i+1).altitude())));

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

    private static double getColorForAltitude(double altitude) {
        return Math.pow(altitude / MAX_ALTITUDE_FLIGHT_LEVEL, POWER_ALTITUDE);
    }
}