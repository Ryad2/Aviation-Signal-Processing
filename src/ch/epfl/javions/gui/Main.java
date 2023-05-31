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


/**
 * La classe Main contient le programme principal. Comme toute classe représentant une application
 * JavaFX, elle hérite d'Application, et est dotée d'une méthode main qui ne fait rien d'autre
 * qu'appeler launch.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class Main extends Application {

    private static final int INITIAL_ZOOM_LEVEL = 8;
    private static final int INITIAL_LATITUDE = 33_530;
    private static final int INITIAL_LONGITUDE = 23_070;
    private static final String TILE_SERVER_URL = "tile.openstreetmap.org";
    private static final Path TILE_CACHE_DIR = Path.of("tile-cache");
    private static final long PURGE_TIME = 1_000_000_000L;
    private static final long FROM_NANO_TO_MILLISECOND = Duration.ofMillis(1).toNanos();
    private static final int WIDTH_WINDOW_OPENING = 800;
    private static final int HEIGHT_WINDOW_OPENING = 600;

    /**
     * Méthode main qui ne fait rien d'autre que d'appeler la méthode lunch
     *
     * @param args les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * Lit tous les messages bruts à partir d'un fichier.
     *
     * @param fileName Le nom du fichier à partir duquel lire les messages.
     * @return Une liste contenant tous les messages bruts lus.
     * @throws IOException En cas d'erreur lors de la lecture du fichier.
     */
    private static List<RawMessage> readAllMessages(String fileName) throws IOException {
        List<RawMessage> list = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            //noinspection InfiniteLoopStatement
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                if (bytesRead != RawMessage.LENGTH) {
                    throw new EOFException();
                }
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                list.add(rawMessage);
            }
        } catch (EOFException e) {
            return list;
        }
    }

    /**
     * Méthode de démarrage de l'application JavaFX.
     *
     * @param primaryStage La scène principale de l'application.
     * @throws Exception si une exception est levée
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        long startTime = System.nanoTime();

        StatusLineController statusLineController = new StatusLineController();
        ObjectProperty<ObservableAircraftState> selectedAircraftStateProperty
                = new SimpleObjectProperty<>();
        ConcurrentLinkedDeque<Message> queue = new ConcurrentLinkedDeque<>();

        TileManager tileManager = new TileManager(TILE_CACHE_DIR, TILE_SERVER_URL);
        MapParameters mapParameters =
                new MapParameters(INITIAL_ZOOM_LEVEL, INITIAL_LATITUDE, INITIAL_LONGITUDE);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParameters);

        URL url = getClass().getResource("/aircraft.zip");
        assert url != null;
        Path path = Path.of(url.toURI());
        AircraftDatabase dataBase = new AircraftDatabase(path.toString());
        AircraftStateManager aircraftStateManager = new AircraftStateManager(dataBase);

        statusLineController.aircraftCountProperty()
                .bind(Bindings.size(aircraftStateManager.states()));

        AircraftController aircraftMapView = new AircraftController(mapParameters,
                aircraftStateManager.states(), selectedAircraftStateProperty);

        StackPane aircraftView = new StackPane(baseMapController.pane(), aircraftMapView.pane());

        AircraftTableController aircraftTable =
                new AircraftTableController(aircraftStateManager.states(),
                        selectedAircraftStateProperty);

        aircraftTable.setOnDoubleClick(s -> baseMapController.centerOn(s.getPosition()));

        BorderPane aircraftTablePane = new BorderPane(aircraftTable.pane());
        aircraftTablePane.setTop(statusLineController.pane());

        Thread thread = (getParameters().getRaw().isEmpty()) ? radioThread(queue) :
                fileThread(queue, startTime);

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
        aircraftAnimation(queue, aircraftStateManager, statusLineController).start();
    }

    /**
     * Crée un thread pour la réception des messages radio.
     *
     * @param queue La file d'attente concurrente dans laquelle ajouter les messages reçus.
     * @return Le thread créé pour la réception des messages radio.
     */
    private Thread radioThread(ConcurrentLinkedDeque<Message> queue) {
        return new Thread(() -> {
            getParameters().getRaw();
            try {
                AdsbDemodulator is = new AdsbDemodulator(System.in);
                RawMessage rawMessage = is.nextMessage();
                while (rawMessage != null) {
                    Message message = MessageParser.parse(rawMessage);
                    if (message != null) queue.add(message);

                    rawMessage = is.nextMessage();
                }
            } catch (IOException ioException) {
                throw new UncheckedIOException(ioException);
            }
        });
    }

    /**
     * Crée un thread pour la lecture des messages à partir d'un fichier.
     *
     * @param queue     la file d'attente concurrente dans laquelle ajouter les messages lus.
     * @param startTime le temps de démarrage du programme en nanosecondes.
     * @return le thread créé pour la lecture des messages.
     */
    private Thread fileThread(ConcurrentLinkedDeque<Message> queue, long startTime) {
        return new Thread(() -> {

            try {
                for (RawMessage rawMessage : readAllMessages(getParameters().getRaw().get(0))) {
                    long currentTime = System.nanoTime() - startTime;
                    if (currentTime < rawMessage.timeStampNs()) {
                        sleep((rawMessage.timeStampNs() - currentTime)
                                / FROM_NANO_TO_MILLISECOND);
                    }
                    Message message = MessageParser.parse(rawMessage);
                    if (message != null) {
                        queue.add(message);
                    }
                }
            } catch (IOException ioException) {
                throw new UncheckedIOException(ioException);
            } catch (InterruptedException interruptedException) {
                throw new Error(interruptedException);
            }
        });
    }

    /**
     * Crée un objet AnimationTimer pour l'animation de l'aéronef.
     *
     * @param queue                la file d'attente concurrente contenant les messages à traiter.
     * @param aircraftStateManager le gestionnaire d'état de l'aéronef.
     * @param statusLineController le contrôleur de la ligne d'état.
     * @return l'objet AnimationTimer pour l'animation de l'aéronef.
     */
    private AnimationTimer aircraftAnimation(ConcurrentLinkedDeque<Message> queue,
                                             AircraftStateManager aircraftStateManager,
                                             StatusLineController statusLineController) {
        return new AnimationTimer() {
            private long lastTimeStampNs = 0L;

            @Override
            public void handle(long now) {
                try {
                    while (!queue.isEmpty()) {
                        Message message = queue.remove();
                        aircraftStateManager.updateWithMessage(message);
                        statusLineController.messageCountProperty().set(
                                statusLineController.messageCountProperty().get() + 1);
                    }
                    if (now - lastTimeStampNs > PURGE_TIME) {
                        aircraftStateManager.purge();
                        lastTimeStampNs = now;
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
    }
}