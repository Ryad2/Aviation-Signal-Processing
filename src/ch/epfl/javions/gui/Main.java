package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.Thread.sleep;

public final class Main extends Application {

    private static final int INITIAL_ZOOM_LEVEL = 8;
    private static final double INITIAL_LATITUDE = 33_530;
    private static final double INITIAL_LONGITUDE = 23_070;
    private static final String TILE_SERVER_URL = "tile.openstreetmap.org";
    private static final Path TILE_CACHE_DIR = Path.of("tile-cache");
    private static final long PURGE_TIME = 1_000_000_000L;
    private static final long FROM_NANO_TO_MILLISECOND = Duration.ofMillis(1).toNanos();
    private static final int WIDTH_WINDOW_OPENING = 800;
    private static final int HEIGHT_WINDOW_OPENING = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {

        long startTime = System.nanoTime();

        StatusLineController statusLineController = new StatusLineController();
        ObjectProperty<ObservableAircraftState> selectedAircraftStateProperty = new SimpleObjectProperty<>();
        ConcurrentLinkedDeque<Message> queue = new ConcurrentLinkedDeque<>();

        TileManager tileManager = new TileManager(TILE_CACHE_DIR, TILE_SERVER_URL);
        MapParameters mapParameters = new MapParameters(INITIAL_ZOOM_LEVEL, INITIAL_LATITUDE, INITIAL_LONGITUDE);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParameters);

        URL url = getClass().getResource("/aircraft.zip");
        assert url != null;
        Path path = Path.of(url.toURI());
        AircraftDatabase dataBase = new AircraftDatabase(path.toString());

        AircraftStateManager aircraftStateManager = new AircraftStateManager(dataBase);

        statusLineController.aircraftCountProperty().bind(Bindings.size(aircraftStateManager.states()));

        AircraftController aircraftMapView = new AircraftController(mapParameters, aircraftStateManager.states(), selectedAircraftStateProperty);
        StackPane aircraftView = new StackPane(baseMapController.pane(), aircraftMapView.pane());

        AircraftTableController aircraftTable = new AircraftTableController(aircraftStateManager.states(), selectedAircraftStateProperty);
        aircraftTable.setOnDoubleClick(s -> baseMapController.centerOn(s.getPosition()));

        BorderPane aircraftTablePane = new BorderPane(aircraftTable.pane());
        aircraftTablePane.setTop(statusLineController.pane());

        Thread thread;

        if(getParameters().getRaw().isEmpty()) {//ToDo mettre tout ça en prv
        thread = new Thread(() -> {
            getParameters().getRaw();
            try  {
                AdsbDemodulator is = new AdsbDemodulator(System.in);
                RawMessage rawMessage= is.nextMessage();
                while (rawMessage != null) {
                    Message message = MessageParser.parse(rawMessage);
                    if(message != null) queue.add(message);
                    rawMessage= is.nextMessage();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        }
        else {
            thread = new Thread(() -> {

                       try {
                           for(RawMessage rawMessage : readAllMessages(getParameters().getRaw().get(0))) {
                               long currentTime = System.nanoTime() - startTime;
                               if(currentTime < rawMessage.timeStampNs()) {
                                   sleep((rawMessage.timeStampNs() - currentTime) / FROM_NANO_TO_MILLISECOND);
                               }
                               Message message = MessageParser.parse(rawMessage);
                               if(message != null) {
                                   queue.add(message);
                               }
                        }
                } catch (IOException ioException) {
                    throw new UncheckedIOException(ioException);
                }
                catch (InterruptedException interruptedException) {
                           interruptedException.printStackTrace();
                       }
            });
        }
        thread.setDaemon(true);
        thread.start();

        SplitPane splitPane = new SplitPane(aircraftView, aircraftTablePane);
        splitPane.setOrientation(Orientation.VERTICAL);

        BorderPane root = new BorderPane(splitPane);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(WIDTH_WINDOW_OPENING);
        primaryStage.setMinHeight(HEIGHT_WINDOW_OPENING);
        primaryStage.setScene(scene);
        primaryStage.show();


        // Animation des aéronefs
        new AnimationTimer() {
            private long lastTimeStampNs = 0L;
            @Override
            public void handle(long now) {
                try {
                    while (!queue.isEmpty()) {
                        Message message = queue.remove();
                        aircraftStateManager.updateWithMessage(message);
                        statusLineController.messageCountProperty().set(statusLineController.messageCountProperty().get() + 1);
                    }
                    if (now - lastTimeStampNs > PURGE_TIME) {
                        aircraftStateManager.purge();
                        lastTimeStampNs = now;
                    }
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

    public static void main(String[] args) {launch(args);}

    static List<RawMessage> readAllMessages(String fileName) throws IOException {
        List<RawMessage> l = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                if (bytesRead != RawMessage.LENGTH) {
                    throw new EOFException();
                }
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                l.add(rawMessage);
            }
        } catch (EOFException e){
            return l;
        }
    }
}