package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * La classe AircraftTableController permet de créer et de gérer la table des aéronefs.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class AircraftTableController {

    /**
     * OACI_COLUMN_SIZE est la taille de la colonne OACI
     */
    private static final int OACI_COLUMN_SIZE = 60;

    /**
     * INDICATIF_COLUMN_SIZE est la taille de la colonne Indicatif
     */
    private static final int CALL_SIGN_COLUMN_SIZE = 70;

    /**
     * IMMATRICULATION_COLUMN_SIZE est la taille de la colonne Immatriculation
     */
    private static final int REGISTRATION_COLUMN_SIZE = 90;

    /**
     * MODEL_COLUMN_SIZE est la taille de la colonne Modèle
     */
    private static final int MODEL_COLUMN_SIZE = 230;

    /**
     * TYPE_COLUMN_SIZE est la taille de la colonne Type
     */
    private static final int TYPE_COLUMN_SIZE = 50;

    /**
     * DESCRIPTION_COLUMN_SIZE est la taille de la colonne Description
     */
    private static final int DESCRIPTION_COLUMN_SIZE = 70;

    /**
     * NUMERIC_COLUMN_SIZE est la taille de toutes les colonnes utilisant des données numériques
     * comme l'altitude, la vitesse, la latitude et la longitude.
     */
    private static final int NUMERIC_COLUMN_SIZE = 85;
    private TableView<ObservableAircraftState> tableView;


    /**
     * Constructeur de AircraftTableController qui sert à créer une colonne de texte et configure
     * les listeners de la table
     *
     * @param aircraftTableStates est l'ensemble des états des aéronefs
     * @param selectedAircraftStateTableProperty est la propriété de l'état de l'aéronef sélectionné
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> aircraftTableStates,
                                   ObjectProperty<ObservableAircraftState> selectedAircraftStateTableProperty) {

        createTable();
        listenerAndAddAndRemoveAircraft(aircraftTableStates, selectedAircraftStateTableProperty);
    }

    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }


    /**
     * Méthode privée qui crée les colonnes, créer le titre de la colonne et rempli les données des
     * colonnes. Ensuite, on ajoute toutes les colonnes à la table
     */
    private void createTable() {
        tableView = new TableView<>();
        setupTableView();
        TableColumn <ObservableAircraftState, String> adresseOACIColumn =
                createTextTableColumn("OACI",
                        f -> new ReadOnlyObjectWrapper<>(f.getIcaoAddress()),
                        IcaoAddress::string, OACI_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> callSignColumn =
                createTextTableColumn("Indicatif",
                        ObservableAircraftState::callSignProperty,
                        CallSign::string,
                        CALL_SIGN_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> registrationColumn =
                createTextTableColumn("Immatriculation",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()),
                        d-> d.registration().string(), REGISTRATION_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> modelColumn =
                createTextTableColumn("Modèle",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()),
                        AircraftData::model, MODEL_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> typeColumn =
                createTextTableColumn("Type",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()),
                        d -> d.typeDesignator().string(), TYPE_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> descriptionColumn =
                createTextTableColumn("Description",
                        f -> new ReadOnlyObjectWrapper<>(f.getAircraftData()),
                        d -> d.description().string(), DESCRIPTION_COLUMN_SIZE);

        TableColumn <ObservableAircraftState, String> longitudeColumn =
                createNumericTableColumn("Longitude (°)",
                        f -> f.positionProperty().map(GeoPos::longitude),
                        4,
                        Units.Angle.DEGREE);

        TableColumn <ObservableAircraftState, String> latitudeColumn =
                createNumericTableColumn("Latitude (°)",
                        f -> f.positionProperty().map(GeoPos::latitude),
                        4,
                        Units.Angle.DEGREE);

        TableColumn <ObservableAircraftState, String> altitudeColumn =
                createNumericTableColumn("Altitude (m)",
                        ObservableAircraftState::altitudeProperty,
                        0,
                        Units.Length.METER);

        TableColumn <ObservableAircraftState, String> velocityColumn =
                createNumericTableColumn("Vitesse (km/h)",
                        ObservableAircraftState::velocityProperty,
                        0,
                        Units.Speed.KILOMETER_PER_HOUR);

        tableView.getColumns().setAll(adresseOACIColumn, callSignColumn, registrationColumn,
                modelColumn, typeColumn, descriptionColumn, longitudeColumn, latitudeColumn,
                altitudeColumn, velocityColumn);
    }


    /**
     * Méthode privée qui sert définir le nombre de chiffres après la virgule pour les colonnes
     * numériques
     *
     * @param goodFormat est le nombre de chiffres après la virgule
     * @return le format de la colonne
     */
    private NumberFormat getGoodFormat(int goodFormat) {
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setMinimumFractionDigits(goodFormat);
        decimalFormat.setMaximumFractionDigits(goodFormat);
        return decimalFormat;
    }

    /**
     * Méthode privée qui permet de pouvoir faire un double clic sur une ligne de la table et
     * voir l'avion sur la carte
     *
     * @param consumer est l'avion qu'on veut voir s'afficher sur la carte
     */
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

    /**
     * Méthode privée qui créée trois listeners pour la table view. C'est grâce au premier listener
     * que quand on appuie sur une ligne dans le tableau cette ligne s'affiche tout en haut.
     * Le deuxième permet de sélectionner une ligne dans le tableau et de la voir s'afficher en et
     * le troisième permet d'ajouter les avions dans le tableau et de les supprimer
     *
     * @param aircraftStates est la liste des avions
     * @param selectedAircraftStateTableProperty est l'avion sélectionné dans le tableau
     */
    private void listenerAndAddAndRemoveAircraft(
            ObservableSet<ObservableAircraftState> aircraftStates,
            ObjectProperty<ObservableAircraftState> selectedAircraftStateTableProperty) {

        selectedAircraftStateTableProperty.addListener((observable, oldValue, newValue) -> {

            if (!Objects.equals(tableView.getSelectionModel().getSelectedItem(), newValue)){
                tableView.scrollTo(newValue);
            }
            tableView.getSelectionModel().select(newValue);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) selectedAircraftStateTableProperty.set(newValue);
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

    /**
     * Méthode privée qui met en forme la table view
     */
    private void setupTableView() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);
        tableView.getStylesheets().add("table.css");
    }

    /**
     * Méthode privée qui permet de créer les colonnes textuelles sans répéter du code
     *
     * @param columnName est le nom de la colonne
     * @param propertyFunction est la fonction qui permet de récupérer la propriété
     * @param columnWidth est la largeur de la colonne
     * @return la colonne
     */
    private <T> TableColumn <ObservableAircraftState, String>
    createTextTableColumn(String columnName,
                          Function<ObservableAircraftState,
                          ObservableValue<T>> propertyFunction,
                          Function <T, String> valueMapper,
                          double columnWidth) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> propertyFunction.apply(cellData.getValue()).map(valueMapper));
        column.setPrefWidth(columnWidth);

        return column;
    }

    /**
     * Méthode privée qui permet de créer les colonnes numériques sans répéter du code
     *
     * @param columnName est le nom de la colonne
     * @param propertyFunction est la fonction qui permet de récupérer la propriété
     * @param goodFormat est le nombre de chiffres après la virgule
     * @param unit est l'unité de la colonne
     * @return la colonne
     */
    private TableColumn <ObservableAircraftState, String>
    createNumericTableColumn(String columnName,
                             Function<ObservableAircraftState, ObservableValue<Number>> propertyFunction,
                             int goodFormat, double unit) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(cellData -> propertyFunction
                .apply(cellData.getValue())
                .map(c -> Double.isNaN(c.doubleValue())
                        ? ""
                        : getGoodFormat(goodFormat).format(Units.convertTo(c.doubleValue(), unit))));
        column.setPrefWidth(NUMERIC_COLUMN_SIZE);
        column.getStyleClass().add("numeric");

        column.setComparator((s1, s2) -> {
            try {
                if (s1.isEmpty() || s2.isEmpty()) {
                    return s1.compareTo(s2);
                } else {
                    double n1 = getGoodFormat(goodFormat).parse(s1).doubleValue();
                    double n2 = getGoodFormat(goodFormat).parse(s2).doubleValue();
                    return Double.compare(n1, n2);
                }
            } catch (ParseException e) {
                throw new Error(e);
            }
        });
        return column;
    }
}