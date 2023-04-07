package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;


/**
 * Interface qui a pour but d'être implémentée dans toutes les classes représentant l'état
 * (modifiable) d'un aéronef
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public interface AircraftStateSetter {

    /**
     * Change l'horodatage du dernier message reçu de l'aéronef à la valeur donnée,
     *
     * @param timeStampNs nouvel horodatage
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Change la catégorie de l'aéronef à la valeur donnée
     *
     * @param category la catégorie en question
     */
    void setCategory(int category);

    /**
     * Change l'indicatif de l'aéronef à la valeur donnée,
     *
     * @param callSign l'indicatif en question
     */
    void setCallSign(CallSign callSign);

    /**
     * Change la position de l'aéronef à la valeur donnée,
     *
     * @param position la position en question
     */
    void setPosition(GeoPos position);

    /**
     * Change l'altitude de l'aéronef à la valeur donnée,
     *
     * @param altitude l'altitude en question
     */
    void setAltitude(double altitude);

    /**
     * Change la vitesse de l'aéronef à la valeur donnée,
     *
     * @param velocity la vitesse de l'avion en question
     */
    void setVelocity(double velocity);

    /**
     * Change la direction de l'aéronef à la valeur donnée.
     *
     * @param trackOrHeading la direction de l'aéronef en question
     */
    void setTrackOrHeading(double trackOrHeading);
}
