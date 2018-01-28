package news.zomia.zomianews.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Created by Andrey on 25.01.2018.
 *
 * A class for Tag info for a response from Zomia Web API
 */

public class TagJson {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("feeds_id")
    @Expose
    private List<Integer> feedsId = null;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getFeedsId() {
        return feedsId;
    }

    public void setFeedsId(List<Integer> feedsId) {
        this.feedsId = feedsId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("feedsId", feedsId).toString();
    }

}