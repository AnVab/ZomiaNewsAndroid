package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Andrey on 26.12.2017.
 */

@Entity(indices = {@Index(value = {"feed_id"},
        unique = true)})
public class Feed {

    //Not serializable field. Used in database
    @ColumnInfo(name = "fid")
    @PrimaryKey
    private transient Integer fId;

    @Expose
    @SerializedName("feed_id")
    @ColumnInfo(name = "feed_id")
    private Integer feedId;

    @Expose
    @SerializedName("title")
    private String title;

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("link")
    private String link;

    @Expose
    @SerializedName("url")
    private String url;

    @Expose
    @SerializedName("tag")
    private String tag;

    @Expose
    @SerializedName("image")
    private String image;

    @Expose
    @SerializedName("broken")
    private Boolean broken;

    @Expose
    @SerializedName("icon")
    private String icon;

    public Integer getFId() {
        return fId;
    }

    public void setFId(Integer fId) {
        this.fId = fId;
    }

    public Integer getFeedId() {
        return feedId;
    }

    public void setFeedId(Integer feedId) {
        this.feedId = feedId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getBroken() {
        return broken;
    }

    public void setBroken(Boolean broken) {
        this.broken = broken;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        } else
        if (!(obj instanceof Feed)) {
            return false;
        }
        Feed obj2 = (Feed)obj;
        return obj2.getFeedId().equals(getFeedId());
    }
}