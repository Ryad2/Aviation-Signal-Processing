package ch.epfl.javions.demodulation;


import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * Représente une fenêtre de taille fixe sur une séquence d'échantillons de
 * puissance produits par un calculateur de puissance
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class PowerComputer {

    private short[] oneBatch;
    private int[] window;
    private SamplesDecoder samplesTable;
    private int batchSize;

    public PowerComputer (InputStream stream, int batchSize){

        Preconditions.checkArgument((batchSize > 0) && (batchSize % 8 == 0));
        this.batchSize=batchSize;
        this.samplesTable = new SamplesDecoder(stream,Short.BYTES*batchSize);
        this.oneBatch = new short[Short.BYTES*batchSize];
        this.window=new int[8];

    }

    public int readBatch (int [] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int count = samplesTable.readBatch(oneBatch);

        for(int i=0; i < count/2; i+=4){
            window[0]=oneBatch[2*i];
            window[1]=oneBatch[(2*i)+1];
            batch[i]=power(window);


            window[2]=oneBatch[(2*i)+2];
            window[3]=oneBatch[(2*i)+3];
            batch[i+1]=power(window);


            window[4]=oneBatch[(2*i)+4];
            window[5]=oneBatch[(2*i)+5];
            batch[i+2]=power(window);


            window[6]=oneBatch[(2*i)+6];
            window[7]=oneBatch[(2*i)+7];
            batch[i+3]=power(window);
        }

        return count/2;
    }

    private int power(int[] window) {
        return (int) (Math.pow(window[0]-window[2]+window[4] -window[6],2)+
                Math.pow(window[1]-window[3]+window[5]-window[7],2));
    }
}