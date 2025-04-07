package KnowledgeGraphCreator.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import KnowledgeGraphCreator.model.Law;

import java.io.File;

public class LawDeserializer {

    /**
     * Reads a JSON file and deserializes it into a Law object.
     * @param filePath The path to the JSON file.
     * @return A Law object representing the data in the file.
     */
    public static Law readLawObjectFromJson(String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(new File(filePath), Law.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read law JSON file", e);
        }
    }
}