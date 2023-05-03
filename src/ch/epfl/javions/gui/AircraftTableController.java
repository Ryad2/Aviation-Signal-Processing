package ch.epfl.javions.gui;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;

import javax.swing.table.TableColumn;
import javax.swing.text.TableView;
import java.util.function.Consumer;

public final class AircraftTableController {

    private static TableView tableView;

    private final ObservableSet<ObservableAircraftState> aircraftTableStates;

    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftTableStates,
                                   ObjectProperty<ObservableAircraftState> aircraftStateTableProperty) {

        this.aircraftTableStates = aircraftTableStates;
    }

    public TableView pane() {
        return pane();
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
    }

    public void createTable() {
        // Créer une TableView
        //TableView tableView = new TableView(aircraftTableStates);

    }



}
