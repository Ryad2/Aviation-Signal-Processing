package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.util.Objects;

import static java.util.Objects.hash;
import static org.junit.jupiter.api.Assertions.*;

public class AircraftDataTest {

    @Test
    public void AircraftDatatestValidCreation() {
        AircraftRegistration reg = new AircraftRegistration("HB-JDC");
        AircraftTypeDesignator td = new AircraftTypeDesignator("A20N");
        AircraftDescription desc = new AircraftDescription("L2J");
        WakeTurbulenceCategory wtc = WakeTurbulenceCategory.MEDIUM;

        AircraftData data = new AircraftData(reg, td, "A320-200", desc, wtc);

        assertNotNull(data);
        assertEquals(reg, data.registration());
        assertEquals(td, data.typeDesignator());
        assertEquals("A320-200", data.model());
        assertEquals(desc, data.description());
        assertEquals(wtc, data.wakeTurbulenceCategory());
    }

    @Test
    public void testNullParameters() {
        assertThrows(NullPointerException.class, () ->
                new AircraftData(null, null, null,
                        null, null));
    }

    @Test
    public void testEqualsMethod() {
        AircraftRegistration reg1 = new AircraftRegistration("HB-JDC");
        AircraftTypeDesignator td1 = new AircraftTypeDesignator("A20N");
        AircraftDescription desc1 = new AircraftDescription("L2J");
        WakeTurbulenceCategory wtc1 = WakeTurbulenceCategory.MEDIUM;

        AircraftData data1 = new AircraftData(reg1, td1, "A320-200", desc1, wtc1);

        AircraftRegistration reg2 = new AircraftRegistration("XYZ789");
        AircraftTypeDesignator td2 = new AircraftTypeDesignator("A320");
        AircraftDescription desc2 = new AircraftDescription("L2J");
        WakeTurbulenceCategory wtc2 = WakeTurbulenceCategory.MEDIUM;

        AircraftData data2 = new AircraftData(reg2, td2, "A320-200", desc2, wtc2);

        assertFalse(data1.equals(data2));
        assertTrue(data1.equals(data1));

    }

    @Test
    void aircraftDataConstructorThrowsWithNullAttribute() {
        var registration = new AircraftRegistration("HB-JAV");
        var typeDesignator = new AircraftTypeDesignator("B738");
        var model = "Boeing 737-800";
        var description = new AircraftDescription("L2J");
        var wakeTurbulenceCategory = WakeTurbulenceCategory.LIGHT;
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(null, typeDesignator, model, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, null, model, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, null, description, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, model, null, wakeTurbulenceCategory);
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftData(registration, typeDesignator, model, description, null);
        });
    }
}