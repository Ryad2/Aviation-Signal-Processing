package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Représente une chaîne d'octets
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class ByteString {

    private byte[] chaine;

    private final static HexFormat ab = HexFormat.of().withUpperCase();

    /**
     * retourne une chaîne d'octets dont le contenu est celui du tableau passé en argument
     * @param bytes la chaîne d'octets
     */
    public ByteString(byte[] bytes) {
        this.chaine = Arrays.copyOf(bytes, bytes.length);
    }

    public static boolean isHexadecimal(char c) {// ASK ASSISTANT IF THIS METHODE ALREADY EXISTE IN JAVA !!!!!!
        return Character.isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * retourne la chaîne d'octets dont la chaîne passée en argument est la représentation hexadécimale, ou,
     * lève une exception
     * Vérifier que la longueur est paire et tester si tous les caractères sont hexadécimaux
     * @param hexString la chaîne d'octet passée en argument
     * @return retourne la chaîne d'octets dont la chaîne passée en argument est la représentation hexadécimale, ou,
     * lève une exception et transforme une chaîne en byteString
     * @throws NumberFormatException La longueur de la chaine doit être paire
     * @throws NumberFormatException La chaine contient un caractère qui n'est pas hexadécimal
     */

    public static ByteString ofHexadecimalString(String hexString) {

        if (hexString.length() % 2 != 0) {
            throw new NumberFormatException("La longueur de la chaine doit être paire");
        }

        for (int i = 0; i < hexString.length(); i ++) {//REVOIR CA POUR L INTERMEDIERE !!!!!
            if(!isHexadecimal(hexString.charAt(i))){
                throw new NumberFormatException("La chaine contient un caractère qui n'est pas hexadécimal");
                }
        }

        return new ByteString(ab.parseHex(hexString));
    }

    /**
     * calcule la taille de la chaîne
     * @return la taille de la chaîne
     */

    public int size() {
        return chaine.length;
    }

    /**
     * Permet de retourner l'octet à l'index donné et on utilise une conversion pour interpréter l'octet comme non signé
     * @param index l'index en question
     * @throws IndexOutOfBoundsException si l'octet retourné est invalide
     * @return l'octet à l'index donné
     */

    public int byteAt(int index) {
        Objects.checkIndex(index, chaine.length);
        return Byte.toUnsignedInt(chaine[index]);
    }

    /**
     * Retourne un octet précis où lève des exceptions
     * Vérifie si la plage est invalide et construit à partir d'octets dans une plage donnée
     * @param fromIndex l'index de départ
     * @param toIndex l'index d'arrivée
     * @throws IllegalArgumentException si la différence entre toIndex et fromIndex n'est pas strictement inférieure
     * au nombre d'octets contenus dans une valeur de type long.
     * @throws IndexOutOfBoundsException si la plage décrite par fromIndex et toIndex n'est pas totalement comprise
     * entre 0 et la taille de la chaîne
     * @return les octets compris entre les index fromIndex (inclus) et toIndex (exclu) sous la forme d'une valeur de
     * type long
     */
    public long bytesInRange(int fromIndex, int toIndex) {
        // Check that the range is valid
        Objects.checkFromToIndex(fromIndex, toIndex, chaine.length);
        Preconditions.checkArgument((toIndex - fromIndex) <= Long.BYTES );

        long result = 0;
        for (int i = fromIndex; i < toIndex; i++) {
            result = (result << 8) + Byte.toUnsignedInt(chaine[i]);
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteString that){
            return Arrays.equals(this.chaine, that.chaine);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.chaine);
    }

    @Override
    public String toString() {
        return ab.formatHex(chaine);
    }

    //TODO : A retirer quand on a fini les tests!
    public byte[] getBytes(){
        return chaine;
    }
}