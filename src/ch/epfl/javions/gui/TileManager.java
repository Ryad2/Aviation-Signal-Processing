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

    public record TileID (int zoom, int x, int y) {
        public static boolean isValid (int zoom, int x, int y) {
            return ((x >= 0) && (x < (1 << zoom)) && (y >= 0) && (y < (1 << zoom)));
        }

        public TileID {
            Preconditions.checkArgument(isValid(zoom,x,y));
        }
    }

    public TileManager(Path disqueDurChemin, String hostname) {
        this.cacheMemory = new LinkedHashMap<>(MAX_CACHE_MEMORY_CAPACITY, LOAD_FACTOR,
                true);
        this.disqueDurChemin = disqueDurChemin;
        this.hostname = hostname;
    }

    public Image imageForTileAt (TileID identiteTuile) throws IOException {
        if (cacheMemory.containsKey(identiteTuile)) return cacheMemory.get(identiteTuile);

        else {

            disqueDurChemin = Path.of(identiteTuile.zoom() + "/" + identiteTuile.x()
                    + "/" + identiteTuile.y() + ".png");

            if (cacheMemory.size() == MAX_CACHE_MEMORY_CAPACITY) {
                cacheMemory.remove(cacheMemory.keySet().iterator().next());
            }
            if (Files.exists(disqueDurChemin)){

                try (FileInputStream reader = new FileInputStream(disqueDurChemin.toFile())){
                    Image image = new Image(reader);
                    return cacheMemory.get(image);
                }
            }
            else
            {
                {
                    URL u = new URL("https://" + hostname + "/" + disqueDurChemin);
                    URLConnection c = u.openConnection();
                    c.setRequestProperty("User-Agent", "Javions");

                    //Vérifier que le dossier zoom et le sous dossier x existe
                    // Si y existe pas je crée et si il existe je fais rien

                    Path zoom = Path.of(identiteTuile.zoom() + "/" + identiteTuile.x());

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
            //Transformer un URL en image puis le mettre dans le cache et le disque dur et retourner l'image
        }
    }
}
