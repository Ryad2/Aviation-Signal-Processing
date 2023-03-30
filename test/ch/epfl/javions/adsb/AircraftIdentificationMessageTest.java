package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AircraftIdentificationMessageTest {

    @Test
    void ExempleDuProfAircraftIdentificationMessage() throws IOException {

        String f = "resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {

            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                if (m.typeCode() >= 1 && m.typeCode() <= 4)
                    System.out.println(AircraftIdentificationMessage.of(m));
            }
        }
    }


    private final static HexFormat hf = HexFormat.of();
    private final static byte [] tab1 = hf.parseHex("8D4D2228234994B7284820323B81");
    private final static ByteString bytes1 = new ByteString(tab1);
    private final static long timeStamp1 = 1499146900;
    private static final RawMessage message1 = new RawMessage(timeStamp1, bytes1);

    @Test
    void ofWorksOnKnownValues1()
    {
        AircraftIdentificationMessage identificationMessage = AircraftIdentificationMessage.of(message1);
        int expectedTimeStampNs = 1499146900;
        IcaoAddress expectedICAO = new IcaoAddress("4D2228");
        int expectedCategory = 163;
        CallSign expectedCallSign = new CallSign("RYR7JD");

        assertEquals(expectedTimeStampNs, identificationMessage.timeStampNs());
        assertEquals(expectedICAO, identificationMessage.icaoAddress());
        assertEquals(expectedCategory, identificationMessage.category());
        assertEquals(expectedCallSign, identificationMessage.callSign());
    }

    private final static byte [] tab2 = hf.parseHex("8F01024C233530F3CF6C60A19669");
    private final static ByteString bytes2 = new ByteString(tab2);
    private final static long timeStamp2 = 2240535600L;
    private static final RawMessage message2 = new RawMessage(timeStamp2, bytes2);

    @Test
    void ofWorksOnKnownValues2()
    {
        AircraftIdentificationMessage identificationMessage = AircraftIdentificationMessage.of(message2);
        long expectedTimeStampNs = 2240535600L;
        IcaoAddress expectedICAO = new IcaoAddress("01024C");
        int expectedCategory = 163;
        CallSign expectedCallSign = new CallSign("MSC3361");

        assertEquals(expectedTimeStampNs, identificationMessage.timeStampNs());
        assertEquals(expectedICAO, identificationMessage.icaoAddress());
        assertEquals(expectedCategory, identificationMessage.category());
        assertEquals(expectedCallSign, identificationMessage.callSign());

    }

    @Test
    void aircraftIdentificationMessageConstructorThrowsWhenTimeStampIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftIdentificationMessage(-1, new IcaoAddress("3950D1"), 0xA0, new CallSign("AFR13TL"));
        });
        assertDoesNotThrow(() -> {
            new AircraftIdentificationMessage(0, new IcaoAddress("3950D1"), 0xA0, new CallSign("AFR13TL"));
        });
    }

    @Test
    void aircraftIdentificationMessageConstructorThrowsWhenAddressOrCallSignIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new AircraftIdentificationMessage(100, null, 0xA0, new CallSign("AFR13TL"));
        });
        assertThrows(NullPointerException.class, () -> {
            new AircraftIdentificationMessage(100, new IcaoAddress("3950D1"), 0xA0, null);
        });
    }

    @Test
    void aircraftIdentificationMessageCategoryWorksForKnownMessages() {
        record MessageAndCategory(String message, int category) {
        }

        var messagesAndCategories = List.of(
                new MessageAndCategory("8D3950D1200464B8D10360A58DCD", 0xa0),
                new MessageAndCategory("8F471A79215940B5CB08205D57B4", 0xa1),
                new MessageAndCategory("8D4D24802258A534DF38208E8EED", 0xa2),
                new MessageAndCategory("8F3C4DC82310C2362E08206D312C", 0xa3),
                new MessageAndCategory("8D400E4E241584F8C504E07A49E7", 0xa4),
                new MessageAndCategory("8D8963CE2515413621A82086E559", 0xa5),
                new MessageAndCategory("8F39AC48274C1355DF3820BB615D", 0xa7),
                new MessageAndCategory("8E4B2BE21A2024510E0820745102", 0xb2),
                new MessageAndCategory("8E383F3C1C18A096620820918150", 0xb4));
        for (var messageAndCategory : messagesAndCategories) {
            var hf = HexFormat.of();
            var message = hf.parseHex(messageAndCategory.message());
            var category = messageAndCategory.category();
            var rawMessage = RawMessage.of(100, message);
            assertNotNull(rawMessage);
            var aircraftIdentificationMessage = AircraftIdentificationMessage.of(rawMessage);
            assertNotNull(aircraftIdentificationMessage);
            assertEquals(category, aircraftIdentificationMessage.category());
        }
    }

    @Test
    void aircraftIdentificationMessageCallSignWorksForKnownMessages() {
        record MessageAndCallSign(String message, String callSign) {
        }

        var messagesAndCallSigns = List.of(
                new MessageAndCallSign("8D3991E1230464B1CD4320FCD23B", "AFR13TL"),
                new MessageAndCallSign("8F394C09200464B3D546A059C5C9", "AFR35TZ"),
                new MessageAndCallSign("8D406CA3230570F1DF1820BADDB9", "AWC171"),
                new MessageAndCallSign("8D44CE73230853324C38207EDEE4", "BEL2SC"),
                new MessageAndCallSign("8D4B1931220C16B9C3182021D5D1", "CAZ901"),
                new MessageAndCallSign("8D503E6A230C3373D41520E93419", "CCM35AT"),
                new MessageAndCallSign("8DA4C70325101336DA0820B4B812", "DAL66"),
                new MessageAndCallSign("8D44083C2314A573CB6060F4E8C4", "EJU326A"),
                new MessageAndCallSign("8D4400902314A579D504609ABEB8", "EJU95PQ"),
                new MessageAndCallSign("8E383F3C1C18A096620820918150", "FJBVX"),
                new MessageAndCallSign("8D461981211931B5CB3660E09D00", "FSF523Y"),
                new MessageAndCallSign("8D4D00C622307338CB5D201112C1", "LGL8254"),
                new MessageAndCallSign("8D06004025341575C31D203EBA81", "MAU5014"),
                new MessageAndCallSign("8D4CA805234994B81588206F63A2", "RYR8EX"),
                new MessageAndCallSign("8D4CAF7E234C14F2D78DE075A8A2", "SAS2587"),
                new MessageAndCallSign("8D3C0CA323515271D37820FC5632", "TUI147"));

        for (var messageAndCallSign : messagesAndCallSigns) {
            var hf = HexFormat.of();
            var message = hf.parseHex(messageAndCallSign.message());
            var callSign = messageAndCallSign.callSign();
            var rawMessage = RawMessage.of(100, message);
            assertNotNull(rawMessage);
            var aircraftIdentificationMessage = AircraftIdentificationMessage.of(rawMessage);
            assertNotNull(aircraftIdentificationMessage);
            assertEquals(callSign, aircraftIdentificationMessage.callSign().string());
        }
    }

    // Code to generate the invalid messages used by the test below.
    List<String> aircraftIdentificationMessagesWithInvalidCharacters() {
        var crcComputer = new Crc24(Crc24.GENERATOR);
        var messages = new ArrayList<String>();

        var byte0 = "8D";
        var icaoAddress = "3944EF";
        var payload = 0x200464B2DD8460L;
        var callSignAlphabet = "?ABCDEFGHIJKLMNOPQRSTUVWXYZ????? ???????????????0123456789??????";
        var characterToCorrupt = 0;
        for (var i = 0; i < callSignAlphabet.length(); i++) {
            var c = callSignAlphabet.charAt(i);
            if (c != '?') continue;

            var mask = 0b111111L << (characterToCorrupt * 6);
            var invalidChar = (long) i << (characterToCorrupt * 6);
            var corruptedPayload = payload & ~mask | invalidChar;
            var messageWithoutCRC = byte0 + icaoAddress + "%014X".formatted(corruptedPayload);
            var messageBytes = HexFormat.of().parseHex(messageWithoutCRC);
            var crc = crcComputer.crc(messageBytes);
            var message = messageWithoutCRC + "%06X".formatted(crc);
            messages.add(message);
            characterToCorrupt = (characterToCorrupt + 1) % 8;
        }
        return messages;
    }

    @Test
    void aircraftIdentificationMessageOfReturnsNullForInvalidCharacters() {
        var invalidMessages = List.of(
                "8D3944EF200464B2DD8440F1ADDC",
                "8D3944EF200464B2DD86E0EB71AC",
                "8D3944EF200464B2DDC4608CE47E",
                "8D3944EF200464B275846031B6E0",
                "8D3944EF2004649EDD84607FE1EE",
                "8D3944EF200467F2DD84600A4B89",
                "8D3944EF200614B2DD84607692E1",
                "8D3944EF208864B2DD84601C3BFD",
                "8D3944EF200464B2DD84630F847E",
                "8D3944EF200464B2DD8920B5B88C",
                "8D3944EF200464B2DE5460835099",
                "8D3944EF200464B29984604DC53A",
                "8D3944EF200464A7DD8460CBAD58",
                "8D3944EF20046A32DD8460E381DF",
                "8D3944EF200694B2DD84600C01E8",
                "8D3944EF20A864B2DD8460B84D24",
                "8D3944EF200464B2DD846B0FF412",
                "8D3944EF200464B2DD8B20A9A38C",
                "8D3944EF200464B2DED4607A40BD",
                "8D3944EF200464B2B9846008B913",
                "8D3944EF200464AFDD84606BB3C9",
                "8D3944EF20046EB2DD8460EE2F80",
                "8D3944EF2007B4B2DD8460987E3A",
                "8D3944EF20F064B2DD8460F47543",
                "8D3944EF200464B2DD847D0F30E7",
                "8D3944EF200464B2DD8FA096934C",
                "8D3944EF200464B2DFF46049D0F5");
        for (var message : invalidMessages) {
            var rawMessage = RawMessage.of(100, HexFormat.of().parseHex(message));
            assertNotNull(rawMessage);
            var aircraftIdentificationMessage = AircraftIdentificationMessage.of(rawMessage);
            assertNull(aircraftIdentificationMessage);
        }
    }


}