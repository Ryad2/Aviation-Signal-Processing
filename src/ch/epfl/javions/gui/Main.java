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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static ch.epfl.javions.gui.StatusLineController.aircraftCountProperty;
import static com.sun.javafx.scene.control.skin.Utils.getResource;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Main extends Application {

    private static final int INITIAL_ZOOM_LEVEL = 8;
    private static final double INITIAL_LATITUDE = 33_530;
    private static final double INITIAL_LONGITUDE = 23_070;
    private static final String TILE_SERVER_URL = "https://tile.openstreetmap.org/";
    private static final Path TILE_CACHE_DIR = Path.of("tile-cache");

    @Override
    public void start(Stage primaryStage) throws Exception {

        ObjectProperty<ObservableAircraftState> selectedAircraftStateProperty = new SimpleObjectProperty<>();
        ConcurrentLinkedDeque<Message> queue = new ConcurrentLinkedDeque<>();
        TileManager tileManager = new TileManager(TILE_CACHE_DIR, TILE_SERVER_URL);
        MapParameters mapParameters = new MapParameters(INITIAL_ZOOM_LEVEL, INITIAL_LATITUDE, INITIAL_LONGITUDE);
        BaseMapController baseMapController = new BaseMapController(tileManager, mapParameters);

        URL url = getClass().getResource("/aircraft.zip");
        assert url != null;
        Path path = Path.of(url.toURI());
        AircraftDatabase dataBase = new AircraftDatabase(path.toString());

        AircraftStateManager asm = new AircraftStateManager(dataBase);

        StatusLineController statusLineController = new StatusLineController();
        statusLineController.aircraftCountProperty().bind(Bindings.size(asm.states()));

        Text aircraftCountText = new Text("Aéronefs visibles : " + aircraftCountProperty());
        aircraftCountText.setFont(Font.font("Arial", 12));

        Text messageCountText = new Text("Messages reçus : " + StatusLineController.messageCountProperty());
        messageCountText.setFont(Font.font("Arial", 12));

        VBox statusLine = new VBox(new TextFlow(aircraftCountText), new TextFlow(messageCountText));

        AircraftController aircraftMapView = new AircraftController(mapParameters, asm.states(), selectedAircraftStateProperty);
        StackPane aircraftView = new StackPane(baseMapController.pane(), aircraftMapView.pane());

        AircraftTableController aircraftTable = new AircraftTableController(asm.states(), selectedAircraftStateProperty);
        BorderPane aircraftTablePane = new BorderPane(aircraftTable.pane(), statusLineController.pane(), null, null, null);

        Thread thread;

        if(getParameters().getRaw().isEmpty()) {//ToDo mettre tout ça en prv
        thread = new Thread(() -> {
            //getParameters().getRaw().get(0);
            try  {
                var is = new AdsbDemodulator(System.in);
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
                           for(var el : readAllMessages(getParameters().getRaw().get(0)) ) {
                           if(System.nanoTime() < el.timeStampNs())
                               sleep(el.timeStampNs() - System.nanoTime());
                            Message message = MessageParser.parse(el);
                            if(message != null) queue.add(message);
                        }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                catch (InterruptedException e) {
                           e.printStackTrace();
                       }
            });
        }
        thread.start();


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


        //Iterator <RawMessage> mi = readAllMessages().iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(queue.isEmpty()) return;
                try {
                    //if (m != null) TODO : mettre ça en place
                    Message m = queue.poll();
                    asm.updateWithMessage(m);
                    statusLineController.messageCountProperty().set(statusLineController.messageCountProperty().get() + 1);
                }
                catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }

//todo : centrer la carte sur l'avion selectionnée
    public static void main(String[] args) {launch(args);}

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