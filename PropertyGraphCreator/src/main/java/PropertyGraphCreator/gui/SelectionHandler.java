package PropertyGraphCreator.gui;

import PropertyGraphCreator.engine.LawSelectorChapters;
import PropertyGraphCreator.engine.LawSelectorArticles;
import PropertyGraphCreator.model.law.Law;
import PropertyGraphCreator.engine.LawDeserializer;
import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import PropertyGraphCreator.model.graph.ProcessedGraph;
import PropertyGraphCreator.postprocessing.export.GraphMLLoader;
import PropertyGraphCreator.postprocessing.export.GraphMLWriter;
import PropertyGraphCreator.postprocessing.export.GraphProcessor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectionHandler extends Application {

    private AnalystChapterSelection chapterSelection;
    private AnalystArticleSelection articleSelection;
    private TextArea outputArea;

    private final Set<String> selectedChapters = new HashSet<>();
    private final Set<String> selectedArticles = new HashSet<>();

    @Override
    public void start(Stage stage) {
        String filePath = "G:/DIPLOMATIKI/LegislativeTextParser/src/main/resources/output/test.json";

        Law law = LawDeserializer.readLawObjectFromJson(filePath);
        outputArea = createOutputArea();

        LawSelectorChapters chapterSelector = new LawSelectorChapters(law);
        LawSelectorArticles articleSelector = new LawSelectorArticles(law);

        chapterSelection = new AnalystChapterSelection(chapterSelector, outputArea, selectedChapters, this);
        articleSelection = new AnalystArticleSelection(articleSelector, outputArea, selectedArticles, this);

        setupUI(stage);
    }

    private TextArea createOutputArea() {
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(600);
        outputArea.setPrefWidth(700);
        return outputArea;
    }

    private void setupUI(Stage stage) {
        Button saveChaptersButton = new Button("Save Selected Chapters");
        saveChaptersButton.setOnAction(event -> chapterSelection.saveSelectedChapters());

        Button saveArticlesButton = new Button("Save Selected Articles");
        saveArticlesButton.setOnAction(event -> articleSelection.saveSelectedArticles());

        ProgressBar progressBar = new ProgressBar();
        progressBar.setVisible(false);

        Button extractButton = new Button("Start Extracting");
        extractButton.setOnAction(event -> {
            progressBar.setVisible(true);
            extractButton.setDisable(true);
            beginRelationshipExtraction(progressBar, extractButton);
        });

        Button cleanGraphButton = new Button("Clean Graph");
        cleanGraphButton.setOnAction(event -> runPostProcessing());

        VBox leftSide = new VBox(10, outputArea, saveChaptersButton, saveArticlesButton, extractButton, cleanGraphButton, progressBar);
        VBox rightSide = new VBox(10, chapterSelection.getChapterList(), articleSelection.getArticleList());

        leftSide.setPadding(new Insets(10));
        rightSide.setPadding(new Insets(10));

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftSide, rightSide);
        splitPane.setDividerPositions(0.6);

        Scene scene = new Scene(splitPane, 1200, 700);
        stage.setTitle("Law Selector");
        stage.setScene(scene);
        stage.show();
    }

    public void refreshChapters() {
        chapterSelection.refreshOutput();
    }

    public void refreshOutput() {
        outputArea.clear();
        chapterSelection.refreshOutput();
        articleSelection.refreshOutput();
    }

    public void loadArticlesForChapter(String chapterName) {
        articleSelection.loadArticlesForChapter(chapterName);
    }

    private void beginRelationshipExtraction(ProgressBar progressBar, Button triggerButton) {
        new Thread(() -> {
            try {
                Path pythonPath = Paths.get("knowledge_graph-main", "extract_graph.py");
                File pythonScript = pythonPath.toFile();
                File pythonProjectDir = pythonScript.getParentFile();

                ProcessBuilder pb = new ProcessBuilder("python", pythonScript.getAbsolutePath());
                pb.directory(pythonProjectDir);
                pb.redirectErrorStream(true);

                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                }

                int exitCode = process.waitFor();
                System.out.println("Python exited with code: " + exitCode);

                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    triggerButton.setDisable(false);
                    if (exitCode == 0) {
                        outputArea.setText("Extraction completed.");
                    } else {
                        outputArea.setText("Python script failed.");
                    }
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    triggerButton.setDisable(false);
                    outputArea.setText("Exception during Python script execution.");
                });
            }
        }).start();
    }

    private void runPostProcessing() {
        try {
            GraphMLLoader loader = new GraphMLLoader();
            Path graphMLPath = Paths.get("knowledge_graph-main", "docs", "graph.graphml");
            List<GraphNode> nodes = loader.loadNodes(graphMLPath.toString());
            List<GraphEdge> edges = loader.loadEdges(graphMLPath.toString());

            GraphProcessor processor = new GraphProcessor();
            ProcessedGraph result = processor.processGraph(nodes, edges);

            GraphMLWriter writer = new GraphMLWriter();
            Path outputPath = Paths.get("knowledge_graph-main", "docs", "subgraph.graphml");
            writer.writeGraph(outputPath.toString(), result.nodes, result.edges);

            outputArea.setText("Post-processing completed. subgraph.graphml created.");
        } catch (Exception e) {
            e.printStackTrace();
            outputArea.setText("Error during graph post-processing.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
