package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;

public final class MapParameters {
    private final IntegerProperty zoom;
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;

    public MapParameters(int zoom, double minX, double minY){
        Preconditions.checkArgument(zoom>=6 && zoom<=19);
        this.minXProperty =new SimpleDoubleProperty(minX);
        this.minYProperty =new SimpleDoubleProperty(minY);
        this.zoom=new SimpleIntegerProperty(zoom);
    }

    public ReadOnlyIntegerProperty zoomProperty(){
        return zoom ;
    }
    public int getZoom(){
        return zoom.get();
    }

    public ReadOnlyDoubleProperty minXProperty(){
        return minXProperty;
    }
    public double getminX(){
        return minXProperty.get();
    }

    public ReadOnlyDoubleProperty minYProperty(){
        return minYProperty;
    }
    public double getminY(){
        return minYProperty.get();
    }


    public void scroll(double x, double y){
        minXProperty.set(getminX()+x); //truc de prendre le max max;
        minYProperty.set(getminY()+y);
    }


    public void changeZoomLevel(int zoomDifference){
       // Math2.clamp
        //getZoom()+=zoomDifference;
    }
}
