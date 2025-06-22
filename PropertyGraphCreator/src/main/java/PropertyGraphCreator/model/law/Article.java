package PropertyGraphCreator.model.law;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 * Represents an article of a legislative document which consists of a list of paragraphs.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Article implements Node {

    @JsonProperty("articleNumber")
    private int articleNumber;

    @JsonProperty("articleID")
    private String articleID;

    @JsonProperty("articleTitle")
    private String articleTitle;

    @JsonProperty("paragraphs")
    private ArrayList<Paragraph> paragraphs;

    public int getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(int articleNumber) {
        this.articleNumber = articleNumber;
    }

    public ArrayList<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(ArrayList<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public String getArticleID() {
        return articleID;
    }

    public void setArticleID(String articleID) {
        this.articleID = articleID;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    @Override
    public String toString() {
        return "Article " + articleID + ": " + articleTitle;
    }

    @JsonIgnore
    @Override
    public String getText() {
        StringBuilder text = new StringBuilder(articleTitle + "\n\n");
        for (Paragraph paragraph : paragraphs) {
            text.append(paragraph.getText()).append("\n");
        }
        return text.toString().trim();
    }

    @JsonIgnore
    @Override
    public String getTitle() {
        return articleTitle;
    }

    @JsonIgnore
    @Override
    public ArrayList<Paragraph> getChildren() {
        return paragraphs;
    }

}

