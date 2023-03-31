package ch.epfl.javions;

/**
 * Permet de projeter des coordonées géographiques selon la projection WebMercator
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class WebMercator {

    /**
     * @throws AssertionError si on essaye d'instancier la classe
     */
    private WebMercator() {
        throw new AssertionError("Classe non instanciable");
    }

    /**
     * Calcul des coordonnées
     * @param zoomLevel le niveau de zoom
     * @param longitude la longitude
     * @return la coordonées x correspondant à la longitude donnée (en radian) au niveau de zoom donné
     */
    public static double x(int zoomLevel, double longitude) {
        return Math.scalb(1, 8 + zoomLevel) * (Units.convertTo(longitude,Units.Angle.TURN) +0.5);
    }

    /**
     * Calcul des coordonnées
     * @param zoomLevel le niveau de zoom
     * @param latitude la latitude
     * @return la coordonnée y correspondant à la latitude donnée (en radians) au niveau de zoom donné
     */

    public static double y(int zoomLevel, double latitude) {
        return Math.scalb(1, 8+zoomLevel)*((Units.convertTo(-Math2.asinh(Math.tan(latitude)),Units.Angle.TURN))+0.5);
        }
}