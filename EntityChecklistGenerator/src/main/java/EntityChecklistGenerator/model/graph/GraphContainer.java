package EntityChecklistGenerator.model.graph;

import java.util.*;

public class GraphContainer {
    private final Map<String, GraphNode> nodes = new LinkedHashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();

    public GraphNode addNode(String id, String label) {
        return nodes.computeIfAbsent(id, k -> new GraphNode(k, label));
    }

    public GraphNode getNode(String id) {
        return nodes.get(id);
    }

    public Collection<GraphNode> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public List<GraphEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public GraphEdge addEdge(String sourceId, String targetId, String label, String paragraphId) {
        GraphNode src = addNode(sourceId, null);
        GraphNode tgt = addNode(targetId, null);
        GraphEdge e  = new GraphEdge(src, tgt, label, paragraphId);
        edges.add(e);
        src.addOutgoing(e);
        tgt.addIncoming(e);
        return e;
    }
}
