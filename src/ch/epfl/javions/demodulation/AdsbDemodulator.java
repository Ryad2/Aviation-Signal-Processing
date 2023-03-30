package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class AdsbDemodulator {
    private InputStream stream;
    private PowerWindow window;
    private int timeStampNs=0;
    private static final int NUMBER_SAMPLES_PREAMBULE = 80;
    private static final int POWER_WINDOW_SIZE = 1200;
    private byte [] message =new byte[14];
    int sumPicsAfter, sumValley, sumPicsActuel, sumPicsPrecedent;

    public AdsbDemodulator (InputStream samplesStream) throws IOException {
        this.stream=samplesStream;
        this.window = new PowerWindow(samplesStream, POWER_WINDOW_SIZE);
        sumPicsActuel=sumPicsActual();
        sumPicsPrecedent = 0;
    }

    public RawMessage nextMessage() throws IOException{

        while (window.isFull()){
            sumPicsAfter = sumPicsAfter();
            sumValley = sumValley();
            if(isValid(sumValley, sumPicsAfter, sumPicsActuel, sumPicsPrecedent))
            {
                RawMessage rawMessage = RawMessage.of(window.position()*100, messageCalculator());

                if (rawMessage != null && rawMessage.downLinkFormat() == 17)
                {
                    window.advanceBy(POWER_WINDOW_SIZE-1);
                    sumPicsPrecedent = sumPicsActual();
                    sumPicsActuel = sumPicsAfter();
                    window.advance();
                    return rawMessage;
                }
            }
            sumPicsPrecedent=sumPicsActuel;
            sumPicsActuel=sumPicsAfter;
            window.advance();
        }
        return null;
    }

    private byte [] messageCalculator(){

        Arrays.fill(message, (byte) 0);

        for(int i=0; i<112; i+=8 ){
            putNextBit(message,i);
        }
        return message;
    }

    private void putNextBit(byte [] message, int index){
        for (int i = 0; i < 8; i++) {
            message[index/8]= (byte) (message[index/8] | getBit(index + i) << (7-i));
        }
    }

    private boolean isValid (int sumValley, int sumPicsAfter, int sumPicsActuel, int sumPicsPrecedent){
        return (sumPicsActuel >= 2*sumValley) && ( sumPicsPrecedent < sumPicsActuel ) && (
                sumPicsActuel>sumPicsAfter);
    }

    private byte getBit(int index) {
        if (window.get(NUMBER_SAMPLES_PREAMBULE + 10 * index) < window.get((NUMBER_SAMPLES_PREAMBULE + 5) + 10 * index)) {
            return 0;
        }
        else {
            return 1;
        }
    }

    private int sumPicsAfter(){
        return window.get(1)+window.get(11)+window.get(36)+window.get(46);
    }

    private int sumPicsActual(){
        return window.get(0)+window.get(10)+window.get(35)+window.get(45);
    }

    private int sumValley() {
        return window.get(5) + window.get(15) + window.get(20) + window.get(25) + window.get(30) + window.get(40);
    }
}