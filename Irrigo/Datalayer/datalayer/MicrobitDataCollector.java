package datalayer;

import com.fazecast.jSerialComm.SerialPort;
import java.sql.*;
import java.util.regex.*;

/**
 * Collects data from Micro:bit via serial connection and stores it in MySQL database
 * Run this class to start collecting sensor data from your Micro:bit device
 */
public class MicrobitDataCollector {
    
    private static SerialPort microbitPort = null;
    private static volatile boolean running = true;
    private static int deviceId = 1; // Default device ID
    
    public static void main(String[] args) {
        System.out.println("=== Irrigo Micro:bit Data Collector ===");
        System.out.println("Connecting to MySQL database...\n");
        
        // Initialize database
        DB.main(new String[0]);
        
        // Get device ID from database (use first available device)
        deviceId = getDefaultDeviceId();
        System.out.println("Using device ID: " + deviceId + "\n");
        
        // Find and connect to Micro:bit
        if (!connectToMicrobit()) {
            System.err.println("Failed to connect to Micro:bit. Exiting.");
            return;
        }
        
        // Add shutdown hook for clean exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            if (microbitPort != null && microbitPort.isOpen()) {
                microbitPort.closePort();
                System.out.println("\nConnection closed gracefully.");
            }
        }));
        
        // Start reading data
        collectData();
    }
    
    /**
     * Get the default device ID from the database
     * @return Device ID to use for measurements
     */
    private static int getDefaultDeviceId() {
        String sql = "SELECT device_id FROM devices ORDER BY device_id LIMIT 1";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("device_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching device ID: " + e.getMessage());
        }
        
        return 1; // Default fallback
    }
    
    /**
     * Find and connect to the Micro:bit serial port
     * @return true if connection successful, false otherwise
     */
    private static boolean connectToMicrobit() {
        SerialPort[] ports = SerialPort.getCommPorts();
        
        System.out.println("Available serial ports:");
        for (SerialPort port : ports) {
            System.out.println("  - " + port.getSystemPortName() + " (" + port.getDescriptivePortName() + ")");
        }
        
        // Try to find Micro:bit port
        for (SerialPort port : ports) {
            String portName = port.getSystemPortName().toLowerCase();
            String description = port.getDescriptivePortName().toLowerCase();
            
            // Common Micro:bit identifiers
            if (portName.contains("com") || portName.contains("ttyacm") || 
                portName.contains("usb") || description.contains("micro") ||
                description.contains("mbed") || description.contains("jlink")) {
                microbitPort = port;
                break;
            }
        }
        
        // If not found automatically, use first available port
        if (microbitPort == null && ports.length > 0) {
            System.out.println("\nMicro:bit not auto-detected. Using first available port.");
            microbitPort = ports[0];
        }
        
        if (microbitPort == null) {
            System.err.println("\n❌ No serial ports found!");
            System.err.println("Make sure your Micro:bit is connected via USB.");
            return false;
        }
        
        // Configure and open port
        microbitPort.setBaudRate(115200);
        microbitPort.setNumDataBits(8);
        microbitPort.setNumStopBits(1);
        microbitPort.setParity(SerialPort.NO_PARITY);
        microbitPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        
        if (microbitPort.openPort()) {
            System.out.println("\n✓ Connected to: " + microbitPort.getSystemPortName());
            System.out.println("Waiting for data from Micro:bit...\n");
            
            // Update last_seen for device
            updateDeviceLastSeen();
            
            return true;
        } else {
            System.err.println("❌ Failed to open port: " + microbitPort.getSystemPortName());
            return false;
        }
    }
    
    /**
     * Update the last_seen timestamp for the device
     */
    private static void updateDeviceLastSeen() {
        String sql = "UPDATE devices SET last_seen = CURRENT_TIMESTAMP WHERE device_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, deviceId);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating device last_seen: " + e.getMessage());
        }
    }
    
    /**
     * Main data collection loop
     * Reads from serial port and processes incoming data
     */
    private static void collectData() {
        StringBuilder buffer = new StringBuilder();
        byte[] readBuffer = new byte[1024];
        int updateCounter = 0;
        
        while (running) {
            try {
                int bytesRead = microbitPort.readBytes(readBuffer, readBuffer.length);
                
                if (bytesRead > 0) {
                    String newData = new String(readBuffer, 0, bytesRead);
                    buffer.append(newData);
                    
                    // Process complete lines
                    String data = buffer.toString();
                    if (data.contains("\n")) {
                        String[] lines = data.split("\n");
                        
                        // Process all complete lines
                        for (int i = 0; i < lines.length - 1; i++) {
                            String line = lines[i].trim();
                            if (!line.isEmpty()) {
                                processLine(line);
                            }
                        }
                        
                        // Keep incomplete line in buffer
                        buffer = new StringBuilder(lines[lines.length - 1]);
                    }
                }
                
                // Update device last_seen every 30 seconds
                updateCounter++;
                if (updateCounter >= 300) { // 300 * 100ms = 30 seconds
                    updateDeviceLastSeen();
                    updateCounter = 0;
                }
                
                Thread.sleep(100); // Small delay to prevent CPU overuse
                
            } catch (InterruptedException e) {
                System.out.println("Data collection interrupted.");
                break;
            } catch (Exception e) {
                System.err.println("Error reading serial data: " + e.getMessage());
            }
        }
    }
    
    /**
     * Process a single line of data from Micro:bit
     * Expected format: "Moisture: 1023  | Float: 0  | Pump: 0"
     * Optional with temperature: "Moisture: 1023  | Float: 0  | Pump: 0 | Temp: 22.5"
     * @param line The data line to process
     */
    private static void processLine(String line) {
        // Pattern to match the expected data format (with optional temperature)
        Pattern pattern = Pattern.compile(
            "Moisture:\\s*(\\d+)\\s*\\|\\s*Float:\\s*(\\d+)\\s*\\|\\s*Pump:\\s*(\\d+)(?:\\s*\\|\\s*Temp:\\s*([\\d.]+))?"
        );
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            try {
                int moisture = Integer.parseInt(matcher.group(1));
                int waterLevel = Integer.parseInt(matcher.group(2));
                int pumpStatus = Integer.parseInt(matcher.group(3));
                
                // Optional temperature
                Double temperature = null;
                if (matcher.group(4) != null) {
                    temperature = Double.parseDouble(matcher.group(4));
                }
                
                // Validate data ranges
                if (moisture >= 0 && moisture <= 1023 && 
                    waterLevel >= 0 && waterLevel <= 1 &&
                    pumpStatus >= 0 && pumpStatus <= 1) {
                    
                    saveToDatabase(moisture, waterLevel, pumpStatus, temperature);
                } else {
                    System.err.println("⚠ Invalid data values: " + line);
                }
                
            } catch (NumberFormatException e) {
                System.err.println("⚠ Failed to parse numbers from: " + line);
            }
        } else {
            // Print non-matching lines for debugging (might be debug output from Micro:bit)
            if (!line.trim().isEmpty() && !line.startsWith("Moisture:")) {
                System.out.println("[Micro:bit] " + line);
            }
        }
    }
    
    /**
     * Save sensor readings to the MySQL database
     * @param moisture Raw moisture sensor value (0-1023)
     * @param waterLevel Water level sensor (0 or 1)
     * @param pumpStatus Pump state (0 or 1)
     * @param temperature Temperature in Celsius (optional, can be null)
     */
    private static void saveToDatabase(int moisture, int waterLevel, int pumpStatus, Double temperature) {
        String sql = "INSERT INTO metingen (device_id, vochtigheid, waterniveau, pomp_status, temperature) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, deviceId);
            pstmt.setInt(2, moisture);
            pstmt.setInt(3, waterLevel);
            pstmt.setBoolean(4, pumpStatus == 1);
            
            if (temperature != null) {
                pstmt.setDouble(5, temperature);
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            pstmt.executeUpdate();
            
            // Calculate moisture percentage for display
            int moisturePercent = (int) ((1023 - moisture) * 100.0 / 1023);
            
            // Format output
            String tempStr = temperature != null ? String.format(" | Temp: %.1f°C", temperature) : "";
            System.out.printf("✓ [%s] Moisture: %d%% (raw: %d) | Water: %s | Pump: %s%s%n",
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()),
                moisturePercent,
                moisture,
                waterLevel == 1 ? "FULL" : "EMPTY",
                pumpStatus == 1 ? "ON" : "OFF",
                tempStr
            );
            
        } catch (SQLException e) {
            System.err.println("⚠ Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test method to verify database connection without Micro:bit
     * Inserts sample data for testing the GUI
     */
    public static void testDatabase() {
        System.out.println("\n=== Testing Database Connection ===");
        System.out.println("Inserting sample data...\n");
        
        // Initialize database
        DB.main(new String[0]);
        deviceId = getDefaultDeviceId();
        
        // Insert test data with varying conditions
        saveToDatabase(512, 1, 0, 22.5);  // 50% moisture, water full, pump off, 22.5°C
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        saveToDatabase(200, 1, 1, 23.0);  // ~80% moisture, water full, pump on, 23°C
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        saveToDatabase(800, 0, 0, 21.8);  // ~22% moisture, water empty, pump off, 21.8°C
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        
        saveToDatabase(400, 1, 0, 22.2);  // ~61% moisture, water full, pump off, 22.2°C
        
        System.out.println("\n✓ Test complete! Check your database for 4 new entries.");
        System.out.println("You can now run the GUI to see the data visualized.");
    }
}