package ch.epfl.javions.adsb;

/**
 * Transformer les messages ADS-B bruts en messages d'un des trois types décrits précédemment
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public class MessageParser {

    /**
     * Parse un message ADS-B brut en un message d'un des trois types décrits précédemment
     * @param rawMessage le message ADS-B brut
     * @return le message d'un des trois types décrits précédemment ou null si le message n'est pas reconnu ou si le
     * message n'est pas valide (n'est pas un des 3 types décrits précédemment)
     */

    public static Message parse(RawMessage rawMessage){
        if (rawMessage.typeCode() == 1 || rawMessage.typeCode() == 2 || rawMessage.typeCode() == 3 ||
                rawMessage.typeCode() == 4) {
            return AircraftIdentificationMessage.of(rawMessage);
        }
        if ((rawMessage.typeCode() >= 9 && rawMessage.typeCode() <= 18) ||
                (rawMessage.typeCode() >= 20 && rawMessage.typeCode() <= 22)) {

            return AirbornePositionMessage.of(rawMessage);
        }
        if (rawMessage.typeCode() == 19) return AirborneVelocityMessage.of(rawMessage);
        else return null;
    }
}