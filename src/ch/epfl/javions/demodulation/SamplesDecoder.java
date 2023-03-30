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

    private int batchSize;
    private InputStream flow;

    /**
     * Retourne un décodeur d'échantillons utilisant le flot d'entrée donné pour obtenir les octets
     * de la radio AirSpy et produisant les échantillons par lot de taille donnée
     * @param stream flow du message
     * @param batchSize taille du lot
     * @throws IOException dans le cas ou le flow est null ou la taille du lot est inférieur ou égale à 0.
     * @throws IllegalArgumentException si le batchSize n'est pas strictement positif
     */

    public SamplesDecoder (InputStream stream, int batchSize)  {//A REVOIR AVEC LES ASSISTANTS !!!!!
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

        byte [] tableauIntermediaire = new byte[batchSize*2];

            int count= flow.readNBytes(tableauIntermediaire,0, batchSize*2);

            for(int i=0; i<(tableauIntermediaire.length)/2; i++){
                int lsb = Byte.toUnsignedInt(tableauIntermediaire[2*i]);
                int msb = Byte.toUnsignedInt(tableauIntermediaire[2*i+1]);
                batch[i]= (short)(  ( (msb<<8) | lsb ) - 2048);
            }

        return (count)/2;
    }
}