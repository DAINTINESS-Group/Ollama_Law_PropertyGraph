package PropertyGraphCreator.engine;

import PropertyGraphCreator.model.law.Law;
import java.util.List;
import java.util.Optional;

public interface LawSelector<T> {
    Optional<T> getByName(String name);
    List<T> getAll();
    void saveSelected(List<String> selectedNames, String filePath);
    void refreshLaw(Law newLaw);
    Optional<T> findById(String id);
    void buildLookupMap();

}
