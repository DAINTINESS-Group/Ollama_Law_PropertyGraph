package EntityChecklistGenerator.report;

import java.io.File;

/**
 * Something that can produce a text‐based report for a given node.
 */
public interface ReportGenerator {
    /**
     * Produce a report (as a String) for the given node ID.
     * @param nodeId the GraphNode id
     * @return the entire report in whatever format (e.g. Markdown)
     * @throws Exception if something goes wrong
     */
    String generateReportForNode(String nodeId) throws Exception;

    /**
     * Generate one big report covering *all* nodes, written to `outputFile`.
     */
    void writeFullReport(File outputFile) throws Exception;
}
