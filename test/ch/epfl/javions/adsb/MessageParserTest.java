package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageParserTest {

    @Test
    void parseWorks(){
        String message = "8D49529958B302E6E15FA352306B";
        ByteString byteString = ByteString.ofHexadecimalString(message);
        long timestamps1 = 75898000;
        RawMessage test = new RawMessage(timestamps1, byteString);
        AirbornePositionMessage apm1 = AirbornePositionMessage.of(test);
        AircraftIdentificationMessage aim1 = AircraftIdentificationMessage.of(test);
        AirborneVelocityMessage avm1 = AirborneVelocityMessage.of(test);
        Message test1 = MessageParser.parse(test);
        assertEquals(apm1,test1);
        assertNotEquals(aim1, test1);
        assertNotEquals(avm1, test1);

        String message2 = "8D4D2228234994B7284820323B81";
        ByteString byteString2 = ByteString.ofHexadecimalString(message2);
        long timestamps2 = 1499146900L;
        RawMessage test2 = new RawMessage(timestamps2 , byteString2);
        AircraftIdentificationMessage aim2 = AircraftIdentificationMessage.of(test2);
        AirbornePositionMessage apm2 = AirbornePositionMessage.of(test2);
        AirborneVelocityMessage avm2 = AirborneVelocityMessage.of(test2);
        Message test3 = MessageParser.parse(test2);
        assertEquals(aim2,test3);
        assertNotEquals(apm2, test3);
        assertNotEquals(avm2, test3);

        String message3 = "8D4D029F9914E09BB8240567C1D6";
        ByteString byteString3 = ByteString.ofHexadecimalString(message3);
        long timestamps3 = 208341000;
        RawMessage test4 = new RawMessage(timestamps3 , byteString3);
        AircraftIdentificationMessage aim3 = AircraftIdentificationMessage.of(test4);
        AirbornePositionMessage apm3 = AirbornePositionMessage.of(test4);
        AirborneVelocityMessage avm3 = AirborneVelocityMessage.of(test4);
        Message test5 = MessageParser.parse(test4);
        assertEquals(avm3,test5);
        assertNotEquals(apm3, test5);
        assertNotEquals(aim3, test5);

        String message4 = "8D4B17E5F8210002004BB8B1F1AC";
        ByteString byteString4 = ByteString.ofHexadecimalString(message4);
        long timestamps4 = 8096200;
        RawMessage test6 = new RawMessage(timestamps4 , byteString4);
        AircraftIdentificationMessage aim4 = AircraftIdentificationMessage.of(test6);
        AirbornePositionMessage apm4 = AirbornePositionMessage.of(test6);
        AirborneVelocityMessage avm4 = AirborneVelocityMessage.of(test6);
        Message test7 = MessageParser.parse(test6);
        assertNull(test7);
    }
}