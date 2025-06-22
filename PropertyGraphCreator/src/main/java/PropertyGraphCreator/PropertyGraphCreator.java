package PropertyGraphCreator;

import PropertyGraphCreator.gui.SelectionHandler;
import PropertyGraphCreator.model.graph.GraphEdge;
import PropertyGraphCreator.model.graph.GraphNode;
import PropertyGraphCreator.postprocessing.export.GraphMLLoader;
import PropertyGraphCreator.postprocessing.export.GraphMLWriter;
import PropertyGraphCreator.postprocessing.export.GraphProcessor;
import PropertyGraphCreator.model.graph.ProcessedGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PropertyGraphCreator {

    public static void main(String[] args) {

        SelectionHandler.main(args);
    }
}
