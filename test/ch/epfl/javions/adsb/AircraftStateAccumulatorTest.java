package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class AircraftStateAccumulatorTest
{

    @Test
    void ExempleDuProfAircraftStateAccumulator() throws IOException {
        String f = "resources/samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a = new AircraftStateAccumulator<>(new AircraftState());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }

    @Test
    void ExempleDuProfAircraftStateAccumulator1() throws IOException  {
        String f = "resources/samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("3C6481");
        try (InputStream s = new FileInputStream(f))
        {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a = new AircraftStateAccumulator <> (new AircraftState());
            while ((m = d.nextMessage()) != null)
            {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }

    //test de moha
    @Test
    public static void main(String[] args) throws IOException {
        String f = "resources/samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(f)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<AircraftState> a =
                    new AircraftStateAccumulator<>(new AircraftState());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }

    @Test
    void test1() throws IOException {
        var message1 = RawMessage.of(100, ByteString.ofHexadecimalString("8D485020994409940838175B284F").getBytes());
        System.out.println(AirborneVelocityMessage.of(message1));
    }

    @Test
    void test2() throws IOException {
        var message2  = RawMessage.of(100, ByteString.ofHexadecimalString("8DA05F219B06B6AF189400CBC33F").getBytes());
        System.out.println(AirborneVelocityMessage.of(message2));
    }
}