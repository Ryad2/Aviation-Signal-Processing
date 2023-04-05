package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Représente un message ADS-B d'identification et de category d'un aéronef
 *
 * @param timeStampNs horodatage du message, en nanosecondes
 * @param icaoAddress l'adresse OACI de l'expéditeur du message
 * @param category    la category de l'aéronef
 * @param callSign    l'indicatif de l'expédition
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */


public record AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress,
                                            int category, CallSign callSign) implements Message {

    private static final int START_BIT_CA = 48;
    private static final int SIZE_CA = 3;
    public static final int START_BIT_FIRST_CHARACTER = 42;
    public static final int SIZE_FIRST_CHARACTER = 6;

    /**
     * Construit un message d'identification et de category à partir des informations données
     *
     * @throws NullPointerException     si l'adresse OACI ou l'indicatif est nul
     * @throws IllegalArgumentException si l'horodatage est strictement négatif
     */
    public AircraftIdentificationMessage {

        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }


    /**
     * Permet de trouver le message d'identification correspondant au message brut donné
     *
     * @param rawMessage le message brut
     * @return le message d'identification correspondant au message brut donné ou null si au moins un
     * des caractères de l'indicatif est invalide
     */
    public static AircraftIdentificationMessage of(RawMessage rawMessage) {

        StringBuilder indicator = new StringBuilder();
        int ca = Bits.extractUInt(rawMessage.payload(), START_BIT_CA, SIZE_CA);
        int category = ((14 - rawMessage.typeCode()) << 4) | ca;

        for (int i = 0; i < Long.BYTES; i++) {

            if (character(Bits.extractUInt(rawMessage.payload(),
                    START_BIT_FIRST_CHARACTER - i * 6, SIZE_FIRST_CHARACTER)) == null) return null;

            indicator.append(character(Bits.extractUInt(rawMessage.payload(),
                    START_BIT_FIRST_CHARACTER - i * 6, SIZE_FIRST_CHARACTER)));
        }

        return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), category,
                new CallSign(indicator.toString().trim()));
    }


    /**
     * Converti un nombre en un caractère
     * @param val le chiffre qui est transformé en caractère
     * @return le caractère en question
     */
    private static Character character(int val) {
        char output;
        if (val == 32) output = ' ';
        else if (val >= 48 && val <= 57) output = (char) val;
        else if (val >= 1 && val <= 26) output = (char) (val + 64);
        else return null;
        return output;
    }
}