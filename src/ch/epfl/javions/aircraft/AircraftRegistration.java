package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente l'immatriculation d'un avion
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record AircraftRegistration(String string) {

    /**
     * Lève une exception si le string n'est pas une immatriculation OACI valide
     * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une immatriculation
     * @throws IllegalArgumentException
     */

    private static Pattern pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}

