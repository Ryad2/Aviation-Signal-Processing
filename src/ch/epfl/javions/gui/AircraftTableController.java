package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;


import java.text.NumberFormat;
import java.util.function.Consumer;

public final class AircraftTableController {

    private final Pane pane;
    private final ObjectProperty<ObservableAircraftState>  aircraftStateProperty;
    private TableView<ObservableAircraftState> tableView;
    private final ObservableSet<ObservableAircraftState> aircraftTableStates;
    private static final int OACI_COLUMN_SIZE = 60;
    private static final int INDICATIF_COLUMN_SIZE = 70;
    private static final int IMMATRIULATION_COLUMN_SIZE = 90;
    private static final int MODEL_COLUMN_SIZE = 230;
    private static final int TYPE_COLUMN_SIZE = 50;
    private static final int DESCRIPTION_COLUMN_SIZE = 70;
    private static final int NUMERIC_COLUMN_SIZE = 85;
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftTableStates,
                                   ObjectProperty<ObservableAircraftState> aircraftStateTableProperty) {

        this.pane = new Pane();
        this.aircraftTableStates = aircraftTableStates;
        this.aircraftStateProperty = aircraftStateTableProperty;
        createTable();
        addAndRemoveAircraftInTheTable(aircraftTableStates);
    }

    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    public void createTable() {

        tableView = new TableView<>();

        NumberFormat numberFormat = NumberFormat.getInstance();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);

        tableView.setTableMenuButtonVisible(true);
        tableView.getStyleClass().add("table.css");


        //TODO : on doit faire des opérateurs ternaires
        //TODO : utiliser la Units.convertTo rend le truc pas observable
        TableColumn<ObservableAircraftState, String> adresseOACIColumn = new TableColumn<>("OACI");
        adresseOACIColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getIcaoAddress()).map(IcaoAddress::string));
        adresseOACIColumn.setPrefWidth(OACI_COLUMN_SIZE);


        TableColumn<ObservableAircraftState, String> indicatifColumn = new TableColumn<>("Indicatif");
        indicatifColumn.setCellValueFactory(f -> f.getValue().callSignProperty().map(CallSign::string));
        indicatifColumn.setPrefWidth(INDICATIF_COLUMN_SIZE);


        TableColumn<ObservableAircraftState, String> immatriculationColumn = new TableColumn<>("Immatriculation");
        immatriculationColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData() != null ? (f.getValue().getAircraftData().registration().string()) : ""));
        immatriculationColumn.setPrefWidth(IMMATRIULATION_COLUMN_SIZE);


        TableColumn<ObservableAircraftState, String> modelColumn = new TableColumn<>("Modèle");
        modelColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData()).map(AircraftData::model));
        modelColumn.setPrefWidth(MODEL_COLUMN_SIZE);


        TableColumn<ObservableAircraftState, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData() != null ? f.getValue().getAircraftData().typeDesignator().string() : ""));
        typeColumn.setPrefWidth(TYPE_COLUMN_SIZE);


        TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData() != null ? f.getValue().getAircraftData().description().string() : ""));
        descriptionColumn.setPrefWidth(DESCRIPTION_COLUMN_SIZE);


        TableColumn<ObservableAircraftState, String> longitudeColumn = new TableColumn<>("Longitude (°)");
        longitudeColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(numberFormat.format(Units.convertTo(f.getValue().getPosition().longitude(), Units.Angle.DEGREE))));
        longitudeColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        longitudeColumn.getStyleClass().add("numeric");


        TableColumn<ObservableAircraftState, String> latitudeColumn = new TableColumn<>("Latitude (°)");
        latitudeColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(numberFormat.format(Units.convertTo(f.getValue().getPosition().latitude(), Units.Angle.DEGREE))));
        latitudeColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        latitudeColumn.getStyleClass().add("numeric");


        TableColumn<ObservableAircraftState, String> altitudeColumn = new TableColumn<>("Altitude (m)");
        altitudeColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(numberFormat.format(f.getValue().altitudeProperty().doubleValue())));
        altitudeColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        altitudeColumn.getStyleClass().add("numeric");


        TableColumn<ObservableAircraftState, String> vitesseColumn = new TableColumn<>("Vitesse (km/h)");
        vitesseColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(numberFormat.format(Units.convertTo(f.getValue().velocityProperty().doubleValue(), Units.Speed.KILOMETER_PER_HOUR))));
        vitesseColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        vitesseColumn.getStyleClass().add("numeric");


       tableView.getColumns().addAll(adresseOACIColumn, indicatifColumn, immatriculationColumn,
                modelColumn, typeColumn, descriptionColumn, longitudeColumn, latitudeColumn,
                altitudeColumn, vitesseColumn);


        /*TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData().description()).map(AircraftDescription::string));
        descriptionColumn.setPrefWidth(DESCRIPTION_COLUMN_SIZE);*/
    }

    private TableColumn<ObservableAircraftState, String> createNotNumericColumn(String name, String property, double preferredWidth) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(name);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(preferredWidth);
        return column;
    }

    private NumberFormat getGoodFormat (int goodFormat){
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(0);
        decimalFormat.setMaximumFractionDigits(goodFormat);
        return decimalFormat;
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

    private void addAndRemoveAircraftInTheTable(ObservableSet<ObservableAircraftState> aircraftStates) {

        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>) change -> {
            if (change.wasAdded()) {
                tableView.getItems().add(change.getElementAdded());
            }

            if (change.wasRemoved())
                tableView.getItems().remove(change.getElementRemoved());
        });
    }
}