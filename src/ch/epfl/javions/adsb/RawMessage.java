package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.Crc24;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.aircraft.IcaoAddress;
import java.util.HexFormat;

/**
 * Représente un message ADS-B "brut", c'est-à-dire dont l'attribut ME n'a pas encore été analysé.
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record RawMessage(long timeStampNs, ByteString bytes) {

    private static Crc24 crc = new Crc24(Crc24.GENERATOR);
    public static final int LENGTH = 14;
    private static final int USABLE_SQUITTER = 17;
    private static HexFormat HEXFORMAT = HexFormat.of().withUpperCase();

    private final static Crc24 crc24 = new Crc24(Crc24.GENERATOR);

    public RawMessage {
        Preconditions.checkArgument(timeStampNs >= 0 && bytes.size() == LENGTH);
    }

    /**
     * Retourne le message ADS-B brut avec l'horodatage et les octets donnés,
     * ou null si le CRC24 des octets ne vaut pas 0,
     * @param timeStampNs l'horodatage du message, exprimé en nanosecondes depuis une origine donnée
     * @param bytes les octets du message
     * @return
     */

    public static RawMessage of(long timeStampNs, byte[] bytes) {

        if (crc.crc(bytes) != 0) return null;
        else return new RawMessage(timeStampNs, new ByteString(bytes));
    }

    /**
     * Donne la taille d'un message dont le premier octet est celui donné, et qui vaut LENGTH si l'attribut DF
     * contenu dans ce premier octet vaut 17, et 0 sinon — indiquant que le message n'est pas d'un type connu,
     * @param byte0 le premier byte du message
     * @return la taille du message
     */

    public static int size (byte byte0){

        if (Bits.extractUInt(byte0,3,5) == USABLE_SQUITTER) return LENGTH;
        else return 0;
    }

    /**
     * Donne le code de type de l'attribut ME passé en argument.
     * @param payload le contenu de charge utile
     * @return l'attribut ME passé en argument
     */

    public static int typeCode(long payload){
        return Bits.extractUInt(payload,51,5);
    }

    /**
     * Donne le format du message, c.-à-d. l'attribut DF stocké dans son premier octet,
     * @return l'attribut DF stocké dans son premier octet
     */

    public int downLinkFormat(){
        int byte0 = (byte) bytes.byteAt(0);
        return Bits.extractUInt(byte0, 3,5);
    }

    /**
     * Donne l'adresse OACI de l'expéditeur du message,
     * @return l'adresse OACI
     */

    public IcaoAddress icaoAddress(){

        long address = bytes.bytesInRange(1,4);
        return new IcaoAddress(HEXFORMAT.toHexDigits(address,6));
    }

    /*
    private byte[] longToBytes(long value){
        byte[] bytes = new byte[3];
        bytes[0] = (byte) ((value >> 16) & 0xFF);
        bytes[1] = (byte) ((value >> 8) & 0xFF);
        bytes[2] = (byte) (value & 0xFF);
        return bytes;
    }
     */

    /**
     * Nous donne l'attribut ME du message : sa «charge utile»,
     * @return l'attribut ME du message
     */

    public long payload(){
        return bytes.bytesInRange(4,11);
    }

    /**
     * Donne le code de type du message, c.-à-d. les cinq bits de poids le plus fort de son attribut ME.
     * @return le code du type du message (les 5 bits de poids le plus fort de son attribut ME)
     */

    public int typeCode(){
        return typeCode(payload());
    }
}