package Irrigo.GUI;

import java.sql.*;

public class Database {
    // Deze gegevens gebruik je later voor je echte database
    private static final String URL = "jdbc:mysql://localhost:3306/irrigo_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            // Geen paniek: als er geen database is, printen we alleen een waarschuwing
            System.out.println("Database verbinding mislukt, we gebruiken fallback data.");
            return null;
        }
    }

    // Haal de meest recente vochtigheid op
    public static int getLatestMoisture() {
        String query = "SELECT waarde FROM vochtigheid ORDER BY tijdstip DESC LIMIT 1";
        try (Connection conn = getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) return rs.getInt("waarde");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 35; // De veilige reserve-waarde als de DB nog niet af is
    }

    // Haal de pompstatus op voor pump.java
    public static boolean isPumpActive() {
        String query = "SELECT status FROM apparaten WHERE naam = 'pomp' LIMIT 1";
        try (Connection conn = getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) return rs.getBoolean("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // Standaard op 'Ingeschakeld' zetten
    }
}