import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
    // Configure these for your environment
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "irrigo_2526";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    // JDBC URL without database (for initial connection)
    private static final String BASE_URL = String.format(
        "jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        HOST, PORT
    );
    
    // JDBC URL with database
    private static final String URL = String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        HOST, PORT, DATABASE
    );

    static {
        try {
            // Ensure driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Try to create database if it doesn't exist
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath.", e);
        }
    }

    // Create database if it doesn't exist
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Create database if not exists
            String createDB = "CREATE DATABASE IF NOT EXISTS " + DATABASE;
            stmt.executeUpdate(createDB);
            System.out.println("Database '" + DATABASE + "' is ready.");
            
        } catch (SQLException ex) {
            System.err.println("Database initialization error: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        // After database is created, create tables
        createTables();
    }

    // Create all tables based on the schema
    private static void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
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
            System.out.println("Table 'users' created successfully.");
            
            // Create devices table
            String createDevicesTable =
                "CREATE TABLE IF NOT EXISTS devices (" +
                "device_id INT PRIMARY KEY AUTO_INCREMENT," +
                "user_id INT NOT NULL," +
                "name VARCHAR(100) NOT NULL," +
                "location VARCHAR(255)," +
                "api_key VARCHAR(255) NOT NULL UNIQUE," +
                "threshold DECIMAL(5,2)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "last_seen TIMESTAMP NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ")";
            stmt.executeUpdate(createDevicesTable);
            System.out.println("Table 'devices' created successfully.");
            
            // Create sensor_data table
            String createSensorDataTable =
                "CREATE TABLE IF NOT EXISTS sensor_data (" +
                "data_id INT PRIMARY KEY AUTO_INCREMENT," +
                "device_id INT NOT NULL," +
                "soil_moisture DECIMAL(5,2)," +
                "pump_status BOOLEAN," +
                "temperature DECIMAL(5,2)," +
                "humidity DECIMAL(5,2)," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (device_id) REFERENCES devices(device_id) ON DELETE CASCADE" +
                ")";
            stmt.executeUpdate(createSensorDataTable);
            System.out.println("Table 'sensor_data' created successfully.");
            
            // Create control_logs table
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
            System.out.println("Table 'control_logs' created successfully.");
            
            System.out.println("All tables created successfully!");
            System.out.println("All tables created successfully!");
            
        } catch (SQLException ex) {
            System.err.println("Table creation error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Get a new connection (caller should close)
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Quick test runner
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connected to MySQL successfully.");
            } else {
                System.err.println("Failed to establish connection.");
            }
        } catch (SQLException ex) {
            System.err.println("Connection error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}