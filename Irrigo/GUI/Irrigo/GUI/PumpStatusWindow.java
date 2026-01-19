package Irrigo.GUI;

import java.awt.*;
import javax.swing.*;

public class PumpStatusWindow extends JFrame {
    
    private JLabel statusLabel;
    private JLabel waterLevelLabel;
    private JButton toggleBtn;
    private Timer updateTimer;
    
    public PumpStatusWindow() {
        setTitle("IRRIGO - Pompstatus");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        boolean isPumpActive = Database.isPumpActive();
        
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(15, 17, 21));
        mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 40, 40));
        
        // Pump status card
        JPanel pumpCard = createPumpCard(isPumpActive);
        
        // Water pressure card
        JPanel pressureCard = createPressureCard();
        
        mainPanel.add(pumpCard);
        mainPanel.add(pressureCard);
        
        add(mainPanel);
        setLocationRelativeTo(null);
        
        // Start real-time updates
        updateTimer = new Timer(2000, e -> updatePumpStatus()); // Update every 2 seconds
        updateTimer.start();
    }
    
    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
        super.dispose();
    }
    
    private void updatePumpStatus() {
        boolean isActive = Database.isPumpActive();
        int waterLevel = Database.getWaterLevel();
        
        if (statusLabel != null) {
            statusLabel.setText(isActive ? "Ingeschakeld" : "Uitgeschakeld");
            statusLabel.setForeground(isActive ? new Color(76, 217, 100) : new Color(255, 69, 58));
        }
        
        if (toggleBtn != null) {
            toggleBtn.setText(isActive ? "Schakel uit" : "Schakel in");
        }
        
        if (waterLevelLabel != null) {
            waterLevelLabel.setText("Waterniveau: " + (waterLevel == 1 ? "Vol" : "Leeg"));
            waterLevelLabel.setForeground(waterLevel == 1 ? new Color(76, 217, 100) : new Color(255, 69, 58));
        }
    }
    
    private JPanel createPumpCard(boolean isActive) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(26, 29, 35));
        card.setPreferredSize(new Dimension(280, 400));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Pompstatus");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(200, 200, 200));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        statusLabel = new JLabel(isActive ? "Ingeschakeld" : "Uitgeschakeld");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 28));
        statusLabel.setForeground(isActive ? new Color(76, 217, 100) : new Color(255, 69, 58));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(statusLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        toggleBtn = new JButton(isActive ? "Schakel uit" : "Schakel in");
        toggleBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        toggleBtn.setForeground(Color.WHITE);
        toggleBtn.setBackground(new Color(26, 29, 35));
        toggleBtn.setBorder(BorderFactory.createLineBorder(new Color(58, 63, 71), 1));
        toggleBtn.setFocusPainted(false);
        toggleBtn.setPreferredSize(new Dimension(240, 40));
        toggleBtn.setMaximumSize(new Dimension(240, 40));
        toggleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        toggleBtn.addActionListener(e -> {
            // Toggle pump status
            boolean currentStatus = statusLabel.getText().equals("Ingeschakeld");
            boolean newStatus = !currentStatus;
            
            // Update database (user_id = 1 for admin)
            Database.setPumpStatus(newStatus, 1);
            
            // Update UI immediately
            statusLabel.setText(newStatus ? "Ingeschakeld" : "Uitgeschakeld");
            statusLabel.setForeground(newStatus ? new Color(76, 217, 100) : new Color(255, 69, 58));
            toggleBtn.setText(newStatus ? "Schakel uit" : "Schakel in");
            
            // Show feedback
            String message = newStatus ? 
                "Pomp is nu ingeschakeld.\nControleer of er voldoende water in het reservoir is." :
                "Pomp is nu uitgeschakeld.";
            
            JOptionPane.showMessageDialog(
                this,
                message,
                "Pompstatus gewijzigd",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        
        card.add(toggleBtn);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Add water level indicator
        int waterLevel = Database.getWaterLevel();
        waterLevelLabel = new JLabel("Waterniveau: " + (waterLevel == 1 ? "Vol" : "Leeg"));
        waterLevelLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        waterLevelLabel.setForeground(waterLevel == 1 ? new Color(76, 217, 100) : new Color(255, 69, 58));
        waterLevelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(waterLevelLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Add temperature if available
        Double temp = Database.getTemperature();
        if (temp != null) {
            JLabel tempLabel = new JLabel(String.format("Temperatuur: %.1f°C", temp));
            tempLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            tempLabel.setForeground(new Color(150, 150, 150));
            tempLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(tempLabel);
        }
        
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private JPanel createPressureCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(26, 29, 35));
        card.setPreferredSize(new Dimension(450, 400));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel title = new JLabel("Waterdruk");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(200, 200, 200));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JLabel status = new JLabel("● Normaal");
        status.setFont(new Font("Arial", Font.PLAIN, 16));
        status.setForeground(new Color(76, 217, 100));
        status.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(status);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Pressure chart
        PressureChart chart = new PressureChart();
        chart.setPreferredSize(new Dimension(410, 220));
        chart.setMaximumSize(new Dimension(410, 220));
        chart.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(chart);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JButton startBtn = new JButton("Start pomp");
        startBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        startBtn.setForeground(Color.WHITE);
        startBtn.setBackground(new Color(26, 29, 35));
        startBtn.setBorder(BorderFactory.createLineBorder(new Color(58, 63, 71), 1));
        startBtn.setFocusPainted(false);
        startBtn.setPreferredSize(new Dimension(410, 40));
        startBtn.setMaximumSize(new Dimension(410, 40));
        startBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        startBtn.addActionListener(e -> {
            int waterLevel = Database.getWaterLevel();
            
            if (waterLevel == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "Waarschuwing: Waterreservoir is leeg!\nVul het reservoir voordat u de pomp start.",
                    "Waterreservoir leeg",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            Database.setPumpStatus(true, 1); // user_id = 1 for admin
            
            JOptionPane.showMessageDialog(
                this,
                "Pomp gestart!\nDe pomp zal water geven aan de planten.",
                "Pomp gestart",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // Update the UI
            updatePumpStatus();
        });
        
        card.add(startBtn);
        
        return card;
    }
}

// Pressure chart component
class PressureChart extends JPanel {
    private double[] pressureData = {2.0, 2.1, 1.9, 2.05, 2.0};
    private int[] timeLabels = {0, 6, 12, 18, 24};
    
    public PressureChart() {
        setBackground(new Color(26, 29, 35));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int padding = 40;
        
        // Draw axes
        g2d.setColor(Color.GRAY);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, padding, padding, height - padding);
        
        // Draw grid lines
        g2d.setColor(new Color(58, 63, 71));
        for (int i = 1; i < 5; i++) {
            int y = padding + i * (height - 2 * padding) / 5;
            g2d.drawLine(padding, y, width - padding, y);
        }
        
        // Draw line
        g2d.setColor(new Color(76, 217, 100));
        g2d.setStroke(new BasicStroke(2));
        
        int segmentWidth = (width - 2 * padding) / (pressureData.length - 1);
        
        for (int i = 0; i < pressureData.length - 1; i++) {
            int x1 = padding + i * segmentWidth;
            int y1 = height - padding - (int)((pressureData[i] - 1.5) * (height - 2 * padding) / 1.0);
            int x2 = padding + (i + 1) * segmentWidth;
            int y2 = height - padding - (int)((pressureData[i + 1] - 1.5) * (height - 2 * padding) / 1.0);
            
            g2d.drawLine(x1, y1, x2, y2);
            
            // Draw points
            g2d.fillOval(x1 - 4, y1 - 4, 8, 8);
            if (i == pressureData.length - 2) {
                g2d.fillOval(x2 - 4, y2 - 4, 8, 8);
            }
        }
        
        // Draw time labels
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i < timeLabels.length; i++) {
            int x = padding + i * segmentWidth;
            g2d.drawString(timeLabels[i] + "h", x - 10, height - padding + 20);
        }
        
        // Draw pressure labels
        String[] pressureLabels = {"1.5", "1.8", "2.0", "2.3", "2.5"};
        for (int i = 0; i < pressureLabels.length; i++) {
            int y = height - padding - i * (height - 2 * padding) / 4;
            g2d.drawString(pressureLabels[i], padding - 30, y + 5);
        }
    }
}