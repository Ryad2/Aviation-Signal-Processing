package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.AircraftStateAccumulator;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.IOException;
import java.util.*;

public final class AircraftStateManager {

    private final Map<IcaoAddress, AircraftStateAccumulator> map = new HashMap<>();

    private final Set<ObservableAircraftState> observableAircraftStates = new HashSet<>();

    private final AircraftDatabase aircraftDatabase;

    private long lastMessageTimeStampNs;

    public AircraftStateManager(AircraftDatabase aircraftDatabase){
        this.aircraftDatabase = aircraftDatabase;
    }

    public ObservableSet<ObservableAircraftState> states(){
        return FXCollections.unmodifiableObservableSet(FXCollections.observableSet(observableAircraftStates));
    }

    public void updateWithMessage(Message message) throws IOException {
        IcaoAddress icaoAddress = message.icaoAddress();
        if (map.containsKey(icaoAddress)){
            map.get(icaoAddress).update(message);
            ObservableAircraftState aircraftState = (ObservableAircraftState) map.get(icaoAddress).stateSetter();
            if (aircraftState.getPosition() != null){
                observableAircraftStates.add(aircraftState);
            }
        } else {
            map.put(icaoAddress, new AircraftStateAccumulator<>
                    (new ObservableAircraftState(icaoAddress,aircraftDatabase.get(icaoAddress))));
        }
        lastMessageTimeStampNs = message.timeStampNs();
    }

    public void purge(){
        observableAircraftStates.removeIf(observableAircraftState ->
                observableAircraftState.getLastMessageTimeStampNs() - lastMessageTimeStampNs
                        >= Units.Time.MINUTE * 10E9);
    }
}