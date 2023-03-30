package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente la description d'un avion
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record AircraftDescription(String string) {

    /**
     * Lève une exception si le string n'est pas une description de type valide
     * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une description
     * @throws IllegalArgumentException
     */
    private static Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");
    public AircraftDescription {
        Preconditions.checkArgument((pattern.matcher(string).matches() || string.isEmpty()));
    }
}

