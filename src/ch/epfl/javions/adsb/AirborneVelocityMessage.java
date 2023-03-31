package ch.epfl.javions.adsb;


import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.Bits.testBit;


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
     * Permet de trouver le message de vitesse en vol correspondant au message brut donné en fonction de son sous-type.
     * Si le sous-type est 1 ou 2, on parlera de la vitesse sol alors que s'il est 3 ou 4, on parle de la vitesse air.
     * Tous les autres sous-types sont invalides.
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
            int vew = extractUInt(attribut21, 11, 10) ;
            byte dns = (byte) extractUInt(attribut21, 10, 1);
            int vns = extractUInt(attribut21, 0, 10) ;


            if (vns == 0 || vew == 0) return null;
            vns--;
            vew--;
            /*if (dew == 0 && dns == 0) theta = Math.atan2(vew, vns);
            if (dew == 0 && dns == 1) theta = Math.atan2(vew, -vns);
            if (dew == 1 && dns == 0) theta = Math.atan2(-vew, vns);
            if (dew == 1 && dns == 1) theta = Math.atan2(-vew, -vns);*/


            speedLength = Math.hypot(vew, vns);
            if (dns == 1) vns=-vns;
            if (dew == 1) vew=-vew;
            theta = Math.atan2(vew, vns);


            if(theta < 0) theta += 2 * Math.PI;
            if (subType == 1) speedLength = Units.convertFrom(speedLength, Units.Speed.KNOT);
            if (subType == 2) speedLength = Units.convertFrom(4 * speedLength, Units.Speed.KNOT);
            if(Double.isNaN(speedLength)) return null;

            trackOrHeading = theta;//C'est faux mais il y a des valeurs donc c'est cool
        }

        if(subType == 3 || subType == 4) {

            int as = extractUInt(attribut21, 0, 10);
            int heading = extractUInt(attribut21, 11, 10);

            if (!(testBit(attribut21, 21) )  || as == 0) return null;
            as--;
            trackOrHeading = Units.convertFrom(Math.scalb(heading,-10),Units.Angle.TURN);//TODO enlever le if() pour intermediére
            if (subType == 3) speedLength = Units.convertFrom(as, Units.Speed.KNOT);
            if (subType == 4) speedLength = Units.convertFrom(4 * as, Units.Speed.KNOT);

        }
        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(), speedLength, trackOrHeading);
    }


}