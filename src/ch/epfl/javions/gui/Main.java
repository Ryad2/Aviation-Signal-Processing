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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static java.lang.Thread.sleep;

public final class Main extends Application {

    //TODO : est-ce qu'un clic dans la table dans mettre la ligne de la table tout en haut?

    private static final int INITIAL_ZOOM_LEVEL = 8;
    private static final double INITIAL_LATITUDE = 33_530;
    private static final double INITIAL_LONGITUDE = 23_070;
    private static final String TILE_SERVER_URL = "https://tile.openstreetmap.org/";
    private static final Path TILE_CACHE_DIR = Path.of("tile-cache");
    private static final long PURGE_TIME = 1_000_000_000L;

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
            getParameters().getRaw().get(0);
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


                //try (InputStream is = new BufferedInputStream(new FileInputStream(getParameters().getRaw().get(0)))) {
                       try {
                           for(RawMessage el : readAllMessages(getParameters().getRaw().get(0))) {
                               long currentTime = System.nanoTime() - startTime;
                               if(currentTime < el.timeStampNs()) {
                                   sleep((el.timeStampNs() - currentTime) / 1_000_000);
                               }
                               Message message = MessageParser.parse(el);
                               if(message != null) {
                                   queue.add(message);
                               }
                        }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                catch (InterruptedException e) {
                           e.printStackTrace();
                       }
            });
        }
        thread.setDaemon(true);
        thread.start();

        SplitPane splitPane = new SplitPane(aircraftView, aircraftTablePane);
        splitPane.setOrientation(Orientation.VERTICAL);

        BorderPane root = new BorderPane(splitPane);
        Scene scene = new Scene(root,800, 600);

        primaryStage.setTitle("Javions");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();




        //Iterator <RawMessage> mi = readAllMessages().iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            private long lastTimeStampNs = 0L;
            @Override
            public void handle(long now) {
                try {
                    while (!queue.isEmpty()) {
                        Message m = queue.remove();
                        aircraftStateManager.updateWithMessage(m);
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
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                l.add(rawMessage);
            }
        } catch (EOFException e){
            return l;
        }
    }
}


/*private static List<RawMessage> readAllMessages(){
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
                //should not
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messageList;
        }*/