package ch.epfl.javions.aircraft;

import static java.util.Objects.requireNonNull;

/**
 * Défini AircraftData et collecte les données fixes d'un aéronef
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 *
 * @param registration l'immatriculation d'un avion
 * @param typeDesignator Indicateur de type d'un avion
 * @param model le model d'un avion
 * @param description la description d'un avion
 * @param wakeTurbulenceCategory la turbulence de sillage d'un aéronef
 */

public record AircraftData (AircraftRegistration registration, AircraftTypeDesignator typeDesignator,
                            String model, AircraftDescription description,
                            WakeTurbulenceCategory wakeTurbulenceCategory) {



    /**
     * Constructeur de AircraftDate qui lève une exception si l'un de ses arguments est nul
     * @throws NullPointerException si l'un des 5 arguments est nul
     */
    public AircraftData {
        requireNonNull(registration);
        requireNonNull(typeDesignator);
        requireNonNull(model);
        requireNonNull(description);
        requireNonNull(wakeTurbulenceCategory);
    }
}

