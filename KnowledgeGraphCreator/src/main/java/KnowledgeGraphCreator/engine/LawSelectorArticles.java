package KnowledgeGraphCreator.engine;

import KnowledgeGraphCreator.model.Article;
import KnowledgeGraphCreator.model.Chapter;
import KnowledgeGraphCreator.model.Law;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LawSelectorArticles implements LawSelector<Article> {

    private Law law;
    private final Map<String, String> articleTitleToIdMap = new HashMap<>();

    public LawSelectorArticles(Law law) {
        this.law = law;
        buildLookupMap();
    }

    public void buildLookupMap() {
        articleTitleToIdMap.clear();

        for (Chapter chapter : law.getChapters()) {
            for (Article article : chapter.getArticles()) {
                String cleanedTitle = article.getArticleTitle().trim().toLowerCase();
                articleTitleToIdMap.put(cleanedTitle, article.getArticleID());
            }
        }
    }

    @Override
    public Optional<Article> getByName(String articleName) {
        String articleId = articleTitleToIdMap.get(articleName.trim().toLowerCase());
        return findById(articleId);
    }

    @Override
    public List<Article> getAll() {
        return law.getChapters()
                .stream()
                .flatMap(chapter -> chapter.getArticles().stream())
                .collect(Collectors.toList());
    }

    @Override
    public void saveSelected(List<String> selectedArticleNames, String filePath) {
        if (selectedArticleNames == null || selectedArticleNames.isEmpty()) {
            throw new IllegalArgumentException("No articles selected for saving.");
        }

        List<Article> selectedArticles = selectedArticleNames.stream()
                .map(name -> articleTitleToIdMap.get(name.toLowerCase().trim()))
                .filter(Objects::nonNull)
                .map(this::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (selectedArticles.isEmpty()) {
            throw new IllegalStateException("No valid articles found for saving.");
        }

        try {
            ArrayList<Chapter> selectedChapters = new ArrayList<>();

            for (Article article : selectedArticles) {
                Chapter parentChapter = law.getChapters().stream()
                        .filter(ch -> ch.getArticles().contains(article))
                        .findFirst()
                        .orElse(null);

                if (parentChapter != null) {
                    Chapter newChapter = new Chapter();
                    newChapter.setChapterNumber(parentChapter.getChapterNumber());
                    newChapter.setChapterTitle(parentChapter.getChapterTitle());
                    newChapter.setArticles(new ArrayList<>(List.of(article)));
                    selectedChapters.add(newChapter);
                }
            }

// Write ONLY the selected chapters to JSON
            File outputFile = new File(filePath);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, selectedChapters);


            System.out.println("New JSON file created: " + filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new JSON file", e);
        }
    }

    public List<String> getArticlesByChapterName(String chapterName) {
        return law.getChapters().stream()
                .filter(ch -> ch.getChapterTitle().equalsIgnoreCase(chapterName))
                .flatMap(ch -> ch.getArticles().stream().map(Article::getArticleTitle))
                .collect(Collectors.toList());
    }

    public Optional<Article> findById(String articleId) {
        return law.getChapters()
                .stream()
                .flatMap(ch -> ch.getArticles().stream())
                .filter(a -> a.getArticleID().equals(articleId))
                .findFirst();
    }

    @Override
    public void refreshLaw(Law newLaw) {
        this.law = newLaw;
        buildLookupMap();
    }
}
