package PropertyGraphCreator.model.graph;

public class GraphEdge {
    private String source;
    private String target;
    private String label;
    private String paragraphId;

    public GraphEdge(String source, String target, String label, String paragraphId) {
        this.source = source;
        this.target = target;
        this.label = label;
        this.paragraphId = paragraphId;
    }

    public String getSource() { return source; }
    public String getTarget() { return target; }
    public String getLabel() { return label; }
    public String getParagraphId() { return paragraphId; }
}
