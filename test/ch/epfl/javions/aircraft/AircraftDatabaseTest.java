package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;


public class AircraftDatabaseTest {

    // Test que le constructeur ne lève pas d'exception avec un nom de fichier valide
    @Test
    public void testConstructorWithValidFileName() {
        String fileName = "14.csv";
        AircraftDatabase database = new AircraftDatabase(fileName);
        assertNotNull(database);//should not work
    }

    // Test que le constructeur lève une NullPointerException avec un nom de fichier nul
    @Test
    public void testConstructorWithNullFileName() {
        assertThrows(NullPointerException.class, () -> {
            String fileName = null;
            AircraftDatabase database = new AircraftDatabase(fileName);
        });

    }

    @Test
    public void testWithTheExmapleOfTheTeacher() throws IOException {

        String nomFichier = getClass().getResource("/aircraft.zip").getFile();
        nomFichier = URLDecoder.decode(nomFichier, UTF_8);

        AircraftDatabase database = new AircraftDatabase(nomFichier);
        AircraftData expected= new AircraftData (new AircraftRegistration("HB-JDC"),
                new AircraftTypeDesignator("A20N"),
                "AIRBUS A-320neo", new AircraftDescription ("L2J"), WakeTurbulenceCategory.MEDIUM);
        IcaoAddress titre = new IcaoAddress("4B1814");
        AircraftData test=database.get(titre);
        assertEquals(expected,test);
    }

    @Test
    public void testWithTheExmapleOfData() throws IOException {

        String nomFichier = getClass().getResource("/aircraft.zip").getFile();
        nomFichier = URLDecoder.decode(nomFichier, UTF_8);

        AircraftDatabase database = new AircraftDatabase(nomFichier);
        AircraftData expected= new AircraftData (new AircraftRegistration("RF-76544"),
                new AircraftTypeDesignator(""),
                "", new AircraftDescription (""), WakeTurbulenceCategory.UNKNOWN);
        IcaoAddress titre = new IcaoAddress("152B00");
        AircraftData test=database.get(titre);
        assertEquals(expected,test);
    }

    @Test
    public void testWithTheExmapleOfData1() throws IOException {

        String nomFichier = getClass().getResource("/aircraft.zip").getFile();
        nomFichier = URLDecoder.decode(nomFichier, UTF_8);

        AircraftDatabase database = new AircraftDatabase(nomFichier);
        AircraftData expected= new AircraftData (new AircraftRegistration("F-GKQA"),
                new AircraftTypeDesignator("DR40"),
                "ROBIN DR-400", new AircraftDescription ("L1P"), WakeTurbulenceCategory.of("L"));
        IcaoAddress titre = new IcaoAddress("392A00");
        AircraftData test=database.get(titre);
        assertEquals(expected,test);
    }



    @Test
    public void testWithTheExmapleThatDontExiste() throws IOException {

        String nomFichier = getClass().getResource("/aircraft.zip").getFile();
        nomFichier = URLDecoder.decode(nomFichier, UTF_8);

        AircraftDatabase database = new AircraftDatabase(nomFichier);

        IcaoAddress titre = new IcaoAddress("A2AAAA");
        AircraftData test=database.get(titre);
        assertNull(test);

    }

    @Test
    public void testWithTheExmapleThatdontWork() throws IOException {

        String nomFichier = getClass().getResource("/aircraft.zip").getFile();
        nomFichier = URLDecoder.decode(nomFichier, UTF_8);

        AircraftDatabase database = new AircraftDatabase(nomFichier);

        IcaoAddress titre = new IcaoAddress("800000");
        AircraftData test=database.get(titre);
        assertNull(test);
    }

    @Test
    public void AircraftDatabaseThrowsNullPointerException ()
    {
        assertThrows(NullPointerException.class, () -> new AircraftDatabase(null));
    }

    private AircraftDatabase getDatabase() {
        // Try to get the database from the resources
        var aircraftResourceUrl = getClass().getResource("/aircraft.zip");
        if (aircraftResourceUrl != null)
            return new AircraftDatabase(URLDecoder.decode(aircraftResourceUrl.getFile(), UTF_8));

        // Try to get the database from the JAVIONS_AIRCRAFT_DATABASE environment variable
        // (only meant to simplify testing of several projects with a single database)
        var aircraftFileName = System.getenv("JAVIONS_AIRCRAFT_DATABASE");
        if (aircraftFileName != null)
            return new AircraftDatabase(aircraftFileName);

        throw new Error("Could not find aircraft database");
    }

    @Test
    void aircraftDatabaseGetReturnsNullWhenAddressDoesNotExist() throws IOException {
        var aircraftDatabase = getDatabase();
        assertNull(aircraftDatabase.get(new IcaoAddress("123456")));
    }

    @Test
    void aircraftDatabaseGetWorksWithFirstLineOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("0086AB"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("ZS-CNA"), aircraftData.registration());
    }

    @Test
    void aircraftDatabaseGetWorksWithLastLineOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("E808C0"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("CC-DAW"), aircraftData.registration());
    }

    @Test
    void aircraftDatabaseGetWorksWithAddressGreaterThanLastOneOfFile() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("FFFF01"));
        assertNull(aircraftData);
    }

    @Test
    void aircraftDatabaseGetReturnsCorrectData() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("4B1805"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("HB-JCN"), aircraftData.registration());
        assertEquals(new AircraftTypeDesignator("BCS3"), aircraftData.typeDesignator());
        assertEquals("AIRBUS A220-300", aircraftData.model());
        assertEquals(new AircraftDescription("L2J"), aircraftData.description());
        assertEquals(WakeTurbulenceCategory.MEDIUM, aircraftData.wakeTurbulenceCategory());
    }

    @Test
    void aircraftDatabaseGetWorksWithEmptyColumns() throws IOException {
        var aircraftDatabase = getDatabase();
        var aircraftData = aircraftDatabase.get(new IcaoAddress("AAAAAA"));
        assertNotNull(aircraftData);
        assertEquals(new AircraftRegistration("N787BK"), aircraftData.registration());
        assertEquals(new AircraftTypeDesignator(""), aircraftData.typeDesignator());
        assertEquals("", aircraftData.model());
        assertEquals(new AircraftDescription(""), aircraftData.description());
        assertEquals(WakeTurbulenceCategory.UNKNOWN, aircraftData.wakeTurbulenceCategory());
    }

}