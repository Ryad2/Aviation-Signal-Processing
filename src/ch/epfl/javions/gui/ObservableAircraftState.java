package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * La classe ObservableAircraftState représente l'état d'un aéronef. Cet état a la caractéristique d'être observable
 * au sens du patron de conception Observer
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class ObservableAircraftState implements AircraftStateSetter {

    private final IcaoAddress icaoAddress;
    private final AircraftData aircraftData;
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty();
    private final IntegerProperty category = new SimpleIntegerProperty();
    private final ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>();
    private final ObjectProperty<GeoPos> position = new SimpleObjectProperty<>();
    private final ObservableList<AirbornePos> observableTrajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> unmodifiableTrajectory =
            FXCollections.unmodifiableObservableList(observableTrajectory);
    private final DoubleProperty altitude = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocity = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty();
    private long lastPositionMessageTimeStampNs;

    /**
     * Constructeur de ObservableAircraftState
     *
     * @param icaoAddress  l'adresse OACI de l'aéronef dont l'état est destiné
     *                     à être représenté par l'instance à créer
     * @param aircraftData les caractéristiques fixes de cet aéronef,
     *                     provenant de la base de données mictronics
     */
    public ObservableAircraftState(IcaoAddress icaoAddress, AircraftData aircraftData) {
        this.icaoAddress = icaoAddress;
        this.aircraftData = aircraftData;
    }

    /**
     * Méthode d'accès de IcaoAddress
     *
     * @return l'IcaoAddress
     */
    public IcaoAddress getIcaoAddress() {
        return icaoAddress;
    }

    /**
     * Méthode d'accès de AircraftData
     *
     * @return l'AircraftData
     */
    public AircraftData getAircraftData() {
        return aircraftData;
    }

    /**
     * Méthode d'accès de lastMessageTimeStampNs en lecture seule
     *
     * @return lastMessageTimeStampsNs
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    /**
     * Methode d'accès de lastMessageTimeStampsNs
     *
     * @return l'horodatage du dernier message reçu de l'aéronef, en nanosecondes
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    /**
     * Méthode d'accès à LastMessageTimeStampNs
     *
     * @param timeStampNs nouvel horodatage
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }

    /**
     * Méthode d'accès à category en lecture seule
     *
     * @return la catégorie en question
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return this.category;
    }

    /**
     * Méthode d'accès de Category
     *
     * @return la catégorie de l'aéronef
     */
    public int getCategory() {
        return category.get();
    }

    /**
     * Méthode d'accès à category
     *
     * @param category la catégorie en question
     */
    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    /**
     * Méthode d'accès à CallSing en lecture seule
     *
     * @return callSign
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    /**
     * Méthode d'accès de CallSign
     *
     * @return l'indicatif de l'aéronef
     */
    public CallSign getCallSign() {
        return callSign.get();
    }

    /**
     * Méthode de modification de callSign
     *
     * @param callSign l'indicatif en question
     */
    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    /**
     * Méthode d'accès à GeoPos en lecture seul
     *
     * @return GeoPos
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    /**
     * Méthode d'accès de position
     *
     * @return la position de l'aéronef à la surface de la Terre (longitude et latitude, en radians)
     */
    public GeoPos getPosition() {
        return position.get();
    }

    /**
     * Méthode de modification de position. La méthode vérifie d'abord que la position n'est pas nulle puis modifie la
     * position, qui est un pair entre la position (de type GeoPos) et l'altitude (de type Double)
     *
     * @param position la position en question
     */
    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        if (!Double.isNaN(getAltitude())) {
            AirbornePos airbornePos = new AirbornePos(position, getAltitude());
            observableTrajectory.add(airbornePos);
            lastPositionMessageTimeStampNs = getLastMessageTimeStampNs();
        }
    }

    /**
     * Méthode d'accès à l'altitude en lecture seule
     *
     * @return l'altitude
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    /**
     * Méthode d'accès de l'altitude
     *
     * @return l'altitude de l'aéronef, en mètres
     */
    public double getAltitude() {
        return altitude.get();
    }

    /**
     * Méthode de modification de l'altitude
     *
     * @param altitude l'altitude en question
     */
    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if (getPosition() != null) addAirbornePos(getPosition(), altitude);
    }

    /**
     * Méthode d'accès à Velocity en lecture seule
     *
     * @return la vitesse de l'aéronef en mètres par seconde
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    /**
     * Méthode d'accès de Velocity
     *
     * @return la vitesse de l'aéronef, en mètres par seconde
     */
    public double getVelocity() {
        return velocity.get();
    }

    /**
     * Méthode de modification de Velocity
     *
     * @param velocity la vitesse de l'avion en question
     */
    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    /**
     * Méthode d'accès à trackOrHeading en lecture seule
     *
     * @return trackOrHeading
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    /**
     * Méthode d'accès de TrackOrHeading
     *
     * @return la route ou le cap de l'aéronef, en radians
     */
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    /**
     * Méthode de modification de trackOrHeading
     *
     * @param trackOrHeading la direction de l'aéronef en question
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    /**
     * Méthode d'accès à la trajectoire en lecture seule
     *
     * @return la trajectoire
     */
    public ObservableList<AirbornePos> getTrajectory() {
        return unmodifiableTrajectory;
    }

    /**
     * Méthode d'accès à la trajectoire observable en lecture
     * seule qui update la trajectoire de l'aéronef
     *
     * @param position la position de l'aéronef
     * @param altitude l'altitude de l'aéronef
     */
    private void addAirbornePos(GeoPos position, double altitude) {
        if (getLastMessageTimeStampNs() == lastPositionMessageTimeStampNs) {
            AirbornePos airbornePos = new AirbornePos(position, altitude);
            observableTrajectory.set(observableTrajectory.size() - 1, airbornePos);
        } else {
            if (observableTrajectory.isEmpty()) {
                AirbornePos airbornePos = new AirbornePos(position, altitude);
                observableTrajectory.add(airbornePos);
                lastPositionMessageTimeStampNs = getLastMessageTimeStampNs();
            }
        }
    }

    /**
     * Enregistrement qui sert à représenter les positions de l'avion dans l'espace.
     * Chaque élément de la trajectoire est une paire constituée d'une position
     * à la surface de la Terre ainsi qu'une altitude
     *
     * @param position position à la surface de la Terre (longitude et latitude)
     * @param altitude l'altitude de l'aéronef
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }
}