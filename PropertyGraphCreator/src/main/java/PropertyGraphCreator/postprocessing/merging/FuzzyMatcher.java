package PropertyGraphCreator.postprocessing.merging;

import org.apache.commons.text.similarity.LevenshteinDistance;

public class FuzzyMatcher {

    private LevenshteinDistance distance = new LevenshteinDistance();

    public boolean isSimilar(String a, String b, double similarityThreshold) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return false;

        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) return true;

        int rawDistance = distance.apply(a, b);
        double similarity = 1.0 - ((double) rawDistance / maxLength);

        return similarity >= similarityThreshold;
    }
}
