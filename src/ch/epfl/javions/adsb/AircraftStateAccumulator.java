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
        if(this.icaoAddress==null ) {
            this.icaoAddress=message.icaoAddress();
        }
        if (this.icaoAddress.toString().equals(message.icaoAddress().toString())) {//Demander au assistant
            stateSetter.setLastMessageTimeStampNs(message.timeStampNs());

                switch (message) {
                    case AircraftIdentificationMessage aim -> {
                        stateSetter.setCategory(aim.category());
                        stateSetter.setCallSign(aim.callSign());
                    }

                    case AirbornePositionMessage apm -> {

                        if (apm.parity() == 1) positionOdd = apm;
                        else positionEven = apm;

                        if (positionEven != null && positionOdd != null) {
                        //if (positionEven != null && positionOdd != null && isValidPosition()) {
                            int k = mostRecentPosition(positionEven, positionOdd);
                            GeoPos position = CprDecoder.decodePosition(positionEven.x(), positionEven.y(),
                                    positionOdd.x(), positionOdd.y(), k);

                           // if(position != null) System.out.println("thiss angel is not null");
                           // if(position == null) System.out.println("this shit is null");
                            stateSetter.setPosition(position);
                        }
                    }

                /*

                stateSetter.setAltitude(apm.altitude());

                if (apm.parity() == 0){
                     if (positionOdd != null && ((apm.timeStampNs() - positionOdd.timeStampNs()) <= Math.pow(10,10))){
                        stateSetter.setPosition(CprDecoder.decodePosition(apm.x(),
                                apm.y(), positionOdd.x(), positionOdd.y(), 0));
                    }
                    positionEven = apm;
                }
                else {
                    if (positionEven != null && ((apm.timeStampNs() - positionEven.timeStampNs()) <= Math.pow(10,10))) {
                        stateSetter.setPosition(CprDecoder.decodePosition(positionEven.x(), positionEven.y(), apm.x(),
                                apm.y(), 1));

                    }
                    positionOdd = apm;
                }

                 */


            /*if (positionEven != null && positionOdd != null && isValidPosition()) {
                GeoPos position = CprDecoder.decodePosition(positionEven.x(),
                        positionEven.y(), positionOdd.x(), positionOdd.y(),
                        mostRecentPosition(positionEven, positionOdd));
                stateSetter.setAltitude(apm.altitude());
                if(position !=null ){
                    stateSetter.setPosition(position);
                }
            }*/

                    case AirborneVelocityMessage avm -> {
                        //stateSetter.setLastMessageTimeStampNs(avm.timeStampNs());
                        stateSetter.setVelocity(avm.speed());
                        stateSetter.setTrackOrHeading(avm.trackOrHeading());
                    }

                    default -> throw new Error("Valeur inattendue: " + message);//TODO : Demander au assistant
                }
            }
    }

    private int mostRecentPosition(AirbornePositionMessage positionEven, AirbornePositionMessage positionOdd) {
        if (positionEven.timeStampNs() > positionOdd.timeStampNs()) return 0;
        else return 1;
    }
    private boolean isValidPosition() {
        return Math.abs(positionEven.timeStampNs() - positionOdd.timeStampNs()) <= Math.pow(10, 10);
    }



    /*private void setInParity(Message message){
        if (message instanceof AirbornePositionMessage){
            if (((AirbornePositionMessage) message).parity()==1){
                positionOdd = (AirbornePositionMessage) message;
            }else{
                positionEven = (AirbornePositionMessage) message;
            }
        }
    }

    private void identification(int category, CallSign callSign){

        this.identification = new AircraftIdentificationMessage(this.identification.timeStampNs(),this.identification.icaoAddress(), category, callSign);
    }

    private AirbornePositionMessage position(double altitude) {

        return new AirbornePositionMessage(this.positionEven.timeStampNs(), this.positionEven.icaoAddress(), altitude, this.positionEven.parity(), this.positionEven.x(), this.positionEven.y());
    }

    private void velocity(double trackOrHeading, double speed) {

        this.velocity = new AirborneVelocityMessage(this.velocity.timeStampNs(), this.velocity.icaoAddress(), speed, trackOrHeading);
    }

    /*private int isValidPositionMessage (int value,int threshold){
        ArrayList<Integer> buffer;
        buffer = new ArrayList<>();
        buffer.add(value);


        if (buffer.size() > 0 && buffer.get(buffer.size() - 1) > threshold) {
            int lastValue = buffer.get(buffer.size() - 1);
            buffer.clear();
            return lastValue;
        } else {
            return -1;
        }
    }

        @Override
        public void setLastMessageTimeStampNs ( long timeStampNs){

        }

        @Override
        public void setCategory ( int category){

        }

        @Override
        public void setCallSign (CallSign callSign){

        }

        @Override
        public void setPosition (GeoPos position){

        }

        @Override
        public void setAltitude ( double altitude){

        }

        @Override
        public void setVelocity ( double velocity){

        }

        @Override
        public void setTrackOrHeading ( double trackOrHeading){

        }*/
}