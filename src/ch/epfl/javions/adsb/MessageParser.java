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
        if ( (rawMessage.typeCode() >= 9 && rawMessage.typeCode() <= 18) || (rawMessage.typeCode() >= 20 && rawMessage.typeCode() <= 22)) {

            return AirbornePositionMessage.of(rawMessage);
        }
        if (rawMessage.typeCode() == 19) return AirborneVelocityMessage.of(rawMessage);
        else return null;
    }

    /*
  position : (5.620176717638969°, 45.71530147455633°)
position : (5.621292097494006°, 45.715926848351955°)
indicatif : CallSign[string=RYR7JD]
position : (5.62225341796875°, 45.71644593961537°)
position : (5.623420681804419°, 45.71704415604472°)
position : (5.624397089704871°, 45.71759032085538°)
position : (5.625617997720838°, 45.71820789948106°)
position : (5.626741759479046°, 45.718826316297054°)
position : (5.627952609211206°, 45.71946484968066°)
position : (5.629119873046875°, 45.72007002308965°)
position : (5.630081193521619°, 45.7205820735544°)
position : (5.631163045763969°, 45.72120669297874°)
indicatif : CallSign[string=RYR7JD]
position : (5.633909627795219°, 45.722671514377°)
position : (5.634819064289331°, 45.72314249351621°)
     */

}