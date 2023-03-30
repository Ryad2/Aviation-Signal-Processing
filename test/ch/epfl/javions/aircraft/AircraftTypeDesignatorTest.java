package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftTypeDesignatorTest {

    @Test
    void AircraftTypeDesignatorIsNotEmpty() {

        assertDoesNotThrow(() -> {AircraftTypeDesignator test = new AircraftTypeDesignator("");
        });
    }

    @Test
    void AircraftTypeDesignatorIsNotValid() {
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftTypeDesignator test = new AircraftTypeDesignator("A20J4");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftTypeDesignator test = new AircraftTypeDesignator("A45j");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftTypeDesignator test = new AircraftTypeDesignator("\\");
        });
    }

    @Test
    void AircraftTypeDesignatorIsValid() {

        assertDoesNotThrow(() -> {AircraftTypeDesignator test = new AircraftTypeDesignator("A20J");
        });
        assertDoesNotThrow(() -> {AircraftTypeDesignator test = new AircraftTypeDesignator("A20N");
        });
    }

    @Test
    void AicraftTypeDesignatorLength (){
        AircraftTypeDesignator test = new AircraftTypeDesignator("AA");
        assertEquals(2, test.string().length());
        AircraftTypeDesignator test1 = new AircraftTypeDesignator("AAA");
        assertEquals(3, test1.string().length());
        AircraftTypeDesignator test2 = new AircraftTypeDesignator("AAAA");
        assertEquals(4, test2.string().length());
    }

    @Test
    void AircraftTypeDesignatorDontAcceptWithLowerCase() {

        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("AjO3");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration test = new AircraftRegistration("jjj");
        });
    }

    @Test
    void aircraftTypeDesignatorConstructorThrowsWithInvalidTypeDesignator() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftTypeDesignator("ABCDE");
        });
    }

    @Test
    void aircraftTypeDesignatorConstructorAcceptsEmptyTypeDesignator() {
        assertDoesNotThrow(() -> {
            new AircraftTypeDesignator("");
        });
    }

    @Test
    void aircraftTypeDesignatorConstructorAcceptsValidTypeDesignator() {
        assertDoesNotThrow(() -> {
            new AircraftTypeDesignator("BCS3");
        });
    }
}