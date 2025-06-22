package PropertyGraphCreator.model.graph;

import java.util.List;

public class ProcessedGraph {
    public final List<GraphNode> nodes;
    public final List<GraphEdge> edges;

    public ProcessedGraph(List<GraphNode> nodes, List<GraphEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }
}
