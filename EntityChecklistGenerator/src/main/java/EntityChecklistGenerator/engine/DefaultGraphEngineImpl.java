package EntityChecklistGenerator.engine;

import EntityChecklistGenerator.engine.dto.RelationshipDetail;
import EntityChecklistGenerator.model.graph.GraphContainer;
import EntityChecklistGenerator.model.graph.GraphNode;
import EntityChecklistGenerator.model.graph.GraphEdge;
import EntityChecklistGenerator.model.law.Article;
import EntityChecklistGenerator.model.law.Chapter;
import EntityChecklistGenerator.parser.GraphMlParser;
import EntityChecklistGenerator.parser.LawJsonParser;
import EntityChecklistGenerator.model.law.Law;
import EntityChecklistGenerator.model.law.Paragraph;
import EntityChecklistGenerator.parser.Parser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default implementation of GraphEngine using DOM‐based GraphMlParser
 * and Jackson‐based LawJsonParser.
 */
public class DefaultGraphEngineImpl implements GraphEngine {
    private GraphContainer graph;
    private Law law;
    private Map<String, Paragraph> paragraphIndex;

    private Map<String, Chapter> chapterByPara;
    private Map<String, Article> articleByPara;


    @Override
    public void loadGraph(File graphmlFile) throws Exception {
        Parser<GraphContainer> graphParser = new GraphMlParser();
        this.graph = graphParser.parse(graphmlFile);
    }

    @Override
    public Collection<GraphNode> getAllNodes() {
        ensureGraphLoaded();
        return graph.getNodes();
    }

    @Override
    public GraphNode getNodeById(String nodeId) {
        ensureGraphLoaded();
        return graph.getNode(nodeId);
    }

    @Override
    public List<GraphEdge> getOutgoingEdges(String nodeId) {
        ensureGraphLoaded();
        GraphNode node = graph.getNode(nodeId);
        return node != null
                ? node.getOutgoing()
                : Collections.emptyList();
    }

    @Override
    public String getParagraphText(String paragraphId) {
        ensureLawLoaded();
        return Arrays.stream(paragraphId.split("\\s*,\\s*"))
                .map(id -> Optional.ofNullable(paragraphIndex.get(id))
                        .map(Paragraph::getText)
                        .orElse("[Text not found for ¶" + id + "]")
                )
                .collect(Collectors.joining("\n\n"));  // separate multiple paragraphs
    }


    @Override
    public List<GraphEdge> getIncomingEdges(String nodeId) {
        ensureGraphLoaded();
        GraphNode node = graph.getNode(nodeId);
        return node != null
                ? node.getIncoming()
                : Collections.emptyList();
    }

    @Override
    public void loadLawJson(File lawJsonFile) throws Exception {
        Parser<Law> lawParser = new LawJsonParser();

        this.law = lawParser.parse(lawJsonFile);
        this.paragraphIndex = LawJsonParser.indexParagraphs(law);

        this.chapterByPara = new HashMap<>();
        this.articleByPara = new HashMap<>();

        for (Chapter ch : law.getChapters()) {
            for (Article art : ch.getArticles()) {
                for (Paragraph para : art.getParagraphs()) {
                    String pid = para.getParagraphID();
                    chapterByPara.put(pid, ch);
                    articleByPara.put(pid, art);
                }
            }
        }

    }

    @Override
    public List<RelationshipDetail> getRelationshipDetails(String nodeId) {
        ensureGraphLoaded();
        ensureLawLoaded();

        GraphNode node = graph.getNode(nodeId);
        if (node == null) {
            return Collections.emptyList();
        }

        List<RelationshipDetail> outgoing = node.getOutgoing().stream()
                .map(edge -> buildDetail(edge.getTarget(), edge, false))
                .collect(Collectors.toList());

        List<RelationshipDetail> incoming = node.getIncoming().stream()
                .map(edge -> buildDetail(edge.getSource(), edge, true))
                .collect(Collectors.toList());

        List<RelationshipDetail> all = new ArrayList<>(outgoing.size() + incoming.size());
        all.addAll(outgoing);
        all.addAll(incoming);
        return all;
    }

    private RelationshipDetail buildDetail(GraphNode other,
                                           GraphEdge edge,
                                           boolean incoming) {
        String rawPid = edge.getParagraphId();
        String[] parts = rawPid.split("\\s*,\\s*");

        String paragraphText = getParagraphText(rawPid);

        String lookupPid = Arrays.stream(parts)
                .filter(paragraphIndex::containsKey)
                .findFirst()
                .orElse(parts[0]);

        Chapter   ch  = chapterByPara.get(lookupPid);
        Article   art = articleByPara.get(lookupPid);

        String paragraphNumbers = Arrays.stream(parts)
                .map(id -> Optional.ofNullable(paragraphIndex.get(id))
                        .map(Paragraph::getParagraphNumber)
                        .map(Object::toString)
                        .orElse(id))
                .collect(Collectors.joining(", "));

        String label = incoming
                ? edge.getLabel() + " ← " + other.getLabel()
                : edge.getLabel() + " → " + other.getLabel();

        return new RelationshipDetail(
                other.getId(),
                other.getLabel(),
                label,
                rawPid,
                paragraphText,
                ch  != null ? ch.getChapterNumber() : -1,
                ch  != null ? ch.getChapterTitle()  : "Unknown chapter",
                art != null ? art.getArticleNumber(): -1,
                art != null ? art.getArticleTitle() : "Unknown article",
                paragraphNumbers
        );
    }




    private void ensureGraphLoaded() {
        if (graph == null) {
            throw new IllegalStateException("Graph not loaded; call loadGraph(...) first");
        }
    }

    private void ensureLawLoaded() {
        if (law == null || paragraphIndex == null) {
            throw new IllegalStateException("Law JSON not loaded; call loadLawJson(...) first");
        }
    }
}
