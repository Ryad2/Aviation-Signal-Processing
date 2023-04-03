package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente l'indicateur de type d'un avion
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une immatriculation
 */

public record AircraftTypeDesignator(String string) {

    private final static Pattern pattern = Pattern.compile("[A-Z0-9]{2,4}");


    /**
     * Construit un indicateur de type à partir de la chaîne passée en argument
     * @throws IllegalArgumentException si le string passé en argument n'est pas un indicateur de type valide
     */
    public AircraftTypeDesignator {
        Preconditions.checkArgument((pattern.matcher(string).matches() || string.isEmpty()));
    }
}

