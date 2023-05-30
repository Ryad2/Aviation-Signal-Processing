package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import static ch.epfl.javions.gui.TileManager.NUMBER_OF_PIXEL;

/**
 * Cette classe gère la base de données afin de dessiner
 * la carte et de gérer les manipulations sur la carte.
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class BaseMapController {
    private final TileManager tileId;
    private final MapParameters mapParameters;
    private final Pane pane;
    private final Canvas canvas;
    private boolean redrawNeeded = true;

    /**
     * Constructeur de la classe BaseMapController.
     *
     * @param tileId        représente l'identifiant de la tuile (coordonnées de la tuile)
     * @param mapParameters représente les paramètres de la carte
     */
    public BaseMapController(TileManager tileId, MapParameters mapParameters) {
        this.tileId = tileId;
        this.canvas = new Canvas();
        this.mapParameters = mapParameters;
        this.pane = new Pane(canvas);

        bindings();
        listeners();
        handlers();
    }

    /**
     * Retourne le panneau de la carte.
     *
     * @return le panneau de la carte
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Centre la carte sur une position géographique spécifiée.
     *
     * @param point la position géographique à centrer
     */
    public void centerOn(GeoPos point) {
        double newMinX = WebMercator.x(mapParameters.getZoom(),
                point.longitude()) - 0.5 * canvas.getWidth() - mapParameters.getminX();
        double newMinY = WebMercator.y(mapParameters.getZoom(),
                point.latitude()) - 0.5 * canvas.getHeight() - mapParameters.getminY();
        mapParameters.scroll(newMinX, newMinY);
    }

    private void bindings() {
        // Effectue une liaison bidirectionnelle entre les propriétés de largeur et de hauteur du canvas
        // avec les propriétés correspondantes du panneau.
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }

    private void listeners() {
        // Ajoute un écouteur pour la propriété "scene" du canvas, qui déclenche un re-dessin si nécessaire
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
        // Ajoute des auditeurs aux propriétés minX, minY, zoom, largeur et hauteur du panneau
        // pour déclencher un re-dessin lorsqu'ils changent.
        mapParameters.minXProperty().addListener(c -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener(c -> redrawOnNextPulse());
        mapParameters.zoomProperty().addListener(c -> redrawOnNextPulse());
        pane.widthProperty().addListener(c -> redrawOnNextPulse());
        pane.heightProperty().addListener(c -> redrawOnNextPulse());
    }

    private void handlers() {
        // Gère les événements de défilement de la souris sur le panneau de la carte.
        // Effectue un zoom avant/arrière et un défilement horizontal/vertical de la carte.
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            // Détermine la direction du zoom (en avant ou en arrière)
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);

            double x = e.getX();
            double y = e.getY();

            // Effectue un défilement de la carte à partir du point de la souris et ajuste le niveau de zoom
            mapParameters.scroll(x, y);
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-x, -y);

        });

        // Gère l'événement de pression de la souris sur le panneau de la carte.
        // Stocke la position initiale de la souris.
        ObjectProperty<Point2D> previousPosition = new SimpleObjectProperty<>();
        pane.setOnMousePressed(e -> previousPosition.set(new Point2D(e.getX(), e.getY())));

        // Gère l'événement de déplacement de la souris sur le panneau de la carte.
        // Effectue un défilement de la carte en fonction du déplacement de la souris.
        pane.setOnMouseDragged(e -> {
            Point2D currentPosition = new Point2D(e.getX(), e.getY());
            mapParameters.scroll((previousPosition.get().getX() - currentPosition.getX()),
                    (previousPosition.get().getY() - currentPosition.getY()));
            previousPosition.set(currentPosition);
        });

        // Gère l'événement de relâchement de la souris sur le panneau de la carte.
        // Réinitialise la position précédente de la souris.
        pane.setOnMouseReleased(e -> previousPosition.set(null));
    }

    private void redrawIfNeeded() {
        // Vérifie si un re-dessin est nécessaire et l'effectue si c'est le cas.
        if (!redrawNeeded) return;
        redrawNeeded = false;

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Calcule les tuiles à afficher en fonction des coordonnées actuelles de la carte et de la taille du canvas.
        int smallerXTile = ((int) mapParameters.getminX()) / TileManager.NUMBER_OF_PIXEL;
        int smallerYTile = ((int) mapParameters.getminY()) / TileManager.NUMBER_OF_PIXEL;
        int greatestXTile = ((int) (mapParameters.getminX()
                + canvas.widthProperty().get())) / TileManager.NUMBER_OF_PIXEL;
        int greatestYTile = ((int) (mapParameters.getminY()
                + canvas.heightProperty().get())) / TileManager.NUMBER_OF_PIXEL;

        // Parcourt les tuiles visibles et dessine les images correspondantes sur le canvas.
        for (int x = smallerXTile; x <= greatestXTile; x++) {
            for (int y = smallerYTile; y <= greatestYTile; y++) {
                try {
                    Image image = tileId.imageForTileAt(new TileManager
                            .TileID(mapParameters.getZoom(), x, y));
                    graphicsContext.drawImage(image,
                            x * NUMBER_OF_PIXEL - mapParameters.getminX(),
                            y * NUMBER_OF_PIXEL - mapParameters.getminY());
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void redrawOnNextPulse() {
        // Marque le re-dessin comme nécessaire et demande une nouvelle pulsation à la plateforme JavaFX.
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}