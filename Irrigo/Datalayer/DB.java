import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Db.java
 * Simple MySQL connector utility for Java (JDBC).
 *
 * Place MySQL Connector/J on the classpath:
 * - Maven: add dependency mysql:mysql-connector-java in pom.xml
 * - Gradle: add implementation 'mysql:mysql-connector-java'
 * - Or add the connector JAR to your project's classpath in VS Code
 *
 * Usage:
 * Connection conn = Db.getConnection();
 * try (Connection c = conn) { ... }
 */


public class Db {
    // Configure these for your environment
    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "irrigo_2526";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    // JDBC URL
    private static final String URL = String.format(
        "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        HOST, PORT, DATABASE
    );

    static {
        try {
            // Ensure driver is loaded (optional with modern JDBC, but harmless)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath.", e);
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