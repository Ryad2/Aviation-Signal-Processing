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
 * @param x           la longitude locale et normalisée — donc comprise entre 0 et 1 — à laquelle se trouvait l'aéronef au
 *                    moment de l'envoi du message
 * @param y           la latitude locale et normalisée à laquelle se trouvait l'aéronef au moment de l'envoi du message.
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude,
                                      int parity, double x, double y) implements Message {


    private static final int[] arrayPositions = new int[]{4, 10, 5, 11};


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
     * @return
     */
    public static AirbornePositionMessage of(RawMessage rawMessage) {

        long timeStampNs = rawMessage.timeStampNs();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        double altitude = 0;
        int alt = extractUInt(rawMessage.payload(), 36, 12);
        int FORMAT = extractUInt(rawMessage.payload(), 34, 1);
        double LAT_CPR = extractUInt(rawMessage.payload(), 17, 17) * Math.scalb(1d, -17);
        double LON_CPR = extractUInt(rawMessage.payload(), 0, 17) * Math.scalb(1d, -17);

        int Q = extractUInt(alt, 4, 1);

        if (Q == 1) {
            int bits1 = extractUInt(alt, 5, 7);
            int bits2 = extractUInt(alt, 0, 4);

            alt = (bits1 << 4) | bits2;
            altitude = Units.convertFrom(-1000 + (alt * 25), Units.Length.FOOT);
        }

        if (Q == 0) {
            int lsbGroupe = extractUInt(unTangler(alt), 0, 3);
            lsbGroupe = greyTranscription(lsbGroupe, 3);
            int msbGroupe = extractUInt(unTangler(alt), 3, 9);
            msbGroupe = greyTranscription(msbGroupe, 9);

            if (lsbGroupe == 0 || lsbGroupe == 5 || lsbGroupe == 6) return null;
            if (lsbGroupe == 7) lsbGroupe = 5;
            if (msbGroupe % 2 != 0) lsbGroupe = 6 - lsbGroupe;

            altitude = Units.convertFrom(-1300 + (lsbGroupe * 100) + (msbGroupe * 500), Units.Length.FOOT);
        }

        return new AirbornePositionMessage(timeStampNs, icaoAddress, altitude, FORMAT, LON_CPR, LAT_CPR);
    }

    private static int unTangler(int alt) {

        int returnal = 0;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 3; j++) {
                returnal |= extractUInt(alt, arrayPositions[i] - j * 2, 1) << 11 - (j + i * 3);
            }
        return returnal;
    }


    private static int greyTranscription(int num, int length) {
        int greyCodeValue = 0;
        for (int i = 0; i < length; i++) greyCodeValue ^= (num >> i);
        return greyCodeValue;
    }
}