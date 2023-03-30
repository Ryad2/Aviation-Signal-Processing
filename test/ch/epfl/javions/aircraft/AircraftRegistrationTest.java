package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AircraftRegistrationTest {
    @Test
    void AircraftRegistrationIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("");
        });
    }

    @Test
    void AircraftRegistrationIsNotValid() {
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("HB_Jdc");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("HB-#JDC");
        });
    }

    @Test
    void AircraftRegistrationIsValid () {

        assertDoesNotThrow(() -> {AircraftRegistration test = new AircraftRegistration("HB-JDC");
        });
        assertDoesNotThrow(() -> {AircraftRegistration test = new AircraftRegistration("HB/JDC-001");
        });
    }

    @Test
    void AircraftRegistrationDontAcceptWithLowerCase() {

        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("AjO3");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("jjj");
        });
    }

    @Test
    void aircraftRegistrationConstructorThrowsWithInvalidRegistration() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftRegistration("abc");
        });
    }

    @Test
    void aircraftRegistrationConstructorThrowsWithEmptyRegistration() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftRegistration("");
        });
    }

    @Test
    void aircraftRegistrationConstructorAcceptsValidRegistration() {
        assertDoesNotThrow(() -> {
            new AircraftRegistration("F-HZUK");
        });
    }

}
