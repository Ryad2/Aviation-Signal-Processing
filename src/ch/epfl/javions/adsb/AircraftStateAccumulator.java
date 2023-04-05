package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;


/**
 * Représente un « accumulateur d'état d'aéronef », c'est-à-dire un objet accumulant les messages ADS-B provenant d'un
 * seul aéronef afin de déterminer son état au cours du temps.
 *
 * @param <T> paramètre de type de AircraftStateAccumulator
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public class AircraftStateAccumulator<T extends AircraftStateSetter> {//TODO ask sur ed si doit etre final

    private final static long MAX_TIME_DIFF_NS = 10_000_000_000L;
    private final T stateSetter;
    private AirbornePositionMessage positionEven;
    private AirbornePositionMessage positionOdd;


    /**
     * Constructeur de AircraftStateAccumulator qui retourne un accumulateur d'état d'aéronef associé à l'état
     * modifiable donné
     *
     * @param stateSetter l'état modifiable associé à l'accumulateur
     * @throws NullPointerException si le stateSetter est nul.
     */
    public AircraftStateAccumulator(T stateSetter) {

        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }


    /**
     * Retourne l'état modifiable de l'aéronef passé à son constructeur
     *
     * @return l'état modifiable de l'aéronef passé à son constructeur
     */
    public T stateSetter() {
        return stateSetter;
    }


    /**
     * Met à jour l'état modifiable en fonction du message donné, en appelant, pour tous les types de message, sa
     * méthode setLastMessageTimeStampNs, ainsi que :
     * s'il s'agit d'un message d'identification et de catégorie, setCategory et setCallSign,
     * s'il s'agit d'un message de positionnement en vol, setAltitude et, si la position peut être déterminée,
     * setPosition,
     * s'il s'agit d'un message de vitesse en vol, setVelocity et setTrackOrHeading.
     *
     * @param message le message ADS-B à traiter
     */
    public void update(Message message) {

        stateSetter.setLastMessageTimeStampNs(message.timeStampNs());
        switch (message) {

            case AircraftIdentificationMessage aim -> {
                stateSetter.setCategory(aim.category());
                stateSetter.setCallSign(aim.callSign());
            }

            case AirbornePositionMessage apm -> {
                stateSetter.setAltitude(apm.altitude());
                if (apm.parity() == 1) positionOdd = apm;
                else positionEven = apm;

                if (positionEven != null && positionOdd != null && isValidPosition()) {

                    GeoPos position = CprDecoder.decodePosition(positionEven.x(), positionEven.y(),
                            positionOdd.x(), positionOdd.y(), apm.parity());
                    if (position != null) stateSetter.setPosition(position);
                }
            }

            case AirborneVelocityMessage avm -> {
                stateSetter.setVelocity(avm.speed());
                stateSetter.setTrackOrHeading(avm.trackOrHeading());
            }

            default -> throw new Error();
        }
    }


    /**
     * Retourne vrai si la différence de temps entre les deux derniers messages de positionnement est inférieure à
     * 10 secondes, ce qui signifie que la position peut être déterminée, sinon retourne faux
     *
     * @return vrai si la différence de temps entre les deux derniers messages de positionnement est inférieure à
     * 10 secondes
     */
    private boolean isValidPosition() {
        return Math.abs(positionEven.timeStampNs() - positionOdd.timeStampNs()) <= MAX_TIME_DIFF_NS;
    }
}