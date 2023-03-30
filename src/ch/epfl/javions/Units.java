package ch.epfl.javions;

/**
 * Contient la définition des préfixes SI utiles au projet, des classes
 * imbriquées contenant les définitions des différentes unités, ainsi que des méthodes de conversion.
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class Units {

    private Units() {
            throw new AssertionError("Classe non instanciable");
        }

    /**
     * @param KILO défini le kilo
     * @param CENTI défini le centi
     */
        public static final double KILO = 1e3;
        public static final double CENTI = 1e-2;

    public static class Angle {

        private Angle() {}

        /**
         * Paramètre toutes les unités concernant les angles
         * RADIAN unité de base des angles
         * TURN le double de pi multiplié par le radian
         * DEGREE est turn divisé par 360
         * T32 est turn divisé par 2 exposant 32
         * MAXT32 est le nombre maximum que peut avoir l'unité T32
         * MINT32 est le nombre minimum que peut avoir l'unité T32
         */
            public static final double RADIAN = 1;
            public static final double TURN = 2*Math.PI*RADIAN;
            public static final double DEGREE = TURN/360;
            public static final double T32 = TURN/Math.scalb(1,32);
        }

        public static class Length {

            private Length() {}

            /**
             * Paramètre toutes les unités concernant les longueurs
             * METER est l'unité de base des longeurs
             * CENTIMETER c'est le mettre multiplié par 0.01
             * KILOMETER est le mettre multiplié par 1000
             * INCH = pouce est le centimètre multiplié par 2,54
             * FOOT = pied est le pouce multiplié par 12
             * NAUTICAL_MILE est le mètre multiplié par 1852
             */

            public static final double METER = 1;
            public static final double CENTIMETER = CENTI*METER;
            public static final double KILOMETER = KILO*METER;
            public static final double INCH = 2.54*CENTIMETER;
            public static final double FOOT = 12*INCH;
            public static final double NAUTICAL_MILE = 1852*METER;
        }

        public static class Time {
            public Time() {}

            /**
             * Paramètre toutes les unités concernant le temps
             * SECOND est l'unité de base du temps
             * MINUTE est les secondes multiplié par 60
             * HOUR est les minutes multiplié par 60
             */

            public static final double SECOND = 1;
            public static final double MINUTE = 60*SECOND;
            public static final double HOUR = 60*MINUTE;
        }

        public static class Speed {
            private Speed() {}

            /**
             * Paramètre toutes les unités concernant la vitesse
             * KNOT c'est les NAUTICAL_MILE devisé par les heures
             * KILOMETER_PER_HOUR c'est les kilomètres divisés par les heures
             */

            public static final double KNOT = Length.NAUTICAL_MILE/Time.HOUR;
            public static final double KILOMETER_PER_HOUR = Length.KILOMETER/Time.HOUR;
        }

    /**
     * Sert à convertir la valeur donnée, exprimée dans l'unité fromUnit, en l'unité toUnit
     * @param value est la valeur qui va être convertie
     * @param fromUnit est l'unité de départ
     * @param toUnit est l'unité d'arrivée
     * @return est la réponse de la convertion
     */
        public static double convert(double value, double fromUnit, double toUnit) {
            return (fromUnit/toUnit)*value;
        }

    /**
     * Sert à convertir lorsque l'unité d'arrivée est l'unité de base et vaut donc 1
     * @param value est la valeur qui va être convertie
     * @param fromUnit est l'unité de départ
     * @return est la réponse de la conversion
     */

    public static double convertFrom(double value, double fromUnit) {
            return value*fromUnit;
        }

    /**
     * Sert à convertir lorsque l'unité de départ est l'unité de base et vaut donc 1
     * @param value est la valeur qui va être convertie
     * @param toUnit est l'unité d'arrivée
     * @return est la réponse de la conversion
     */
        public static double convertTo(double value, double toUnit) {
            return value/toUnit;
        }
    }