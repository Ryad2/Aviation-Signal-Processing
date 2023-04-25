package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.PrintStream;

public final class BaseMapController {

    private final TileManager identiteTuile;
    private final MapParameters mapParameters;
    private final GraphicsContext graficsContext;
    private boolean redrawNeeded = true;

    private javafx.scene.image.Image image;
    public BaseMapController(TileManager identiteTuile, MapParameters mapParameters) {
        this.identiteTuile = identiteTuile;
        Canvas canvas = new Canvas();
        this.graficsContext= canvas.getGraphicsContext2D();
        Pane pane = new Pane();
        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());

        this.mapParameters = mapParameters;

        //redrawIfNeeded();

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded); //TODO : est-ce que c'est tout bon?
        });
    }

    public Pane pane (){
        return null;
    }


    public void centerOn (GeoPos point){
         double newMinX = WebMercator.x(mapParameters.getZoom(),point.longitude()) - 0.5 * widthVisiblePart;
         double newMinY = WebMercator.y(mapParameters.getZoom(),point.latitude()) - 0.5 * heightVisiblePart;
        mapParameters.scroll(newMinX, newMinY);
        //todo mettre dans mapParameters
    }


    private void redrawIfNeeded() {

        if (!redrawNeeded) return;
        redrawNeeded = false;

        try{
            this.image = identiteTuile.imageForTileAt(new TileManager.TileID(mapParameters.getZoom(),
                    (int) mapParameters.getminX()/NUMBER_OF_PIXEL , (int) mapParameters.getminY()/NUMBER_OF_PIXEL));
            //TODO : c'est correct?
        }
        catch (Exception e){}//todo vérifier la gestion d'exception

        graficsContext.drawImage(image, mapParameters.getminX(), mapParameters.getminY()); //TODO : vérifier assistant

        // … à faire : dessin de la carte
    }


    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}