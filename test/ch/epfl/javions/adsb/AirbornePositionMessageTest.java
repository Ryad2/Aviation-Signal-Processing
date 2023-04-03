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

public class AirbornePositionMessageTest {
    private final static HexFormat hf = HexFormat.of();

    /*


    @Test
    void ExempleDuProfAirbornePositionMessage() throws IOException {

        String f = "resources/samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(f)) {

            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            while ((m = d.nextMessage()) != null) {
                if ((m.typeCode() >= 9 && m.typeCode() <= 18) || (m.typeCode() >= 20 && m.typeCode() <= 22))
                    System.out.println(AirbornePositionMessage.of(m));
            }
        }
    }

     */

    private final static byte [] tab1 = hf.parseHex("8D49529958B302E6E15FA352306B");
    private final static ByteString bytes1 = new ByteString(tab1);
    private final static long timeStamp1 = 75898000;
    private static final RawMessage message = new RawMessage(timeStamp1, bytes1);

    @Test
    void ofWorksOnKnownValues1()
    {
        AirbornePositionMessage PositionMessage = AirbornePositionMessage.of(message);
        int expectedTimstamp = 75898000;
        IcaoAddress expectedICAO = new IcaoAddress("495299");
        double expectedAltitude = 10546.08;
        double expectedParity = 0;
        double expectedX = 0.6867904663085938;
        double expectedY = 0.7254638671875;

        assertEquals(expectedTimstamp, PositionMessage.timeStampNs());
        assertEquals(expectedICAO, PositionMessage.icaoAddress());
        assertEquals(expectedAltitude, PositionMessage.altitude());
        assertEquals(expectedParity, PositionMessage.parity());
        assertEquals(expectedX, PositionMessage.x());
        assertEquals(expectedY, PositionMessage.y());
    }

    private final static byte [] tab2 = hf.parseHex("8D4241A9601B32DA4367C4C3965E");
    private final static ByteString bytes2 = new ByteString(tab2);
    private final static long timeStamp2 = 116538700;
    private static final RawMessage message2 = new RawMessage(timeStamp2, bytes2);

    @Test
    void ofWorksOnKnownValues2()
    {
        AirbornePositionMessage PositionMessage1 = AirbornePositionMessage.of(message2);
        int expectedTimeStampNs = 116538700;
        IcaoAddress expectedICAO = new IcaoAddress("4241A9");
        double expectedAltitude = 1303.02;
        double expectedParity = 0;
        double expectedX = 0.702667236328125;
        double expectedY = 0.7131423950195312;

        assertEquals(expectedTimeStampNs, PositionMessage1.timeStampNs());
        assertEquals(expectedICAO, PositionMessage1.icaoAddress());
        assertEquals(expectedAltitude, PositionMessage1.altitude());
        assertEquals(expectedParity, PositionMessage1.parity());
        assertEquals(expectedX, PositionMessage1.x());
        assertEquals(expectedY, PositionMessage1.y());
    }

    private final static byte [] tab3 = hf.parseHex("8D4D222860B985F7F53FAB33CE76");
    private final static ByteString bytes3 = new ByteString(tab3);
    private final static long timeStamp3 = 138560100;
    private static final RawMessage message3 = new RawMessage(timeStamp3, bytes3);

    @Test
    void ofWorksOnKnownValues3()
    {
        AirbornePositionMessage PositionMessage1 = AirbornePositionMessage.of(message3);
        int expectedTimeStampNs = 138560100;
        IcaoAddress expectedICAO = new IcaoAddress("4D2228");
        double expectedAltitude = 10972.800000000001;
        double expectedParity = 1;
        double expectedX = 0.6243515014648438;
        double expectedY = 0.4921417236328125;

        assertEquals(expectedTimeStampNs, PositionMessage1.timeStampNs());
        assertEquals(expectedICAO, PositionMessage1.icaoAddress());
        assertEquals(expectedAltitude, PositionMessage1.altitude());
        assertEquals(expectedParity, PositionMessage1.parity());
        assertEquals(expectedX, PositionMessage1.x());
        assertEquals(expectedY, PositionMessage1.y());
    }

    private final static byte [] tab4 = hf.parseHex("8D39D300990CE72C70089058AD77");
    private final static ByteString bytes4 = new ByteString(tab4);
    private final static long timeStamp4 = 100775400;
    private static final RawMessage message4 = new RawMessage(timeStamp4, bytes4);

