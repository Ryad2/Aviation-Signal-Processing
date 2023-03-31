package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public class AircraftStateAccumulator<T extends  AircraftStateSetter > {

    private final T stateSetter;

    private AirbornePositionMessage positionEven;
    private AirbornePositionMessage positionOdd;
    private IcaoAddress icaoAddress;

    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter, "StateSetter ne peut pas être nul");
        this.stateSetter = stateSetter;
    }

    public T stateSetter() {
        return stateSetter;
    }

    public void update(Message message) {
        if(this.icaoAddress == null) { //Demander au stef
            this.icaoAddress=message.icaoAddress();
        }
        if (this.icaoAddress.toString().equals(message.icaoAddress().toString())) {//Demander au stef

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

                        if(position != null) stateSetter.setPosition(position);
                    }
                }

                case AirborneVelocityMessage avm -> {
                    stateSetter.setVelocity(avm.speed());
                    stateSetter.setTrackOrHeading(avm.trackOrHeading());
                }

                default -> throw new Error();
            }
        }
    }

    private int mostRecentPosition(AirbornePositionMessage positionEven, AirbornePositionMessage positionOdd) {
        if (positionEven.timeStampNs() > positionOdd.timeStampNs()) return 0;
        else return 1;
    }
    private boolean isValidPosition() {
        return Math.abs(positionEven.timeStampNs() - positionOdd.timeStampNs()) <= 10_000_000_000l;
    }
}