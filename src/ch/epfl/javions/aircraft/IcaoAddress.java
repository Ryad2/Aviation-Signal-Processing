package ch.epfl.javions.aircraft;
import java.util.regex.Pattern;
import ch.epfl.javions.Preconditions;

/**
 * Représente une adresse OACI
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record IcaoAddress(String string) {

    /**
     * Lève une exception si le string n'est pas une adresse OACI valide
     * @param string représente la chaîne contenant la représentation textuelle de l'adresse OACI
     * @throws IllegalArgumentException si le string passé en argument n'est pas une adresse OACi valide
     */

    private static Pattern pattern = Pattern.compile("[0-9A-F]{6}");
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}

