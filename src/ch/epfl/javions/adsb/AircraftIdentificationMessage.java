package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Représente un message ADS-B d'identification et de category d'un aéronef
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */


public record  AircraftIdentificationMessage(long timeStampNs, IcaoAddress icaoAddress,
                                            int category, CallSign callSign) implements Message {

    /**
     * Construit un message d'identification et de category à partir des informations données
     * @param timeStampNs horodatage du message, en nanosecondes
     * @param icaoAddress l'adresse OACI de l'expéditeur du message
     * @param category la category de l'aéronef
     * @param callSign l'indicatif de l'expédition
     * @throws NullPointerException si l'adresse OACI ou l'indicatif est nuls
     * @throws IllegalArgumentException si l'horodatage est strictement négatif
     */

    public AircraftIdentificationMessage {
        Objects.requireNonNull(icaoAddress);
        Objects.requireNonNull(callSign);
        Preconditions.checkArgument(timeStampNs >= 0);
    }

    /**
     * Permet de trouver le message d'identification correspondant au message brut donné
     * @param rawMessage le message brut
     * @return le message d'identification correspondant au message brut donné ou null si au moins un
     * des caractères de l'indicatif est invalide
     */

    public static AircraftIdentificationMessage of(RawMessage rawMessage){
        String indicator="";


        int ca= Bits.extractUInt(rawMessage.payload(),48,3);
        int category =( (14- rawMessage.typeCode())<<4 ) | ca;

        for(int i=0; i<8;i++){
            if (character(Bits.extractUInt(rawMessage.payload(), 42 - i * 6, 6)) == null) return null;

            indicator+= character(Bits.extractUInt(rawMessage.payload(), 42 - i * 6, 6));
        }
    return new AircraftIdentificationMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),category,
            new CallSign(indicator.trim()));
    }

    private static Character character(int val){
        char output;
        if (val == 32) {
            output= ' ';
        } else if (val >= 48 && val <= 57) {
            output= (char) val;
        } else if (val >= 1 && val <= 26) {
            output = (char) (val + 64);
        } else return null;

        return output;
    }
}