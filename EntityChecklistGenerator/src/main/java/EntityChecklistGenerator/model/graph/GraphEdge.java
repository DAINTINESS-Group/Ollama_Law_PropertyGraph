package EntityChecklistGenerator.model.graph;

import java.util.Objects;

public class GraphEdge {
    private final GraphNode source;
    private final GraphNode target;
    private final String label;
    private final String paragraphId;

    public GraphEdge(GraphNode source, GraphNode target, String label, String paragraphId) {
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        this.label = (label != null ? label : "");
        this.paragraphId = paragraphId;
    }

    public GraphNode getSource() {
        return source;
    }
    public GraphNode getTarget() {
        return target;
    }
    public String getLabel() {
        return label;
    }
    public String getParagraphId() {
        return paragraphId;
    }

    @Override
    public String toString() {
        return source.getLabel() + " —[" + label + "]→ " + target.getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphEdge)) return false;
        GraphEdge other = (GraphEdge) o;
        return source.equals(other.source)
                && target.equals(other.target)
                && Objects.equals(label, other.label)
                && Objects.equals(paragraphId, other.paragraphId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(source, target, label, paragraphId);
    }
}
