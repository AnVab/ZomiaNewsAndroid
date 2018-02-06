package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.DiffCallback;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    public static DiffCallback<Story> DIFF_CALLBACK = new DiffCallback<Story>() {
        @Override
        public boolean areItemsTheSame(@NonNull Story oldItem, @NonNull Story newItem) {
            return oldItem.getFeedId() == newItem.getFeedId() &&
                    oldItem.getStoryId() == newItem.getStoryId() &&
                    oldItem.getTitle() == newItem.getTitle() &&
                    oldItem.getDate() == newItem.getDate();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Story oldItem, @NonNull Story newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        Story story = (Story) obj;

        return story.getFeedId() == this.getFeedId() &&
                story.getStoryId() == this.getStoryId() &&
                story.getTitle() == this.getTitle() &&
                story.getDate() == this.getDate() &&
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

    @SerializedName("date")
    @Expose
    private Date date;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("content")
    @Expose
    private String content;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
}
