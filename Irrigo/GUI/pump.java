import javafx.application.Application;

import javafx.scene.Scene;

import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;
 
public class Main extends Application {
 
    @Override

    public void start(Stage primaryStage) {

        PumpStatusScreen screen = new PumpStatusScreen();
 
        BorderPane root = new BorderPane();

        root.setStyle("-fx-background-color: #0f1115;");

        root.setCenter(screen);
 
        Scene scene = new Scene(root, 900, 550);
 
        primaryStage.setTitle("IRRIGO - Pompstatus");

        primaryStage.setScene(scene);

        primaryStage.show();

    }
 
    public static void main(String[] args) {

        launch(args);

    }

}

 
import javafx.geometry.Insets;

import javafx.geometry.Pos;

import javafx.scene.chart.LineChart;

import javafx.scene.chart.NumberAxis;

import javafx.scene.chart.XYChart;

import javafx.scene.control.Button;

import javafx.scene.control.Label;

import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;
 
public class PumpStatusScreen extends HBox {
 
    public PumpStatusScreen() {
 
        setSpacing(40);

        setPadding(new Insets(40));

        setStyle("-fx-background-color: #0f1115;");
 
        // -----------------------------

        // LINKER KAART: POMPSTATUS

        // -----------------------------

        VBox pumpBox = new VBox(15);

        pumpBox.setPadding(new Insets(20));

        pumpBox.setPrefWidth(280);

        pumpBox.setStyle("-fx-background-color: #1a1d23; -fx-background-radius: 12;");
 
        Label pumpTitle = new Label("Pomptstatus");

        pumpTitle.setStyle("-fx-text-fill: #c8c8c8; -fx-font-size: 18; -fx-font-weight: bold;");
 
        Label pumpState = new Label("Ingeschakeld");

        pumpState.setStyle("-fx-text-fill: #4cd964; -fx-font-size: 28; -fx-font-weight: bold;");
 
        Button offButton = new Button("Schakel uit");

        offButton.setStyle(

            "-fx-background-color: transparent;" +

            "-fx-border-color: #3a3f47;" +

            "-fx-border-radius: 8;" +

            "-fx-text-fill: white;" +

            "-fx-font-size: 14;" +

            "-fx-padding: 10 20;"

        );
 
        pumpBox.getChildren().addAll(pumpTitle, pumpState, offButton);
 
        // -----------------------------

        // RECHTER KAART: WATERDRUK

        // -----------------------------

        VBox pressureBox = new VBox(15);

        pressureBox.setPadding(new Insets(20));

        pressureBox.setPrefWidth(350);

        pressureBox.setStyle("-fx-background-color: #1a1d23; -fx-background-radius: 12;");
 
        Label pressureTitle = new Label("Waterdruk");

        pressureTitle.setStyle("-fx-text-fill: #c8c8c8; -fx-font-size: 18; -fx-font-weight: bold;");
 
        Label pressureStatus = new Label("● Normaal");

        pressureStatus.setStyle("-fx-text-fill: #4cd964; -fx-font-size: 16;");
 
        // -------- grafiek ----------

        NumberAxis xAxis = new NumberAxis();

        xAxis.setTickLabelFill(javafx.scene.paint.Color.GRAY);
 
        NumberAxis yAxis = new NumberAxis();

        yAxis.setTickLabelFill(javafx.scene.paint.Color.GRAY);
 
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setLegendVisible(false);

        chart.setAnimated(false);

        chart.setPrefHeight(220);

        chart.setStyle("-fx-background-color: #1a1d23;");
 
        XYChart.Series<Number, Number> data = new XYChart.Series<>();

        data.getData().add(new XYChart.Data<>(0, 2));

        data.getData().add(new XYChart.Data<>(6, 2.1));

        data.getData().add(new XYChart.Data<>(12, 1.9));

        data.getData().add(new XYChart.Data<>(18, 2.05));

        data.getData().add(new XYChart.Data<>(24, 2));
 
        chart.getData().add(data);
 
        // -------- start pomp knop ----------

        Button startPump = new Button("Start pomp");

        startPump.setStyle(

            "-fx-background-color: transparent;" +

            "-fx-border-color: #3a3f47;" +

            "-fx-border-radius: 8;" +

            "-fx-text-fill: white;" +

            "-fx-font-size: 14;" +

            "-fx-padding: 10 20;"

        );
 
        pressureBox.getChildren().addAll(pressureTitle, pressureStatus, chart, startPump);
 
        // Add beide kaarten naast elkaar

        this.getChildren().addAll(pumpBox, pressureBox);

        setAlignment(Pos.TOP_LEFT);

    }

}

 