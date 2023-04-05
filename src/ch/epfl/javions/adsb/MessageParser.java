package ch.epfl.javions.adsb;


/**
 * Transformer les messages ADS-B bruts en messages d'un des trois types décrits précédemment :
 * identification, position en vol et vitesse en vol
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public class MessageParser {

    private static final int TYPE_CODE_AIRCRAFT_IDENTIFICATION1 = 1;
    private static final int TYPE_CODE_AIRCRAFT_IDENTIFICATION2 = 2;
    private static final int TYPE_CODE_AIRCRAFT_IDENTIFICATION3 = 3;
    private static final int TYPE_CODE_AIRCRAFT_IDENTIFICATION4 = 4;
    private static final int TYPE_CODE_AIRBORNE_POSITION1 = 9;
    private static final int TYPE_CODE_AIRBORNE_POSITION2 = 18;
    private static final int TYPE_CODE_AIRBORNE_POSITION3 = 20;
    private static final int TYPE_CODE_AIRBORNE_POSITION4 = 22;
    private static final int TYPE_CODE_AIRBORNE_VELOCITY = 19;


    /**
     * Parse un message ADS-B brut en un message d'un des trois types décrits précédemment
     *
     * @param rawMessage le message ADS-B brut
     * @return l'instance des trois types décrits précédemment, ou null si le code de type de ce dernier ne correspond
     * à aucun de ces trois types de messages, ou s'il est invalide.
     */
    public static Message parse(RawMessage rawMessage) {
        if (rawMessage.typeCode() == TYPE_CODE_AIRCRAFT_IDENTIFICATION1
                || rawMessage.typeCode() == TYPE_CODE_AIRCRAFT_IDENTIFICATION2
                || rawMessage.typeCode() == TYPE_CODE_AIRCRAFT_IDENTIFICATION3
                || rawMessage.typeCode() == TYPE_CODE_AIRCRAFT_IDENTIFICATION4)
            return AircraftIdentificationMessage.of(rawMessage);


        if ((rawMessage.typeCode() >= TYPE_CODE_AIRBORNE_POSITION1
                && rawMessage.typeCode() <= TYPE_CODE_AIRBORNE_POSITION2)
                || (rawMessage.typeCode() >= TYPE_CODE_AIRBORNE_POSITION3
                && rawMessage.typeCode() <= TYPE_CODE_AIRBORNE_POSITION4)) {
            return AirbornePositionMessage.of(rawMessage);
        }

        if (rawMessage.typeCode() == TYPE_CODE_AIRBORNE_VELOCITY) return AirborneVelocityMessage.of(rawMessage);

        else return null;
    }
}