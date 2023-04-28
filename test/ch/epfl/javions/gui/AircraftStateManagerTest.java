/*
package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AircraftStateManagerTest {
    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.getIcaoAddress().string();
            String s2 = o2.getIcaoAddress().string();
            return s1.compareTo(s2);
        }
    }
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
            return "️↘";
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
        String d = getClass().getResource("/messages_20230318_0915.bin").getFile();
        String f = getClass().getResource("/aircraft.zip").getFile();
        d = URLDecoder.decode(d, UTF_8);
        f = URLDecoder.decode(f, UTF_8);
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(d)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftStateManager manager = new AircraftStateManager(new AircraftDatabase(f));
            AddressComparator comparator = new AddressComparator();

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                Message parsedMessage = MessageParser.parse(rawMessage);
                if (parsedMessage == null) continue;
                manager.updateWithMessage(parsedMessage);
                manager.purge();
                List<ObservableAircraftState> statesList = new ArrayList<>(manager.states());
                statesList.sort(comparator);

                for (ObservableAircraftState state : statesList) {
                    if (state.getPosition() != null) {
                        System.out.printf("%-6s | %-7s | %-8s | %-32s | %-18s | %-18s | %-5s | %-5s | %s%n",
                                state.getIcaoAddress().string(),
                                (state.getCallSign() != null) ? state.getCallSign().string() : "    ",
                                state.getAircraftData().registration().string(),
                                state.getAircraftData().model(),
                                Units.convertTo(state.getPosition().longitude(), Units.Angle.DEGREE),
                                Units.convertTo(state.getPosition().latitude(), Units.Angle.DEGREE),
                                (int) state.getAltitude(),
                                (int) (state.getVelocity() * 3.6),
                                findArrow(Units.convertTo(state.trackOrHeadingProperty().get(), Units.Angle.DEGREE)));

                        Thread.sleep(10);
                    }
                }
            }
        } catch (EOFException e) { */
/* nothing to do *//*
 } catch (InterruptedException e) {throw new RuntimeException(e);}
    }
}*/
