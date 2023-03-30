package ch.epfl.javions.aircraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;



public class WakeTurbulenceCategoryTest {

    @Test
    public void testOf() {
        assertEquals(WakeTurbulenceCategory.LIGHT, WakeTurbulenceCategory.of("L"));
        assertEquals(WakeTurbulenceCategory.MEDIUM, WakeTurbulenceCategory.of("M"));
        assertEquals(WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.of("H"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of(""));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("X"));
    }

    @Test
    void wakeTurbulenceCategoryOfWorks() {
        assertEquals(WakeTurbulenceCategory.LIGHT, WakeTurbulenceCategory.of("L"));
        assertEquals(WakeTurbulenceCategory.MEDIUM, WakeTurbulenceCategory.of("M"));
        assertEquals(WakeTurbulenceCategory.HEAVY, WakeTurbulenceCategory.of("H"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("X"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("l"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("m"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("h"));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of(""));
        assertEquals(WakeTurbulenceCategory.UNKNOWN, WakeTurbulenceCategory.of("LIGHT"));
    }


}
