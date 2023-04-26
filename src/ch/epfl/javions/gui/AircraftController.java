package ch.epfl.javions.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SetProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

public final class AircraftController {

    ObservableSet<ObservableAircraftState> unmodifiableAircraftState;

    private Pane pane;

    Group aeronef;

    public AircraftController(MapParameters mapParameters, SetProperty<ObservableAircraftState> aircraftState,
                              ObjectProperty<ObservableAircraftState> aircraftStateProperty, Pane pane) {

        this.pane = pane;

        unmodifiableAircraftState.addListener((SetChangeListener <ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                aircraftStateProperty.set(change.getElementAdded());
            }
            if (change.wasRemoved()) {
                aircraftStateProperty.set(null);
            }
        });
    }

    public Pane pane (){

        return pane;
    }
}