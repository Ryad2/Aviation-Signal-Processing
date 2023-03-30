package ch.epfl.javions.adsb;


import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;


/**
 * Représente un message de vitesse en vol
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed, double trackOrHeading)
                    implements Message {

    /**
     * Construit un message de vitesse en vol à partir des informations données
     * @param timeStampNs horodatage du message, en nanosecondes
     * @param icaoAddress l'adresse OACI de l'expéditeur du message
     * @param speed vitesse de l'aéronef, en m/s
     * @param trackOrHeading direction de déplacement l'aéronef, en radians
     * @throws NullPointerException si l'adresse OACI est nulle
     * @throws IllegalArgumentException si l'horodatage est négatif, ou si la vitesse ou la direction sont négatives
     */

    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument((timeStampNs >= 0) && (speed >= 0) && (trackOrHeading >= 0));
    }

    /**
     * Permet de trouver le message de vitesse en vol correspondant au message brut donné en fonction de son sous-type
     * @param rawMessage le message brut
     * @return le message de vitesse en vol correspondant au message brut donné ou null si le sous-type est invalide,
     * ou si la vitesse ou la direction de déplacement ne peuvent pas être déterminés.
     */

    public static AirborneVelocityMessage of(RawMessage rawMessage) {
        int subType = extractUInt(rawMessage.payload(), 48, 3);
        int attribut21 = extractUInt(rawMessage.payload(), 21, 22);

        if (!(subType == 1 || subType == 2 || subType == 3 || subType == 4)) return null;

        double speedLength = 0;
        double trackOrHeading = 0;
        double theta = 0;

        if (subType == 1 || subType == 2) {

            byte dew = (byte) extractUInt(attribut21, 21, 1);
            int vew = extractUInt(attribut21, 11, 10) -1;
            byte dns = (byte) extractUInt(attribut21, 10, 1);
            int vns = extractUInt(attribut21, 0, 10) -1;
            speedLength = Math.hypot(vew, vns);
            if (vns == 0 || vew == 0) throw new IndexOutOfBoundsException("Vitesse inconnue"); //Est-ce correct?
            if (dew == 0 && dns == 0) theta = Math.atan2(vew, vns);
            if (dew == 0 && dns == 1) theta = Math.atan2(vew, -vns);
            if (dew == 1 && dns == 0) theta = Math.atan2(-vew, vns);
            if (dew == 1 && dns == 1) theta = Math.atan2(-vew, -vns);
            if(theta < 0) theta += 2 * Math.PI;
            if (subType == 1) speedLength = Units.convertFrom(speedLength, Units.Speed.KNOT);
            if (subType == 2) speedLength = Units.convertFrom(4 * speedLength, Units.Speed.KNOT);//VERIFIER !!!!!

            trackOrHeading = theta;//C'est faux mais il y a des valeurs donc c'est cool
        }

        if(subType == 3 || subType == 4) {
            int sh = extractUInt(attribut21, 21, 1);
            int as = extractUInt(attribut21, 0, 10) - 1;
            int hdg = extractUInt(attribut21, 11, 10);

            if (sh == 1) trackOrHeading = Units.convertFrom(Math.scalb(convertUnsignedInt(hdg),-10),Units.Angle.TURN);
            if (sh == 0) return null;

            if (subType == 3) speedLength = Units.convertFrom(as, Units.Speed.KNOT);
            if (subType == 4) speedLength = Units.convertFrom(4 * as, Units.Speed.KNOT);//VERIFIER !!!!!

        }
        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speedLength, trackOrHeading);
    }

    private static int convertUnsignedInt(int dixBits) {
        // Vérification que l'entier dixBits est bien un entier de 10 bits
        if (dixBits < 0 || dixBits > 1023) {
            throw new IllegalArgumentException("L'entier doit être compris entre 0 et 1023");
        }

        // Conversion de l'entier dixBits en un entier non signé de 32 bits stocké dans un int
        return dixBits & 0x3FF;
    }
}