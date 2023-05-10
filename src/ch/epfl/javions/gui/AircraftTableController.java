package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Comparator;
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
    private static final int MINIMUM_FRACTION_DIGITS = 0;
    private TableView<ObservableAircraftState> tableView;

    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftTableStates,
                                   ObjectProperty<ObservableAircraftState> SelectedAircraftStateTableProperty) {

        createTable();
        listenerAndAddAndRemoveAircraft(aircraftTableStates, SelectedAircraftStateTableProperty);
    }

    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    public void createTable() {

        tableView = new TableView<>();

        setupTableView();

        TableColumn <ObservableAircraftState, String> adresseOACIColumn =
                createTextTableColumn("OACI", f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress())
                        .map(IcaoAddress::string), OACI_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> indicatifColumn =
                createTextTableColumn("Indicatif", f -> f.callSignProperty().map(CallSign::string), INDICATIF_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> immatriculationColumn =
                createTextTableColumn("Immatriculation", f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                        .map(d-> d.registration().string()), IMMATRICULATION_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> modelColumn =
                createTextTableColumn("Modèle", f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                        .map(AircraftData::model), MODEL_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> typeColumn =
                createTextTableColumn("Type", f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                        .map(d -> d.typeDesignator().string()), TYPE_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> descriptionColumn =
                createTextTableColumn("Description", f -> new ReadOnlyObjectWrapper<>(f.getAircraftData())
                        .map(d -> d.description().string()), DESCRIPTION_COLUMN_SIZE);


        Comparator<String> numberComparator = ((o1, o2) -> {
            double difference;
            try {
                difference = getGoodFormat(MINIMUM_FRACTION_DIGITS).parse(o1).doubleValue()
                        - getGoodFormat(MINIMUM_FRACTION_DIGITS).parse(o2).doubleValue();

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            return (difference > 0) ? 1 : (difference < 0) ? -1 : 0;
        });

        /*Comparator<String> numberComparator = Comparator.comparing(
                (String s) -> Double.valueOf(s),
                Double::compare
        );*/


        TableColumn <ObservableAircraftState, String> longitudeColumn =
                createNumericTableColumn("Longitude (°)", f -> f.positionProperty()
                .map(v -> getGoodFormat(4).
                        format(Units.convertTo(v.longitude(), Units.Angle.DEGREE))));

        TableColumn <ObservableAircraftState, String> latitudeColumn =
                createNumericTableColumn("Latitude (°)", f -> f.positionProperty()
                .map(v -> getGoodFormat(4).
                        format(Units.convertTo(v.latitude(), Units.Angle.DEGREE))));

        TableColumn <ObservableAircraftState, String> altitudeColumn =
                createNumericTableColumn("Altitude (m)", f -> f.altitudeProperty()
                .map(v -> getGoodFormat(MINIMUM_FRACTION_DIGITS).
                        format(v.doubleValue())));

        TableColumn <ObservableAircraftState, String> vitesseColumn =
                createNumericTableColumn("Vitesse (km/h)", f -> f.velocityProperty()
                .map(v -> getGoodFormat(MINIMUM_FRACTION_DIGITS).
                        format(Units.convertTo(v.doubleValue(), Units.Speed.KILOMETER_PER_HOUR))));


        tableView.getColumns().addAll(adresseOACIColumn, indicatifColumn, immatriculationColumn,
                modelColumn, typeColumn, descriptionColumn, longitudeColumn, latitudeColumn,
                altitudeColumn, vitesseColumn);


        //TODO : check si c'est ok
        tableView.setOnMouseClicked(event -> {
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
        });
    }

    private NumberFormat getGoodFormat(int goodFormat) {
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(MINIMUM_FRACTION_DIGITS);
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

    private void listenerAndAddAndRemoveAircraft(ObservableSet<ObservableAircraftState> aircraftStates,
                                                 ObjectProperty<ObservableAircraftState> selectedAircraftStateTableProperty) {

        selectedAircraftStateTableProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tableView.getSelectionModel().select(newValue);
                if (oldValue == null || !oldValue.equals(newValue)) {
                    tableView.scrollTo(newValue);
                }
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedAircraftStateTableProperty.set(newValue);
            }
        });


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

    private TableColumn <ObservableAircraftState, String> createTextTableColumn(String columnName, Function<ObservableAircraftState, ObservableValue<String>> propertyFunction, double columnWidth) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> propertyFunction.apply(cellData.getValue()));
        column.setPrefWidth(columnWidth);

        return column;
    }

    private TableColumn <ObservableAircraftState, String> createNumericTableColumn(String columnName, Function<ObservableAircraftState, ObservableValue<String>> propertyFunction) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> propertyFunction.apply(cellData.getValue()));
        column.setPrefWidth(NUMERIC_COLUMN_SIZE);
        column.getStyleClass().add("numeric");

        return column;
    }

    //TODO : faire la méthode pour les colonnes numériques
    /*private TableColumn <ObservableAircraftState, String> createNumericTableColumn1(String columnName, Function<ObservableAircraftState, ObservableValue<String>> propertyFunction, int goodFormat, double unit) {
        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> getGoodFormat(goodFormat).format(Units.convertTo(propertyFunction.apply(cellData.getValue()), unit)));
        column.setPrefWidth(NUMERIC_COLUMN_SIZE);
        column.getStyleClass().add("numeric");

        return column;
    }*/
}