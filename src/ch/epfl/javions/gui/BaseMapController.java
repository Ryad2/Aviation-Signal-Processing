package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public final class BaseMapController {

    private final TileManager identiteTuile;
    private final MapParameters mapParameters;
    private final GraphicsContext graficsContext;
    private boolean redrawNeeded = true;
    private Pane pane;
    private final double widthVisiblePart = pane.widthProperty().get();
    private final double heightVisiblePart = pane.heightProperty().get();


    public BaseMapController(TileManager identiteTuile, MapParameters mapParameters) {

        this.identiteTuile = identiteTuile;
        Canvas canvas = new Canvas();
        this.graficsContext= canvas.getGraphicsContext2D();
        this.pane = new Pane(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());//todo mettre en methode prv
        this.mapParameters = mapParameters;

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
            //TODO : mettre en methode prv avec tout les autre lisner
        });

        mapParameters.minXProperty().addListener(c->{ redrawOnNextPulse(); });
    }

    public Pane pane (){
        return pane;
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