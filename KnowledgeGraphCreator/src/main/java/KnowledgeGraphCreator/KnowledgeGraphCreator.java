package KnowledgeGraphCreator;

import KnowledgeGraphCreator.gui.SelectionHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KnowledgeGraphCreator {

    public static void main(String[] args) {

        SelectionHandler.main(args);
        //runPythonScript();
    }

    private static void runPythonScript() {
        try {
            long startTime = System.currentTimeMillis();
            Path pythonPath = Paths.get("knowledge_graph-main", "extract_graph.py");
            File pythonScript = pythonPath.toFile();
            File pythonProjectDir = pythonScript.getParentFile();

            System.out.println("Starting Python script...");
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScript.getAbsolutePath());
            processBuilder.directory(pythonProjectDir);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("Python script exited with code: " + exitCode);

            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("Total time taken: " + elapsed + " seconds");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
