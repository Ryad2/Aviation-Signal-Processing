package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

public final class BaseMapController {

    private final TileManager identiteTuile;
    private final MapParameters mapParameters;
    private boolean redrawNeeded = true;
    private final Pane pane;
    private final Canvas canvas;

    public BaseMapController(TileManager identiteTuile, MapParameters mapParameters) {

        this.identiteTuile = identiteTuile;
        this.canvas = new Canvas();

        this.pane = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());//todo mettre en methode prv
        this.mapParameters = mapParameters;


        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
            //TODO : mettre en methode prv avec tout les autre lisner!
        });

        mapParameters.minXProperty().addListener(c-> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(c-> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(c->redrawOnNextPulse());
        pane.widthProperty().addListener(c->redrawOnNextPulse());
        pane.heightProperty().addListener(c->redrawOnNextPulse());


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

        ObjectProperty<Point2D> previousPosition= new SimpleObjectProperty<>();
        pane.setOnMousePressed(e->{
            previousPosition.set(new Point2D (e.getX(),e.getY()));
        });

        pane.setOnMouseDragged(e->{
            Point2D currentPosition = new Point2D(e.getX(),e.getY());
            mapParameters.scroll(-(currentPosition.getX() - previousPosition.get().getX()),
                    -(currentPosition.getY() - previousPosition.get().getY()));
            previousPosition.set(currentPosition);
        });

        pane.setOnMouseReleased(e->{
            previousPosition.set(null);
        });
    }

    public Pane pane (){
        return pane;
    }


    public void centerOn (GeoPos point) {
         double newMinX = WebMercator.x(mapParameters.getZoom(), point.longitude()) - 0.5 * canvas.getWidth();
         double newMinY = WebMercator.y(mapParameters.getZoom(), point.latitude()) - 0.5 * canvas.getHeight();
         mapParameters.scroll(newMinX, newMinY);
        //todo mettre dans mapParameters
    }


    private void redrawIfNeeded() {

        if (!redrawNeeded) return;
        redrawNeeded = false;

        GraphicsContext graficsContext = canvas.getGraphicsContext2D();
        graficsContext.clearRect(0,0,canvas.getWidth(),canvas.getHeight());

        int NUMBER_OF_PIXEL = 256;

        int smallerXTile = ((int)mapParameters.getminX())/ NUMBER_OF_PIXEL;
        int smallerYTile = ((int) mapParameters.getminY())/ NUMBER_OF_PIXEL;
        int greatestXTile = ((int)(mapParameters.getminX() + canvas.widthProperty().get()))/ NUMBER_OF_PIXEL;
        int greatestYTile = ((int)(mapParameters.getminY() + canvas.heightProperty().get()))/ NUMBER_OF_PIXEL;


        for(int y = smallerYTile; y <= greatestYTile; y++){
            for(int x = smallerXTile; x <= greatestXTile; x++){
                try {
                    //todo demander si toute les attribut devrait etre final
                    System.out.println("x : " + x + " y : " + y);
                    Image image = identiteTuile.imageForTileAt(new TileManager.TileID(mapParameters.getZoom(), x, y));
                    graficsContext.drawImage(image,
                            x * NUMBER_OF_PIXEL - mapParameters.getminX(),
                            y * NUMBER_OF_PIXEL - mapParameters.getminY());
                }
                catch (Exception e){
                }
            }
        }
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}