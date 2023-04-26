package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

/**
 * La classe AircraftStateManager a pour but de garder à jour les états d'un ensemble d'aéronefs en fonction des
 * messages reçus d'eux
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class AircraftStateManager {

    private final Map<IcaoAddress, AircraftStateAccumulator<ObservableAircraftState>> map = new HashMap<>();

    private final Set<ObservableAircraftState> observableAircraftStates = new HashSet<>();

    private final AircraftDatabase aircraftDatabase;
    private final long MAX_TEMPS = Duration.ofMinutes(1).toNanos();

    private long lastMessageTimeStampNs;

    /**
     * Constructeur de AircraftStateManager qui prend comme argument la base de données contenant les caractéristiques
     * fixes des aéronefs
     *
     * @param aircraftDatabase les caractéristiques fixes des aéronefs
     */
    public AircraftStateManager(AircraftDatabase aircraftDatabase) {
        this.aircraftDatabase = aircraftDatabase;
    }

    /**
     * Méthode qui retourne l'ensemble observable, mais non modifiable, des états observables des aéronefs dont la
     * position est connue
     *
     * @return l'ensemble observable, mais non modifiable, des états observables des aéronefs dont la position est
     * connue
     */
    public ObservableSet<ObservableAircraftState> states() {
        return FXCollections.unmodifiableObservableSet(FXCollections.observableSet(observableAircraftStates));
    }

    /**
     * La méthode prend en argument un message et l'utilisant pour mettre à jour l'état de l'aéronef qui l'a envoyé
     * - créant cet état lorsque le message est le premier reçu de cet aéronef
     *
     * @param message le message en question
     * @throws IOException lorsque qu'il y a des problèmes d'entrée/sortie
     */
    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress icaoAddress = message.icaoAddress();
        if (map.containsKey(icaoAddress)) {
            map.get(icaoAddress).update(message);
            ObservableAircraftState aircraftState = map.get(icaoAddress).stateSetter();
            if (aircraftState.getPosition() != null) {
                observableAircraftStates.add(aircraftState);
            }
        } else {
            map.put(icaoAddress, new AircraftStateAccumulator<>
                    (new ObservableAircraftState(icaoAddress, aircraftDatabase.get(icaoAddress))));
        }
        lastMessageTimeStampNs = message.timeStampNs();
    }

    /**
     * Méthode qui supprime de l'ensemble des états observables tous ceux correspondant à des aéronefs dont aucun
     * message n'a été reçu dans la minute précédant la réception du dernier message passé à updateWithMessage
     */
    public void purge() {
        observableAircraftStates.removeIf(observableAircraftState -> lastMessageTimeStampNs -
                observableAircraftState.getLastMessageTimeStampNs()
                > MAX_TEMPS);
    }
}