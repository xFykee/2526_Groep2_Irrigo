package Irrigo.GUI;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    // Vervang deze gegevens zodra je database klaar is
    private static final String URL = "jdbc:mysql://localhost:3306/irrigo_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Database niet verbonden: " + e.getMessage());
            return null;
        }
    }

    // Haal de meest recente vochtigheid op (met fallback naar 35%)
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
        return 35; // De waarde die je al in Gui.java en index.html had
    }

    // Haal de pompstatus op
    public static boolean isPumpActive() {
        String query = "SELECT status FROM pomp_instellingen LIMIT 1";
        try (Connection conn = getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) return rs.getBoolean("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // Fallback naar 'Ingeschakeld' zoals in je originele code
    }
}