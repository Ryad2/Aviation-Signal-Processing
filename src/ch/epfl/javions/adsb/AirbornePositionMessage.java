package ch.epfl.javions.adsb;

import ch.epfl.javions.Bits;
import ch.epfl.javions.Preconditions;
import ch.epfl.javions.Units;
import ch.epfl.javions.aircraft.IcaoAddress;

import java.util.Objects;

/**
 * Une classe qui représente un message ADS-B de positionnement en vol. Ces messages ont un code de type compris soit
 * entre 9 (inclus) et 18 (inclus), soit entre 20 (inclus) et 22 (inclus)
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */

public record AirbornePositionMessage(long timeStampNs, IcaoAddress icaoAddress, double altitude, int parity,
                                      double x, double y) implements Message {

    /**
     * Construit un message ADS-B de positionnement en vol à partir des paramètres donnés
     * @param timeStampNs l'horodatage du message, en nanosecondes
     * @param icaoAddress l'adresse ICAO de l'expéditeur du message
     * @param altitude l'altitude à laquelle se trouvait l'aéronef au moment de l'envoi du message, en mètres
     * @param parity la parité du message (0 s'il est pair, 1 s'il est impair)
     * @param x la longitude locale et normalisée — donc comprise entre 0 et 1 — à laquelle se trouvait l'aéronef au
     *          moment de l'envoi du message
     * @param y la latitude locale et normalisée à laquelle se trouvait l'aéronef au moment de l'envoi du message.
     * @throws NullPointerException si l'adresse ICAO est nulle
     * @throws IllegalArgumentException si l'horodatage est négatif, si la parité n'est pas 0 ou 1, ou x ou y ne sont
     *         pas compris entre 0 (inclus) et 1 (exclu).
     */

    public AirbornePositionMessage {
        Objects.requireNonNull(icaoAddress);
        Preconditions.checkArgument((timeStampNs >= 0) && (parity == 1 || parity == 0) && (x >= 0)
                && (x < 1) && (y >= 0) && (y < 1));
    }

    /**
     * Construit un message ADS-B de positionnement en vol à partir d'un message ADS-B brut en fonction de la valeur du
     * bit d'index 4 de l'attribut ATL
     * @param rawMessage le message ADS-B brut
     * @return le message ADS-B de positionnement en vol construit à partir du message ADS-B brut ou null si l'altitude
     *         qu'il contient est invalide
     */

    public static AirbornePositionMessage of(RawMessage rawMessage) {



        long timeStampNs = rawMessage.timeStampNs();
        IcaoAddress icaoAddress = rawMessage.icaoAddress();
        double altitude = 0;
        int ALT = Bits.extractUInt(rawMessage.payload(), 36,12);
        int FORMAT = Bits.extractUInt(rawMessage.payload(),34,1);
        double LAT_CPR = Bits.extractUInt(rawMessage.payload(),17,17)*Math.scalb(1d,-17);
        double LON_CPR = Bits.extractUInt(rawMessage.payload(),0,17)*Math.scalb(1d,-17);

        int Q = Bits.extractUInt(ALT, 4,1);

        if (Q == 1){

            int bits1 = Bits.extractUInt(ALT, 5,7);
            int bits2 = Bits.extractUInt(ALT, 0,4);

            ALT = (bits1 << 4) | bits2;
            altitude = Units.convertFrom(-1000 + (ALT * 25), Units.Length.FOOT);
        }

        if (Q == 0){
            int lsbGroupe=Bits.extractUInt(untangler(ALT),0,3);
            lsbGroupe=greyTrasslator(lsbGroupe, 3);
            int msbGroupe=Bits.extractUInt(untangler(ALT),3,9);
            msbGroupe=greyTrasslator(msbGroupe, 9);

            if (lsbGroupe == 0 || lsbGroupe == 5 || lsbGroupe == 6) return null;
            if(lsbGroupe == 7) lsbGroupe = 5;
            if (msbGroupe %2 != 0) lsbGroupe = 6 - lsbGroupe;

            altitude = Units.convertFrom(-1300+(lsbGroupe*100)+(msbGroupe*500), Units.Length.FOOT);
        }

        return new AirbornePositionMessage(timeStampNs, icaoAddress, altitude, FORMAT, LON_CPR, LAT_CPR);
    }
    /*



    private static byte[] intToBitArray(int num) {
        byte[] bits = new byte[12]; // Un int a 32 bits
        for (int i = 0; i < 12; i++) {
            bits[i] = (byte) ((num >> i) & 1); // Extraction du i-ème bit de num
        }
        return bits;
    }

    private static int byteArrayToLSBInt(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < 12; i++) {
            result |= bytes[i];
            result<<=1;// Décalage de 8 bits à chaque itération
        }
        return result;
    }

    private static int untangler(int ALT){
        byte [] altArray=intToBitArray(ALT);
        byte[] temp=new byte[12];
        temp[0]=altArray[4];
        temp[1]=altArray[2];
        temp[2]=altArray[0];
        temp[3]=altArray[10];
        temp[4]=altArray[8];
        temp[5]=altArray[6];
        temp[6]=altArray[5];
        temp[7]=altArray[3];
        temp[8]=altArray[1];
        temp[9]=altArray[11];
        temp[10]=altArray[9];
        temp[11]=altArray[7];
        return byteArrayToLSBInt(temp);
    }

     */


    private static int greyTrasslator(int num, int length){

        int greyCodeValue = 0;
        for (int i=0; i< length;i++) greyCodeValue^=(num>>i);
        return greyCodeValue;
    }


    private static int untangler(int ALT)
    {
        int D1 = Bits.extractUInt(ALT, 4, 1);
        int D2 = Bits.extractUInt(ALT, 2, 1);
        int D4 = Bits.extractUInt(ALT, 0, 1);
        int A1 = Bits.extractUInt(ALT, 10, 1);
        int A2 = Bits.extractUInt(ALT, 8, 1);
        int A4 = Bits.extractUInt(ALT, 6, 1);
        int B1 = Bits.extractUInt(ALT, 5, 1);
        int B2 = Bits.extractUInt(ALT, 3, 1);
        int B4 = Bits.extractUInt(ALT, 1, 1);
        int C1 = Bits.extractUInt(ALT, 11, 1);
        int C2 = Bits.extractUInt(ALT, 9, 1);
        int C4 = Bits.extractUInt(ALT, 7, 1);

        return ((((((((((((D1 << 11) | D2 << 10) | D4 << 9) | A1 << 8) | A2 << 7) | A4 << 6) | B1 << 5) | B2 << 4) |
                B4 << 3) | C1 << 2) | C2 << 1) | C4);
    }
}