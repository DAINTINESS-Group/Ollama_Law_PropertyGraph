package EntityChecklistGenerator.engine.dto;

public class RelationshipDetail {
    // existing…
    private final String connectedNodeId;
    private final String connectedNodeLabel;
    private final String edgeLabel;
    private final String paragraphId;
    private final String paragraphText;

    // new fields
    private final int    chapterNumber;
    private final String chapterTitle;
    private final int    articleNumber;
    private final String articleTitle;
    private final String    paragraphNumber;

    public RelationshipDetail(
            String connectedNodeId,
            String connectedNodeLabel,
            String edgeLabel,
            String paragraphId,
            String paragraphText,

            int chapterNumber,
            String chapterTitle,
            int articleNumber,
            String articleTitle,
            String paragraphNumber
    ) {
        this.connectedNodeId    = connectedNodeId;
        this.connectedNodeLabel = connectedNodeLabel;
        this.edgeLabel          = edgeLabel;
        this.paragraphId        = paragraphId;
        this.paragraphText      = paragraphText;

        this.chapterNumber      = chapterNumber;
        this.chapterTitle       = chapterTitle;
        this.articleNumber      = articleNumber;
        this.articleTitle       = articleTitle;
        this.paragraphNumber    = paragraphNumber;
    }

    public String getConnectedNodeId()    { return connectedNodeId; }
    public String getConnectedNodeLabel() { return connectedNodeLabel; }
    public String getEdgeLabel()          { return edgeLabel; }
    public String getParagraphId()        { return paragraphId; }
    public String getParagraphText()      { return paragraphText; }

    // new getters:
    public int    getChapterNumber()   { return chapterNumber; }
    public String getChapterTitle()    { return chapterTitle; }
    public int    getArticleNumber()   { return articleNumber; }
    public String getArticleTitle()    { return articleTitle; }
    public String    getParagraphNumber() { return paragraphNumber; }
}
