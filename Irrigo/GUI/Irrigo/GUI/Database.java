package Irrigo.GUI;

import datalayer.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**micr
 * Database helper class for GUI components
 * Provides methods to retrieve sensor data from the MySQL database
 */
public class Database {
    
    /**
     * Get the latest moisture reading from the database
     * @return Raw moisture value (0-1023)
     */
    public static int getLatestMoisture() {
        String sql = "SELECT vochtigheid FROM metingen ORDER BY tijdstip DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("vochtigheid");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching latest moisture: " + e.getMessage());
        }
        
        return 512; // Default middle value if no data
    }
    
    /**
     * Get the last N moisture readings as percentages
     * @param count Number of readings to retrieve
     * @return List of moisture percentages (0-100)
     */
    public static List<Integer> getLatestMoistureData(int count) {
        List<Integer> data = new ArrayList<>();
        String sql = "SELECT vochtigheid FROM metingen ORDER BY tijdstip DESC LIMIT ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, count);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int rawValue = rs.getInt("vochtigheid");
                // Convert to percentage (higher raw = drier, so invert)
                int percentage = (int) ((1023 - rawValue) * 100.0 / 1023);
                data.add(0, percentage); // Add to front to maintain chronological order
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching moisture data: " + e.getMessage());
        }
        
        // Fill with default values if not enough data
        while (data.size() < count) {
            data.add(0, 50);
        }
        
        return data;
    }
    
    /**
     * Check if the pump is currently active
     * @return true if pump is on, false otherwise
     */
    public static boolean isPumpActive() {
        String sql = "SELECT pomp_status FROM metingen ORDER BY tijdstip DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBoolean("pomp_status");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pump status: " + e.getMessage());
        }
        
        return false; // Default to off if no data
    }
    
    /**
     * Get the latest water level reading
     * @return Water level (0 = empty, 1 = full)
     */
    public static int getWaterLevel() {
        String sql = "SELECT waterniveau FROM metingen ORDER BY tijdstip DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("waterniveau");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching water level: " + e.getMessage());
        }
        
        return 0; // Default to empty if no data
    }
    
    /**
     * Get the latest temperature reading (if available)
     * @return Temperature in Celsius, or null if not available
     */
    public static Double getTemperature() {
        String sql = "SELECT temperature FROM metingen ORDER BY tijdstip DESC LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double temp = rs.getDouble("temperature");
                if (!rs.wasNull()) {
                    return temp;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching temperature: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Set pump status (for manual control)
     * This creates a new measurement entry with updated pump status
     * @param active true to turn pump on, false to turn off
     * @param userId User ID performing the action (for logging)
     */
    public static void setPumpStatus(boolean active, int userId) {
        Connection conn = null;
        try {
            conn = DB.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Get the latest measurement values
            String selectSql = "SELECT device_id, vochtigheid, waterniveau, temperature " +
                             "FROM metingen ORDER BY tijdstip DESC LIMIT 1";
            
            int deviceId = 1;
            int moisture = 512;
            int waterLevel = 0;
            Double temperature = null;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                if (rs.next()) {
                    deviceId = rs.getInt("device_id");
                    moisture = rs.getInt("vochtigheid");
                    waterLevel = rs.getInt("waterniveau");
                    temperature = rs.getDouble("temperature");
                    if (rs.wasNull()) temperature = null;
                }
            }
            
            // Insert new measurement with updated pump status
            String insertSql = "INSERT INTO metingen (device_id, vochtigheid, waterniveau, pomp_status, temperature) " +
                             "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, deviceId);
                pstmt.setInt(2, moisture);
                pstmt.setInt(3, waterLevel);
                pstmt.setBoolean(4, active);
                if (temperature != null) {
                    pstmt.setDouble(5, temperature);
                } else {
                    pstmt.setNull(5, Types.DECIMAL);
                }
                pstmt.executeUpdate();
            }
            
            // Log the control action
            String logSql = "INSERT INTO control_logs (device_id, user_id, action) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(logSql)) {
                pstmt.setInt(1, deviceId);
                pstmt.setInt(2, userId);
                pstmt.setString(3, active ? "PUMP_ON" : "PUMP_OFF");
                pstmt.executeUpdate();
            }
            
            conn.commit(); // Commit transaction
            System.out.println("✓ Pump status updated to: " + (active ? "ON" : "OFF"));
            
        } catch (SQLException e) {
            System.err.println("Error updating pump status: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Set pump status without user tracking (for backwards compatibility)
     * @param active true to turn pump on, false to turn off
     */
    public static void setPumpStatus(boolean active) {
        setPumpStatus(active, 1); // Default to admin user
    }
    
    /**
     * Update the motor power status in the devices table
     * @param deviceId Device ID to update
     * @param powerOn true to turn motor on, false to turn off
     */
    public static void updateMotorPower(int deviceId, boolean powerOn) {
        String sql = "UPDATE devices SET motor_power = ?, last_seen = CURRENT_TIMESTAMP WHERE device_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, powerOn);
            pstmt.setInt(2, deviceId);
            pstmt.executeUpdate();
            
            System.out.println("✓ Motor power updated for device " + deviceId);
            
        } catch (SQLException e) {
            System.err.println("Error updating motor power: " + e.getMessage());
        }
    }
    
    /**
     * Get the current motor power status from devices table
     * @param deviceId Device ID to check
     * @return true if motor is on, false otherwise
     */
    public static boolean getMotorPower(int deviceId) {
        String sql = "SELECT motor_power FROM devices WHERE device_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, deviceId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("motor_power");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching motor power: " + e.getMessage());
        }
        
        return false;
    }
}