    @Test
    void ofWorksOnKnownValues4()
    {
        AirbornePositionMessage PositionMessage = AirbornePositionMessage.of(message);
        int expectedTimstamp = 75898000;
        IcaoAddress expectedICAO = new IcaoAddress("495299");
        double expectedAltitude = 10546.08;
        double expectedParity = 0;
        double expectedX = 0.6867904663085938;
        double expectedY = 0.7254638671875;

        assertEquals(expectedTimstamp, PositionMessage.timeStampNs());
        assertEquals(expectedICAO, PositionMessage.icaoAddress());
        assertEquals(expectedAltitude, PositionMessage.altitude());
        assertEquals(expectedParity, PositionMessage.parity());
        assertEquals(expectedX, PositionMessage.x());
        assertEquals(expectedY, PositionMessage.y());
    }

    @Test
    void testConstructorThrowsNullPointerException() {
        IcaoAddress icaoAddress = null;
        assertThrows(
                NullPointerException.class,
                () -> new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 0, 0.5)
        );
    }

    @Test
    void testConstructorThrowsIllegalArgumentExceptionForNegativeTimestamp() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirbornePositionMessage(-1L, icaoAddress, 1000.0, 0, 0.5, 0.5)
        );
    }

    @Test
    void testConstructorThrowsIllegalArgumentExceptionForInvalidParity() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 2, 0.5, 0.5)
        );
    }

    @Test
    void testConstructorThrowsIllegalArgumentExceptionForInvalidXValue() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, -0.5, 0.5)
        );
    }

    @Test
    void testConstructorThrowsIllegalArgumentExceptionForInvalidYValue() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");
        assertThrows(
                IllegalArgumentException.class,
                () -> new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 0.5, 2.0)
        );
    }

    @Test
    void testConstructorWithValidArguments() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");
        AirbornePositionMessage message = new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 0.5, 0.5);
        assertNotNull(message);
        assertEquals(icaoAddress, message.icaoAddress());
        assertEquals(1000.0, message.altitude());
        assertEquals(0, message.parity());
        assertEquals(0.5, message.x());
        assertEquals(0.5, message.y());
    }

    @Test
    void testGettersAndSetters() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");
        AirbornePositionMessage message = new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 0.5, 0.5);

        assertEquals(1000.0, message.altitude());
        assertEquals(0, message.parity());
        assertEquals(0.5, message.x());
        assertEquals(0.5, message.y());
    }


    @Test
    void testConstructorWithInvalidArguments() {
        IcaoAddress icaoAddress = new IcaoAddress("123456");

        assertThrows(NullPointerException.class, () -> {
            new AirbornePositionMessage(System.nanoTime(), null, 1000.0, 0, 0.5, 0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(-1, icaoAddress, 1000.0, 0, 0.5, 0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 2, 0.5, 0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, -0.5, 0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 1.5, 0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 0.5, -0.5);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(System.nanoTime(), icaoAddress, 1000.0, 0, 0.5, 1.5);
        });
    }

    @Test
    void airbornePositionMessageConstructorThrowsWhenTimeStampIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AirbornePositionMessage(-1, new IcaoAddress("ABCDEF"), 1000, 0, 0, 0);
        });
        assertDoesNotThrow(() -> {
            new AirbornePositionMessage(0, new IcaoAddress("ABCDEF"), 1000, 0, 0, 0);
        });
    }

    @Test
    void airbornePositionMessageConstructorThrowsWhenIcaoAddressIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new AirbornePositionMessage(100, null, 1000, 0, 0, 0);
        });
    }

    @Test
    void airbornePositionMessageConstructorThrowsWhenParityIsInvalid() {
        var icaoAddress = new IcaoAddress("ABCDEF");
        for (int i = -100; i <= 100; i += 1) {
            if (i == 0 || i == 1) continue;
            var invalidParity = i;
            assertThrows(IllegalArgumentException.class, () -> {
                new AirbornePositionMessage(100, icaoAddress, 100, invalidParity, 0.5, 0.5);
            });
        }
    }

    @Test
    void airbornePositionMessageConstructorThrowsWhenXYAreInvalid() {
        var icaoAddress = new IcaoAddress("ABCDEF");
        for (var invalidXY = 1d; invalidXY < 5d; invalidXY += 0.1) {
            var xy = invalidXY;
            assertThrows(IllegalArgumentException.class, () -> {
                new AirbornePositionMessage(100, icaoAddress, 100, 0, xy, 0.5);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new AirbornePositionMessage(100, icaoAddress, 100, 0, -xy, 0.5);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new AirbornePositionMessage(100, icaoAddress, 100, 0, 0.5, xy);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                new AirbornePositionMessage(100, icaoAddress, 100, 0, 0.5, -xy);
            });
        }
    }

    @Test
    void airbornePositionMessageOfCorrectlyDecodesAltitudeWhenQIs0() {
        record MessageAndAltitude(String message, double altitude) {
        }
        var testValues = List.of(
                new MessageAndAltitude("8D4B1BB5598486491F4BDBF44FC6", 1584.96),
                new MessageAndAltitude("8D4B1BB5592C22D2A155F49835EF", 1798.32),
                new MessageAndAltitude("8D4B1BB5592422D2BB55FD991FA4", 1828.80),
                new MessageAndAltitude("8D4B1BB559A4264FDB4DDDC058EA", 1859.28),
                new MessageAndAltitude("8D4B1BB5598426509F4E1F032D5D", 1889.76),
                new MessageAndAltitude("8D4B1BB5598406514D4E5FEC1AC3", 1920.24),
                new MessageAndAltitude("8D4B1BB559A40653594F35F9A08F", 1950.72),
                new MessageAndAltitude("8D4B1BB55924065661506DA3728A", 1981.20),
                new MessageAndAltitude("8D4B1BB5592C02EE175E9AF78185", 2011.68),
                new MessageAndAltitude("8D4B1BB5590C02FAED63A05783E1", 2042.16),
                new MessageAndAltitude("8DADA2FD593682D9D99C2643E7DA", 2743.20));
        for (var testValue : testValues) {
            var message = RawMessage.of(100, HexFormat.of().parseHex(testValue.message));
            assertNotNull(message);
            var airbornePositionMessage = AirbornePositionMessage.of(message);
            assertNotNull(airbornePositionMessage);
            assertEquals(testValue.altitude, airbornePositionMessage.altitude(), 0.005);
        }
    }

    @Test
    void airbornePositionMessageOfCorrectlyDecodesAltitudeWhenQIs1() {
        record MessageAndAltitude(String message, double altitude) {
        }
        var testValues = List.of(
                new MessageAndAltitude("8D406666580D1652395CBE0A4D3E", 434.34),
                new MessageAndAltitude("8D4B1BDD5911A68127785A8F1273", 746.76),
                new MessageAndAltitude("8D344645584592A80D5BC637ED82", 3909.06),
                new MessageAndAltitude("8F405B66585915E28714229EFD13", 5067.30),
                new MessageAndAltitude("8D4B1A23586B8307F5B26CB39D00", 6217.92),
                new MessageAndAltitude("8D4402F2587563156B9880D4D855", 6812.28),
                new MessageAndAltitude("8D4402F25887D6AFD7A1A3769B45", 7962.90),
                new MessageAndAltitude("8D347307589B66396B69C91DD7B1", 9128.76),
                new MessageAndAltitude("8D4CA24558ADE68009DEF6E531E5", 10287.00),
                new MessageAndAltitude("8D49328A59CB16A537939E3B583D", 12016.74),
                new MessageAndAltitude("8D3B754358D311E57545B2100575", 12504.42));
        for (var testValue : testValues) {
            var message = RawMessage.of(100, HexFormat.of().parseHex(testValue.message));
            assertNotNull(message);
            var airbornePositionMessage = AirbornePositionMessage.of(message);
            assertNotNull(airbornePositionMessage);
            assertEquals(testValue.altitude, airbornePositionMessage.altitude(), 0.005);
        }
    }

    // Code to generate the invalid messages used by the test below.
    List<String> airbornePositionMessagesWithInvalidAltitude() {
        var crcComputer = new Crc24(Crc24 .GENERATOR);
        var messages = new ArrayList<String>();

        var byte0 = "8D";
        var icaoAddress = "406666";
        var payload = 0x580D1652395CBEL;
        var altMask = ((1L << 12) - 1) << 36;
        var invalidAlts = new long[]{0b000000000000, 0b101010000000, 0b100010000000};
        for (var alt : invalidAlts) {
            var corruptedPayload = payload & ~altMask | (alt & 0xFFF) << 36;
            var messageWithoutCRC = byte0 + icaoAddress + "%014X".formatted(corruptedPayload);
            var messageBytes = HexFormat.of().parseHex(messageWithoutCRC);
            var crc = crcComputer.crc(messageBytes);
            var message = messageWithoutCRC + "%06X".formatted(crc);
            messages.add(message);
        }
        return messages;
    }

    @Test
    void airbornePositionMessageOfReturnsNullWhenAltitudeIsInvalid() {
        var messages = List.of(
                "8D40666658000652395CBEB25722",
                "8D40666658A80652395CBED10630",
                "8D40666658880652395CBE7570E9");
        for (var testValue : messages) {
            var message = RawMessage.of(100, HexFormat.of().parseHex(testValue));
            assertNotNull(message);
            var airbornePositionMessage = AirbornePositionMessage.of(message);
            assertNull(airbornePositionMessage);
        }
    }

}