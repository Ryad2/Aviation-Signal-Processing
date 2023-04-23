package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public final class BaseMapController {

    TileManager identiteTuile;
    MapParameters mapParameters;
    GraphicsContext graficsContext;
    private final boolean redrawNeeded=true;
    public BaseMapController(TileManager identiteTuile, MapParameters mapParameters) {
        this.identiteTuile = identiteTuile;
        Canvas canvas = new Canvas();
        this.graficsContext= canvas.getGraphicsContext2D();
        Pane pane = new Pane();
        pane.getChildren().add(canvas);
        canvas.widthProperty().bind(pane.widthProperty());

        this.mapParameters = mapParameters;
    }

    public Pane pane (){
        return null;
    }

    public void centerOn (GeoPos point){
    }


    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        // … à faire : dessin de la carte
    }



}