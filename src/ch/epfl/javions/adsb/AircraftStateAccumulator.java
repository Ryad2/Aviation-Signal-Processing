package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

public class AircraftStateAccumulator<T extends AircraftStateSetter> {
    private final static long MAX_TIME_DIFF_NS = 10_000_000_000L;
    private final T stateSetter;
    private AirbornePositionMessage positionEven;
    private AirbornePositionMessage positionOdd;
    private IcaoAddress icaoAddress;

    public AircraftStateAccumulator(T stateSetter) {
        Objects.requireNonNull(stateSetter);
        this.stateSetter = stateSetter;
    }

    public T stateSetter() {
        return stateSetter;
    }

    public void update(Message message) {
        if (this.icaoAddress == null) {
            this.icaoAddress = message.icaoAddress();
        }
        if (this.icaoAddress.toString().equals(message.icaoAddress().toString())) {

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
    }

    private boolean isValidPosition() {
        return Math.abs(positionEven.timeStampNs() - positionOdd.timeStampNs()) <= MAX_TIME_DIFF_NS;
    }
}