package ch.epfl.javions;

/**
 * Représente un calculateur de CRC de 24 bits.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public final class Crc24 {


    private static final int CRC_BITS = 24;
    /**
     * GENERATOR : Contient les 24 bits de poids faible du générateur utilisé pour calculer le CRC24 des messages ADS-B
     */
    public static int GENERATOR = 0xFFF409;
    private final int[] table;


    /**
     * Retourne un calculateur de CRC24 utilisant le générateur dont
     * les 24 bits de poids faible sont ceux de generator.
     *
     * @param generator le générateur en question
     */
    public Crc24(int generator) {

        table = buildTable(generator);
    }



    /**
     * Retourne le CRC24 du tableau donné.
     *
     * @param message le message du CRC24
     * @return le CRC24 du tableau donné
     */
    public int crc(byte[] message) {

        int crc = 0;
        for (var bit : message) {
            crc = ((crc << Byte.SIZE) | Byte.toUnsignedInt(bit)) ^
                    table[Bits.extractUInt(crc, CRC_BITS - Byte.SIZE, Byte.SIZE)];
        }
        for (int i = 0; i < 3; i++) {
            crc = (crc << Byte.SIZE) ^ table[Bits.extractUInt(crc, CRC_BITS - Byte.SIZE, Byte.SIZE)];
        }
        return Bits.extractUInt(crc, 0, CRC_BITS);
    }





    //todo should be comment
    private static int[] buildTable(int generator) {

        int[] table = new int[1 << Byte.SIZE];

        for (int i = 0; i < table.length; i++) {
            byte[] bit_w = new byte[]{(byte) i};
            table[i] = crc_bitwise(generator, bit_w);
        }
        return table;
    }

    //todo should be commented
    private static int crc_bitwise(int generateur, byte[] message) {

        int[] tab = new int[]{0, generateur};
        var crc = 0;

        for (byte bytes : message) {
            for (int j = Byte.SIZE - 1; j >= 0; j--) {
                crc = ((crc << 1) | Bits.extractUInt(bytes, j, 1))
                        ^ tab[Bits.extractUInt(crc, CRC_BITS - 1, 1)];
            }
        }

        for (int i = 0; i < CRC_BITS; i++) {
            crc = (crc << 1) ^ tab[Bits.extractUInt(crc, CRC_BITS - 1, 1)];
        }

        return Bits.extractUInt(crc, 0, CRC_BITS);
    }
}