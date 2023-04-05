package ch.epfl.javions;

import java.util.Objects;

/**
 * Permet d'extraire un sous-ensemble des 64 bits d'une valeur de type long
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class Bits {


    /**
     * Constructeur de la class Bits non instantiable
     */
    private Bits() {}


    /**
     * Extrait du vecteur de 64 bits value la plage de size bits commençant au bit d'index start, qu'elle interprète
     * comme une valeur nn signée ou lève 2 types d'exceptions
     *
     * @param value vecteur de 64 bits
     * @param start l'endroit où se trouve le bit d'index
     * @param size  la taille de bits
     * @return la valeur donnée
     * @throws IllegalArgumentException  si la taille n'est pas strictement supérieure à 0 et strictement inférieure à 32
     * @throws IndexOutOfBoundsException si la plage décrite par start et size n'est pas totalement comprise entre 0
     *                                   (inclus) et 64 (exclu)
     */
    public static int extractUInt(long value, int start, int size) {

        Preconditions.checkArgument((size > 0) && (size < Integer.SIZE));
        Objects.checkFromIndexSize(start, size, Long.SIZE);
        long mask = (1L << size) - 1;
        return (int) ((value >>> start) & mask);
    }


    /**
     * Retourne vrai si et seulement si le bit de value d'index donné vaut 1 sinon lève une exception
     *
     * @param value la valeur en question
     * @param index l'index en question
     * @return vrai ssi le bit de value d'index donné vaut 1
     */
    public static boolean testBit(long value, int index) {

        Objects.checkIndex(index, Long.SIZE);
        long mask = 1L << index;
        return (value & mask) != 0;
    }
}