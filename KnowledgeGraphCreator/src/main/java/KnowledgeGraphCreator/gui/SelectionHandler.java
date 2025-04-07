package KnowledgeGraphCreator.gui;

import KnowledgeGraphCreator.engine.LawSelectorChapters;
import KnowledgeGraphCreator.engine.LawSelectorArticles;
import KnowledgeGraphCreator.model.Law;
import KnowledgeGraphCreator.engine.LawDeserializer;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.HashSet;
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

        setupUI(stage, filePath);
    }

    private TextArea createOutputArea() {
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefHeight(600);
        outputArea.setPrefWidth(700);
        return outputArea;
    }

    private void setupUI(Stage stage, String filePath) {

        Button saveChaptersButton = new Button("Save Selected Chapters");
        saveChaptersButton.setOnAction(event -> chapterSelection.saveSelectedChapters());

        Button saveArticlesButton = new Button("Save Selected Articles");
        saveArticlesButton.setOnAction(event -> articleSelection.saveSelectedArticles());


        VBox leftSide = new VBox(10, outputArea, saveChaptersButton, saveArticlesButton);
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

    public static void main(String[] args) {
        launch(args);
    }
}
