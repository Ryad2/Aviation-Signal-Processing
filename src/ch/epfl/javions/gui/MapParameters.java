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

    public MapParameters(int zoom, double minX, double minY){
        Preconditions.checkArgument(zoom>=MIN_ZOOM && zoom<=MAX_ZOOM);
        this.minXProperty = new SimpleDoubleProperty(WebMercator.x(zoom,minX)); //TODO : sauf-il faire ça?
        this.minYProperty = new SimpleDoubleProperty(WebMercator.y(zoom,minY));
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
       zoom.set(Math2.clamp(MIN_ZOOM, zoomDifference + getZoom(), MAX_ZOOM));
       minXProperty.set(Math.scalb(getminX(), zoomDifference));
       minYProperty.set(Math.scalb(getminY(), zoomDifference));
    }
}