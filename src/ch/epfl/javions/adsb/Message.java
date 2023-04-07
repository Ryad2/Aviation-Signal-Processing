package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Interface qui a pour but d'être implémentée dans toutes les classes représentant des messages
 * ADS-B "analysés"
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public interface Message {
    /**
     * Retourne l'horodatage du message, en nanosecondes,
     *
     * @return l'horodatage du message en question
     */
    long timeStampNs();

    /**
     * Retourne l'adresse OACI de l'expéditeur du message.
     *
     * @return l'adresse OACI de l'expédition du message en question
     */
    IcaoAddress icaoAddress();
}
