package ch.epfl.javions;

/**
 * Permet de projeter des coordonées géographiques selon la projection WebMercator
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class WebMercator {


    /**
     * Constructeur de WebMercator qui n'est pas instantiable
     */
    private WebMercator() {}


    /**
     * Calcul des coordonnées WebMercator pour la longitude
     *
     * @param zoomLevel le niveau de zoom
     * @param longitude la longitude
     * @return la coordonées x correspondant à la longitude donnée (en radian) au niveau de zoom donné
     */
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(Units.convertTo(longitude, Units.Angle.TURN) + 0.5, 8 + zoomLevel);
    }


    /**
     * Calcul des coordonnées WebMercator pour la latitude
     *
     * @param zoomLevel le niveau de zoom
     * @param latitude  la latitude
     * @return la coordonnée y correspondant à la latitude donnée (en radians) au niveau de zoom donné
     */
    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(Units.convertTo(-Math2.asinh(Math.tan(latitude)),
                     Units.Angle.TURN) + 0.5, 8 + zoomLevel);
    }
}