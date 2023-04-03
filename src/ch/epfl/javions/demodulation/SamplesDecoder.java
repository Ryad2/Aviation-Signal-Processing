package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * Représente un "décodeur d'échantillons", c.-à-d. un objet capable de transformer
 * les octets provenant de la AirSpy en des échantillons de 12 bits signés.
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class SamplesDecoder {


    /**
     * batchSize représente la taille d'un lot
     */
    private final int batchSize;


    /**
     * flow représente le flot d'entrée
     */
    private final InputStream flow;


    /**
     * OFFSET représente le décalage à appliquer aux échantillons et permet de recentréer les échantillons autour de 0
     * par une soustraction de 2048
     */
    private final static int OFFSET = 1 << 11;


    /**
     * Construit un SamplesDecoder et retourne un décodeur d'échantillons utilisant le flot d'entrée donné pour obtenir
     * les octets de la radio AirSpy et produisant les échantillons par lot de taille donnée
     *
     * @throws NullPointerException si le flot est nul
     * @throws IllegalArgumentException si la taille des lots n'est pas strictement positive
     */
    public SamplesDecoder (InputStream stream, int batchSize)  {

        Preconditions.checkArgument(batchSize>0);
        Objects.requireNonNull(stream);
        this.batchSize=batchSize;
        this.flow=stream;
    }


    /**
     * Lit depuis le flot passé au constructeur le nombre d'octets correspondant à un lot, puis convertit ces octets
     * en échantillons signés, qui sont placés dans le tableau passé en argument.
     * @param batch tableau du lot
     * @return le nombre d'échantillons converti
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si la taille du tableau passé en argument n'est pas égale à la taille d'un lot
     */
    public int readBatch(short[] batch) throws IOException {
        
        Preconditions.checkArgument(batch.length == batchSize);

        byte [] intermediateTable = new byte[batchSize*2];

            int count= flow.readNBytes(intermediateTable,0, batchSize*2);

            for(int i=0; i<(intermediateTable.length)/2; i++){
                int lsb = Byte.toUnsignedInt(intermediateTable[2*i]);
                int msb = Byte.toUnsignedInt(intermediateTable[2*i+1]);
                batch[i]= (short)(((msb << 8) | lsb) - OFFSET);
            }

        return count/2;
    }
}