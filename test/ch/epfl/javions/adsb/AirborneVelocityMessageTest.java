package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AirborneVelocityMessageTest {

    @Test
    void ExempleDuProfAirborneVelocityMessage() throws IOException {

        int count= 0;
        String f = "resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {

            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                if (m.typeCode() == 19){
                    System.out.println(AirborneVelocityMessage.of(m));
                    count++;
                }
            }
            assertEquals(147, count);
        }
    }

    @Test
    void testSubType12(){
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(1000, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));
    }

    @Test
    void testSubType34() {

        var message1  = RawMessage.of(100, ByteString.ofHexadecimalString("8DA05F219B06B6AF189400CBC33F").getBytes());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(10000, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));
    }
}