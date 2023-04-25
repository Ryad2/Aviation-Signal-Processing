package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

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
    }


    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        // … à faire : dessin de la carte
    }


    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}