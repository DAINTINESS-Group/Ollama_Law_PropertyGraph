package parser;

import EntityChecklistGenerator.model.graph.GraphContainer;
import EntityChecklistGenerator.model.graph.GraphEdge;
import EntityChecklistGenerator.model.graph.GraphNode;
import EntityChecklistGenerator.parser.GraphMlParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class GraphMlParserTest {

    @Test
    public void testParseGraphMLFile() throws Exception {
        URL resourceUrl = getClass().getClassLoader().getResource("test_graph.graphml");
        assertNotNull(resourceUrl, "GraphML file should exist in test/resources");

        File graphMLFile = new File(resourceUrl.toURI());

        GraphMlParser parser = new GraphMlParser();
        GraphContainer graph = parser.parse(graphMLFile);

        assertEquals(2, graph.getNodes().size(), "Expected 2 nodes");
        GraphNode nodeA = graph.getNode("n0");
        GraphNode nodeB = graph.getNode("n1");
        assertNotNull(nodeA);
        assertNotNull(nodeB);
        assertEquals("Node A", nodeA.getLabel());
        assertEquals("Node B", nodeB.getLabel());

        assertEquals(1, graph.getEdges().size(), "Expected 1 edge");
        GraphEdge edge = graph.getEdges().get(0);
        assertEquals("relates to", edge.getLabel());
        assertEquals("para-99", edge.getParagraphId());
        assertEquals(nodeA, edge.getSource());
        assertEquals(nodeB, edge.getTarget());
    }
}