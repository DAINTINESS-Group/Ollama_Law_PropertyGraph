package PropertyGraphCreator.postprocessing.merging;

public class TextNormalizer {
    public String normalize(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

