package ch.epfl.javions.adsb;

import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;
import java.util.Objects;
import static ch.epfl.javions.Bits.extractUInt;
import static ch.epfl.javions.Bits.testBit;

/**
 * Représente un message de vitesse en vol
 *
 * @param timeStampNs    horodatage du message, en nanosecondes
 * @param icaoAddress    l'adresse OACI de l'expéditeur du message
 * @param speed          vitesse de l'aéronef, en m/s
 * @param trackOrHeading direction de déplacement l'aéronef, en radians. Il contient soit la route
 *                       de l'aéronef, soit son cap. Dans les deux cas, cette valeur est représentée
 *                       par l'angle — positif et mesuré dans le sens des aiguilles d'une montre
 *                       — entre le nord et la direction de déplacement de l'aéronef.
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public record AirborneVelocityMessage(long timeStampNs, IcaoAddress icaoAddress, double speed,
                                      double trackOrHeading) implements Message {
    private static final int SUBTYPE_SPECIFIC_INDEX = 21;
    private static final int START_DIRECTION_EAST_WEST = 21;
    private static final int SIZE_DIRECTION_EAST_WEST = 1;
    private static final int START_SPEED_EAST_WEST = 11;
    private static final int SIZE_SPEED_EAST_WEST = 10;
    private static final int START_DIRECTION_NORTH_SOUTH = 10;
    private static final int SIZE_DIRECTION_NORTH_SOUTH = 1;
    private static final int START_SPEED_NORTH_SOUTH = 0;
    private static final int SIZE_SPEED_NORTH_SOUTH = 10;
    private static final int START_SUB_TYPE = 48;
    private static final int SIZE_SUB_TYPE = 3;
    private static final int START_DATA = 21;
    private static final int SIZE_DATA = 22;
    private static final int START_AIR_SPEED = 0;
    private static final int SIZE_AIR_SPEED = 10;
    private static final int START_HEADING = 11;
    private static final int SIZE_HEADING = 10;

    /**
     * Construit un message de vitesse en vol à partir des informations données
     *
     * @throws NullPointerException     si l'adresse OACI est nul
     * @throws IllegalArgumentException si l'horodatage, la vitesse ou la direction sont strictement négatives
     */
    public AirborneVelocityMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument((timeStampNs >= 0) && (speed >= 0) && (trackOrHeading >= 0));
    }

    /**
     * Permet de trouver le message de vitesse en vol correspondant au message brut donné en
     * fonction de son sous-type. Si le sous-type est 1 ou 2, on parlera de la vitesse sol alors que
     * s'il est 3 ou 4, on parle de la vitesse air.
     * Tous les autres sous-types sont invalides.
     *
     * @param rawMessage le message brut.
     * @return le message de vitesse en vol correspondant au message brut donné ou null si le
     * sous-type est invalide, ou si la vitesse ou la direction de déplacement ne peuvent pas être
     * déterminés.
     */
    public static AirborneVelocityMessage of(RawMessage rawMessage) {

        int subType = extractUInt(rawMessage.payload(), START_SUB_TYPE, SIZE_SUB_TYPE);
        int data = extractUInt(rawMessage.payload(), START_DATA, SIZE_DATA);

        if (!(subType == 1 || subType == 2 || subType == 3 || subType == 4)) return null;

        double speedLength = 0;
        double trackOrHeading = 0;

        if (subType == 1 || subType == 2) {

            byte directionEastWest = (byte) extractUInt(data, START_DIRECTION_EAST_WEST,
                    SIZE_DIRECTION_EAST_WEST);
            int speedEastWest = extractUInt(data, START_SPEED_EAST_WEST, SIZE_SPEED_EAST_WEST);
            byte directionNorthSouth = (byte) extractUInt(data, START_DIRECTION_NORTH_SOUTH,
                    SIZE_DIRECTION_NORTH_SOUTH);
            int speedNorthSouth = extractUInt(data, START_SPEED_NORTH_SOUTH, SIZE_SPEED_NORTH_SOUTH);

            if (speedNorthSouth == 0 || speedEastWest == 0) return null;

            speedNorthSouth--;
            speedEastWest--;

            speedNorthSouth = directionNorthSouth == 1 ? -(speedNorthSouth) : speedNorthSouth;
            speedEastWest = directionEastWest == 1 ? -(speedEastWest) : speedEastWest;

            speedLength = Math.hypot(speedEastWest, speedNorthSouth);
            trackOrHeading = Math.atan2(speedEastWest, speedNorthSouth);

            if (trackOrHeading < 0) trackOrHeading += 2 * Math.PI;

            // Si le subType n'est pas 1, le subType est obligatoirement 2
            speedLength = subType == 1 ? Units.convertFrom(speedLength, Units.Speed.KNOT) :
                    Units.convertFrom(4 * speedLength, Units.Speed.KNOT);

            if (Double.isNaN(speedLength)) return null;
        }

        if (subType == 3 || subType == 4) {
            int airSpeed = extractUInt(data, START_AIR_SPEED, SIZE_AIR_SPEED);
            int heading = extractUInt(data, START_HEADING, SIZE_HEADING);

            if (!testBit(data, SUBTYPE_SPECIFIC_INDEX) || airSpeed-- == 0) return null;
            trackOrHeading = Units.convertFrom(Math.scalb(heading, -10), Units.Angle.TURN);

            // Si le subType n'est pas 3, le subType est obligatoirement 4
            speedLength = subType == 3 ? Units.convertFrom(airSpeed, Units.Speed.KNOT) :
                    Units.convertFrom(4 * airSpeed, Units.Speed.KNOT);
        }
        return new AirborneVelocityMessage(rawMessage.timeStampNs(), rawMessage.icaoAddress(),
                speedLength, trackOrHeading);
    }
}