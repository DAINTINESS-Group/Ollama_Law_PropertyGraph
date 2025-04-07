package KnowledgeGraphCreator.gui;

import KnowledgeGraphCreator.engine.LawSelectorChapters;
import KnowledgeGraphCreator.model.Chapter;

import javafx.stage.FileChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalystChapterSelection {

    private final LawSelectorChapters chapterSelector;
    private final TextArea outputArea;
    private final Set<String> selectedChapters;
    private final SelectionHandler handler;
    private final ListView<CheckBox> chapterList;

    public AnalystChapterSelection(LawSelectorChapters chapterSelector, TextArea outputArea, Set<String> selectedChapters, SelectionHandler handler) {
        this.chapterSelector = chapterSelector;
        this.outputArea = outputArea;
        this.selectedChapters = selectedChapters;
        this.handler = handler;

        chapterList = new ListView<>();
        chapterList.getItems().addAll(getAllChapterCheckBoxes());
    }

    /**
     * Generate checkboxes for all chapters
     */
    private List<CheckBox> getAllChapterCheckBoxes() {
        return chapterSelector.getAll()
                .stream()
                .map(chapter -> {
                    CheckBox checkBox = new CheckBox(chapter.getChapterTitle());

                    checkBox.setOnAction(event -> {
                        if (checkBox.isSelected()) {
                            selectedChapters.add(checkBox.getText());
                            handler.loadArticlesForChapter(checkBox.getText());
                        } else {
                            selectedChapters.remove(checkBox.getText());
                        }
                        handler.refreshOutput(); // 🔥 Refresh output after state change
                    });

                    return checkBox;
                })
                .collect(Collectors.toList());
    }

    /**
     * Refresh output to display selected chapters
     */
    public void refreshOutput() {
        outputArea.clear();

        for (String chapterName : selectedChapters) {
            Optional<Chapter> chapter = chapterSelector.getByName(chapterName);
            chapter.ifPresent(ch -> outputArea.appendText("\n Chapter:\n\n" + ch.getText()));
        }
    }

    /**
     * Save selected chapters to a file chosen by the user
     */
    public void saveSelectedChapters() {
        if (selectedChapters.isEmpty()) {
            outputArea.setText(" No chapters selected.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save JSON File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        fileChooser.setInitialFileName("output.json");

        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            String filePath = file.getAbsolutePath();

            chapterSelector.saveSelected(selectedChapters.stream().toList(), filePath);

            outputArea.setText(" JSON file saved to: " + filePath);
        }
    }

    /**
     * Get the chapter list for display
     */
    public ListView<CheckBox> getChapterList() {
        return chapterList;
    }

    /**
     * Search and display selected chapters
     */
    public void searchSelectedChapters() {
        refreshOutput();
    }
}
