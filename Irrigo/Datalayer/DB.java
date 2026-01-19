import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DB {
    // Database configuratie
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "irrigo_2526";
    private static final String USER = "root";
    private static final String PASSWORD = "R22t44t66t88";  // Pas aan naar jouw wachtwoord

    private static final String BASE_URL = String.format(
        "jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        HOST, PORT
    );
    
    private static final String URL = String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        HOST, PORT, DATABASE
    );

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver niet gevonden!", e);
        }
    }

    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DATABASE);
            System.out.println("✓ Database '" + DATABASE + "' aangemaakt");
            
        } catch (SQLException ex) {
            System.err.println("Database fout: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        if (!tablesExist()) {
            createTables();
        } else {
            System.out.println("✓ Tabellen bestaan al, overslaan.");
        }
    }

    private static boolean tablesExist() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
            
            Set<String> existingTables = new HashSet<>();
            while (rs.next()) {
                existingTables.add(rs.getString(1));
            }
            
            return existingTables.contains("users") &&
                   existingTables.contains("devices") &&
                   existingTables.contains("metingen") &&
                   existingTables.contains("control_logs");
            
        } catch (SQLException e) {
            return false;
        }
    }

    private static void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tabel voor gebruikers
            String createUsersTable =
                "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(50) NOT NULL UNIQUE," +
                "password_hash VARCHAR(255) NOT NULL," +
                "email VARCHAR(100) NOT NULL UNIQUE," +
                "role ENUM('admin', 'user') DEFAULT 'user'," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createUsersTable);
            System.out.println("✓ Tabel 'users' aangemaakt");
            
            // Tabel voor apparaten/devices
            String createDevicesTable =
                "CREATE TABLE IF NOT EXISTS devices (" +
                "device_id INT PRIMARY KEY AUTO_INCREMENT," +
                "user_id INT NOT NULL," +
                "name VARCHAR(100) NOT NULL," +
                "location VARCHAR(255)," +
                "api_key VARCHAR(255) NOT NULL UNIQUE," +
                "threshold DECIMAL(5,2) DEFAULT 30.0," +
                "auto_mode BOOLEAN DEFAULT TRUE," +
                "motor_power BOOLEAN DEFAULT FALSE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_seen TIMESTAMP NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")";
            stmt.executeUpdate(createDevicesTable);
            System.out.println("✓ Tabel 'devices' aangemaakt");
            
            // Tabel voor metingen (sensor data van Micro:bit)
            String createMetingenTable =
                "CREATE TABLE IF NOT EXISTS metingen (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "device_id INT NOT NULL," +
                "vochtigheid INT NOT NULL," +
                "waterniveau INT NOT NULL," +
                "pomp_status BOOLEAN NOT NULL," +
                "temperature DECIMAL(5,2)," +
                "tijdstip TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE," +
                "INDEX idx_tijdstip (tijdstip)," +
                "INDEX idx_device_tijdstip (device_id, tijdstip)" +
                ")";
            stmt.executeUpdate(createMetingenTable);
            System.out.println("✓ Tabel 'metingen' aangemaakt");
            
            // Tabel voor controle logs
            String createControlLogsTable =
                "CREATE TABLE IF NOT EXISTS control_logs (" +
                "log_id INT PRIMARY KEY AUTO_INCREMENT," +
                "device_id INT NOT NULL," +
                "user_id INT," +
                "action VARCHAR(50) NOT NULL," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL" +
                ")";
            stmt.executeUpdate(createControlLogsTable);
            System.out.println("✓ Tabel 'control_logs' aangemaakt");
            
            // Voeg een standaard gebruiker en device toe
            insertDefaultData(conn);
            
            System.out.println("\n🎉 Database volledig geconfigureerd!");
            
        } catch (SQLException ex) {
            System.err.println("Tabel aanmaak fout: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private static void insertDefaultData(Connection conn) throws SQLException {
        // Check of er al een gebruiker bestaat
        String checkUser = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkUser)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Voeg standaard admin gebruiker toe
                String insertUser = "INSERT INTO users (username, password_hash, email, role) " +
                                   "VALUES ('admin', 'admin123', 'admin@irrigo.nl', 'admin')";
                stmt.executeUpdate(insertUser);
                System.out.println("✓ Standaard admin gebruiker aangemaakt (admin/admin123)");
                
                // Voeg standaard device toe
                String insertDevice = "INSERT INTO devices (user_id, name, location, api_key, threshold) " +
                                     "VALUES (1, 'Micro:bit Tuin', 'Achtertuin', 'DEFAULT_API_KEY_123', 30.0)";
                stmt.executeUpdate(insertDevice);
                System.out.println("✓ Standaard device aangemaakt");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        System.out.println("Database setup gestart...\n");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("\n✓ Database verbinding succesvol!");
                
                // Test query
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM metingen");
                if (rs.next()) {
                    System.out.println("Aantal metingen in database: " + rs.getInt("total"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Verbindingsfout: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}