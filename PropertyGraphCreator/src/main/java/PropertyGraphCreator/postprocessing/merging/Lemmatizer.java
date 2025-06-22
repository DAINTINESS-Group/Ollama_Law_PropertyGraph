package PropertyGraphCreator.postprocessing.merging;

import edu.stanford.nlp.simple.Sentence;

import java.util.List;

public class Lemmatizer {
    private AcronymProtector protector;

    public Lemmatizer(AcronymProtector protector) {
        this.protector = protector;
    }

    public String lemmatize(String label) {
        if (label == null || label.trim().isEmpty()) return label;
        Sentence sentence = new Sentence(label);
        List<String> words = sentence.words();
        List<String> lemmas = sentence.lemmas();

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String original = words.get(i);
            String lemma = protector.isProtected(original) ? original : lemmas.get(i);
            result.append(lemma).append(" ");
        }
        return result.toString().trim();
    }
}
