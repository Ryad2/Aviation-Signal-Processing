package ch.epfl.javions;

/**
 * Permet de projeter des coordonnées géographiques selon la projection WebMercator
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class WebMercator {

    private static final int EXPOSANT = 8;
    private static final double OFFSET = 0.5;

    /**
     * Constructeur de WebMercator qui n'est pas instantiable
     */
    private WebMercator() {
    }

    /**
     * Calcul des coordonnées WebMercator pour la longitude
     *
     * @param zoomLevel le niveau de zoom
     * @param longitude la longitude
     * @return la coordonnée x correspondant à la longitude donnée (en radian) au niveau de zoom
     * donné
     */
    public static double x(int zoomLevel, double longitude) {
        return coordinateOf(longitude, zoomLevel);
    }

    /**
     * Calcul des coordonnées WebMercator pour la latitude
     *
     * @param zoomLevel le niveau de zoom
     * @param latitude  la latitude
     * @return la coordonnée y correspondant à la latitude donnée (en radians) au niveau de zoom
     * donné
     */
    public static double y(int zoomLevel, double latitude) {
        return coordinateOf(-Math2.asinh(Math.tan(latitude)), zoomLevel);
    }

    private static double coordinateOf(double value, int zoomLevel) {
        return Math.scalb(Units.convertTo(value, Units.Angle.TURN) + OFFSET, EXPOSANT + zoomLevel);
    }
}