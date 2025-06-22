package EntityChecklistGenerator.parser;

import EntityChecklistGenerator.model.law.Article;
import EntityChecklistGenerator.model.law.Chapter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import EntityChecklistGenerator.model.law.Law;
import EntityChecklistGenerator.model.law.Paragraph;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LawJsonParser implements Parser<Law> {
    /** Parse the JSON file into a Law object. */
    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public Law parse(File jsonFile) throws Exception {
        try (JsonParser p = om.getFactory().createParser(jsonFile)) {
            JsonToken t = p.nextToken();
            Law law = new Law();

            if (t == JsonToken.START_ARRAY) {

                List<Chapter> chapters = om.readValue(
                        p,
                        om.getTypeFactory().constructCollectionType(List.class, Chapter.class)
                );
                law.setChapters(new ArrayList<>(chapters));
            } else if (t == JsonToken.START_OBJECT) {

                law = om.readValue(jsonFile, Law.class);
            } else {
                throw new IllegalStateException("Unexpected JSON root: " + t);
            }

            return law;
        }
    }

    /** Walks the Law and builds a map: paragraphID → Paragraph */
    public static Map<String,Paragraph> indexParagraphs(Law law) {
        Map<String,Paragraph> paragraphMap = new HashMap<>();
        for (Chapter chapter : law.getChapters()) {
            for (Article article : chapter.getArticles()) {
                for (Paragraph paragraph : article.getParagraphs()) {
                    paragraphMap.put(paragraph.getParagraphID(), paragraph);
                }
            }
        }
        return paragraphMap;
    }
}
