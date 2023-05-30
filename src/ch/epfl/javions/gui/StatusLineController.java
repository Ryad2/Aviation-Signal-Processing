package ch.epfl.javions.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * La classe StatusLineController gère la ligne d'état.
 * Elle possède un constructeur par défaut, qui construit le graphe de scène, ainsi que trois
 * méthodes publiques.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class StatusLineController {

    private final BorderPane rootPane;
    private final LongProperty aircraftCountProperty = new SimpleLongProperty(0L);
    private final LongProperty messageCountProperty = new SimpleLongProperty(0L);


    /**
     * Constructeur de StatusLineController qui construit le graphe de scène
     */
    public StatusLineController() {

        Text aircraftCountText = textInStatusLine("Aéronefs visibles", aircraftCountProperty);

        Text messageCountText = textInStatusLine("Messages reçus", messageCountProperty);

        rootPane = new BorderPane(null, null,
                messageCountText, null, aircraftCountText);

        rootPane.getStylesheets().add("status.css");
    }

    /**
     * Retourne le panneau contenant la ligne d'état.
     *
     * @return le panneau contenant la ligne d'état.
     */
    public BorderPane pane() {
        return rootPane;
    }

    /**
     * Retourne la propriété (modifiable) contenant le nombre d'aéronefs actuellement visibles.
     *
     * @return la propriété (modifiable) contenant le nombre d'aéronefs actuellement visibles.
     */
    public LongProperty aircraftCountProperty() {
        return aircraftCountProperty;
    }

    /**
     * Retourne la propriété (modifiable) contenant le nombre de messages reçus depuis le début de
     * l'exécution du programme.
     *
     * @return la propriété (modifiable) contenant le nombre de messages reçus.
     */
    public LongProperty messageCountProperty() {
        return messageCountProperty;
    }

    /**
     * Méthode privée qui retourne les deux textes qui seront dans la ligne d'état.
     *
     * @param indicatif l'indicatif du texte
     * @param property la propriété du texte
     * @return les deux textes qui seront dans la ligne d'état
     */
    private Text textInStatusLine (String indicatif, LongProperty property){
        Text name = new Text();
        name.textProperty().bind(Bindings.format("%s : %d", indicatif, property));
        return name;
    }
}