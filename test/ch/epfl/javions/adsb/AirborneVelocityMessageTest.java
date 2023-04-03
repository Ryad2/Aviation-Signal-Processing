package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AirborneVelocityMessageTest {

    /*


    @Test
    void ExempleDuProfAirborneVelocityMessage() throws IOException {

        int count = 0;
        String f = "resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {

            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                if (m.typeCode() == 19) {
                    //System.out.println(AirborneVelocityMessage.of(m));
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
                    //System.out.println(avm);
                    ++i;
                }
            }
            assertEquals(230,i);
        }
    }

     */

    @Test
    void SubType1Or2Works1(){
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message1 != null;
        //System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(1000, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5").getBytes());
        assert message2 != null;
        //System.out.println(AirborneVelocityMessage.of(message2));
    }

    @Test
    void SubType1Or2Works2(){
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        assert message1 != null;
        //System.out.println(AirborneVelocityMessage.of(message1));
        var message2  = RawMessage.of(1000, ByteString.ofHexadecimalString("8D4B1A00EA0DC89E8F7C0857D5F5").getBytes());
        assert message2 != null;
        //System.out.println(AirborneVelocityMessage.of(message2));
    }

    @Test
    void SubType3Or4Works1() {

        var message1  = RawMessage.of(100, ByteString.ofHexadecimalString("8DA05F219B06B6AF189400CBC33F").getBytes());
        assert message1 != null;
        //System.out.println(AirborneVelocityMessage.of(message1));


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
        //System.out.println(AirborneVelocityMessage.of(message2));

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
        //System.out.println(avm);
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




    private static final double KNOT = 1852d / (60d * 60d);

    @Test
    void airbornePositionMessageConstructorThrowsWhenIcaoAddressIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new AirborneVelocityMessage(100, null, 1000, 1);
        });
    }

    @Test
    void airborneVelocityMessageConstructorThrowsWhenTimeStampIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AirborneVelocityMessage(-1, new IcaoAddress("ABCDEF"), 1, 1);
        });
        assertDoesNotThrow(() -> {
            new AirborneVelocityMessage(0, new IcaoAddress("ABCDEF"), 1, 1);
        });
    }

    @Test
    void airborneVelocityMessageConstructorThrowsWhenSpeedIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AirborneVelocityMessage(1, new IcaoAddress("ABCDEF"), -1, 1);
        });
    }

    @Test
    void airborneVelocityMessageConstructorThrowsWhenTrackOrHeadingIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AirborneVelocityMessage(1, new IcaoAddress("ABCDEF"), 1, -1);
        });
    }

    String messageWithNewPayload(String baseMessage, long newPayload) {
        var newPayloadString = String.format("%014X", newPayload);
        var newMessageStringNoCrc = baseMessage.substring(0, 8) + newPayloadString;
        var newMessageBytesNoCrc = HexFormat.of().parseHex(newMessageStringNoCrc);
        var crc = new Crc24(Crc24.GENERATOR).crc(newMessageBytesNoCrc);
        return newMessageStringNoCrc + "%06X".formatted(crc);
    }

    @Test
    void airborneVelocityMessageOfReturnsNullWhenSubtypeIsInvalid() {
        var invalidMessages = List.of(
                "8D3C4DC8988CA882E0A409EC0FAE",
                "8D3C4DC89D8CA882E0A409BE7697",
                "8D3C4DC89E8CA882E0A409250D87",
                "8D3C4DC89F8CA882E0A409F97770");
        for (String message : invalidMessages) {
            var messageBytes = HexFormat.of().parseHex(message);
            var rawMessage = RawMessage.of(0L, messageBytes);
            assertNotNull(rawMessage);
            assertNull(AirborneVelocityMessage.of(rawMessage));
        }
    }

    // Code used to generate the messages used in the test below
    List<String> messagesWithZeroGroundSpeed() {
        var baseMessage = "8D40773999147D8F78400A8A3C7B";
        // var payload = 0b10011_001_0_0_010_1000111110110001111011_11000010000_00_00001010L;

        var payloadVns0 = 0b10011_001_0_0_010_1000111110110000000000_11000010000_00_00001010L;

        var messageWithVns0 = messageWithNewPayload(baseMessage, payloadVns0);

        var payloadVns1 = 0b10011_001_0_0_010_1000000000010001111011_11000010000_00_00001010L;
        var messageWithVns1 = messageWithNewPayload(baseMessage, payloadVns1);

        return List.of(messageWithVns0, messageWithVns1);
    }

    @Test
    void airborneVelocityMessageOfReturnsNullWhenGroundSpeedIsUnknown() {
        var messages = List.of("8D40773999147D8018400A365428", "8D4077399914008F78400A0E0D5D");
        for (String message : messages) {
            var messageBytesWithVns0 = HexFormat.of().parseHex(message);
            var rawMessageWithVns0 = RawMessage.of(0L, messageBytesWithVns0);
            assertNotNull(rawMessageWithVns0);
            assertNull(AirborneVelocityMessage.of(rawMessageWithVns0));
        }
    }

    // Code used to generate the message used in the test below
    String messageWithUnknownHeading() {
        var baseMessage = "8D40773999147D8F78400A8A3C7B";
        var payload = 0b10011_011_0_0_010_0111111111111111111111_11000010000_00_00001010L;
        return messageWithNewPayload(baseMessage, payload);
    }

    @Test
    void airborneVelocityMessageOfReturnsNullWhenHeadingIsUnknown() {
        var message = "8D4077399B13FFFFF8400A50C602";
        var messageBytes = HexFormat.of().parseHex(message);
        var rawMessage = RawMessage.of(0L, messageBytes);
        assertNotNull(rawMessage);
        assertNull(AirborneVelocityMessage.of(rawMessage));
    }

    // Code used to generate the message used in the test below
    String messageWithZeroAirSpeed() {
        var baseMessage = "8D40773999147D8F78400A8A3C7B";
        var payload = 0b10011_011_0_0_010_1111111111110000000000_11000010000_00_00001010L;
        return messageWithNewPayload(baseMessage, payload);
    }

    @Test
    void airborneVelocityMessageOfReturnsNullWhenAirSpeedIsUnknown() {
        var message = "8D4077399B17FF8018400AED4D65";
        var messageBytes = HexFormat.of().parseHex(message);
        var rawMessage = RawMessage.of(0L, messageBytes);
        assertNotNull(rawMessage);
        assertNull(AirborneVelocityMessage.of(rawMessage));
    }

    String messageWithNewSubType(String message, int newSubType) {
        var payload = Long.parseLong(message.substring(8, 22), 16);
        var newPayload = payload & ~(0b111L << 48) | ((long) newSubType) << 48;
        return messageWithNewPayload(message, newPayload);
    }

    @Test
    void airborneVelocityMessageOfWorksWithSubtypes1And2() {
        // Messages A from:
        // https://mode-s.org/decode/content/ads-b/5-airborne-velocity.html
        var messageBytesA = HexFormat.of().parseHex("8D485020994409940838175B284F");
        var rawMessageA = RawMessage.of(0L, messageBytesA);
        assertNotNull(rawMessageA);
        var messageA = AirborneVelocityMessage.of(rawMessageA);
        assertEquals(159.20 * KNOT, messageA.speed(), 0.005);
        assertEquals(182.88, Math.toDegrees(messageA.trackOrHeading()), 0.005);

        // Messages received from actual planes
        record MessageAndSpeedAndTrack(String message, double speed, double track) {}
        var messages = List.of(
                new MessageAndSpeedAndTrack("8D3C4DC8998CA882E0A409307559", 86.654498, 4.581407),
                new MessageAndSpeedAndTrack("8D40627999907492F858094A0D56", 97.235444, 2.487510),
                new MessageAndSpeedAndTrack("8D344645990CA093703C0ABEF91E", 113.873646, 3.942964),
                new MessageAndSpeedAndTrack("8D8963CE990A0381400483318D0C", 264.464976, 1.588304),
                new MessageAndSpeedAndTrack("8D34610D99090134B00403B4C733", 253.039738, 0.547393),
                new MessageAndSpeedAndTrack("8D3985A6990CD02F10088396B1FF", 220.356439, 5.778808),
                new MessageAndSpeedAndTrack("8D3C0C0A99120385D83C85E27568", 265.435886, 1.658122),
                new MessageAndSpeedAndTrack("8D3C6424990D499DB80485B37C5C", 207.876300, 4.088687),
                new MessageAndSpeedAndTrack("8D4402F299096A19908C0BA0600B", 213.063239, 1.058538),
                new MessageAndSpeedAndTrack("8D40627999906D93785409E85EF6", 96.764798, 2.529996));
        for (MessageAndSpeedAndTrack messageAndSpeedAndTrack : messages) {
            var messageBytes = HexFormat.of().parseHex(messageAndSpeedAndTrack.message());
            var rawMessage = RawMessage.of(0L, messageBytes);
            assertNotNull(rawMessage);
            var message = AirborneVelocityMessage.of(rawMessage);
            assertEquals(messageAndSpeedAndTrack.speed(), message.speed(), 1e-5);
            assertEquals(messageAndSpeedAndTrack.track(), message.trackOrHeading(), 1e-5);
        }

        // Fake message with supersonic speed
        var supersonicMessage = "8D3C4DC89A8CA882E0A409AB0E49";
        var supersonicMessageBytes = HexFormat.of().parseHex(supersonicMessage);
        var supersonicRawMessage = RawMessage.of(0L, supersonicMessageBytes);
        assertNotNull(supersonicRawMessage);
        var supersonicAVM = AirborneVelocityMessage.of(supersonicRawMessage);
        assertEquals(4d * 86.654498, supersonicAVM.speed(), 1e-5);
        assertEquals(4.581407, supersonicAVM.trackOrHeading(), 1e-5);
    }

    @Test
    void airborneVelocityMessageOfWorksWithSubtypes3And4() {
        // Messages B from:
        // https://mode-s.org/decode/content/ads-b/5-airborne-velocity.html
        var messageBytesB = HexFormat.of().parseHex("8DA05F219B06B6AF189400CBC33F");
        var rawMessageB = RawMessage.of(0L, messageBytesB);
        assertNotNull(rawMessageB);
        var messageB = AirborneVelocityMessage.of(rawMessageB);
        assertEquals(375 * KNOT, messageB.speed(), 0.005);
        assertEquals(243.98, Math.toDegrees(messageB.trackOrHeading()), 0.005);

        // Fake message with supersonic speed
        var supersonicMessage = "8DA05F219C06B6AF189400DEBBE1";
        var supersonicMessageBytes = HexFormat.of().parseHex(supersonicMessage);
        var supersonicRawMessage = RawMessage.of(0L, supersonicMessageBytes);
        assertNotNull(supersonicRawMessage);
        var supersonicAVM = AirborneVelocityMessage.of(supersonicRawMessage);
        assertEquals(4d * 375 * KNOT, supersonicAVM.speed(), 1e-5);
        assertEquals(243.98, Math.toDegrees(messageB.trackOrHeading()), 0.005);
    }

}