package ch.epfl.javions;

/**
 * Offre des méthodes permettant d'effectuer certains calculs mathématiques
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class Math2 {


    /**
     * Constructeur de Math2 qui n'est pas instantiable
     */
    private Math2() {}


    /**
     * Limite la valeur v à l'intervalle allant de min à max
     *
     * @param min le minimum
     * @param v   la valeur en question
     * @param max la maximum
     * @return min si v est inférieur à min, max si v est supérieur à max et sinon lèvre une exception si
     * min est strictement supérieur à max
     */
    public static int clamp(int min, int v, int max) {

        Preconditions.checkArgument(min <= max);
        if (v < min) return min;
        else return Math.min(v, max);
    }


    /**
     * Permet de calculer un sinus hyperbolique
     *
     * @param x l'argument du sinus hyperbolique
     * @return le sinon hyperbolique réciproque de son argument
     */
    public static double asinh(double x) {
        return Math.log(x + Math.sqrt(1 + x * x));
    }
}
