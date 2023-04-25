package ch.epfl.javions.gui;

import javafx.scene.layout.Pane;

public final class AircraftController {

    private Pane pane;
    public AircraftController() {

        this.pane = new Pane();
    }

    public Pane pane (){
        return pane;
    }
}
