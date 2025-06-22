package EntityChecklistGenerator.engine;

import EntityChecklistGenerator.engine.dto.RelationshipDetail;
import EntityChecklistGenerator.model.graph.GraphNode;
import EntityChecklistGenerator.model.graph.GraphEdge;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface GraphEngine {
    /**
     * Loads and parses the GraphML file into the engine.
     * @param graphmlFile a .graphml file
     * @throws Exception on parse/load errors
     */
    void loadGraph(File graphmlFile) throws Exception;

    /**
     * Returns all nodes in insertion order.
     */
    Collection<GraphNode> getAllNodes();

    /**
     * Finds a node by its ID (or null if not present).
     */
    GraphNode getNodeById(String nodeId);

    /**
     * Returns the full text for the given paragraphId (e.g. "5.1.3"),
     * or a default message if not found.
     */
    String getParagraphText(String paragraphId);

    /**
     * Returns all outgoing edges for the given node ID.
     */
    List<GraphEdge> getOutgoingEdges(String nodeId);

    /**
     * Returns all incoming edges for the given node ID.
     */
    List<GraphEdge> getIncomingEdges(String nodeId);

    /**
     * Loads and parses the Law JSON file into the engine, so we can look up paragraph text.
     * @param lawJsonFile a .json file matching your Law model
     * @throws Exception on parse/load errors
     */
    void loadLawJson(File lawJsonFile) throws Exception;

    /**
     * Returns a list of enriched relationship details (edge + paragraphText) for the given node.
     */
    List<RelationshipDetail> getRelationshipDetails(String nodeId);
}
