package EntityChecklistGenerator.gui;

import EntityChecklistGenerator.engine.GraphEngine;
import EntityChecklistGenerator.model.graph.GraphNode;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.Collection;
import java.util.function.Consumer;

public class RelationshipViewerPanel {
    private final ListView<GraphNode> listView = new ListView<>();
    private Consumer<GraphNode> onNodeSelected = n -> {};

    public RelationshipViewerPanel(GraphEngine engine) {
        Collection<GraphNode> all = engine.getAllNodes();
        listView.getItems().setAll(all);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(GraphNode node, boolean empty) {
                super.updateItem(node, empty);
                setText(empty || node == null ? null : node.getLabel());
            }
        });

        listView.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {
                    if (newSel != null) {
                        onNodeSelected.accept(newSel);
                    }
                });

        listView.getStyleClass().add("graph-entity-list");
    }

    /**
     * @return the view to embed in your MainApp—in this case the ListView itself
     */
    public Node getView() {

        listView.setPrefWidth(300);
        return listView;
    }

    /**
     * Register a callback for clicks (or selection) on a node.
     */
    public void setOnNodeSelected(Consumer<GraphNode> callback) {
        this.onNodeSelected = callback;
    }
}
