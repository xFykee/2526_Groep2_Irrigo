package Irrigo.GUI;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Pas deze gegevens aan naar jouw database-instellingen
    private static final String URL = "jdbc:mysql://localhost:3306/irrigo_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Haal de meest recente grondvochtigheid op
    public double getLatestMoisture() {
        String query = "SELECT waarde FROM vochtigheid ORDER BY tijdstip DESC LIMIT 1";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getDouble("waarde");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 35.0; // Fallback waarde zodat je GUI niet breekt
    }

    // Haal de pompstatus op
    public boolean getPumpStatus() {
        String query = "SELECT status FROM apparaten WHERE naam = 'pomp'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getBoolean("status");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}