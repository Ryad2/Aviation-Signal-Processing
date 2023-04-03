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

    private final short[] oneBatch;
    private final int[] window;
    private final SamplesDecoder samplesTable;
    private final int batchSize;


    /**
     * Construit un calculateur de puissance à partir d'un flot d'entrée et d'une taille de lots et retourne un
     * calculateur de puissance utilisant le flot d'entré&e donnée pour obtenir les octets de la radio AirSpy et
     * produisant des échantillons de puissance par lots de taille donnée
     *
     * @param stream le flot d'entrée
     * @param batchSize la taille des lots
     * @throws IllegalArgumentException si la taille des lots donnée n'est pas un multiple de 8 strictement positif
     */
    public PowerComputer (InputStream stream, int batchSize) {

        Preconditions.checkArgument((batchSize > 0) && (batchSize % 8 == 0));
        this.batchSize = batchSize;
        this.samplesTable = new SamplesDecoder(stream,Short.BYTES * batchSize);
        this.oneBatch = new short[Short.BYTES * batchSize];
        this.window = new int[8];
    }


    /**
     * Lit depuis le décodeur d'échantillons le nombre d'échantillons nécessaire au calcul d'un lot d'échantillons de
     * puissance, puis les calcule au moyen de la formule donnée à la §2.4.6 et les place dans le tableau passé en
     * argument
     * @param batch le lot d'échantillons de puissance
     * @return le nombre d'échantillons de puissance placés dans le tableau
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si la taille du tableau passé en argument n'est pas égale à la taille d'un lot
     */
    public int readBatch (int [] batch) throws IOException {

        Preconditions.checkArgument(batch.length == batchSize);
        int count = samplesTable.readBatch(oneBatch);

        for(int i = 0; i < count/2; i++) {
            window[(2*i) % 8] = oneBatch[2*i];
            window[(2*i + 1) % 8] = oneBatch[2*i + 1];
            batch[i] = power(window);
        }
        return count/2;
    }


    private int power(int[] window) {
        return (int) (Math.pow(window[0] - window[2] + window[4] - window[6],2) +
                Math.pow(window[1] - window[3] + window[5] - window[7], 2));
    }
}