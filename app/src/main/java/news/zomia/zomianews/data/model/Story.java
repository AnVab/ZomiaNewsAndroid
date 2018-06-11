package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Andrey on 26.12.2017.
 */

@Entity(foreignKeys = @ForeignKey(entity = Feed.class,
        parentColumns = "feed_id",
        childColumns = "feed_id"),
        indices = {@Index(value = {"story_id"},
                unique = true)})
public class Story {

    public static DiffUtil.ItemCallback<Story> DIFF_CALLBACK = new DiffUtil.ItemCallback<Story>() {
        @Override
        public boolean areItemsTheSame(@NonNull Story oldItem, @NonNull Story newItem) {
            return oldItem.getStoryId() == newItem.getStoryId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Story oldItem, @NonNull Story newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;

        if (obj == this)
            return true;

        Story story = (Story) obj;

        return story.getFeedId() == this.getFeedId() &&
                story.getStoryId() == this.getStoryId() &&
                story.getTitle() == this.getTitle() &&
                story.getDate() == this.getDate() &&
                story.getCreated() == this.getCreated() &&
                story.getContent() == this.getContent();
    }


    //Not serializable field. Used in database
    @ColumnInfo(name = "sid")
    @PrimaryKey
    private transient Integer sId;

    @Expose
    @SerializedName("id")
    @ColumnInfo(name = "story_id")
    private Integer storyId;

    //When story story was added to the zomia site
    @SerializedName("created")
    @Expose
    private Long created;

    //When story story was created on a news site
    @SerializedName("date")
    @Expose
    private Long date;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("content")
    @Expose
    private String content;

    @SerializedName("short_text")
    @Expose
    private String shortText;

    @SerializedName("status")
    @Expose
    private Integer status;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("link")
    @Expose
    private String link;

    //Non serializable field. Used only for Database storage.
    @Expose
    @ColumnInfo(name = "feed_id")
    private transient Integer feedId;

    public Integer getSId() {
        return sId;
    }

    public void setSId(Integer sId) {
        this.sId = sId;
    }

    public Integer getStoryId() {
        return storyId;
    }

    public void setStoryId(Integer storyId) {
        this.storyId = storyId;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setFeedId(Integer id) {
        this.feedId = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }
}
