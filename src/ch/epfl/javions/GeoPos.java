package ch.epfl.javions;

/**
 * Représente des coordonnées géographiques : un couple longitude/latitude. Ces données sont exprimées
 * en t32 et stockées sous la forme d'entiers de 32 bits (type int)
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

/**
 * Lance une exception si la latitude et la longitude n'est pas exprimée en T32
 * @param longitudeT32 la longitude exprimée en T32
 * @param latitudeT32 la latitude exprimée en T32
 * @throws IllegalArgumentException si le paramètre n'est pas compris entre -2^30 (inclus, et qui correspond à -90°) et
 * 2^30 (inclus, et qui correspond à +90°)
 * @throws IllegalArgumentException si le paramètre n'est pas une longitude valide
 */

public record GeoPos (int longitudeT32, int latitudeT32) {

    public static final int MAXT32 = (1 << 30);
    public static final int MINT32 = -MAXT32;

    public GeoPos {
        Preconditions.checkArgument(isValidLatitudeT32(latitudeT32));
    }

    /**
     * Permet de vérifier si la latitude est valide
     * @param latitudeT32 paramètre donné
     * @return vrai ssi la valeur passée, interprétée comme une latitude exprimée en t32, est valide : comprise
     * entre -2^30 et 2^30 inclus
     */

    public static boolean isValidLatitudeT32(int latitudeT32) {

        return (latitudeT32 >= MINT32) && (latitudeT32 <= MAXT32);
    }

    /**
     * converti la longitude de T32 à radian
     * @return la longitude en radian
     */
    public double longitude () {
        return Units.convertFrom(longitudeT32, Units.Angle.T32);
    }

    /**
     * converti la latitude de T32 à radian
     * @return la latitude en radian
     */
    public double latitude () {
        return Units.convertFrom(latitudeT32, Units.Angle.T32);
    }

    /**
     * Convertir des données textuellement
     * @return une représentation textuelle de la position dans laquelle la longitude et la latitude sont données
     * dans cet ordre, en degrés
     */
    @Override
    public String toString () {
        return "("+ Units.convert(longitudeT32, Units.Angle.T32, Units.Angle.DEGREE) + "°, " +
                Units.convert(latitudeT32, Units.Angle.T32, Units.Angle.DEGREE)+"°)";
    }
}
