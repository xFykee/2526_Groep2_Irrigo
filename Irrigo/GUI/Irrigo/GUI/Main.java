package Irrigo.GUI;

import datalayer.DB;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;

public class Main extends JFrame {
    
    private MoistureGauge moistureGauge;
    private SimpleChart chart;
    private Timer updateTimer;
    
    public Main() {
        setTitle("Irrigo Dashboard");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initial data
        updateData();
        
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(17, 22, 29));
        
        // Sidebar
        JPanel sidebar = createSidebar();
        
        // Content area
        JPanel content = createContent();
        
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(content, BorderLayout.CENTER);
        
        add(mainPanel);
        setLocationRelativeTo(null);
        
        // Start real-time updates
        updateTimer = new Timer(2000, e -> updateData()); // Update every 2 seconds
        updateTimer.start();
    }
    
    private void updateData() {
        int rawMoisture = Database.getLatestMoisture();
        int percentage = (int) ((1023 - rawMoisture) * 100.0 / 1023); // Convert to % (higher raw = drier)
        if (moistureGauge != null) {
            moistureGauge.setPercentage(percentage);
            moistureGauge.repaint();
        }
        if (chart != null) {
            List<Integer> data = Database.getLatestMoistureData(4); // Get last 4 readings
            chart.setData(data);
            chart.repaint();
        }
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(13, 17, 23));
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        JLabel title = new JLabel("IRRIGO");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));
        
        JLabel grondLabel = new JLabel("Grondvochtigheid");
        grondLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        grondLabel.setForeground(new Color(46, 204, 113));
        grondLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sidebar.add(grondLabel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel pompLabel = new JLabel("Pompstatus");
        pompLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        pompLabel.setForeground(Color.GRAY);
        pompLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pompLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pompLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                openPumpStatus();
            }
        });
        
        sidebar.add(pompLabel);
        sidebar.add(Box.createVerticalGlue());
        
        return sidebar;
    }
    
    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(17, 22, 29));
        content.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        JLabel header = new JLabel("Grondvochtigheid");
        header.setFont(new Font("Arial", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel desc = new JLabel("Website-dashboard toont actuele vochtigheid van de grond");
        desc.setFont(new Font("Arial", Font.PLAIN, 15));
        desc.setForeground(Color.GRAY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        content.add(header);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(desc);
        content.add(Box.createRigidArea(new Dimension(0, 30)));
        
        // Main row with gauge and chart
        JPanel mainRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 0));
        mainRow.setBackground(new Color(17, 22, 29));
        mainRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Gauge section
        JPanel gaugeSection = createGaugeSection();
        
        // Chart section
        JPanel chartSection = createChartSection();
        
        mainRow.add(gaugeSection);
        mainRow.add(chartSection);
        
        content.add(mainRow);
        
        return content;
    }
    
    private JPanel createGaugeSection() {
        JPanel gaugeSection = new JPanel();
        gaugeSection.setLayout(new BoxLayout(gaugeSection, BoxLayout.Y_AXIS));
        gaugeSection.setBackground(new Color(17, 22, 29));
        
        // Gauge panel
        moistureGauge = new MoistureGauge(0); // Initial value, will be updated
        moistureGauge.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        gaugeSection.add(moistureGauge);
        gaugeSection.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Labels
        JPanel labels = new JPanel(new FlowLayout(FlowLayout.CENTER, 160, 0));
        labels.setBackground(new Color(17, 22, 29));
        
        JLabel droog = new JLabel("Droog");
        droog.setFont(new Font("Arial", Font.PLAIN, 16));
        droog.setForeground(Color.GRAY);
        
        JLabel nat = new JLabel("Nat");
        nat.setFont(new Font("Arial", Font.PLAIN, 16));
        nat.setForeground(Color.GRAY);
        
        labels.add(droog);
        labels.add(nat);
        labels.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        gaugeSection.add(labels);
        
        return gaugeSection;
    }
    
    private JPanel createChartSection() {
        JPanel chartSection = new JPanel();
        chartSection.setLayout(new BoxLayout(chartSection, BoxLayout.Y_AXIS));
        chartSection.setBackground(new Color(17, 22, 29));
        chartSection.setPreferredSize(new Dimension(450, 300));
        
        // Simple chart
        chart = new SimpleChart();
        chart.setPreferredSize(new Dimension(450, 200));
        chart.setMaximumSize(new Dimension(450, 200));
        chart.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        chartSection.add(chart);
        chartSection.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Button
        JButton autoBtn = new JButton("AUTOMATISCH WATER GEVEN");
        autoBtn.setFont(new Font("Arial", Font.BOLD, 16));
        autoBtn.setForeground(Color.BLACK);
        autoBtn.setBackground(new Color(46, 204, 113));
        autoBtn.setFocusPainted(false);
        autoBtn.setBorderPainted(false);
        autoBtn.setPreferredSize(new Dimension(300, 45));
        autoBtn.setMaximumSize(new Dimension(300, 45));
        autoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        chartSection.add(autoBtn);
        
        return chartSection;
    }
    
    private void openPumpStatus() {
        PumpStatusWindow pumpWindow = new PumpStatusWindow();
        pumpWindow.setVisible(true);
    }
    
    public static void main(String[] args) {
        // Run DB setup first
        DB.main(new String[0]);
        
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}

// Custom component for moisture gauge
class MoistureGauge extends JPanel {
    private int percentage;
    
    public MoistureGauge(int percentage) {
        this.percentage = percentage;
        setPreferredSize(new Dimension(200, 200));
        setBackground(new Color(17, 22, 29));
    }
    
    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = 90;
        
        // Background arc
        g2d.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(40, 40, 40));
        g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, -270);
        
        // Percentage arc
        g2d.setColor(new Color(46, 204, 113));
        int angle = (int) (percentage * -2.7); // Scale to 270 degrees max
        g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, 90, angle);
        
        // Percentage text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        String text = percentage + "%";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, centerX - textWidth / 2, centerY + fm.getAscent() / 2);
    }
}

// Simple chart component
class SimpleChart extends JPanel {
    private List<Integer> dataPoints = new ArrayList<>();
    
    public SimpleChart() {
        setBackground(new Color(17, 22, 29));
        // Initialize with defaults
        for (int i = 0; i < 4; i++) {
            dataPoints.add(20 + i * 20);
        }
    }
    
    public void setData(List<Integer> data) {
        this.dataPoints = new ArrayList<>(data);
        if (dataPoints.size() < 4) {
            while (dataPoints.size() < 4) {
                dataPoints.add(0, 0); // Pad with zeros if less than 4
            }
        } else if (dataPoints.size() > 4) {
            dataPoints = dataPoints.subList(dataPoints.size() - 4, dataPoints.size());
        }
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
        
        // Draw line
        g2d.setColor(new Color(46, 204, 113));
        g2d.setStroke(new BasicStroke(2));
        
        int segmentWidth = (width - 2 * padding) / (dataPoints.size() - 1);
        
        for (int i = 0; i < dataPoints.size() - 1; i++) {
            int x1 = padding + i * segmentWidth;
            int y1 = height - padding - (dataPoints.get(i) * (height - 2 * padding) / 100);
            int x2 = padding + (i + 1) * segmentWidth;
            int y2 = height - padding - (dataPoints.get(i + 1) * (height - 2 * padding) / 100);
            
            g2d.drawLine(x1, y1, x2, y2);
            
            // Draw points
            g2d.fillOval(x1 - 4, y1 - 4, 8, 8);
            if (i == dataPoints.size() - 2) {
                g2d.fillOval(x2 - 4, y2 - 4, 8, 8);
            }
        }
    }
}