package ch.epfl.javions.adsb;

import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente l'indicatif d'un aéronef
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
*/

public record CallSign (String string) {
    private static Pattern pattern = Pattern.compile("[A-Z0-9 ]{0,8}");

    /**
     * Lève une exception quand l'ADSB n'est pas valide
     * @param string représente la chaîne contenant la représentation textuelle de l'indicatif d'un aéronef
     * @throws IllegalArgumentException
     */
    public CallSign {
        Preconditions.checkArgument(pattern.matcher(string).matches() || string.isEmpty());
    }
}