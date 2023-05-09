package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.Consumer;
import java.util.function.Function;

public final class AircraftTableController {

    private static final int OACI_COLUMN_SIZE = 60;
    private static final int INDICATIF_COLUMN_SIZE = 70;
    private static final int IMMATRICULATION_COLUMN_SIZE = 90;
    private static final int MODEL_COLUMN_SIZE = 230;
    private static final int TYPE_COLUMN_SIZE = 50;
    private static final int DESCRIPTION_COLUMN_SIZE = 70;
    private static final int NUMERIC_COLUMN_SIZE = 85;
    private TableView<ObservableAircraftState> tableView;

    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftTableStates,
                                   ObjectProperty<ObservableAircraftState> aircraftStateTableProperty) {

        createTable();
        addAndRemoveAircraftInTheTable(aircraftTableStates);
    }

    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    public void createTable() {

        tableView = new TableView<>();

        //TODO : pq utilise getInstance et setMinimumFractionDigits ?
        DecimalFormat decimalFormatLongitudeAndLatitude = new DecimalFormat("#.####");
        DecimalFormat decimalFormatSpeedAndAltitude = new DecimalFormat("#");

        setupTableView();


        TableColumn<ObservableAircraftState, String> adresseOACIColumn = new TableColumn<>("OACI");
        adresseOACIColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getIcaoAddress()).map(IcaoAddress::string));
        adresseOACIColumn.setPrefWidth(OACI_COLUMN_SIZE);

        TableColumn<ObservableAircraftState, String> indicatifColumn = new TableColumn<>("Indicatif");
        indicatifColumn.setCellValueFactory(f -> f.getValue().callSignProperty().map(CallSign::string));
        indicatifColumn.setPrefWidth(INDICATIF_COLUMN_SIZE);

        TableColumn<ObservableAircraftState, String> immatriculationColumn = new TableColumn<>("Immatriculation");
        immatriculationColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<> (f.getValue().getAircraftData()).map(d-> d.registration().string()));
        immatriculationColumn.setPrefWidth(IMMATRICULATION_COLUMN_SIZE);

        TableColumn<ObservableAircraftState, String> modelColumn = new TableColumn<>("Modèle");
        modelColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getAircraftData())
                .map(AircraftData::model));
        modelColumn.setPrefWidth(MODEL_COLUMN_SIZE);

        TableColumn<ObservableAircraftState, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<> (f.getValue().getAircraftData()).map(d -> d.typeDesignator().string()));
        typeColumn.setPrefWidth(TYPE_COLUMN_SIZE);

        TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<> (f.getValue().getAircraftData()).map(d -> d.description().string()));
        descriptionColumn.setPrefWidth(DESCRIPTION_COLUMN_SIZE);



        /*TableColumn<ObservableAircraftState, String> longitudeColumn = new TableColumn<>("Longitude (°)");
        longitudeColumn.setCellValueFactory(f -> f.getValue().positionProperty()
                .map(v -> decimalFormatLongitudeAndLatitude.format(Units.convertTo(v.longitude(), Units.Angle.DEGREE))));
        longitudeColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        longitudeColumn.getStyleClass().add("numeric");

        TableColumn<ObservableAircraftState, String> latitudeColumn = new TableColumn<>("Latitude (°)");
        latitudeColumn.setCellValueFactory(f -> f.getValue().positionProperty()
                .map(v -> decimalFormatLongitudeAndLatitude.format(Units.convertTo(v.latitude(), Units.Angle.DEGREE))));
        latitudeColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        latitudeColumn.getStyleClass().add("numeric");

        TableColumn<ObservableAircraftState, String> altitudeColumn = new TableColumn<>("Altitude (m)");
        altitudeColumn.setCellValueFactory(f -> f.getValue().altitudeProperty()
                .map(v -> decimalFormatSpeedAndAltitude.format(v.doubleValue())));
        altitudeColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        altitudeColumn.getStyleClass().add("numeric");

        TableColumn<ObservableAircraftState, String> vitesseColumn = new TableColumn<>("Vitesse (km/h)");
        vitesseColumn.setCellValueFactory(f -> f.getValue().velocityProperty()
                .map(v -> decimalFormatSpeedAndAltitude.format(Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR))));
        vitesseColumn.setPrefWidth(NUMERIC_COLUMN_SIZE);
        vitesseColumn.getStyleClass().add("numeric");*/


        TableColumn longitudeColumn =  createTableColumn("Longitude (°)", f -> f.positionProperty()
                .map(v -> decimalFormatLongitudeAndLatitude.format(Units.convertTo(v.longitude(), Units.Angle.DEGREE))));
        TableColumn latitudeColumn = createTableColumn("Latitude (°)", f -> f.positionProperty()
                .map(v -> decimalFormatLongitudeAndLatitude.format(Units.convertTo(v.latitude(), Units.Angle.DEGREE))));
        TableColumn altitudeColumn = createTableColumn("Altitude (m)", f -> f.altitudeProperty()
                .map(v -> decimalFormatSpeedAndAltitude.format(v.doubleValue())));
        TableColumn vitesseColumn = createTableColumn("Vitesse (km/h)", f -> f.velocityProperty()
                .map(v -> decimalFormatSpeedAndAltitude.format(Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR))));

        tableView.getColumns().addAll(adresseOACIColumn, indicatifColumn, immatriculationColumn,
                modelColumn, typeColumn, descriptionColumn, longitudeColumn, latitudeColumn,
                altitudeColumn, vitesseColumn);


        /*tableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {

                altitudeColumn.setComparator((s1, s2) -> {
                    if (s1.isEmpty() || s2.isEmpty()) {
                        return s1.compareTo(s2);
                    } else {
                        double n1 = Double.parseDouble(s1);
                        double n2 = Double.parseDouble(s2);
                        return Double.compare(n1, n2);
                    }
                });
            }
        });*/
    }

    //TODO : a implémenter!
    private NumberFormat getGoodFormat(int goodFormat) {
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(goodFormat);
        decimalFormat.setMaximumFractionDigits(goodFormat);
        return decimalFormat;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> consumer) {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                ObservableAircraftState selectedAircraft = tableView.getSelectionModel().getSelectedItem();
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
                tableView.sort();
            }

            if (change.wasRemoved())
                tableView.getItems().remove(change.getElementRemoved());
        });
    }

    private void setupTableView() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        tableView.getStylesheets().add("table.css");
    }

    private TableColumn createTableColumn(String columnName, Function<ObservableAircraftState, ObservableValue<String>> propertyFunction) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> propertyFunction.apply(cellData.getValue()));
        column.setPrefWidth(NUMERIC_COLUMN_SIZE);
        column.getStyleClass().add("numeric");

        return column;
    }
}