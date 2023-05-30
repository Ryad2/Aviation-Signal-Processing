package ch.epfl.javions.gui;

import ch.epfl.javions.Preconditions;
import javafx.scene.paint.Color;

/**
 * Représente un dégradé de couleurs
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class ColorRamp {

    public static final ColorRamp PLASMA = new ColorRamp(
            Color.valueOf("0x0d0887ff"), Color.valueOf("0x220690ff"),
            Color.valueOf("0x320597ff"), Color.valueOf("0x40049dff"),
            Color.valueOf("0x4e02a2ff"), Color.valueOf("0x5b01a5ff"),
            Color.valueOf("0x6800a8ff"), Color.valueOf("0x7501a8ff"),
            Color.valueOf("0x8104a7ff"), Color.valueOf("0x8d0ba5ff"),
            Color.valueOf("0x9814a0ff"), Color.valueOf("0xa31d9aff"),
            Color.valueOf("0xad2693ff"), Color.valueOf("0xb6308bff"),
            Color.valueOf("0xbf3984ff"), Color.valueOf("0xc7427cff"),
            Color.valueOf("0xcf4c74ff"), Color.valueOf("0xd6556dff"),
            Color.valueOf("0xdd5e66ff"), Color.valueOf("0xe3685fff"),
            Color.valueOf("0xe97258ff"), Color.valueOf("0xee7c51ff"),
            Color.valueOf("0xf3874aff"), Color.valueOf("0xf79243ff"),
            Color.valueOf("0xfa9d3bff"), Color.valueOf("0xfca935ff"),
            Color.valueOf("0xfdb52eff"), Color.valueOf("0xfdc229ff"),
            Color.valueOf("0xfccf25ff"), Color.valueOf("0xf9dd24ff"),
            Color.valueOf("0xf5eb27ff"), Color.valueOf("0xf0f921ff"));
    private final Color[] colorList;
    private final double colorDifference;

    /**
     * Constructeur privé de ColorRamp qui prend en argument un nombre arbitraire de couleurs
     * et qui initialise colorList avec un tableau contenant ces couleurs et initialise
     * colorDifference
     *
     * @param colors les couleurs du dégradé
     * @throws IllegalArgumentException si le nombre de couleurs est inférieur à 2.
     */
    private ColorRamp(Color... colors) {
        Preconditions.checkArgument(colors.length >= 2);

        colorList = colors.clone();
        this.colorDifference = 1d / (colors.length - 1);
    }

    /**
     * Retourne la couleur correspondant à la valeur donnée. Lorsqu'on lui passe une valeur
     * inférieure à 0, elle retourne la première couleur et lorsqu'on lui passe une valeur
     * supérieure à 1, la méthode retourne la dernière couleur du dégradé. Quand on lui passe une
     * valeur qui se trouve entre deux points pour lesquels une couleur est connue, la couleur est
     * un mélange, dans les bonnes proportions, des couleurs de ces deux points.
     *
     * @param value la valeur donnée
     * @return la couleur correspondant à la valeur donnée
     */
    public Color at(double value) {

        if (value < 0) return colorList[0];
        else if (value > 1) return colorList[colorList.length - 1];

        else {

            //Trouver la valeur la plus proche et on retourne interpolate entre les deux couleurs
            int index = (int) (value / colorDifference);
            double reste = value % colorDifference;
            double pourcentage = reste / colorDifference;

            return colorList[index].interpolate(colorList[index + 1], pourcentage);
        }
    }
}
