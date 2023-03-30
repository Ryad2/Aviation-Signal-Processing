package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftDescriptionTest {

    @Test
    void AircraftDescriptionIsNotEmpty() {

        assertDoesNotThrow(() -> {AircraftDescription test = new AircraftDescription("");
        });
    }

    @Test
    void AircraftDescriptionLength (){
        AircraftDescription test = new AircraftDescription("A3-");
        assertEquals(3, test.string().length());
        AircraftDescription test1 = new AircraftDescription("-4-");
        assertEquals(3, test1.string().length());
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftDescription test2 = new AircraftDescription("A204");
        });
    }

    @Test
    void AircraftDescriptionIsNotValid() {
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftDescription test = new AircraftDescription("Z4E");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftDescription test = new AircraftDescription("A4A");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftDescription test = new AircraftDescription("EEE");
        });
    }

    @Test
    void AircraftDescriptionIsValid() {
        assertDoesNotThrow(() -> {AircraftDescription test = new AircraftDescription("L2J");
        });
        assertDoesNotThrow(() -> {AircraftDescription test = new AircraftDescription("-2-");
        });
    }

    @Test
    void AircraftDescriptionDontAcceptWithLowerCase() {

        assertThrows(IllegalArgumentException.class, () -> {
            AircraftDescription test = new AircraftDescription("A4t");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftDescription test = new AircraftDescription("a4E");
        });
    }

    @Test
    void aircraftDescriptionConstructorThrowsWithInvalidDescription() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftDescription("abc");
        });
    }

    @Test
    void aircraftDescriptionConstructorAcceptsEmptyDescription() {
        assertDoesNotThrow(() -> {
            new AircraftDescription("");
        });
    }

    @Test
    void aircraftDescriptionConstructorAcceptsValidDescription() {
        assertDoesNotThrow(() -> {
            new AircraftDescription("A0E");
        });
    }

}

