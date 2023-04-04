package ch.epfl.javions;

/**
 * Contient la définition des préfixes SI utiles au projet, des classes
 * imbriquées contenant les définitions des différentes unités, ainsi que des méthodes de conversion.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class Units {

    /**
     * KILO est le nombre de mètres dans un kilomètre
     */
    public static final double KILO = 1e3;
    /**
     * CENTI est le nombre de mètres dans un centimètre
     */
    public static final double CENTI = 1e-2;


    /**
     * Constructeur de Units lance une erreur si on essaye créer une instance
     *
     * @throws AssertionError si la classe venait à être instancié
     */

    private Units() {
        throw new AssertionError("Classe non instanciable");
    }

    /**
     * Sert à convertir la valeur donnée, exprimée dans l'unité fromUnit, en l'unité toUnit
     *
     * @param value    est la valeur qui va être convertie
     * @param fromUnit est l'unité de départ
     * @param toUnit   est l'unité d'arrivée
     * @return la réponse de la convertion
     */
    public static double convert(double value, double fromUnit, double toUnit) {
        return (fromUnit / toUnit) * value;
    }

    /**
     * Sert à convertir lorsque l'unité d'arrivée est l'unité de base et vaut donc 1
     *
     * @param value    est la valeur qui va être convertie
     * @param fromUnit est l'unité de départ
     * @return la réponse de la conversion
     */
    public static double convertFrom(double value, double fromUnit) {
        return value * fromUnit;
    }

    /**
     * Sert à convertir lorsque l'unité de départ est l'unité de base et vaut donc 1
     *
     * @param value  est la valeur qui va être convertie
     * @param toUnit est l'unité d'arrivée
     * @return la réponse de la conversion
     */
    public static double convertTo(double value, double toUnit) {
        return value / toUnit;
    }

    /**
     * Paramètre toutes les unités concernant les angles
     */
    public static class Angle {


        /**
         * RADIAN est l'unité de base des angles
         */
        public static final double RADIAN = 1;
        /**
         * TURN est le double de pi multiplié par le radian
         */
        public static final double TURN = 2 * Math.PI * RADIAN;
        /**
         * DEGREES_IN_A_TURN est le nombre de degrés dans un tour
         */
        private static final int DEGREES_IN_A_TURN = 360;
        /**
         * DEGREE est turn divisé par 360
         */
        public static final double DEGREE = TURN / DEGREES_IN_A_TURN;
        /**
         * SCALE_FACTOR est le nombre de T32 dans un turn
         */
        private static final int SCALE_FACTOR = 32;
        /**
         * T32 est turn divisé par 2 exposant 32
         */
        public static final double T32 = TURN / Math.scalb(1, SCALE_FACTOR);


        private Angle() {
        }
    }

    /**
     * Paramètre toutes les unités concernant les longueurs
     */
    public static class Length {

        /**
         * METER est l'unité de base des longeurs
         */
        public static final double METER = 1;
        /**
         * CENTIMETER c'est le mettre multiplié par 0.01
         */
        public static final double CENTIMETER = CENTI * METER;
        /**
         * INCH est le centimètre multiplié par 2.54
         */
        public static final double INCH = 2.54 * CENTIMETER;
        /**
         * FOOT est le pouce multiplié par 12
         */
        public static final double FOOT = 12 * INCH;
        /**
         * KILOMETER est le mettre multiplié par 1000
         */
        public static final double KILOMETER = KILO * METER;
        /**
         * NAUTICAL_MILE est le mètre multiplié par 1852
         */
        public static final double NAUTICAL_MILE = 1852 * METER;


        private Length() {
        }
    }

    /**
     * Paramètre toutes les unités concernant les masses
     */
    public static class Time {
        /**
         * SECOND est l'unité de base du temps
         */
        public static final double SECOND = 1;
        /**
         * TIME_MULTIPLICATION_FACTOR est le nombre de secondes dans une minute et le nombre de minute dans une
         * heure
         */
        private static final int TIME_MULTIPLICATION_FACTOR = 60;
        /**
         * MINUTE est les secondes multiplié par 60
         */
        public static final double MINUTE = TIME_MULTIPLICATION_FACTOR * SECOND;
        /**
         * HOUR est les minutes multiplié par 60
         */
        public static final double HOUR = TIME_MULTIPLICATION_FACTOR * MINUTE;


        public Time() {
        }
    }

    /**
     * Paramètre toutes les unités concernant la vitesse
     */
    public static class Speed {
        /**
         * KNOT est les NAUTICAL_MILE devisé par les heures
         */
        public static final double KNOT = Length.NAUTICAL_MILE / Time.HOUR;
        /**
         * KILOMETER_PER_HOUR c'est les kilomètres divisés par les heures
         */
        public static final double KILOMETER_PER_HOUR = Length.KILOMETER / Time.HOUR;


        private Speed() {
        }
    }
}