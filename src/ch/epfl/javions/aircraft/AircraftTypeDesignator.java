package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente l'indicateur de type d'un avion
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record AircraftTypeDesignator(String string) {

    /**
     * Lève une exception si le string n'est pas un indicateur de type valide
     * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une immatriculation
     * @throws IllegalArgumentException si le string n'est pas un indicateur de type valide
     */

    private static Pattern pattern = Pattern.compile("[A-Z0-9]{2,4}");
    public AircraftTypeDesignator {
        Preconditions.checkArgument((pattern.matcher(string).matches() || string.isEmpty()));
    }
}

