package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public final class ObservableAircraftState implements AircraftStateSetter {

    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty();
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>();
    private final ObjectProperty<GeoPos> position = new SimpleObjectProperty<>();
    private final DoubleProperty altitude = new SimpleDoubleProperty();
    private final DoubleProperty velocity = new SimpleDoubleProperty();
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty();
    private final ListProperty<AirbornePos> trajectory1 = new SimpleListProperty<>();
    private final List<AirbornePos> trajectory = new ArrayList<>();
    private final ObservableList<AirbornePos> observableTrajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> unmodifiableTrajectory =
            FXCollections.unmodifiableObservableList(observableTrajectory);
    private long lastPositionMessageTimeStampNs;

    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData){
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
    }

    public IcaoAddress getIcaoAddress(){
        return icaoAddress;
    }

    public AircraftData getAircraftData() {
        return aircraftData;
    }

    public record AirbornePos(GeoPos position, double altitude){}

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty(){
        return lastMessageTimeStampNs;
    }

    public long getLastMessageTimeStampNs(){
        return lastMessageTimeStampNs.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }

    public ReadOnlyIntegerProperty categoryProperty(){
        return category;
    }

    public int getCategory(){
        return category.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public ReadOnlyObjectProperty<CallSign> callSignProperty(){
        return callSign;
    }

    public CallSign getCallSign(){
        return callSign.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    public ReadOnlyObjectProperty<GeoPos> positionProperty(){
        return position;
    }

    public GeoPos getPosition(){
        return position.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        if (position != null){
            this.position.set(position);
            addAirbornePos(position,getAltitude());
        }
    }

    public ReadOnlyDoubleProperty altitudeProperty(){
        return altitude;
    }

    public double getAltitude(){
        return altitude.get();
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        addAirbornePos(getPosition(), altitude);
    }

    public ReadOnlyDoubleProperty velocityProperty(){
        return velocity;
    }

    public double getVelocity(){
        return velocity.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty(){
        return trackOrHeading;
    }

    public double getTrackOrHeading(){
        return trackOrHeading.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public ReadOnlyListProperty<AirbornePos> trajectoryProperty(){
        return trajectory1;
    }

    public List<AirbornePos> getTrajectory(){
        return unmodifiableTrajectory;
    }

    private void addAirbornePos(GeoPos position, double altitude){
        if (getLastMessageTimeStampNs() == lastPositionMessageTimeStampNs){
            AirbornePos airbornePos = new AirbornePos(position,altitude);
            trajectory.set(trajectory.size()-1,airbornePos);
        } else {
            if (observableTrajectory.isEmpty() ||
                    position.latitude() != observableTrajectory.get(trajectory.size()-1).position.latitude() ||
                    position.longitude() != observableTrajectory.get(trajectory.size()-1).position.longitude()){
                AirbornePos airbornePos = new AirbornePos(position,altitude);
                trajectory.add(airbornePos);
                lastPositionMessageTimeStampNs = getLastMessageTimeStampNs();
            }
        }
    }
}