package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;


/**
 * Représente la description d'un avion
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une description
 */
public record AircraftDescription(String string) {

    private final static Pattern pattern = Pattern.compile("[ABDGHLPRSTV-][0123468][EJPT-]");


    /**
     * Construit une description à partir de la chaîne passée en argument
     * @throws IllegalArgumentException si le string passé en argument n'est pas une description valide
     */
    public AircraftDescription {
        Preconditions.checkArgument((pattern.matcher(string).matches() || string.isEmpty()));
    }
}

