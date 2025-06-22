package EntityChecklistGenerator.model.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GraphNode {
    private final String id;
    private String label;
    private final List<GraphEdge> outgoing = new ArrayList<>();
    private final List<GraphEdge> incoming = new ArrayList<>();

    public GraphNode(String id, String label) {
        this.id = Objects.requireNonNull(id);
        this.label = (label != null ? label : id);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = Objects.requireNonNull(label);
    }

    public void addOutgoing(GraphEdge edge) {
        outgoing.add(edge);
    }

    public void addIncoming(GraphEdge edge) {
        incoming.add(edge);
    }

    /** Unmodifiable views to prevent external mutation */
    public List<GraphEdge> getOutgoing() {
        return Collections.unmodifiableList(outgoing);
    }
    public List<GraphEdge> getIncoming() {
        return Collections.unmodifiableList(incoming);
    }

    @Override
    public String toString() {
        return label + " (" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphNode)) return false;
        GraphNode that = (GraphNode) o;
        return id.equals(that.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

