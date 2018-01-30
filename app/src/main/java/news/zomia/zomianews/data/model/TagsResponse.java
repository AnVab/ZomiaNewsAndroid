package news.zomia.zomianews.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Andrey on 30.01.2018.
 */

public class TagsResponse {

    @Expose
    @SerializedName("next")
    private String next;

    @Expose
    @SerializedName("previous")
    private Object previous;

    @Expose
    @SerializedName("results")
    private List<TagJson> results = null;

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public Object getPrevious() {
        return previous;
    }

    public void setPrevious(Object previous) {
        this.previous = previous;
    }

    public List<TagJson> getResults() {
        return results;
    }

    public void setResults(List<TagJson> results) {
        this.results = results;
    }
}
