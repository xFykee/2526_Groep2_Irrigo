import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        int vochtigheid = Database.getLatestMoisture();

        // Sidebar ---------------------------
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(40, 20, 20, 20));
        sidebar.setStyle("-fx-background-color: #0d1117;");

        Label title = new Label("IRRIGO");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label grondLabel = new Label("Grondvochtigheid");
        grondLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px;");

        Label pompLabel = new Label("Pompstatus");
        pompLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 16px;");

        sidebar.getChildren().addAll(title, grondLabel, pompLabel);

        // Main Content ----------------------
        VBox content = new VBox(20);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #11161d;");

        Label header = new Label("Grondvochtigheid");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Label desc = new Label("Website-dashboard toont actuele vochtigheid van de grond");
        desc.setStyle("-fx-text-fill: gray; -fx-font-size: 15px;");

        // Moisture Gauge --------------------
        StackPane gaugePane = new StackPane();
        gaugePane.setPrefSize(200, 200);

        Arc arc = new Arc(100, 100, 90, 90, 90, (vochtigheid * -3.6));  
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.web("#2ecc71"));
        arc.setStrokeWidth(15);
        arc.setType(ArcType.OPEN);

        Label percentage = new Label(vochtigheid + "%");
        percentage.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        gaugePane.getChildren().addAll(arc, percentage);

        Label droog = new Label("Droog");
        droog.setStyle("-fx-text-fill: gray; -fx-font-size: 16px;");

        Label nat = new Label("Nat");
        nat.setStyle("-fx-text-fill: gray; -fx-font-size: 16px;");

        HBox moistureLabels = new HBox(160, droog, nat);

        VBox gaugeSection = new VBox(10, gaugePane, moistureLabels);
        gaugeSection.setAlignment(Pos.CENTER);

        // Line Chart (moisture trend) -------
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setStyle("""
            -fx-background-color: transparent;
            -fx-font-size: 14px;
        """);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(1, 20));
        series.getData().add(new XYChart.Data<>(2, 45));
        series.getData().add(new XYChart.Data<>(3, 35));
        series.getData().add(new XYChart.Data<>(4, 60));
        chart.getData().add(series);

        chart.setMinHeight(180);

        // Auto Water Button -----------------
        Button autoBtn = new Button("AUTOMATISCH WATER GEVEN");
        autoBtn.setStyle("""
            -fx-background-color: #2ecc71;
            -fx-text-fill: black;
            -fx-font-size: 16px;
            -fx-padding: 12 25;
            -fx-background-radius: 8;
        """);

        // Layout rows -----------------------
        HBox mainRow = new HBox(40, gaugeSection, new VBox(chart, autoBtn));
        content.getChildren().addAll(header, desc, mainRow);

        // Final layout -----------------------
        HBox root = new HBox(sidebar, content);

        Scene scene = new Scene(root, 1100, 600);
        stage.setScene(scene);
        stage.setTitle("Irrigo Dashboard");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

/* vochtigheid, pomp aan/uit, flow switch */