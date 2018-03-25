package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Andrey on 25.03.2018.
 */

@Entity(foreignKeys = @ForeignKey(entity = Feed.class,
        parentColumns = "feed_id",
        childColumns = "feed_id"))
public class StoryCache {

    @ColumnInfo(name = "link")
    @PrimaryKey
    @NonNull
    private String link;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "feed_id")
    private Integer feedId;

    @ColumnInfo(name = "date")
    private Long date;

    public StoryCache(String link, String content, Integer feedId, Long date) {
        this.link = link;
        this.content = content;
        this.feedId = feedId;
        this.date = date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String id) {
        this.link = link;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getFeedId() {
        return feedId;
    }

    public void setFeedId(Integer feedId) {
        this.feedId = feedId;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }
}
