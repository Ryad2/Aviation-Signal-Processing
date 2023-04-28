package ch.epfl.javions.gui;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.epfl.javions.Preconditions;
import javafx.scene.image.Image;


public class TileManager {

    private Path disqueDurChemin;
    private final String hostname;
    public static final float LOAD_FACTOR = 0.75f;
    private static final int MAX_CACHE_MEMORY_CAPACITY = 100;
    private final Map<TileID, Image> cacheMemory;

    /**
     * Enregistrement TileId, imbriqué dans la classe TileManager qui représente l'identité d'une tuile OSM
     *
     * @param zoom le niveau de zoom de la tuile
     * @param x l'index X de la tuile
     * @param y l'index Y de la tuile
     */
    public record TileID (int zoom, int x, int y) {

        /**
         * Vérifie qu'il constitue une identité de tuile valide
         *
         * @param zoom le niveau de zoom de la tuile
         * @param x l'index X de la tuile
         * @param y l'index Y de la tuile
         * @return vrai si et seulement si X et Y sont position et plus petit que 2 exposant le zoom
         */
        public static boolean isValid (int zoom, int x, int y) {
            return ((x >= 0) && (x < (1 << zoom)) && (y >= 0) && (y < (1 << zoom)));
        }

        /**
         * Vérifie la condition et lance une exception si elle n'est pas respectée
         *
         * @param zoom le niveau de zoom de la tuile
         * @param x l'index X de la tuile
         * @param y l'index Y de la tuile
         * @throws IllegalArgumentException si la condition n'est pas respectée
         */
        public TileID {
            Preconditions.checkArgument(isValid(zoom,x,y));
        }
    }

    /**
     * Constructeur de TileManager qui créer un mémoire de cache et initiale le chemin du disque dur et le hostname
     *
     * @param disqueDurChemin le chemin pour arriver au disque dur
     * @param hostname le hostname du serveur
     */
    public TileManager(Path disqueDurChemin, String hostname) {
        this.cacheMemory = new LinkedHashMap<>(MAX_CACHE_MEMORY_CAPACITY, LOAD_FACTOR,
                true);
        this.disqueDurChemin = disqueDurChemin;
        this.hostname = hostname;
    }

    /**
     * Prend en argument l'identité de la tuile et en fonction de ce paramètre retourne l'image correspondante
     *
     * @param identiteTuile l'identité de la tuile
     * @throws IOException s'il y a des erreurs d'entrée/sortie
     * @return l'image qui correspond à l'identité de la tuile
     */
    public Image imageForTileAt (TileID identiteTuile) throws IOException {

        //Si l'image se trouve dans le cache mémoire il va retourner l'image correspondant à l'identité de la tuile
        if (cacheMemory.containsKey(identiteTuile)) return cacheMemory.get(identiteTuile);

        else {

            //TODO : faire ça en un bloc
            Path cachePath = Path.of(hardDiskPath.toString(), identiteTuile.zoom() + "/" + identiteTuile.x()
                    + "/" + identiteTuile.y() + ".png");
            Path cachePath1WithoutFirstPart = Path.of(identiteTuile.zoom() + "/" + identiteTuile.x()
                    + "/" + identiteTuile.y() + ".png");

            //Si le cache mémoire est remplie, on retire l'image qui a été utilisé en dernier pour liberer de la place
            if (cacheMemory.size() == MAX_CACHE_MEMORY_CAPACITY) {
                cacheMemory.remove(cacheMemory.keySet().iterator().next());
            }

            //Si le fichier est dans le disque dur il prend le fichier et le met dans le cache mémoire
            if (Files.exists(disqueDurChemin)){

                try (FileInputStream reader = new FileInputStream(disqueDurChemin.toFile())){
                    Image image = new Image(reader);
                    return cacheMemory.get(image);
                }
            }
            else
            {
                {

                    //Si le fichier n'est ni dans le cache mémoire si dans le disque dur
                    //alors, il faut le télécharger d'internet et le mettre dans le cache mémoire et le disque dur
                    URL u = new URL("https://" + hostname + "/" + cachePath1WithoutFirstPart);
                    URLConnection c = u.openConnection();
                    c.setRequestProperty("User-Agent", "Javions");

                    Path zoom = Path.of(hardDiskPath.toString(),identiteTuile.zoom() + "/" + identiteTuile.x());

                    Files.createDirectories(zoom);

                    try (InputStream i = c.getInputStream();
                    FileOutputStream o = new FileOutputStream(disqueDurChemin.toFile()))
                    {
                        byte [] donnee = i.readAllBytes();
                        o.write(donnee);
                        cacheMemory.put(identiteTuile, new Image(new ByteArrayInputStream(donnee)));
                    }
                    return cacheMemory.get(identiteTuile);
                }
            }
        }
    }
}