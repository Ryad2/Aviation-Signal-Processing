package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import static ch.epfl.javions.gui.TileManager.NUMBER_OF_PIXEL;


/**
 * Cette classe gère la base de donnée afin de dessiner
 * la carte et de gérer les manipulations sur la carte.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class BaseMapController {
    private final TileManager tileId;
    private final MapParameters mapParameters;
    private final Pane pane;
    private final Canvas canvas;
    private boolean redrawNeeded = true;

    /**
     * Constucteur de la class BaseMapController
     *
     * @param tileId        représente l'identité de la tuile (coordonnées de la tuile)
     * @param mapParameters représente les paramètres de la carte
     */
    public BaseMapController(TileManager tileId, MapParameters mapParameters) {
        this.tileId = tileId;
        this.canvas = new Canvas();
        this.mapParameters = mapParameters;
        this.pane = new Pane(canvas);

        bindings();
        listeners();
        handlers();
    }

    public Pane pane() {
        return pane;
    }


    public void centerOn(GeoPos point) {
        double newMinX = WebMercator.x(mapParameters.getZoom(),
                point.longitude()) - 0.5 * canvas.getWidth() - mapParameters.getminX();
        double newMinY = WebMercator.y(mapParameters.getZoom(),
                point.latitude()) - 0.5 * canvas.getHeight() - mapParameters.getminY();
        mapParameters.scroll(newMinX, newMinY);
    }

    private void bindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }


    private void listeners() {
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
        mapParameters.minXProperty().addListener(c -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(c -> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(c -> redrawOnNextPulse());
        pane.widthProperty().addListener(c -> redrawOnNextPulse());
        pane.heightProperty().addListener(c -> redrawOnNextPulse());
    }

    private void handlers() {
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            double x = e.getX();
            double y = e.getY();

            mapParameters.scroll(x, y);
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-x, -y);

        });

        ObjectProperty<Point2D> previousPosition = new SimpleObjectProperty<>();
        pane.setOnMousePressed(e -> previousPosition.set(new Point2D(e.getX(), e.getY())));

        pane.setOnMouseDragged(e -> {
            Point2D currentPosition = new Point2D(e.getX(), e.getY());
            mapParameters.scroll(-(currentPosition.getX() - previousPosition.get().getX()),
                    -(currentPosition.getY() - previousPosition.get().getY()));
            previousPosition.set(currentPosition);
        });

        pane.setOnMouseReleased(e -> previousPosition.set(null));
    }


    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());


        int smallerXTile = ((int) mapParameters.getminX()) / TileManager.NUMBER_OF_PIXEL;
        int smallerYTile = ((int) mapParameters.getminY()) / TileManager.NUMBER_OF_PIXEL;
        int greatestXTile = ((int) (mapParameters.getminX()
                + canvas.widthProperty().get())) / TileManager.NUMBER_OF_PIXEL;

        int greatestYTile = ((int) (mapParameters.getminY()
                + canvas.heightProperty().get())) / TileManager.NUMBER_OF_PIXEL;

        for (int x = smallerXTile; x <= greatestXTile; x++) {
            for (int y = smallerYTile; y <= greatestYTile; y++) {
                try {
                    Image image = tileId.imageForTileAt(new TileManager.TileID(mapParameters.getZoom(), x, y));
                    graphicsContext.drawImage(image,
                            x * NUMBER_OF_PIXEL - mapParameters.getminX(),
                            y * NUMBER_OF_PIXEL - mapParameters.getminY());
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}