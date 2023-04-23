package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URLDecoder;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AircraftStateManagerTest {
    private static String findArrow(double trackOrHeading) {
        if ((0 <= trackOrHeading && trackOrHeading <= 22.5) || (337.5 <= trackOrHeading && trackOrHeading <= 360)) {
            return "↑";
        }
        if (22.5 < trackOrHeading && trackOrHeading <= 67.5) {
            return "↗";
        }
        if (67.5 < trackOrHeading && trackOrHeading <= 112.5) {
            return "→";
        }
        if (112.5 < trackOrHeading && trackOrHeading <= 157.5) {
            return "↘";
        }
        if (157.5 < trackOrHeading && trackOrHeading <= 202.5) {
            return "↓";
        }
        if (202.5 < trackOrHeading && trackOrHeading <= 247.5) {
            return "↙";
        }
        if (247.5 < trackOrHeading && trackOrHeading <= 292.5) {
            return "←";
        }
        if (292.5 < trackOrHeading && trackOrHeading <= 337.5) {
            return "↖";
        }
        return "";
    }

    @Test
    void generalTest() throws IOException {
        String d = Objects.requireNonNull(getClass().getResource("/messages_20230318_0915.bin")).getFile();
        String f = Objects.requireNonNull(getClass().getResource("/aircraft.zip")).getFile();
        d = URLDecoder.decode(d, UTF_8);
        f = URLDecoder.decode(f, UTF_8);
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(d)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftStateManager manager = new AircraftStateManager(new AircraftDatabase(f));

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                //System.out.printf("%13d: %s\n", timeStampNs, message);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                manager.updateWithMessage(Objects.requireNonNull(MessageParser.parse(rawMessage)));


                for (ObservableAircraftState state : manager.states()) {
                    if (state.getPosition() != null) {
                        System.out.println("-----------------------");
                        System.out.println(state.getIcaoAddress());
                        System.out.println(state.getCallSign());
                        System.out.println(state.getAircraftData().registration());
                        System.out.println(state.getAircraftData().model());
                        System.out.println(Units.convertTo(state.getPosition().longitude(),
                                Units.Angle.DEGREE));
                        System.out.println(Units.convertTo(state.getPosition().latitude(),
                                Units.Angle.DEGREE));
                        System.out.println(state.getAltitude());
                        System.out.println(state.getVelocity() * 3.6);
                        System.out.println(findArrow(Units.convertTo(state.trackOrHeadingProperty().get(),Units.Angle.DEGREE)));
                        Thread.sleep(10);
                    }
                }
            }
        } catch (EOFException e) { /* nothing to do */ } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
