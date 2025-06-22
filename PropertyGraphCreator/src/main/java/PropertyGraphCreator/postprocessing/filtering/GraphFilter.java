package PropertyGraphCreator.postprocessing.filtering;

import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import PropertyGraphCreator.model.graph.ProcessedGraph;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class GraphFilter {

    private final int maxNodeWords = 6;
    private final int maxEdgeWords = 10;

    private final List<GraphNode> removedNodes = new ArrayList<>();
    private final List<GraphEdge> removedEdges = new ArrayList<>();


    public List<GraphEdge> filterLongEdgeLabels(List<GraphEdge> edges) {
        List<GraphEdge> filtered = new ArrayList<>();
        for (GraphEdge edge : edges) {
            if (wordCount(edge.getLabel()) <= maxEdgeWords) {
                filtered.add(edge);
            } else {
                removedEdges.add(edge);
            }
        }
        return filtered;
    }


    public Set<String> findLongLabelNodeIds(List<GraphNode> nodes) {
        return nodes.stream()
                .filter(node -> wordCount(node.getLabel()) > maxNodeWords)
                .map(GraphNode::getId)
                .collect(Collectors.toSet());
    }

    public ProcessedGraph filterDanglingAndLongLabelNodes(
            List<GraphNode> nodes, List<GraphEdge> edges, Set<String> nodesToRemove
    ) {
        List<GraphEdge> filteredEdges = new ArrayList<>();
        for (GraphEdge edge : edges) {
            if (nodesToRemove.contains(edge.getSource()) || nodesToRemove.contains(edge.getTarget())) {
                removedEdges.add(edge);
            } else {
                filteredEdges.add(edge);
            }
        }

        Set<String> referenced = new HashSet<>();
        for (GraphEdge edge : filteredEdges) {
            referenced.add(edge.getSource());
            referenced.add(edge.getTarget());
        }

        List<GraphNode> finalNodes = new ArrayList<>();
        for (GraphNode node : nodes) {
            if (nodesToRemove.contains(node.getId()) || !referenced.contains(node.getId())) {
                removedNodes.add(node);
            } else {
                finalNodes.add(node);
            }
        }

        return new ProcessedGraph(finalNodes, filteredEdges);
    }

    public void writeRemovedToFile(String path, List<GraphNode> allNodes) {
        Map<String, String> idToLabel = allNodes.stream()
                .collect(Collectors.toMap(GraphNode::getId, GraphNode::getLabel));

        try (PrintWriter out = new PrintWriter(path)) {
            out.println("=== REMOVED NODES ===");
            for (GraphNode node : removedNodes) {
                out.println("- " + node.getLabel());
            }

            out.println("\n=== REMOVED EDGES ===");
            for (GraphEdge edge : removedEdges) {
                String sourceLabel = idToLabel.getOrDefault(edge.getSource(), "[unknown]");
                String targetLabel = idToLabel.getOrDefault(edge.getTarget(), "[unknown]");

                out.printf("- %s → %s: %s (%s)%n",
                        sourceLabel,
                        targetLabel,
                        sanitize(edge.getLabel()),
                        edge.getParagraphId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String sanitize(String text) {
        return text == null ? "" : text.replaceAll("\\s+", " ").trim();
    }



    private int wordCount(String text) {
        return text == null ? 0 : text.trim().split("\\s+").length;
    }
}
