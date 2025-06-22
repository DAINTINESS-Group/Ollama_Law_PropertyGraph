package EntityChecklistGenerator.model.law;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Law object that contains a list of chapters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Law implements Node {


    @JsonProperty("chapters")
    private ArrayList<Chapter> chapters;

    @JsonProperty("text")
    private String text;

    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }



    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getTitle() {
        return "Law";
    }

    @Override
    public List<Chapter> getChildren() {
        return chapters;
    }

    public void setChildren(List<Chapter> chapters) {
        this.chapters = (ArrayList<Chapter>) chapters;
    }


    @Override
    public String toString() {
        return "Law" ;
    }


    public String toJsonString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Law to JSON", e);
        }
    }

}



