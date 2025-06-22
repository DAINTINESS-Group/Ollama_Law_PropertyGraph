package PropertyGraphCreator.engine;

import PropertyGraphCreator.model.law.Chapter;
import PropertyGraphCreator.model.law.Law;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LawSelectorChapters implements LawSelector<Chapter> {

    private Law law;
    private final Map<String, Integer> chapterTitleToIdMap = new HashMap<>();

    public LawSelectorChapters(Law law) {
        this.law = law;
        buildLookupMap();
    }

    public void buildLookupMap() {
        chapterTitleToIdMap.clear();
        for (Chapter chapter : law.getChapters()) {
            String cleanedTitle = chapter.getChapterTitle().trim().toLowerCase();
            chapterTitleToIdMap.put(cleanedTitle, chapter.getChapterNumber());
        }
    }

    @Override
    public Optional<Chapter> getByName(String chapterName) {
        Integer chapterId = chapterTitleToIdMap.get(chapterName.toLowerCase().trim());
        return getChapterByNumber(chapterId);
    }

    private Optional<Chapter> getChapterByNumber(int chapterNumber) {
        return law.getChapters()
                .stream()
                .filter(ch -> ch.getChapterNumber() == chapterNumber)
                .findFirst();
    }

    @Override
    public List<Chapter> getAll() {
        return law.getChapters();
    }

    @Override
    public void saveSelected(List<String> selectedChapterNames, String filePath) {
        if (selectedChapterNames == null || selectedChapterNames.isEmpty()) {
            throw new IllegalArgumentException("No chapters selected for saving.");
        }

        List<Chapter> selectedChapters = selectedChapterNames.stream()
                .map(name -> chapterTitleToIdMap.get(name.toLowerCase().trim()))
                .filter(Objects::nonNull)
                .map(this::getChapterByNumber)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (selectedChapters.isEmpty()) {
            throw new IllegalStateException("No valid chapters found for saving.");
        }

        try {
            File outputFile = new File(filePath);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, selectedChapters);


            System.out.println("New JSON file created: " + filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new JSON file", e);
        }
    }


    @Override
    public void refreshLaw(Law newLaw) {
        this.law = newLaw;
        buildLookupMap();
    }

    @Override
    public Optional<Chapter> findById(String id) {
        int chapterNumber = Integer.parseInt(id);
        return law.getChapters()
                .stream()
                .filter(ch -> ch.getChapterNumber() == chapterNumber)
                .findFirst();
    }
}
