package PropertyGraphCreator.postprocessing.merging;

import java.util.Set;

public class AcronymProtector {
    private Set<String> protectedTerms;

    public AcronymProtector(Set<String> protectedTerms) {
        this.protectedTerms = protectedTerms;
    }

    public boolean isProtected(String word) {
        return protectedTerms.contains(word.toUpperCase());
    }
}

