package PropertyGraphCreator.model.law;

import java.util.List;

public interface Node {

    String getText();
    String getTitle();
    List<? extends Node> getChildren();
    String toString();
}
