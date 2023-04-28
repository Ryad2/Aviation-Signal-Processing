package ch.epfl.javions.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import org.w3c.dom.Text;

public final class AircraftController {

    ObservableSet<ObservableAircraftState> unmodifiableAircraftState;

    Pane pane;
    Group adresseOACI;
    Group EtiquetteAndIcone;
    Group icone;
    SVGPath aircraft;
    Group trajectory;
    Line immatriculationOrIndicatif;
    Line vitesse;
    Line altitude;
    Group etiquette;
    Rectangle rectangle;
    Text textEtiquette;

    public AircraftController(MapParameters mapParameters, SetProperty<ObservableAircraftState> aircraftState,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty, Pane pane) {

        this.pane = pane;
        pane.getStylesheets().add("aircraft.css");

        unmodifiableAircraftState.addListener((SetChangeListener <ObservableAircraftState>) change -> {
            if (change.wasAdded()) {



                //pane.getChildren(adresseOACI(change.getElementAdded()));


            }
            if (change.wasRemoved()) {
                aircraftStateProperty.set(null);
            }
        });
    }

    public Pane pane (){

        return pane;
    }

    private void TrajectoryGroups(){
        trajectory = new Group();
        immatriculationOrIndicatif = new Line();
        vitesse = new Line();
        altitude = new Line();
        trajectory.getChildren().addAll(immatriculationOrIndicatif, vitesse, altitude);
    }

    private void EtiquetteGroups(){
        etiquette = new Group();
        rectangle = new Rectangle();
        //textEtiquette = new Text();
        //etiquette.getChildren().addAll(rectangle, textEtiquette);
    }

    private void IconeGroups(){
        icone = new Group();
        aircraft = new SVGPath();
        icone.getChildren().add(aircraft);
    }

    private void AdresseOACIGroups(){
        adresseOACI = new Group(icone(), etiquette(), trajectory());
        iconeGroups();
        etiquetteGroups();
        trajectoryGroups();

    }

    private void Pane (){
        AdresseOACIGroups();
        pane.getChildren().add(adresseOACI);
    }

    private void GroupEtiquetteAndIcone(){
        EtiquetteAndIcone = new Group();
        EtiquetteGroups();
        IconeGroups();
        EtiquetteAndIcone.getChildren().addAll(etiquette, icone);
    }





























}*/