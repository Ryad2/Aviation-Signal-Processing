package ch.epfl.javions.adsb;


/**
 * Transformer les messages ADS-B bruts en messages d'un des trois types décrits précédemment :
 * identification, position en vol et vitesse en vol
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class MessageParser {

    /**
     * Constructeur de MessageParser qui n'est pas instantiable
     */
    private MessageParser() {}

    /**
     * Parse un message ADS-B brut en un message d'un des trois types décrits précédemment
     *
     * @param rawMessage le message ADS-B brut
     * @return l'instance des trois types décrits précédemment, ou null si le code de type de ce
     * dernier ne correspond à aucun de ces trois types de messages, ou s'il est invalide.
     */
    public static Message parse(RawMessage rawMessage) {

        return switch (rawMessage.typeCode()) {
            case 1, 2, 3, 4 -> AircraftIdentificationMessage.of(rawMessage);
            case 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22 -> AirbornePositionMessage.of(rawMessage);
            case 19 -> AirborneVelocityMessage.of(rawMessage);
            default -> null;
        };
    }
}