package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AirborneVelocityMessageTest {

    @Test
    void ExempleDuProfAirborneVelocityMessage() throws IOException {

        int count = 0;
        String f = "resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {

            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                if (m.typeCode() == 19) {
                    System.out.println(AirborneVelocityMessage.of(m));
                    count++;
                }
            }
            assertEquals(147, count);
        }
    }

    @Test
    void GenerallyWorksWithNoCondition() throws IOException {
        try (InputStream s = new FileInputStream(("resources/samples_20230304_1442.bin"))) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int i = 0;
            while ((m = d.nextMessage()) != null) {
                AirborneVelocityMessage avm = AirborneVelocityMessage.of(m);
                if (avm != null) {
                    System.out.println(avm);
                    ++i;
                }
            }
            assertEquals(230,i);
        }
    }

    @Test
    void SubType1Or2Works1(){
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(1000, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));
    }

    @Test
    void SubType1Or2Works2(){
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(1000, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));
    }

    @Test
    void SubType3Or4Works1() {

        var message1  = RawMessage.of(100, ByteString.ofHexadecimalString("8DA05F219B06B6AF189400CBC33F").getBytes());
        assert message1 != null;
        System.out.println(AirborneVelocityMessage.of(message1));


        String message = "8DA05F219B06B6AF189400CBC33F";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timestamps = 0;
        RawMessage test = new RawMessage(timestamps, byteString);
        AirborneVelocityMessage avm = AirborneVelocityMessage.of(test);
        assertEquals(192.91666666666669 , avm.speed());
        assertEquals(4.25833066717054 , avm.trackOrHeading());
    }

    @Test
    void SubType3Or4Works2() {

        var message2  = RawMessage.of(10000, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message2 != null;
        System.out.println(AirborneVelocityMessage.of(message2));

        String message = "8D485020994409940838175B284F";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timestamps = 0;
        RawMessage test = new RawMessage(timestamps, byteString);
        AirborneVelocityMessage avm = AirborneVelocityMessage.of(test);
        assertEquals(81.90013721178153 , avm.speed(),1e-10);
        assertEquals(3.191864725587521 , avm.trackOrHeading(),1e-10);
    }

    @Test
    void Subtype2Works() {
        String message =  "8D4B1A00EA0DC89E8F7C0857D5F5";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timeStampsNs = 0;
        RawMessage testMessage = new RawMessage(timeStampsNs, byteString);
        AirborneVelocityMessage avm = AirborneVelocityMessage.of(testMessage);
        System.out.println(avm);
    }

    @Test
    void AirborneVelocityMessageReturnsNullWithInvalidMessagesQuiMarchePas(){
        RawMessage rm1 = new RawMessage(0, ByteString.ofHexadecimalString("8D485020994409800838175B284F"));
        RawMessage rm2 = new RawMessage(0, ByteString.ofHexadecimalString("8D485020994400140838175B284F"));
        RawMessage rm3 = new RawMessage(0, ByteString.ofHexadecimalString("8D4850209C4609800838175B284F"));
        RawMessage rm4 = new RawMessage(0, ByteString.ofHexadecimalString("8D4850209C4409940838175B284F"));

        assertNull(AirborneVelocityMessage.of(rm1));//vns=0
        assertNull(AirborneVelocityMessage.of(rm2));//vew=0
        assertNull(AirborneVelocityMessage.of(rm3));//as==0
       // assertNull(AirborneVelocityMessage.of(rm4));//sh==0;
    }

    @Test
    void AirborneVelocityMessageReturnsNullWithInvalidMessages(){
        RawMessage rm1 = new RawMessage(0, ByteString.ofHexadecimalString("8D485020994409800838175B284F"));
        RawMessage rm2 = new RawMessage(0, ByteString.ofHexadecimalString("8D485020994400140838175B284F"));
        RawMessage rm3 = new RawMessage(0, ByteString.ofHexadecimalString("8D4850209C4609800838175B284F"));
        RawMessage rm4 = new RawMessage(0, ByteString.ofHexadecimalString("8bfffffffffffffffbffffffffff"));

        assertNull(AirborneVelocityMessage.of(rm1));
        assertNull(AirborneVelocityMessage.of(rm2));
        assertNull(AirborneVelocityMessage.of(rm3));
        assertNull(AirborneVelocityMessage.of(rm4));
    }

    @Test
    void AirborneVelocityMessageWorksOnGivenValues() {
        RawMessage rm = new RawMessage(0, ByteString.ofHexadecimalString("8D485020994409940838175B284F"));
        AirborneVelocityMessage message1 = AirborneVelocityMessage.of(rm);

        double velocity = 81.90013721178154;
        double track = 3.1918647255875205;
        assertEquals(velocity, message1.speed());
        assertEquals(track, message1.trackOrHeading());
    }

    @Test
    void AirborneVelocityMessageWorksOnGivenValues2(){
        RawMessage rm = new RawMessage(0, ByteString.ofHexadecimalString("8DA05F219B06B6AF189400CBC33F"));
        AirborneVelocityMessage message = AirborneVelocityMessage.of(rm);

        double velocity = 192.91666666666669;
        double track = 4.25833066717054;
        assertEquals(velocity, message.speed());
        assertEquals(track, message.trackOrHeading());

    }

    @Test
    void AirborneVelocityMessageWorksOnGivenValues3(){
        RawMessage rm = new RawMessage(0, ByteString.ofHexadecimalString("8DA05F219C06B6AF189400CBC33F"));
        AirborneVelocityMessage message = AirborneVelocityMessage.of(rm);

        double velocity = 4*192.91666666666669;
        double track = 4.25833066717054;
        assertEquals(velocity, message.speed());
        assertEquals(track, message.trackOrHeading());

    }

    @Test
    void AirborneVelocityMessageWorksOnGivenValues4(){
        RawMessage rm = new RawMessage(0, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5"));
        AirborneVelocityMessage message = AirborneVelocityMessage.of(rm);

        double velocity = 1061.4503686262444;
        double track = 4.221861463749146;
        assertEquals(velocity, message.speed());
        assertEquals(track, message.trackOrHeading());
    }

    @Test
    void SousType3Or4Works(){
        String message = "8DA05F219B06B6AF189400CBC33F";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timestamps = 0;
        RawMessage test = new RawMessage(timestamps, byteString);
        AirborneVelocityMessage avm = AirborneVelocityMessage.of(test);
        assertEquals(192.91666666666669 , avm.speed());
        assertEquals(4.25833066717054 , avm.trackOrHeading());
    }
}