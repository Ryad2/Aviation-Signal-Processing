package ch.epfl.javions.gui;

import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
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
    private LongProperty messageCount = new SimpleLongProperty(0);

    @Override
    public void start(Stage primaryStage) {

        /*URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path path = Path.of(u.toURI());
        AircraftDatabase database = new AircraftDatabase(path.toString());

        Text aircraftCountText = new Text("Aéronefs visibles : " + path.states().size());
        Text messageCountText = new Text("Messages reçus : " + messageCount.get());
        VBox statusLine = new VBox(new TextFlow(aircraftCountText), new TextFlow(messageCountText));


        aircraftCountText.setFont(Font.font("Arial", 12));
        //Bindings.bindBidirectional(aircraftCountText.textProperty(), aircraftStateManager.states().size());
        messageCountText.setFont(Font.font("Arial", 12));
        messageCountText.textProperty().bind(Bindings.createStringBinding(() -> "Messages reçus : " + messageCount.get(), messageCount));


        AircraftController aircraftMapView = new AircraftController(INITIAL_ZOOM_LEVEL, INITIAL_LATITUDE, INITIAL_LONGITUDE, TILE_SERVER_URL, TILE_CACHE_DIR);
        StackPane aircraftView = new StackPane(aircraftMapView);


        AircraftTableController aircraftTable = new AircraftTableController(aircraftStateManager);
        BorderPane aircraftTablePane = new BorderPane();
        aircraftTablePane.setCenter(aircraftTable);

        SplitPane splitPane = new SplitPane(aircraftView, aircraftTablePane);
        splitPane.setOrientation(Orientation.VERTICAL);


        BorderPane root = new BorderPane(splitPane);
        root.setTop(statusLine);
        Scene scene = new Scene(root, 800, 600);


        Runnable messageReader = new ADSBMessage(System.in, aircraftStateManager, messageCount);
        AnimationTimer aircraftAnimationTimer = new AircraftAnimationTimer(aircraftStateManager, aircraftMapView);


        new Thread(messageReader).start();
        aircraftAnimationTimer.start();

        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();*/
    }

    public static void main(String[] args) {
        launch(args);
    }
}
