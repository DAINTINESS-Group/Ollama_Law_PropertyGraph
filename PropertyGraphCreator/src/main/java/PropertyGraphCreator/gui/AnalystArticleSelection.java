package PropertyGraphCreator.gui;

import PropertyGraphCreator.engine.LawSelectorArticles;
import PropertyGraphCreator.model.law.Article;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalystArticleSelection {

    private final LawSelectorArticles articleSelector;
    private final TextArea outputArea;
    private final Set<String> selectedArticles;
    private final SelectionHandler handler;
    private final ListView<CheckBox> articleList;

    public AnalystArticleSelection(LawSelectorArticles articleSelector, TextArea outputArea, Set<String> selectedArticles, SelectionHandler handler) {
        this.articleSelector = articleSelector;
        this.outputArea = outputArea;
        this.selectedArticles = selectedArticles;
        this.handler = handler;

        articleList = new ListView<>();
    }

    /**
     * Load articles as checkboxes for the selected chapter
     */
    public void loadArticlesForChapter(String chapterName) {
        articleList.getItems().clear();

        List<CheckBox> checkBoxes = articleSelector.getArticlesByChapterName(chapterName)
                .stream()
                .map(articleTitle -> {
                    CheckBox checkBox = new CheckBox(articleTitle);

                    checkBox.setOnAction(event -> {
                        if (checkBox.isSelected()) {
                            selectedArticles.add(articleTitle);

                            refreshOutput();
                        } else {
                            selectedArticles.remove(articleTitle);
                            refreshOutput();
                        }
                    });

                    return checkBox;
                })
                .collect(Collectors.toList());

        articleList.getItems().addAll(checkBoxes);
    }

    /**
     * Refresh output based on state:
     *  - If articles are selected Show ONLY articles
     *  - If no articles are selected ️ Show chapters
     */
    public void refreshOutput() {
        outputArea.clear();

        if (!selectedArticles.isEmpty()) {
            for (String articleName : selectedArticles) {
                Optional<Article> article = articleSelector.getByName(articleName);
                article.ifPresent(a -> {
                    String newContent = "\n\nArticle:\n\n" + a.getText();
                    outputArea.appendText(newContent);
                });
            }
        } else {
            handler.refreshChapters();
        }
    }

    /**
     * Save selected articles to JSON
     */
    public void saveSelectedArticles() {
        if (selectedArticles.isEmpty()) {
            outputArea.setText("⚠️ No articles selected.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save JSON File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("articles.json");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            String filePath = file.getAbsolutePath();

            articleSelector.saveSelected(selectedArticles.stream().toList(), filePath);

            outputArea.setText("JSON file saved to: " + filePath);
        }
    }


    /**
     * Return the ListView for the UI
     */
    public ListView<CheckBox> getArticleList() {
        return articleList;
    }
}
