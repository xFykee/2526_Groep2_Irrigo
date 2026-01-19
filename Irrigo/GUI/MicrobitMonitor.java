import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

public class MicrobitMonitor {

    public static void main(String[] args) {
        // 1. Selecteer de juiste poort (COM3 op basis van jouw Arduino IDE)
        SerialPort comPort = SerialPort.getCommPort("COM3");
        
        // 2. Stel de snelheid in (moet 9600 zijn zoals in je Arduino code)
        comPort.setBaudRate(9600);

        // 3. Probeer de poort te openen
        if (comPort.openPort()) {
            System.out.println("Succes! Verbonden met Micro:bit op COM3.");
            System.out.println("Luisteren naar data...");
        } else {
            System.err.println("Fout: Kon COM3 niet openen. Sluit de Serial Monitor in Arduino IDE!");
            return;
        }

        // 4. Gebruik een Scanner om de binnenkomende tekstregels te lezen
        try (Scanner dataScanner = new Scanner(comPort.getInputStream())) {
            while (dataScanner.hasNextLine()) {
                String rawLine = dataScanner.nextLine();
                System.out.println("Raw data: " + rawLine);

                // Check of de regel de verwachte data bevat
                if (rawLine.contains("Moisture:")) {
                    processLine(rawLine);
                }
            }
        } catch (Exception e) {
            System.err.println("Fout tijdens het lezen: " + e.getMessage());
        } finally {
            comPort.closePort();
            System.out.println("Poort gesloten.");
        }
    }

    private static void processLine(String line) {
        try {
            // De regel ziet er zo uit: "Moisture: 1023 | Float: 0 | Pump: 0"
            // We splitsen op het '|' teken
            String[] parts = line.split("\\|");

            // Haal de cijfers uit elk deel
            int moisture = Integer.parseInt(parts[0].replaceAll("[^0-9]", "").trim());
            int floatVal = Integer.parseInt(parts[1].replaceAll("[^0-9]", "").trim());
            int pump = Integer.parseInt(parts[2].replaceAll("[^0-9]", "").trim());

            System.out.println("Gevonden -> Vocht: " + moisture + " | Water: " + floatVal + " | Pomp: " + pump);

            // 5. Opslaan in de database
            // Let op: Zorg dat je een methode 'insertMeting' hebt in je Database.java
            Database.insertMeting(moisture, floatVal, pump);

        } catch (Exception e) {
            System.err.println("Kon regel niet verwerken: " + line);
        }
    }
}
