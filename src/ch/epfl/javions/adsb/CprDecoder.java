package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;

import static ch.epfl.javions.Units.convert;
import static ch.epfl.javions.Units.convertFrom;


/**
 * Représente un décodeur de position CPR
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public class CprDecoder {

    private static final double EVEN_LATITUDE_NUMBER_ZONE = 60;
    private static final double EVEN_LATITUDE_LENGTH = 1d / EVEN_LATITUDE_NUMBER_ZONE;
    private static final double ODD_LATITUDE_NUMBER_ZONE = 59;

    /**
     * Constructeur de CprDecoder qui n'est pas instantiable
     */
    private CprDecoder() {
    }

    /**
     * Décode la position d'un aéronef à partir de deux messages CPR
     *
     * @param x0         la longitude du message pair
     * @param y0         la latitude du message pair
     * @param x1         la longitude du message impair
     * @param y1         la latitude du message impair
     * @param mostRecent 0 si le message impair est le plus récent, 1 si le message pair est le plus récent
     * @return la position de l'aéronef ou null si la latitude de la position décodée n'est pas valide ou si la position
     * ne peut pas être déterminée en raison d'un changement de bande de latitude
     * @throws IllegalArgumentException si mostRecent n'est pas 0 ou 1
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {

        Preconditions.checkArgument(mostRecent == 1 || mostRecent == 0);


        double evenLatitudePosition = (EVEN_LATITUDE_LENGTH) * (latitudeZoneFonder(y0, y1
                , EVEN_LATITUDE_NUMBER_ZONE) + y0);

        if (evenLatitudePosition >= 0.5) evenLatitudePosition -= 1;


        double oddLatitudePosition = (1d / ODD_LATITUDE_NUMBER_ZONE) * (latitudeZoneFonder(y0, y1
                , ODD_LATITUDE_NUMBER_ZONE) + y1);

        if (oddLatitudePosition >= 0.5) oddLatitudePosition -= 1;


        double ZoneNumberEven = calculatorArccos(evenLatitudePosition);
        int longitudeZoneNumberEven = (Double.isNaN(ZoneNumberEven)) ? 1 : (int) Math.floor((2 * Math.PI) / ZoneNumberEven);


        double ZoneNumberOdd = calculatorArccos(oddLatitudePosition);
        int longitudeZoneNumberTestOdd = Double.isNaN(ZoneNumberOdd) ? 1 : (int) Math.floor((2 * Math.PI) / ZoneNumberOdd);


        if (longitudeZoneNumberEven != longitudeZoneNumberTestOdd) return null;


        int longitudeZoneNumberOdd = longitudeZoneNumberEven - 1;
        double positionLongitudeEven = getLongitudeEven(x0, x1, longitudeZoneNumberEven, longitudeZoneNumberOdd);
        double positionLongitudeOdd = getLongitudeOdd(x0, x1, longitudeZoneNumberEven, longitudeZoneNumberOdd);


        if (positionLongitudeEven >= 0.5) positionLongitudeEven -= 1;
        if (positionLongitudeOdd >= 0.5) positionLongitudeOdd -= 1;


        int positionLatitudeOddT32 = (int) Math.rint(convert(oddLatitudePosition, Units.Angle.TURN, Units.Angle.T32));
        int positionLatitudeEvenT32 = (int) Math.rint(convert(evenLatitudePosition, Units.Angle.TURN, Units.Angle.T32));


        if (!(GeoPos.isValidLatitudeT32(positionLatitudeOddT32) && GeoPos.isValidLatitudeT32(positionLatitudeEvenT32))) {
            return null;
        }

        return new GeoPos(
                (int) Math.rint(convert(mostRecent(positionLongitudeEven, positionLongitudeOdd, mostRecent),
                        Units.Angle.TURN, Units.Angle.T32)),
                (int) mostRecent(positionLatitudeEvenT32, positionLatitudeOddT32, mostRecent));
    }


    private static double latitudeZoneFonder(double y0, double y1, double latitudeZoneNumber) {
        double latitudeZone = Math.rint((y0 * ODD_LATITUDE_NUMBER_ZONE) - (y1 * EVEN_LATITUDE_NUMBER_ZONE));
        if (latitudeZone < 0) return latitudeZone + latitudeZoneNumber;
        else return latitudeZone;
    }

    private static int longitudeZoneFinder(double x0, double x1, int longitudeZoneNumberEven, int longitudeZoneNumberOdd) {

        return (int) Math.rint((x0 * longitudeZoneNumberOdd) - (x1 * longitudeZoneNumberEven));

    }


    private static double getLongitudeEven(double x0, double x1, int longitudeZoneNumberEven, int longitudeZoneNumberOdd) {

        if (longitudeZoneNumberEven == 1) return x0;


        int zLamda = longitudeZoneFinder(x0, x1, longitudeZoneNumberEven, longitudeZoneNumberOdd);
        if (zLamda < 0) zLamda += longitudeZoneNumberEven;

        return ((1d / longitudeZoneNumberEven) * (zLamda + x0));
    }


    private static double getLongitudeOdd(double x0, double x1, int longitudeZoneNumberEven, int longitudeZoneNumberOdd) {

        if (longitudeZoneNumberOdd == 0) return x1;

        int zLamda = longitudeZoneFinder(x0, x1, longitudeZoneNumberEven, longitudeZoneNumberOdd);
        if (zLamda < 0) zLamda += longitudeZoneNumberOdd;

        return ((1d / longitudeZoneNumberOdd) * (zLamda + x1));
    }


    private static double calculatorArccos(double positionLatitudeEven) {
        double angle = Math.cos(convertFrom(positionLatitudeEven, Units.Angle.TURN));

        return Math.acos(1 - (1 - Math.cos((2 * Math.PI * EVEN_LATITUDE_LENGTH))) / (angle * angle));
    }


    private static double mostRecent(double even, double odd, int mostResent) {

        if (mostResent == 0) return even;
        else return odd;
    }
}