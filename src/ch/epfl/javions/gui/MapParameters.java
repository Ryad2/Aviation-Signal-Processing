package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.WebMercator;
import javafx.beans.property.*;

public final class MapParameters {
    private final IntegerProperty zoom;
    private final int MAX_ZOOM = 19;
    private final int MIN_ZOOM = 6;
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;

    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(zoom >= MIN_ZOOM && zoom <= MAX_ZOOM);
        this.minXProperty = new SimpleDoubleProperty(minX);
        this.minYProperty = new SimpleDoubleProperty(minY);
        this.zoom = new SimpleIntegerProperty(zoom);
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
        minXProperty.set(getminX()+x);
        minYProperty.set(getminY()+y);
    }

    public void changeZoomLevel(int zoomDifference){
        int previousZoom = zoom.get();
        zoom.set(Math2.clamp(MIN_ZOOM, zoomDifference + getZoom(), MAX_ZOOM));
        int actualZoom = zoom.get();
        if (previousZoom != actualZoom) {
            minXProperty.set(Math.scalb(getminX(), zoomDifference));
            minYProperty.set(Math.scalb(getminY(), zoomDifference));
        }
    }
}
