package EntityChecklistGenerator.gui;

import EntityChecklistGenerator.engine.DefaultGraphEngineImpl;
import EntityChecklistGenerator.engine.GraphEngine;
import EntityChecklistGenerator.report.MarkdownReportGenerator;
import EntityChecklistGenerator.report.ReportGenerator;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Path;

public class MainApp extends Application {
    private Stage primaryStage;
    private TextField graphmlField;
    private TextField jsonField;
    private ReportGenerator reporter;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Entity Checklist Generator");
        showFileSelection();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showFileSelection() {
        Label gLabel = new Label("GraphML File:");
        graphmlField = new TextField(); graphmlField.setPrefColumnCount(30);
        graphmlField.setEditable(false);
        Button gBrowse = new Button("Browse…");
        gBrowse.setOnAction(e -> onBrowse(graphmlField, "GraphML files", "*.graphml"));

        Label jLabel = new Label("Law JSON File:");
        jsonField = new TextField(); jsonField.setPrefColumnCount(30);
        jsonField.setEditable(false);
        Button jBrowse = new Button("Browse…");
        jBrowse.setOnAction(e -> onBrowse(jsonField, "JSON files", "*.json"));

        Button start = new Button("Start");
        start.setDefaultButton(true);
        start.setOnAction(e -> onStart());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        grid.addRow(0, gLabel, graphmlField, gBrowse);
        grid.addRow(1, jLabel, jsonField,  jBrowse);
        grid.add(start, 1, 2);
        GridPane.setHalignment(start, HPos.CENTER);

        primaryStage.setScene(new Scene(grid, 600, 180));
    }

    private void onBrowse(TextField targetField, String filterDesc, String filterExt) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select " + filterDesc);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(filterDesc, filterExt)
        );
        File picked = chooser.showOpenDialog(primaryStage);
        if (picked != null) {
            targetField.setText(picked.getAbsolutePath());
        }
    }

    private void onStart() {
        String gPath = graphmlField.getText();
        String jPath = jsonField.getText();
        if (gPath.isEmpty() || jPath.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Please choose both files first.");
            return;
        }

        try {
            GraphEngine engine = new DefaultGraphEngineImpl();
            engine.loadGraph(new File(gPath));
            engine.loadLawJson(new File(jPath));

            reporter = new MarkdownReportGenerator(engine);

            RelationshipViewerPanel graphCanvas = new RelationshipViewerPanel(engine);
            ChecklistPane checklist   = new ChecklistPane(engine);

            graphCanvas.setOnNodeSelected(node ->
                    checklist.showEdgesFor(node.getId())
            );

            Label leftTitle  = new Label("Graph Entities");
            Label rightTitle = new Label("Relationships Between Entities");
            leftTitle .getStyleClass().add("pane-title");
            rightTitle.getStyleClass().add("pane-title");

            VBox leftBox  = new VBox(10, leftTitle, graphCanvas.getView());
            VBox rightBox = new VBox(10, rightTitle, checklist);
            leftBox .setPadding(new Insets(10));
            rightBox.setPadding(new Insets(10));
            VBox.setVgrow(graphCanvas.getView(), Priority.ALWAYS);
            VBox.setVgrow(checklist,           Priority.ALWAYS);

            Button genReport = new Button("Generate Report");
            genReport.setMaxWidth(Double.MAX_VALUE);
            genReport.setOnAction(e -> {
                try {
                    File out = Path.of("src","main","resources","reports","report.md")
                            .toFile();
                    reporter.writeFullReport(out);
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Report successfully written to:\n" + out.getAbsolutePath()
                    );
                } catch (Exception ex) {
                    showAlert(
                            Alert.AlertType.ERROR,
                            "Failed to generate report:\n" + ex.getMessage()
                    );
                }
            });
            rightBox.getChildren().add(genReport);

            SplitPane split = new SplitPane(leftBox, rightBox);
            split.setDividerPositions(0.6);

            Scene scene = new Scene(split, 1400, 900);
            scene.getStylesheets().add(
                    getClass().getResource("/app.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Failed to load files:\n" + ex.getMessage());
        }
    }


    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.initOwner(primaryStage);
        a.showAndWait();
    }
}
