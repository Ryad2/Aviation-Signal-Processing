package ch.epfl.javions.aircraft;

/**
 * Représente la catégorie de turbulence de sillage d'un aéronef.
 * Il contient quatre valeurs qui sont : LIGHT, MEDIUM, HEAVY et UNKNOWN
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public enum WakeTurbulenceCategory {
    LIGHT,
    MEDIUM,
    HEAVY,
    UNKNOWN;

    /**
     * Retourne la catégorie de turbulence de sillage correspondant à la chaîne donnée.
     *
     * @param string représente la lettre contenant la représentation textuelle de turbulence
     *               d'un aéronef.
     * @return la catégorie de turbulence grâce à lettre donnée.
     */
    public static WakeTurbulenceCategory of(String string) {
        return switch (string) {
            case "L" -> LIGHT;
            case "M" -> MEDIUM;
            case "H" -> HEAVY;
            default -> UNKNOWN;
        };
    }
}