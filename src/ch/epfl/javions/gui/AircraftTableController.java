package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;


import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AircraftTableController {

    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState>  aircraftStateProperty;
    private TableView tableView;
    private final ObservableSet<ObservableAircraftState> aircraftTableStates;
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftTableStates,
                                   ObjectProperty<ObservableAircraftState> aircraftStateTableProperty) {

        this.pane = new Pane();
        this.aircraftTableStates = aircraftTableStates;
        this.aircraftStateProperty = aircraftStateTableProperty;
    }

    public Pane pane() {
        return pane();
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ObservableAircraftState selectedAircraft = (ObservableAircraftState) tableView.getSelectionModel().getSelectedItem();
                if (selectedAircraft != null) {
                    consumer.accept(selectedAircraft);
                }
            }
        });
    }

    public void createTable() {
        // Créer une TableView
        TableView <ObservableAircraftState> tableView = new TableView<>();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);

        tableView.setTableMenuButtonVisible(true);
        tableView.getStyleClass().add("table.css");

        //TODO : comment faire?
        TableColumn<ObservableAircraftState, String> adresseOACIColumn = new TableColumn<>("OACI");
        //adresseOACIColumn.setCellValueFactory(f -> f.getValue().getIcaoAddress().map(IcaoAddress::string));
        adresseOACIColumn.setPrefWidth(60);


        TableColumn<ObservableAircraftState, String> indicatifColumn = new TableColumn<>("Indicatif");
        indicatifColumn.setCellValueFactory(f -> f.getValue().callSignProperty().map(CallSign::string));
        indicatifColumn.setPrefWidth(70);


        TableColumn<ObservableAircraftState, String> immatriculationColumn = new TableColumn<>("Immatriculation");
        //immatriculationColumn.setCellValueFactory(f -> f.getValue().getAircraftData().registration().map(AircraftRegistration::string));
        immatriculationColumn.setPrefWidth(90);


        TableColumn<ObservableAircraftState, String> modelColumn = new TableColumn<>("Modèle");
        //modelColumn.setCellValueFactory(f -> f.getValue().getAircraftData().model().map());
        modelColumn.setPrefWidth(230);


        TableColumn<ObservableAircraftState, String> typeColumn = new TableColumn<>("Indicateur de type");
        //typeColumn.setCellValueFactory(f -> f.getValue().getAircraftData().typeDesignator().map(AircraftTypeDesignator::string));
        typeColumn.setPrefWidth(50);


        TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>("Description");
        //descriptionColumn.setCellValueFactory(f -> f.getValue().getAircraftData().description().map(AircraftDescription::string));
        descriptionColumn.setPrefWidth(70);


        /*TableColumn<ObservableAircraftState, String> adresseOACIColumn = createNotNumericColumn("OACI", "oaci", 60);
        TableColumn<ObservableAircraftState, String> indicatifColumn = createNotNumericColumn("Indicatif", "indicatif", 70);
        TableColumn<ObservableAircraftState, String> immatriculationColumn = createNotNumericColumn("Immatriculation", "immatriculation", 90);
        TableColumn<ObservableAircraftState, String> modelColumn = createNotNumericColumn("Modèle", "modele", 230);
        TableColumn<ObservableAircraftState, String> typeColumn = createNotNumericColumn("Type", "type", 50);
        TableColumn<ObservableAircraftState, String> descriptionColumn = createNotNumericColumn("Description", "description", 70);

        TableColumn<ObservableAircraftState, Double> longitudeColumn = createNumericColumn("Longitude", "longitude", 85, 4);
        TableColumn<ObservableAircraftState, Double> latitudeColumn = createNumericColumn("Latitude", "latitude", 85, 4);
        TableColumn<ObservableAircraftState, Double> altitudeColumn = createNumericColumn("Altitude", "altitude", 85, 0);
        TableColumn<ObservableAircraftState, Double> vitesseColumn = createNumericColumn("Vitesse", "vitesse", 85, 0);
         */


        tableView.getColumns().addAll(adresseOACIColumn, indicatifColumn, immatriculationColumn, modelColumn, typeColumn, descriptionColumn);

        /*tableView.setItems(aircraftTableStates);

        // Bind the selected aircraft state property to the table view selection model
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            aircraftStateProperty.set(newValue);
        });*/
    }

    private TableColumn<ObservableAircraftState, String> createNotNumericColumn(String name, String property, double preferredWidth) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(name);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(preferredWidth);
        return column;
    }

    private TableColumn<ObservableAircraftState, String> createTableColumn(String columnName, Function<ObservableAircraftState, Optional<String>> valueMapper, double prefWidth) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        //column.setCellValueFactory(f -> valueMapper.apply(f.getValue()).orElse(""));
        column.setPrefWidth(prefWidth);
        return column;
    }
}