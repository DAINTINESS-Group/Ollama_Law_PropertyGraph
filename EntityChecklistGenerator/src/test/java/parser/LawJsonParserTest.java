package parser;

import EntityChecklistGenerator.model.law.Law;
import EntityChecklistGenerator.model.law.Paragraph;
import EntityChecklistGenerator.parser.LawJsonParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class LawJsonParserTest {

    @Test
    public void testMinimalJsonParsing() throws Exception {
        URL url = getClass().getClassLoader().getResource("test_law.json");
        assertNotNull(url);
        File file = new File(url.toURI());

        Law law = new LawJsonParser().parse(file);
        assertNotNull(law);
        assertEquals(1, law.getChapters().size());
        assertEquals("Penalties", law.getChapters().get(0).getChapterTitle());

        var paragraphs = LawJsonParser.indexParagraphs(law);
        assertTrue(paragraphs.containsKey("12.1.1"));
        Paragraph p = paragraphs.get("12.1.1");
        assertEquals("12.1.1", p.getParagraphID());
        assertEquals(20, p.getText().length());
        assertTrue(p.getText().contains("Article 99"));
    }
}
