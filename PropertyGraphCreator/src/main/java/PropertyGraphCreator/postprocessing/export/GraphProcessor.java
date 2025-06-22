package PropertyGraphCreator.postprocessing.export;

import PropertyGraphCreator.postprocessing.filtering.GraphFilter;
import PropertyGraphCreator.model.graph.GraphNode;
import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.ProcessedGraph;
import PropertyGraphCreator.postprocessing.merging.*;

import java.util.*;

public class GraphProcessor {
    public ProcessedGraph processGraph(List<GraphNode> nodes, List<GraphEdge> edges) {
        AcronymProtector protector = new AcronymProtector(Set.of("AI", "EU", "GDPR"));
        Lemmatizer lemmatizer = new Lemmatizer(protector);
        TextNormalizer normalizer = new TextNormalizer();
        FuzzyMatcher matcher = new FuzzyMatcher();
        NodeMerger merger = new NodeMerger(lemmatizer, normalizer, matcher);

        Map<String, String> canonicalMap = merger.generateCanonicalMap(nodes);
        List<GraphNode> mergedNodes = merger.mergeNodes(nodes, canonicalMap);
        List<GraphEdge> updatedEdges = merger.updateEdges(edges, canonicalMap);

        GraphFilter filter = new GraphFilter();

        List<GraphEdge> filteredEdges = filter.filterLongEdgeLabels(updatedEdges);

        Set<String> nodesToRemove = filter.findLongLabelNodeIds(mergedNodes);

        ProcessedGraph finalGraph = filter.filterDanglingAndLongLabelNodes(
                mergedNodes, filteredEdges, nodesToRemove
        );

        filter.writeRemovedToFile("knowledge_graph-main/docs/removed_elements.log",mergedNodes);


        return finalGraph;
    }

}

