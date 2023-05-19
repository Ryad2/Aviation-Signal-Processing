package ch.epfl.javions.gui;

import ch.epfl.javions.Math2;
import ch.epfl.javions.Preconditions;
import javafx.beans.property.*;


/**
 * Cette classe gère l'affichage ainsi que l'interaction avec le fond de carte.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class MapParameters {
    private final IntegerProperty zoom;
    private final DoubleProperty minXProperty;
    private final DoubleProperty minYProperty;
    private static final int MAX_ZOOM = 19;
    private static final int MIN_ZOOM = 6;

    /**
     * Constructeur de la classe MapParameters qui initialise minXProperty, minYProperty et zoom
     * comme des propriétés au sens de JavaFX
     *
     * @param zoom le niveau de zoom
     * @param minX le point en haut à gauche de la fenêtre sur l'axe X
     * @param minY le point en haut à gauche de la fenêtre sur l'axe Y
     * @throws IllegalArgumentException si le zoom n'est pas compris entre 6 et 19
     */
    public MapParameters(int zoom, double minX, double minY) {
        Preconditions.checkArgument(zoom >= MIN_ZOOM && zoom <= MAX_ZOOM);
        this.minXProperty = new SimpleDoubleProperty(minX);
        this.minYProperty = new SimpleDoubleProperty(minY);
        this.zoom = new SimpleIntegerProperty(zoom);
    }

    /**
     * Méthode privée qui permet d'accéder à la valeur du zoom en tant que propriété en sens de
     * JavaFX
     *
     * @return le niveau de zoom
     */
    public ReadOnlyIntegerProperty zoomProperty(){
        return zoom ;
    }

    /**
     * Méthode privée qui permet d'accéder à la valeur du zoom
     *
     * @return le niveau de zoom
     */
    public int getZoom(){
        return zoom.get();
    }

    /**
     * Méthode privée qui permet d'accéder à la valeur du coin en haut à gauche de la fenêtre sur
     * l'axe X en tant que propriété en sens de JavaFX
     *
     * @return le coin en haut à gauche de la fenêtre sur l'axe X
     */
    public ReadOnlyDoubleProperty minXProperty(){
        return minXProperty;
    }

    /**
     * Méthode privée qui permet d'accéder à la valeur du coin en haut à gauche de la fenêtre sur
     * l'axe X
     *
     * @return la valeur du coin en haut à gauche de la fenêtre sur l'axe X
     */
    public double getminX(){
        return minXProperty.get();
    }

    /**
     * Méthode privée qui permet d'accéder à la valeur du coin en haut à gauche de la fenêtre sur
     * l'axe Y en tant que propriété en sens de JavaFX
     *
     * @return le coin en haut à gauche de la fenêtre sur l'axe Y
     */
    public ReadOnlyDoubleProperty minYProperty(){
        return minYProperty;
    }

    /**
     * Méthode privée qui permet d'accéder à la valeur du coin en haut à gauche de la fenêtre sur
     * l'axe Y
     *
     * @return la valeur du coin en haut à gauche de la fenêtre sur l'axe Y
     */
    public double getminY(){
        return minYProperty.get();
    }

    /**
     * Méthode qui permet de déplacer la fenêtre en fonction de la valeur de X et Y.
     *
     * @param x la valeur de déplacement sur l'axe X
     * @param y la valeur de déplacement sur l'axe Y
     */
    public void scroll(double x, double y){
        minXProperty.set(getminX() + x);
        minYProperty.set(getminY() + y);
    }

    /**
     * Méthode qui permet de changer le niveau de zoom
     *
     * @param zoomDifference la différence de zoom
     */
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