package postprocessing.export;

import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import PropertyGraphCreator.postprocessing.export.GraphMLLoader;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphMLLoaderTest {

    @Test
    void testLoadNodes() {
        GraphMLLoader loader = new GraphMLLoader();
        String path = getPath("sample_graph.graphml");

        List<GraphNode> nodes = loader.loadNodes(path);
        assertEquals(2, nodes.size());

        GraphNode first = nodes.get(0);
        assertEquals("n0", first.getId());
        assertEquals("Start", first.getLabel());

        GraphNode second = nodes.get(1);
        assertEquals("n1", second.getId());
        assertEquals("End", second.getLabel());
    }

    @Test
    void testLoadEdges() {
        GraphMLLoader loader = new GraphMLLoader();
        String path = getPath("sample_graph.graphml");

        List<GraphEdge> edges = loader.loadEdges(path);
        assertEquals(1, edges.size());

        GraphEdge edge = edges.get(0);
        assertEquals("n0", edge.getSource());
        assertEquals("n1", edge.getTarget());
        assertEquals("connects", edge.getLabel());
        assertEquals("paragraph-42", edge.getParagraphId());
    }

    @Test
    void testLoadNodes_withInvalidGraphML_shouldHandleException() {
        GraphMLLoader loader = new GraphMLLoader();
        String path = getPath("invalid_graph.graphml");

        List<GraphNode> nodes = loader.loadNodes(path);

        assertTrue(nodes.isEmpty(), "Nodes should be empty when parsing invalid GraphML");
    }

    @Test
    void testLoadEdges_withInvalidGraphML_shouldHandleException() {
        GraphMLLoader loader = new GraphMLLoader();
        String path = getPath("invalid_graph.graphml");

        List<GraphEdge> edges = loader.loadEdges(path);

        assertTrue(edges.isEmpty(), "Edges should be empty when parsing invalid GraphML");
    }


    private String getPath(String resourceName) {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        assertNotNull(resource, "Test resource not found: " + resourceName);
        return resource.getPath();
    }
}
