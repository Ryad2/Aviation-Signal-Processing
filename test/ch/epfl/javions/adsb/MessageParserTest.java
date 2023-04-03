package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

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







    // Code to generate the variants of the messages used below.
    List<String> rawMessageWithTypeCodes(String baseMessage, int... typeCodes) {
        var crcComputer = new Crc24(Crc24.GENERATOR);
        var message = HexFormat.of().parseHex(baseMessage);
        var variants = new ArrayList<String>();
        for (int typeCode : typeCodes) {
            var byte4 = message[4];
            byte4 = (byte) (typeCode << 3 | (byte4 & 0b111));
            message[4] = byte4;
            var crc = crcComputer.crc(Arrays.copyOfRange(message, 0, 11));
            message[11] = (byte) (crc >> 16);
            message[12] = (byte) (crc >> 8);
            message[13] = (byte) crc;
            var rawMessage = RawMessage.of(100, message);
            variants.add(rawMessage.bytes().toString());
        }
        return variants;
    }

    @Test
    void messageParserParsesAllAircraftIdentificationMessages() {
        var variants = List.of(
                "8D3991E10B0464B1CD43206F07E8",
                "8D3991E1130464B1CD4320B4E75E",
                "8D3991E11B0464B1CD43205714CB",
                "8D3991E1230464B1CD4320FCD23B");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNotNull(message);
            assertTrue(message instanceof AircraftIdentificationMessage);
        }
    }

    @Test
    void messageParserParsesAllAirbornePositionMessages() {
        var variants = List.of(
                "8D406666480D1652395CBE325E1D",
                "8D406666500D1652395CBEE9BEAB",
                "8D406666580D1652395CBE0A4D3E",
                "8D406666600D1652395CBEA18BCE",
                "8D406666680D1652395CBE42785B",
                "8D406666700D1652395CBE9998ED",
                "8D406666780D1652395CBE7A6B78",
                "8D406666800D1652395CBE0E8C15",
                "8D406666880D1652395CBEED7F80",
                "8D406666900D1652395CBE369F36",
                "8D406666A00D1652395CBE7EAA53",
                "8D406666A80D1652395CBE9D59C6",
                "8D406666B00D1652395CBE46B970");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNotNull(message);
            assertTrue(message instanceof AirbornePositionMessage);
        }
    }

    @Test
    void messageParserParsesAllAirborneVelocityMessages() {
        var variants = List.of("8D485020994409940838175B284F");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNotNull(message);
            assertTrue(message instanceof AirborneVelocityMessage);
        }
    }

    @Test
    void messagesParserReturnsNullForUnknownTypeCodes() {
        var variants = List.of(
                "8D48502001440994083817BFA5E8",
                "8D485020294409940838172C703B",
                "8D48502031440994083817F7908D",
                "8D48502039440994083817146318",
                "8D485020414409940838175FE964",
                "8D485020B94409940838172B0E09",
                "8D485020C1440994083817608475",
                "8D485020C94409940838178377E0",
                "8D485020D1440994083817589756",
                "8D485020D9440994083817BB64C3",
                "8D485020E144099408381710A233",
                "8D485020E9440994083817F351A6",
                "8D485020F144099408381728B110",
                "8D485020F9440994083817CB4285");
        for (String variant : variants) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(variant));
            assertNotNull(rawMessage);
            var message = MessageParser.parse(rawMessage);
            assertNull(message);
        }
    }

}