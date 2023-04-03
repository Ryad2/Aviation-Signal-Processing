package ch.epfl.javions;

/**
 * Offre des méthodes permettant d'effectuer certains calculs mathématiques
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class Math2 {


    /**
     * Constructeur de Math2 lance une erreur si on essaye créer une instance
     * @throws AssertionError si la classe venait à être instancié
     */
    private Math2 () {
        throw new AssertionError("Classe non instanciable");
    }


    /**
     * limite la valeur v à l'intervalle allant de min à max
     * @param min le minimum
     * @param v la valeur en question
     * @param max la maximum
     * @return min si v est inférieur à min, max si v est supérieur à max et sinon lèvre une exception si
     * min est strictement supérieur à max
     */
    public static int clamp (int min, int v, int max)
    {
        Preconditions.checkArgument(min <= max);
        if (v < min) return min;
        else if (v > max) return max;
        else return v;
    }


    /**
     * Permet de calculer un sinus hyperbolique
     * @param x l'argument du sinus hyperbolique
     * @return le sinon hyperbolique réciproque de son argument
     */
    public static double asinh (double x) {
        return Math.log(x + Math.sqrt(1 + (x*x) ));
    } //TODO : PAS UTILISER
}
