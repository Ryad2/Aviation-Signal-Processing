package ch.epfl.javions.aircraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipFile;

/**
 * Représente la base de données mictronics des aéronefs
 *
 * @author Ethan Boren (361582)
 * @author Ryad Aouak (315258)
 */
public final class AircraftDatabase {

    private final String fileName;

    /**
     * Construit une base de données à partir du nom du fichier
     *
     * @param fileName nom du fichier
     * @throws NullPointerException si le nom du fichier est null
     */
    public AircraftDatabase(String fileName) {
        Objects.requireNonNull(fileName);
        this.fileName = fileName;
    }

    /**
     * Arrête la recherche lorsque nous avons passé l'adresse cible
     *
     * @param address prend ICO address d'un aéronef
     * @return AircraftData si address est dans le fichier et retourne
     * @throws IOException en cas d'erreur d'entrée/sorties
     */
    public AircraftData get(IcaoAddress address) throws IOException {

        String crc = address.string();
        String fileAddress = crc.substring(crc.length() - 2);

        try (ZipFile fichierZip = new ZipFile(fileName);
             InputStream stream = fichierZip.getInputStream(
                     fichierZip.getEntry(fileAddress + ".csv"));
             Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

             BufferedReader bufferedReader = new BufferedReader(reader)) {
                String[] columns;
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith(address.string())) {
                        columns = line.split(",", -1);

                        return new AircraftData(new AircraftRegistration(columns[1]),
                                new AircraftTypeDesignator(columns[2]), columns[3],
                                new AircraftDescription(columns[4]),
                                WakeTurbulenceCategory.of(columns[5]));
                    }
                    if (line.compareTo(address.toString()) > 0) return null;
                }
            }
        return null;
    }
}