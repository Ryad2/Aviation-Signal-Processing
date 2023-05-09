package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.URL;

public final class Main extends Application {

    private static final int INITIAL_ZOOM_LEVEL = 8;
    private static final double INITIAL_LATITUDE = 33530;
    private static final double INITIAL_LONGITUDE = 23070;
    private static final String TILE_SERVER_URL = "https://tile.openstreetmap.org/";
    private static final String TILE_CACHE_DIR = "tile-cache";
    private MapParameters mapParameters;
    private ObservableSet<ObservableAircraftState> aircraftStates;
    private ObjectProperty<ObservableAircraftState> aircraftStateProperty;

    @Override
    public void start(Stage primaryStage) {

        TableView<ObservableAircraftState> tableView = new TableView<>();

        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
//        Path p = Path.of(u.toURI());
//        AircraftDatabase db = new AircraftDatabase(p.toString());

        Text aircraftCountText = new Text("Aéronefs visibles : " + StatusLineController.aircraftCountProperty());
        aircraftCountText.setFont(Font.font("Arial", 12));

        Text messageCountText = new Text("Messages reçus : " + StatusLineController.messageCountProperty());
        messageCountText.setFont(Font.font("Arial", 12));

        VBox statusLine = new VBox(new TextFlow(aircraftCountText), new TextFlow(messageCountText));

        AircraftController aircraftMapView = new AircraftController(mapParameters, aircraftStates, aircraftStateProperty);
        StackPane aircraftView = new StackPane();

        AircraftTableController aircraftTable = new AircraftTableController(aircraftStates, aircraftStateProperty);
        BorderPane aircraftTablePane = new BorderPane();
        aircraftTablePane.setCenter(aircraftMapView.pane());


        SplitPane splitPane = new SplitPane(aircraftView, aircraftTablePane);
        splitPane.setOrientation(Orientation.VERTICAL);

        BorderPane root = new BorderPane(splitPane);
        root.setTop(statusLine);
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}