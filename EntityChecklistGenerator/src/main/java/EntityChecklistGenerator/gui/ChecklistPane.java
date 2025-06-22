package EntityChecklistGenerator.gui;

import EntityChecklistGenerator.engine.GraphEngine;
import EntityChecklistGenerator.engine.dto.RelationshipDetail;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

public class ChecklistPane extends VBox {
    private final GraphEngine engine;
    private final VBox contentBox = new VBox(8);

    public ChecklistPane(GraphEngine engine) {
        this.engine = engine;

        ScrollPane sp = new ScrollPane(contentBox);
        sp.setFitToWidth(true);
        sp.setPrefWidth(500);
        VBox.setVgrow(sp, Priority.ALWAYS);

        getChildren().add(sp);
        getStyleClass().add("checklist-pane");
    }

    /**
     * Populate the right‐hand pane: for each relationship hit addDetail(...)
     */
    public void showEdgesFor(String nodeId) {
        contentBox.getChildren().clear();

        List<RelationshipDetail> all = engine.getRelationshipDetails(nodeId);

        List<RelationshipDetail> outgoing = all.stream()
                .filter(d -> d.getEdgeLabel().contains("→"))
                .toList();
        List<RelationshipDetail> incoming = all.stream()
                .filter(d -> d.getEdgeLabel().contains("←"))
                .toList();

        if (outgoing.isEmpty() && incoming.isEmpty()) {
            contentBox.getChildren().add(new Label("No relationships found."));
            return;
        }

        if (!outgoing.isEmpty()) {
            Label outHeader = new Label("Outgoing:");
            outHeader.getStyleClass().add("section-header");
            contentBox.getChildren().add(outHeader);
            outgoing.forEach(this::addDetail);
        }

        if (!incoming.isEmpty()) {
            Label inHeader = new Label("Incoming:");
            inHeader.getStyleClass().add("section-header");
            contentBox.getChildren().add(inHeader);
            incoming.forEach(this::addDetail);
        }
    }


    /**
     * Builds one “relationship → chapter → article → paragraph → snippet” block.
     */
    private void addDetail(RelationshipDetail d) {
        Label rel = new Label("• " + d.getEdgeLabel());
        rel.getStyleClass().add("relation-label");

        Text chapNum = new Text("Chapter " + d.getChapterNumber());
        chapNum.getStyleClass().add("chapter-num");
        Text chapTitle = new Text(": " + d.getChapterTitle());
        chapTitle.getStyleClass().add("chapter-title");
        TextFlow chapFlow = new TextFlow(chapNum, chapTitle);
        chapFlow.getStyleClass().add("chapter-header");

        Text artNum = new Text("Article " + d.getArticleNumber());
        artNum.getStyleClass().add("article-num");
        Text artTitle = new Text(": " + d.getArticleTitle());
        artTitle.getStyleClass().add("article-title");
        TextFlow artFlow = new TextFlow(artNum, artTitle);
        artFlow.getStyleClass().add("article-header");

        Label paraNum = new Label("Paragraph " + d.getParagraphNumber());
        paraNum.getStyleClass().add("paragraph-number");

        Label subtitle = new Label("Related Paragraph:");
        subtitle.getStyleClass().add("paragraph-subtitle");

        Label txt = new Label(d.getParagraphText());
        txt.setWrapText(true);
        txt.getStyleClass().add("paragraph-text");

        VBox snippetBox = new VBox(txt);
        snippetBox.setPadding(new Insets(4, 8, 12, 8));
        snippetBox.getStyleClass().add("snippet-box");

        VBox container = new VBox(4,
                rel,
                chapFlow,
                artFlow,
                paraNum,
                subtitle,
                snippetBox
        );
        container.setPadding(new Insets(4, 0, 12, 0));
        contentBox.getChildren().add(container);
    }

}
