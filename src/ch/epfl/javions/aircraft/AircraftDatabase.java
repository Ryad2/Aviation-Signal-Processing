package ch.epfl.javions.aircraft;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class AircraftDatabase {
    private final String fileName;

    public AircraftDatabase(String fileName) {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;
    }

    /**
     * Arrête la recherche lorsque nous avons passé l'adresse cible
     * @param address prend ICO adress d'un aeronerf
     * @return AircraftData si l'adress est dans le fichier et retourne
     * @throws IOException en cas d'erreur d'entrée/sorties
     */
    public AircraftData get(IcaoAddress address) throws IOException {

        String crc = address.string();
        String fileAddress = crc.substring(crc.length()-2);

        try (ZipFile fichierZipe = new ZipFile(fileName);
             InputStream stream = fichierZipe.getInputStream(fichierZipe.getEntry(fileAddress+".csv"));
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader))

        {
            String [] columns;
            String line = "";
             while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(address.string())) {
                    columns = line.split(",",-1);

                    return new AircraftData(new AircraftRegistration(columns[1]),
                            new AircraftTypeDesignator(columns[2]),
                            columns[3],
                            new AircraftDescription(columns[4]),
                            WakeTurbulenceCategory.of(columns[5]));

                }
                if (line.compareTo(address.toString()) > 0) {
                    return null; //Ou break?
                }
            }
        }
        return null;
    }
}