package ch.epfl.javions.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
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
    ObservableSet<ObservableAircraftState> unmodifiableAircraftStates;

    public AircraftController(MapParameters mapParameters,
                              SetProperty<ObservableAircraftState> aircraftState,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty) {


        this.pane = new Pane();
        pane.getStylesheets().add("aircraft.css");
        pane.setId("adr.OACI");
        pane.getStyleClass().add("trajectory");
        pane.getStyleClass().add("label");
        pane.getStyleClass().add("aircraft");

        unmodifiableAircraftStates.addListener((SetChangeListener <ObservableAircraftState>) change -> {
            if (change.wasAdded()) {

                if (mapParameters.getZoom() >= 11) {
                    pane.getChildren().add(GroupEtiquetteAndIcone());
                }
                pane.setPickOnBounds(false);

                Group planGroup = new Group();
                Group planView = new Group();

                planGroup.getChildren().addAll(iconGroups(), etiquetteGroups());

                planGroup.viewOrderProperty();

                planView.getChildren().add(planGroup);

                pane.getChildren().add(iconGroups());
            }

            if (change.wasRemoved()) {
                aircraftStateProperty.set(null);
            }
        });
    }

    public Pane pane (){
        return pane;
    }

    private Node trajectoryGroups(){
        return new Group(new Line(),new Line(),new Line());
    }

    private Node etiquetteGroups(){
       return new Group(new Rectangle(), new Text());
    }

    private Node iconGroups(){
        return new Group(new SVGPath());
    }

     private Node etiquetteIconGroups(){
        return new Group(etiquetteGroups(), iconGroups());
    }

    private Node adresseOACIGroups() {
        return new Group(iconGroups(), etiquetteGroups(), trajectoryGroups());
    }

    private Node Pane (){
        return new Pane(adresseOACIGroups());
    }

    private Node GroupEtiquetteAndIcone(){
        return new Group(etiquetteIconGroups(), trajectoryGroups());
    }

    private void affichage (){}
}