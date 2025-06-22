package EntityChecklistGenerator.report;

import EntityChecklistGenerator.engine.GraphEngine;
import EntityChecklistGenerator.engine.dto.RelationshipDetail;
import EntityChecklistGenerator.model.graph.GraphNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MarkdownReportGenerator implements ReportGenerator {
    private final GraphEngine engine;
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MarkdownReportGenerator(GraphEngine engine) {
        this.engine = engine;
    }

    @Override
    public String generateReportForNode(String nodeId) throws Exception {
        GraphNode node = engine.getNodeById(nodeId);
        if (node == null) {
            throw new IllegalArgumentException("Unknown nodeId: " + nodeId);
        }

        List<RelationshipDetail> details = engine.getRelationshipDetails(nodeId);
        List<RelationshipDetail> outgoing = details.stream()
                .filter(d -> d.getEdgeLabel().contains("→"))
                .collect(Collectors.toList());
        List<RelationshipDetail> incoming = details.stream()
                .filter(d -> d.getEdgeLabel().contains("←"))
                .collect(Collectors.toList());

        String anchor = toAnchor(node.getLabel());
        StringBuilder md = new StringBuilder();

        // node heading + anchor
        md.append("## Node: ").append(node.getLabel()).append("\n")
                .append("<a name=\"node-").append(anchor).append("\"></a>\n\n");

        // counts
        md.append("*")
                .append(outgoing.size()).append(" outgoing, ")
                .append(incoming.size()).append(" incoming*")
                .append("\n\n");

        md.append("### Outgoing relationships\n\n");
        if (outgoing.isEmpty()) {
            md.append("_(none)_\n\n");
        } else {
            for (RelationshipDetail d : outgoing) {
                appendDetail(md, d);
            }
        }

        md.append("### Incoming relationships\n\n");
        if (incoming.isEmpty()) {
            md.append("_(none)_\n\n");
        } else {
            for (RelationshipDetail d : incoming) {
                appendDetail(md, d);
            }
        }

        return md.toString();
    }

    /**
     * Dump one big report.md with title, timestamp, TOC and all nodes
     */
    @Override
    public void writeFullReport(File outputFile) throws Exception {
        File dir = outputFile.getParentFile();
        if (dir != null) dir.mkdirs();

        StringBuilder full = new StringBuilder();

        full.append("# Checklist Report\n\n")
                .append("*Generated on: ")
                .append(LocalDateTime.now().format(TS_FMT))
                .append("*\n\n");

        full.append("## Table of Contents\n\n");
        for (GraphNode node : engine.getAllNodes()) {
            String label = node.getLabel();
            String anchor = toAnchor(label);
            full.append("- [")
                    .append(label)
                    .append("](#node-")
                    .append(anchor)
                    .append(")\n");
        }
        full.append("\n---\n\n");

        for (GraphNode node : engine.getAllNodes()) {
            full.append(generateReportForNode(node.getId()))
                    .append("\n\n---\n\n");
        }

        Files.writeString(
                outputFile.toPath(),
                full.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    /** Render one relationship-detail in markdown */
    private void appendDetail(StringBuilder md, RelationshipDetail d) {
        md.append("- **")
                .append(d.getEdgeLabel())
                .append("**\n");
        md.append("  - Chapter ")
                .append(d.getChapterNumber())
                .append(": ")
                .append(d.getChapterTitle())
                .append("\n");
        md.append("  - Article ")
                .append(d.getArticleNumber())
                .append(": ")
                .append(d.getArticleTitle())
                .append("\n");
        md.append("  - Paragraph ")
                .append(d.getParagraphNumber())
                .append("\n\n");
        md.append("    > ")
                .append(d.getParagraphText().replaceAll("\n", "\n    > "))
                .append("\n\n");
    }

    /** Normalize a label into an HTML anchor name (lowercase, hyphens) */
    private String toAnchor(String label) {
        return label
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
