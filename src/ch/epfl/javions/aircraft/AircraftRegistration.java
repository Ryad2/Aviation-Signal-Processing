package ch.epfl.javions.aircraft;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente l'immatriculation d'un avion
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'adresse d'une immatriculation
 */

public record AircraftRegistration(String string) {

    private final static Pattern pattern = Pattern.compile("[A-Z0-9 .?/_+-]+");

    // TODO : Faut-il passer a ligne après chaque point?


    /**
     * Construit une immatriculation à partir de la chaîne passée en argument
     * @throws IllegalArgumentException si le string passé en argument n'est pas une immatriculation valide
     */
    public AircraftRegistration {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}

