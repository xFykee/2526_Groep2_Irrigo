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