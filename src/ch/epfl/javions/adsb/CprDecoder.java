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
    private CprDecoder() {}

    /**
     * Décode la position d'un aéronef à partir de deux messages CPR
     *
     * @param x0         la longitude locale du message pair
     * @param y0         la latitude locale du message pair
     * @param x1         la longitude locale du message impair
     * @param y1         la latitude locale du message impair
     * @param mostRecent l'index de position le plus récent :
     *                   0 si le message impair est le plus récent, 1 si le message pair est le
     *                   plus récent
     * @return la position de l'aéronef ou null si la latitude de la position décodée n'est pas
     * valide ou si la position ne peut pas être déterminée en raison d'un changement de bande de
     * latitude
     * @throws IllegalArgumentException si mostRecent n'est pas 0 ou 1
     */
    public static GeoPos decodePosition(double x0, double y0, double x1, double y1, int mostRecent) {
        Preconditions.checkArgument(mostRecent == 1 || mostRecent == 0);

        double evenLatitudePosition = EVEN_LATITUDE_LENGTH * (latitudeZoneFinder(y0, y1,
                EVEN_LATITUDE_NUMBER_ZONE) + y0);

        if (evenLatitudePosition >= 0.5) evenLatitudePosition -= 1;

        double oddLatitudePosition = (1d / ODD_LATITUDE_NUMBER_ZONE) * (latitudeZoneFinder(y0, y1,
                ODD_LATITUDE_NUMBER_ZONE) + y1);

        if (oddLatitudePosition >= 0.5) oddLatitudePosition -= 1;

        double evenNumberZone = calculatorArccos(evenLatitudePosition);
        int evenLongitudeNumberZone = (Double.isNaN(evenNumberZone)) ? 1 :
                (int) Math.floor((2 * Math.PI) / evenNumberZone);

        double oddNumberZone = calculatorArccos(oddLatitudePosition);
        int oddLongitudeNumberZoneVerification = Double.isNaN(oddNumberZone) ? 1 :
                (int) Math.floor((2 * Math.PI) / oddNumberZone);

        if (evenLongitudeNumberZone != oddLongitudeNumberZoneVerification) return null;
        int oddLongitudeNumberZone = evenLongitudeNumberZone - 1;

        double evenLongitudePosition = getLongitudeEven(x0, x1,
                evenLongitudeNumberZone, oddLongitudeNumberZone);

        double oddLongitudePosition = getLongitudeOdd(x0, x1,
                evenLongitudeNumberZone, oddLongitudeNumberZone);

        if (evenLongitudePosition >= 0.5) evenLongitudePosition -= 1;
        if (oddLongitudePosition >= 0.5) oddLongitudePosition -= 1;

        int oddLatitudePositionT32 = (int) Math.rint(convert(oddLatitudePosition,
                Units.Angle.TURN, Units.Angle.T32));

        int evenLatitudePositionT32 = (int) Math.rint(convert(evenLatitudePosition,
                Units.Angle.TURN, Units.Angle.T32));

        if (!(GeoPos.isValidLatitudeT32(oddLatitudePositionT32)
                && GeoPos.isValidLatitudeT32(evenLatitudePositionT32))) return null;

        return new GeoPos((int) Math.rint(convert(
                mostRecent(evenLongitudePosition, oddLongitudePosition, mostRecent),
                Units.Angle.TURN, Units.Angle.T32)),
                (int) mostRecent(evenLatitudePositionT32,
                        oddLatitudePositionT32, mostRecent));
    }

    private static double latitudeZoneFinder(double y0, double y1, double latitudeZoneNumber) {
        double latitudeZone =
                Math.rint((y0 * ODD_LATITUDE_NUMBER_ZONE) - (y1 * EVEN_LATITUDE_NUMBER_ZONE));
        if (latitudeZone < 0) return latitudeZone + latitudeZoneNumber;
        else return latitudeZone;
    }

    private static int longitudeZoneFinder(double x0, double x1, int longitudeZoneNumberEven,
                                           int longitudeZoneNumberOdd) {
        return (int) Math.rint((x0 * longitudeZoneNumberOdd) - (x1 * longitudeZoneNumberEven));
    }

    private static double getLongitudeEven(double x0, double x1, int longitudeZoneNumberEven,
                                           int longitudeZoneNumberOdd) {
        if (longitudeZoneNumberEven == 1) return x0;
        int zLamda = longitudeZoneFinder(x0, x1, longitudeZoneNumberEven, longitudeZoneNumberOdd);
        if (zLamda < 0) zLamda += longitudeZoneNumberEven;

        return ((1d / longitudeZoneNumberEven) * (zLamda + x0));
    }

    private static double getLongitudeOdd(double x0, double x1, int longitudeZoneNumberEven,
                                          int longitudeZoneNumberOdd) {
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
        return (mostResent == 0) ? even : odd;
    }
}