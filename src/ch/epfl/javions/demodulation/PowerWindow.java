package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


/**
 * Représente une fenêtre de taille fixe sur une séquence d'échantillons de puissance produits par un calculateur de
 * puissance
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class PowerWindow {

    private final int windowSize;
    private long position;
    private int index;
    private final PowerComputer powers;
    private final static int BATCH_SIZE = 1 << 16;
    private int[] batch1;
    private int[] batch2;
    private int counter;


    /**
     * Construit une fenêtre de puissance et retourne une fenêtre de taille donnée sur la séquence d'échantillons de
     * puissance calculés à partir des octets fournis par le flot d'entrée donné
     *
     * @param stream le flot d'entrée
     * @param windowSize la taille de la fenêtre
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si la taille de la fenêtre donnée n'est pas comprise entre 0
     * (exclu) et 2^16 (inclus).
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= BATCH_SIZE);
        this.windowSize = windowSize;
        this.position = 0;
        this.index = 0;
        this.powers = new PowerComputer(stream, BATCH_SIZE);

        batch1 = new int[BATCH_SIZE];
        batch2 = new int[BATCH_SIZE];
        counter = powers.readBatch(batch1);
    }


    /**
     * Retourne la taille de l'échantillon
     *
     * @return la taille de l'échantillon
     */
    public int size() {
        return windowSize;
    }

    /**
     * Retourne la position actuelle de la fenêtre par rapport au début du flot de valeurs de puissance,
     * qui vaut initialement 0 et est incrémentée à chaque appel à advance
     *
     * @return la position actuelle de la fenêtre
     */
    public long position() {
        return position;
    }

    /**
     * Retourne vrai ssi la fenêtre est pleine, c.-à-d. qu'elle contient autant d'échantillons que sa taille
     *
     * @return vrai si la fenêtre est pleine
     */

    //TODO : ==?
    public boolean isFull() {
        return counter >= windowSize;
    }

    /**
     * Retourne l'échantillon de puissance à l'index donné de la fenêtre ou lève IndexOutOfBoundsException
     *
     * @param i l'index en question
     * @return l'échantillon de puissance à l'index donné de la fenêtre
     * @throws IndexOutOfBoundsException si cet index n'est pas compris entre 0 (inclus) et la taille de la fenêtre
     * (exclu)
     */

    public int get(int i) {
        Objects.checkIndex(i, windowSize);

        if (i+index < BATCH_SIZE) return batch1[index+i];
        else return batch2[i+index-BATCH_SIZE];
    }

    /**
     * Avance la fenêtre d'un échantillon,
     *
     * @throws IOException en cas d'erreur d'entrée/sortie
     */

    public void advance() throws IOException {

        position++;
        index++;
        counter--;

        if(index + windowSize - 1 == BATCH_SIZE){
            counter+=powers.readBatch(batch2);
        }

        else if(index >= BATCH_SIZE){
            int[] temp= batch2;
            batch2=batch1;
            batch1=temp;
            index=0;
        }
    }

    /**
     * Avance la fenêtre du nombre d'échantillons donné, comme si la méthode advance avait été appelée le nombre
     * de fois donné, ou lève IllegalArgumentException si ce nombre n'est pas positif ou nul.
     *
     * @param offset le nombre d'échantillons à avancer
     * @throws IOException en cas d'erreur d'entrée/sortie
     * @throws IllegalArgumentException si offset n'est pas positif ou nul
     */
    public void advanceBy (int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}