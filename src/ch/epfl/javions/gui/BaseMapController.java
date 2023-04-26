package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.awt.event.MouseEvent;

public final class BaseMapController {

    private final TileManager identiteTuile;
    private final MapParameters mapParameters;
    private final GraphicsContext graficsContext;
    private boolean redrawNeeded = true;
    private Pane pane;
    private final Canvas canvas;

    public BaseMapController(TileManager identiteTuile, MapParameters mapParameters) {

        this.identiteTuile = identiteTuile;
        this.canvas = new Canvas();

        this.graficsContext= canvas.getGraphicsContext2D();
        this.pane = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());//todo mettre en methode prv
        this.mapParameters = mapParameters;



        //LISNNERSSS
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
            //TODO : mettre en methode prv avec tout les autre lisner
        });

        mapParameters.minXProperty().addListener(c->{ redrawOnNextPulse(); });

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            mapParameters.scroll(e.getX(), e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-e.getX(), -e.getY());
            // … à faire : appeler les méthodes de MapParameters
        });

        ObjectProperty<Point2D> previousPosition= new SimpleObjectProperty<>();
        pane.setOnMousePressed(e->{
            previousPosition.set(new Point2D (e.getX(),e.getY()));
        });
        pane.setOnMouseDragged(e->{
            Point2D currentPosition = new Point2D(e.getX(),e.getY());
            mapParameters.scroll(currentPosition.getX()-previousPosition.get().getX(),currentPosition.getY()-previousPosition.get().getY());
            previousPosition.set(currentPosition);
        });
        pane.setOnMouseReleased(e->{
            previousPosition.set(null);
        });
    }

    public Pane pane (){
        return pane;
    }


    public void centerOn (GeoPos point){
         double newMinX = WebMercator.x(mapParameters.getZoom(),point.longitude()) - 0.5 * canvas.getWidth();
         double newMinY = WebMercator.y(mapParameters.getZoom(),point.latitude()) - 0.5 * canvas.getHeight();
         mapParameters.scroll(newMinX, newMinY);
        //todo mettre dans mapParameters
    }


    private void redrawIfNeeded() {

        if (!redrawNeeded) return;
        redrawNeeded = false;

        int NUMBER_OF_PIXEL = 256;
        int smallerXTile = ((int)mapParameters.getminX())/ NUMBER_OF_PIXEL;
        int smallerYTile = ((int) mapParameters.getminY())/ NUMBER_OF_PIXEL;
        int greatestXTile = (int)(mapParameters.getminX() + pane.widthProperty().get())/ NUMBER_OF_PIXEL;
        int greatestYTile = (int)(mapParameters.getminY() + pane.heightProperty().get())/ NUMBER_OF_PIXEL;

        for(int y = smallerYTile; y<= greatestYTile; y++){
            for(int x = smallerXTile; x <= greatestXTile; x ++){

                try{
                    //todo demander si toute les attribut devrait etre final
                    javafx.scene.image.Image image = identiteTuile.imageForTileAt(new TileManager.TileID(mapParameters.getZoom(), x, y));//toDO enlever cette attribut
                    graficsContext.drawImage(image, mapParameters.getminX(), mapParameters.getminY());
                }
                catch (Exception e){}
            }

        }
    }


    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}