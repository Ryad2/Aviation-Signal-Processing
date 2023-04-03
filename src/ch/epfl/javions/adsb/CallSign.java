package ch.epfl.javions.adsb;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente l'indicatif d'un aéronef
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'indicatif d'un aéronef
*/

public record CallSign (String string) {
    private final static Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");


    /**
     * Construit un indicatif à partir de la chaîne passée en argument
     * @throws IllegalArgumentException si le string passé en argument n'est pas un indicatif valide
     */
    public CallSign {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty());
    }
}