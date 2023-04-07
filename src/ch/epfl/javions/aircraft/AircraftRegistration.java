package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;
import java.util.regex.Pattern;

/**
 * Représente l'immatriculation d'un avion
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une
 *               immatriculation
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record AircraftRegistration(String string) {

    private final static Pattern pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");
    
    /**
     * Construit une immatriculation à partir de la chaîne passée en argument
     *
     * @throws IllegalArgumentException si le string passé en argument n'est pas une immatriculation
     * valide
     */
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}

