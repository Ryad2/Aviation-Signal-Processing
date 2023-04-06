package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;


/**
 * Une classe qui représente un message ADS-B de positionnement en vol. Ces messages ont un code de type compris soit
 * entre 9 (inclus) et 18 (inclus), soit entre 20 (inclus) et 22 (inclus)
 * Construit un message ADS-B de positionnement en vol à partir des paramètres donnés
 *
 * @param timeStampNs l'horodatage du message, en nanosecondes
 * @param icaoAddress l'adresse ICAO de l'expéditeur du message
 * @param altitude    l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres
 * @param parity      la parité du message (0 s'il est pair, 1 s'il est impair)
 * @param x           la longitude locale et normalisée — comprise entre 0 et 1 — à laquelle se trouvait l'aéronef au
 *                    moment de l'envoi du message
 * @param y           la latitude locale et normalisée — comprise entre 0 et 1 — à laquelle se trouvait l'aéronef au
 *                    moment de l'envoi du message.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude,
                                      int parity, double x, double y) implements Message {


    private static final int START_CPR_LATITUDE = 17;
    private static final int START_CPR_LONGITUDE = 0;
    private static final int START_ALT = 36;
    private static final int SIZE_ALT = 12;
    private static final int START_FORMAT = 34;
    private static final int SIZE_FORMAT = 1;
    private static final int[] arrayPositions = new int[]{4, 10, 5, 11};
    private static final int Q_OFFSET = 4;
    private static final int LOCALISATION_BIT_SIZE = 17;
    private static final double DIVISOR = Math.scalb(1d, -17);
    private static final int START_BIT_LSB_GROUP = 0;
    private static final int SIZE_BIT_LSB_GROUP = 3;
    private static final int START_BIT_MSB_GROUP = 3;
    private static final int SIZE_BIT_MSB_GROUP = 9;
    private static final int START_BITS_1 = 5;
    private static final int START_BITS_2 = 0;
    private static final int SIZE_BITS_1 = 7;
    private static final int SIZE_BITS_2 = 4;


    /**
     * @throws NullPointerException     si l'adresse ICAO est nulle
     * @throws IllegalArgumentException si l'horodatage est négatif, si la parité n'est pas 0 ou 1, ou x ou y ne sont
     *                                  pas compris entre 0 (inclus) et 1 (exclu).
     */
    public AirbornePositionMessage {

        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument((timeStampNs >= 0) && (parity == 1 || parity == 0) && (x >= 0)
                && (x < 1) && (y >= 0) && (y < 1));
    }


    /**
     * Construit un message ADS-B de positionnement en vol à partir d'un message ADS-B brut en fonction de la valeur du
     * bit d'index 4 de l'attribut ATL
     *
     * @param rawMessage le message ADS-B brut
     * @return le message ADS-B de positionnement en vol construit
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {

        long timeStampNs = rawMessage.timeStampNs();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        double altitude = 0;
        int alt = extractUInt(rawMessage.payload(), START_ALT, SIZE_ALT);
        int FORMAT = extractUInt(rawMessage.payload(), START_FORMAT, SIZE_FORMAT);
        double LAT_CPR = extractUInt(rawMessage.payload(), START_CPR_LATITUDE, LOCALISATION_BIT_SIZE) * DIVISOR;
        double LON_CPR = extractUInt(rawMessage.payload(), START_CPR_LONGITUDE, LOCALISATION_BIT_SIZE) * DIVISOR;

        int Q = extractUInt(alt, Q_OFFSET, 1);

        if (Q == 1) {

            // On coupe alt en deux parties pour supprimer le Q bit puis on les recolle ensemble
            int bits1 = extractUInt(alt, START_BITS_1, SIZE_BITS_1);
            int bits2 = extractUInt(alt, START_BITS_2, SIZE_BITS_2);

            alt = (bits1 << Q_OFFSET) | bits2;
            altitude = Units.convertFrom(-1000 + (alt * 25), Units.Length.FOOT);
        }

        if (Q == 0) {
            int lsbGroupe = extractUInt(unTangler(alt), START_BIT_LSB_GROUP, SIZE_BIT_LSB_GROUP);
            lsbGroupe = greyTranscription(lsbGroupe, SIZE_BIT_LSB_GROUP);
            int msbGroupe = extractUInt(unTangler(alt), START_BIT_MSB_GROUP, SIZE_BIT_MSB_GROUP);
            msbGroupe = greyTranscription(msbGroupe, SIZE_BIT_MSB_GROUP);

            if (lsbGroupe == 0 || lsbGroupe == 5 || lsbGroupe == 6) return null;
            if (lsbGroupe == 7) lsbGroupe = 5;
            if (msbGroupe % 2 != 0) lsbGroupe = 6 - lsbGroupe;

            altitude = Units.convertFrom(-1300 + (lsbGroupe * 100) + (msbGroupe * 500), Units.Length.FOOT);
        }

        return new AirbornePositionMessage(timeStampNs, icaoAddress, altitude, FORMAT, LON_CPR, LAT_CPR);
    }

    private static int unTangler(int alt) {

        int returnal = 0;
        for (int i = 0; i < arrayPositions.length; i++)
            for (int j = 0; j < 3; j++) {
                returnal |= extractUInt(alt, arrayPositions[i] - j * 2, 1) << 11 - (j + i * 3);
            }
        return returnal;
    }


    private static int greyTranscription(int num, int size) {
        int greyCodeValue = 0;
        for (int i = 0; i < size; i++) greyCodeValue ^= (num >> i);
        return greyCodeValue;
    }
}