package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CallSignTest {

    @Test
    public void testValidCallSignCreation() {
        String validCallSign = "SWA1234";
        CallSign callSign = new CallSign(validCallSign);

        assertNotNull(callSign);
        assertEquals(validCallSign.trim(), callSign.string());
    }

    @Test
    public void testInvalidCallSignCreation() {
        String invalidCallSign = "A1234ABCDEFG";
        assertThrows(IllegalArgumentException.class, () -> new CallSign(invalidCallSign));
    }

    @Test
    public void testEqualsMethod() {
        CallSign callSign1 = new CallSign("ABC123 ");
        CallSign callSign2 = new CallSign("XYZ789 ");

        assertFalse(callSign1.equals(callSign2));
        assertTrue(callSign1.equals(callSign1));
    }

    @Test
    public void testHashCode() {
        CallSign callSign = new CallSign("SWA1234");
        assertEquals("SWA1234".hashCode(), callSign.hashCode());
    }

    @Test
    void callSignConstructorThrowsWithInvalidCallSign() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CallSign("callsign");
        });
    }

    @Test
    void callSignConstructorAcceptsEmptyCallSign() {
        assertDoesNotThrow(() -> {
            new CallSign("");
        });
    }

    @Test
    void callSignConstructorAcceptsValidCallSign() {
        assertDoesNotThrow(() -> {
            new CallSign("AFR39BR");
        });
    }

}