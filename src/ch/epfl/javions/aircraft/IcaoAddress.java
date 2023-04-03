package ch.epfl.javions.aircraft;
import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente une adresse OACI
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'adresse OACI
 */

public record IcaoAddress(String string) {

    //TODO : faut commenter ça?
    private static final Pattern pattern = Pattern.compile("[0-9A-F]{6}");

    /**
     * Construit une adresse OACI à partir de la chaîne passée en argument
     * @throws IllegalArgumentException si le string passé en argument n'est pas une adresse OACi valide
     */
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}

