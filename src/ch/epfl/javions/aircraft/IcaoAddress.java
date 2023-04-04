package ch.epfl.javions.aircraft;

import ch.epfl.javions.Preconditions;

import java.util.regex.Pattern;

/**
 * Représente une adresse OACI
 *
 * @param string représente la chaîne contenant la représentation textuelle de l'adresse OACI
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record IcaoAddress(String string) {


    /**
     * Pattern de IcaoAddress
     */
    private static final Pattern pattern = Pattern.compile("[0-9A-F]{6}");

    /**
     * Construit une adresse OACI à partir de la chaîne passée en argument
     *
     * @throws IllegalArgumentException si le string passé en argument n'est pas une adresse OACi valide
     */
    public IcaoAddress {
        Preconditions.checkArgument(pattern.matcher(string).matches());
    }
}

