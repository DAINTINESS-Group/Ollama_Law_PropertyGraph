package postprocessing.export;

import org.junit.jupiter.api.Test;
import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import PropertyGraphCreator.postprocessing.export.GraphMLWriter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphMLWriterTest {

    @Test
    public void testWriteGraphToResourcesFolder() throws Exception {
        GraphNode node1 = new GraphNode("n1", "Test Node 1");
        GraphNode node2 = new GraphNode("n2", "Test Node 2");
        GraphEdge edge = new GraphEdge("n1", "n2", "connects", "p1");

        List<GraphNode> nodes = List.of(node1, node2);
        List<GraphEdge> edges = List.of(edge);

        Path outputPath = Path.of("src", "test", "resources", "test_output.graphml");
        Files.createDirectories(outputPath.getParent());

        GraphMLWriter writer = new GraphMLWriter();
        writer.writeGraph(outputPath.toString(), nodes, edges);

        File outFile = outputPath.toFile();
        assertTrue(outFile.exists(), "GraphML file should be created");
        assertTrue(outFile.length() > 0, "GraphML file should not be empty");

        System.out.println("✅ GraphML test file created: " + outFile.getAbsolutePath());
    }
}

