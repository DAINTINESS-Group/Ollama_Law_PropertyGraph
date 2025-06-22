package PropertyGraphCreator.postprocessing.merging;

import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;

import java.util.*;
import java.util.stream.Collectors;

public class NodeMerger {
    private Lemmatizer lemmatizer;
    private TextNormalizer normalizer;
    private FuzzyMatcher matcher;
    private double threshold = 0.5;

    public NodeMerger(Lemmatizer lemmatizer, TextNormalizer normalizer, FuzzyMatcher matcher) {
        this.lemmatizer = lemmatizer;
        this.normalizer = normalizer;
        this.matcher = matcher;
    }

    public Map<String, String> generateCanonicalMap(List<GraphNode> nodes) {
        Map<String, String> canonicalMap = new HashMap<>();
        Map<String, String> normalizedToId = new HashMap<>();

        for (GraphNode node : nodes) {
            String label = node.getLabel();
            String lemmatized = lemmatizer.lemmatize(label);
            String normalized = normalizer.normalize(lemmatized);

            Optional<String> existing = normalizedToId.keySet().stream()
                    .filter(key -> matcher.isSimilar(key, normalized, threshold))
                    .findFirst();

            if (existing.isPresent()) {
                canonicalMap.put(node.getId(), normalizedToId.get(existing.get()));
            } else {
                normalizedToId.put(normalized, node.getId());
                canonicalMap.put(node.getId(), node.getId()); // self
            }
        }

        return canonicalMap;
    }

    public List<GraphNode> mergeNodes(List<GraphNode> nodes, Map<String, String> canonicalMap) {
        Map<String, GraphNode> merged = new HashMap<>();

        for (GraphNode node : nodes) {
            String canonId = canonicalMap.get(node.getId());
            if (!merged.containsKey(canonId)) {
                merged.put(canonId, new GraphNode(canonId, node.getLabel()));
            }
        }

        return new ArrayList<>(merged.values());
    }

    public List<GraphEdge> updateEdges(List<GraphEdge> edges, Map<String, String> canonicalMap) {
        List<GraphEdge> updatedEdges = edges.stream()
                .map(edge -> new GraphEdge(
                        canonicalMap.get(edge.getSource()),
                        canonicalMap.get(edge.getTarget()),
                        edge.getLabel(),
                        edge.getParagraphId()))
                .collect(Collectors.toList());

        Set<String> seen = new HashSet<>();
        List<GraphEdge> deduplicatedEdges = new ArrayList<>();

        for (GraphEdge edge : updatedEdges) {
            String key = edge.getSource() + "|" + edge.getTarget() + "|" + edge.getLabel() + "|" + edge.getParagraphId();
            if (seen.add(key)) {
                deduplicatedEdges.add(edge);
            }
        }

        return deduplicatedEdges;
    }

}

