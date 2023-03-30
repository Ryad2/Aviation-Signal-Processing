package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IcaoAddressTest {

    @Test
    void IcaoAddressIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            IcaoAddress test = new IcaoAddress("");
        });
    }

    @Test
    void IcaoAddressIsValid (){

        assertDoesNotThrow(() -> {IcaoAddress test = new IcaoAddress("000001");
        });
    }

    @Test
    void IcaoAddressIsNotValid() {
        assertThrows(IllegalArgumentException.class, () -> {
            IcaoAddress test = new IcaoAddress("1234567");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            IcaoAddress test = new IcaoAddress("TTTTTT");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            IcaoAddress test = new IcaoAddress("T11111");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            IcaoAddress test = new IcaoAddress("a11111");
        });
    }

    @Test
    void IcaoAddressLength (){
        IcaoAddress test = new IcaoAddress("AAAAAA");
        assertEquals(6, test.string().length());
        IcaoAddress test1 = new IcaoAddress("123456");
        assertEquals(6, test1.string().length());
    }

    /*
    @Test
    void IcaoAddressDontAcceptWithLowerCase() {

        for (int i = 0x000000; i <= 0xFFFFFF; i++) {
            String test = Integer.toHexString(i);
            test.length();
            if (test.matches(".*[a-z].*")) {
                assertThrows(IllegalArgumentException.class, () -> {
                    new IcaoAddress(test);
                });
            }
        }
    }
     */

    @Test
    void icaoAddressConstructorThrowsWithInvalidAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("00000a");
        });
    }

    @Test
    void icaoAddressConstructorThrowsWithEmptyAddress() {
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("");
        });
    }

    @Test
    void icaoAddressConstructorAcceptsValidAddress() {
        assertDoesNotThrow(() -> {
            new IcaoAddress("ABCDEF");
        });
    }
}