package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.sun.javafx.scene.control.skin.Utils.getResource;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Main extends Application {

    private static final int INITIAL_ZOOM_LEVEL = 8;
    private static final double INITIAL_LATITUDE = 33_530;
    private static final double INITIAL_LONGITUDE = 23_070;
    private static final String TILE_SERVER_URL = "https://tile.openstreetmap.org/";
    private static final Path TILE_CACHE_DIR = Path.of("tile-cache");
    private MapParameters mapParameters;
    private ObservableSet<ObservableAircraftState> aircraftStates;
    private ObjectProperty<ObservableAircraftState> aircraftStateProperty;

    @Override
    public void start(Stage primaryStage) throws Exception {

        TableView<ObservableAircraftState> tableView = new TableView<>();

        TileManager tileManager = new TileManager(TILE_CACHE_DIR, TILE_SERVER_URL);
        MapParameters mapParameters = new MapParameters(INITIAL_ZOOM_LEVEL, INITIAL_LATITUDE, INITIAL_LONGITUDE);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParameters);

        URL u = getClass().getResource("/aircraft.zip");
        assert u != null;
        Path p = Path.of(u.toURI());
        AircraftDatabase dataBase = new AircraftDatabase(p.toString());

        AircraftStateManager asm = new AircraftStateManager(dataBase);

        Text aircraftCountText = new Text("Aéronefs visibles : " + StatusLineController.aircraftCountProperty());
        aircraftCountText.setFont(Font.font("Arial", 12));

        Text messageCountText = new Text("Messages reçus : " + StatusLineController.messageCountProperty());
        messageCountText.setFont(Font.font("Arial", 12));

        VBox statusLine = new VBox(new TextFlow(aircraftCountText), new TextFlow(messageCountText));

        AircraftController aircraftMapView = new AircraftController(mapParameters, aircraftStates, aircraftStateProperty);
        StackPane aircraftView = new StackPane();

        AircraftTableController aircraftTable = new AircraftTableController(aircraftStates, aircraftStateProperty);
        BorderPane aircraftTablePane = new BorderPane(aircraftTable.pane(), aircraftTable.pane(), null, null, null);


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

        var mi = readAllMessages().iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 10; i += 1) {
                        Message m = MessageParser.parse(mi.next());
                        if (m != null) asm.updateWithMessage(m);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }


    public static void main(String[] args) {
        launch(args);
    }

    static List<RawMessage> readAllMessages(){
        List<RawMessage> messageList = new ArrayList<>();
        String f = getResource("messages_20230318_0915.bin").getFile();
        f = URLDecoder.decode(f, UTF_8);

        try (DataInputStream s = new DataInputStream(
        new BufferedInputStream(
        new FileInputStream(f)))){
        byte[] bytes = new byte[RawMessage.LENGTH];
        while (s.available() > 0) {
        long timeStampNs = s.readLong();
        int bytesRead = s.readNBytes(bytes, 0, bytes.length);
        assert bytesRead == RawMessage.LENGTH;
        ByteString message = new ByteString(bytes);
        messageList.add(new RawMessage(timeStampNs, message));
        }
        }catch (IOException e) {
        throw new RuntimeException(e);
        }
        return messageList;
        }
}