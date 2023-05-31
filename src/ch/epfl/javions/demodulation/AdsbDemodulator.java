package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


/**
 * Représente un démodulateur de messages ADS-B
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class AdsbDemodulator {

    private static final int NUMBER_SAMPLES_PREAMBULE = 80;
    private static final int POWER_WINDOW_SIZE = 1200;
    private static final int INDEX_PEAKS_1 = 0;
    private static final int INDEX_PEAKS_2 = 10;
    private static final int INDEX_PEAKS_3 = 35;
    private static final int INDEX_PEAKS_4 = 45;
    private static final int INDEX_PEAKS_AFTER_1 = 1;
    private static final int INDEX_PEAKS_AFTER_2 = 11;
    private static final int INDEX_PEAKS_AFTER_3 = 36;
    private static final int INDEX_PEAKS_AFTER_4 = 46;
    private static final int INDEX_VALLEYS_1 = 5;
    private static final int INDEX_VALLEYS_2 = 15;
    private static final int INDEX_VALLEYS_3 = 20;
    private static final int INDEX_VALLEYS_4 = 25;
    private static final int INDEX_VALLEYS_5 = 30;
    private static final int INDEX_VALLEYS_6 = 40;
    private static final int NANOSEC_BY_POSITION = 100;
    public static final int DOWNLINK_FORMAT = 17;
    private final PowerWindow window;
    private final byte[] message = new byte[RawMessage.LENGTH];

    /**
     * Construit un démodulateur de messages ADS-B à partir du flux d'échantillons donné et retourne
     * un démodulateur obtenant les octets contenant les échantillons du flot passé en argument
     *
     * @param samplesStream le flux d'échantillons
     * @throws IOException si une erreur d'entrée-sortie survient
     */
    public AdsbDemodulator(InputStream samplesStream) throws IOException {
        this.window = new PowerWindow(samplesStream, POWER_WINDOW_SIZE);
    }

    /**
     * Retourne le prochain message ADS-B du flot d'échantillons passé au constructeur, ou null s'il
     * n'y en a plus,
     *
     * @return retourne le prochain message ADS-B du flot d'échantillons passé au constructeur,
     * ou null s'il n'y en a plus, c'est-à-dire que la fin du flot d'échantillons a été atteinte
     * @throws IOException si une erreur d'entrée-sortie survient
     */
    public RawMessage nextMessage() throws IOException {
        //On appelle la méthode actualSumPeaks() pour seulement initialiser l'attribut sumPeaksActuel
        // pour la toute premiere somme de peaks
        int actualSumPeaks = actualSumPeaks();
        int previousSumPeaks = 0;

        while (window.isFull()) {

            int nextSumPeaks = nextSumPeaks();
            int sumValley = sumValley();
            if (isValid(sumValley, nextSumPeaks, actualSumPeaks, previousSumPeaks)) {
                RawMessage rawMessage = RawMessage.of(window.position()
                        * NANOSEC_BY_POSITION, messageCalculator());

                if (rawMessage != null && rawMessage.downLinkFormat() == DOWNLINK_FORMAT) {
                    window.advanceBy(POWER_WINDOW_SIZE - 1);
                    window.advance();
                    return rawMessage;
                }
            }
            previousSumPeaks = actualSumPeaks;
            actualSumPeaks = nextSumPeaks;
            window.advance();
        }
        return null;
    }

    private byte[] messageCalculator() {
        Arrays.fill(message, (byte) 0);
        for (int i = 0; i < message.length*Long.BYTES; i += Long.BYTES) {
            putNextBit(message, i);
        }
        return message;
    }

    private void putNextBit(byte[] message, int index) {
        for (int i = 0; i < Long.BYTES; i++) {
            message[index / Long.BYTES] = (byte) (message[index / Long.BYTES]
                    | getBit(index + i) << (7 - i));
        }
    }

    private boolean isValid(int sumValley, int sumPeaksAfter, int sumPeaksActuel, int sumPeaksPrecedent) {
        return (sumPeaksActuel >= 2 * sumValley) && (sumPeaksPrecedent < sumPeaksActuel) &&
                (sumPeaksActuel > sumPeaksAfter);
    }

    private byte getBit(int index) {
        return (byte) (window.get(NUMBER_SAMPLES_PREAMBULE + 10 * index)
                < window.get((NUMBER_SAMPLES_PREAMBULE + 5) + 10 * index) ? 0 : 1);
    }

    private int nextSumPeaks() {
        return window.get(INDEX_PEAKS_AFTER_1) + window.get(INDEX_PEAKS_AFTER_2) +
                window.get(INDEX_PEAKS_AFTER_3) + window.get(INDEX_PEAKS_AFTER_4);
    }

    private int actualSumPeaks() {
        return window.get(INDEX_PEAKS_1) + window.get(INDEX_PEAKS_2) + window.get(INDEX_PEAKS_3)
                + window.get(INDEX_PEAKS_4);
    }

    private int sumValley() {
        return window.get(INDEX_VALLEYS_1) + window.get(INDEX_VALLEYS_2) + window.get(INDEX_VALLEYS_3)
                + window.get(INDEX_VALLEYS_4) + window.get(INDEX_VALLEYS_5)
                + window.get(INDEX_VALLEYS_6);
    }
}