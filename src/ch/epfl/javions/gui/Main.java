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

public class Main {
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
    public static void main(String[] args){
        String d = Main.class.getResource("/messages_20230318_0915.bin").getFile();
        String f = Main.class.getResource("/aircraft.zip").getFile();
        d = URLDecoder.decode(d, UTF_8);
        f = URLDecoder.decode(f, UTF_8);
        long startTime = System.nanoTime();
        AircraftStateManager manager;
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(d)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            manager = new AircraftStateManager(new AircraftDatabase(f));
            String CSI = "\u001B[";
            String MOVE_TO_TOP_LEFT = CSI + ";H";
            String CLEAR_SCREEN = CSI + "2J";
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                RawMessage rawMessage = new RawMessage(timeStampNs, message);
                manager.updateWithMessage(Objects.requireNonNull(MessageParser.parse(rawMessage)));
                manager.purge();
                System.out.println("OACI   CallSign    Registration    Model    Longitude    Latitude    Altitude    Speed    Direction");
                System.out.println("---------------------------------------------------------------------------------------------------");
                for (ObservableAircraftState state : manager.states()) {
                    System.out.printf(state.getIcaoAddress().string() + " ");
                    if (state.getCallSign() != null)
                        System.out.printf(state.getCallSign().string());
                    if(state.getAircraftData().registration() != null)
                        System.out.printf(state.getAircraftData().registration().string() + " ");
                    if(state.getAircraftData().model() != null)
                        System.out.print(state.getAircraftData().model() + " ");
                    System.out.print(Units.convertTo(state.getPosition().longitude(),
                            Units.Angle.DEGREE) + " ");
                    System.out.print(Units.convertTo(state.getPosition().latitude(),
                            Units.Angle.DEGREE) + " ");
                    System.out.print(state.getAltitude() + " ");
                    System.out.print(state.getVelocity() * 3.6 + " ");
                    System.out.print(findArrow(Units.convertTo(state.trackOrHeadingProperty().get(), Units.Angle.DEGREE)) + " ");
                    System.out.println();
                    Thread.sleep(100);
                }
                System.out.println();
                System.out.print(CLEAR_SCREEN);
            }
        } catch (EOFException e) { /* nothing to do */ } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}