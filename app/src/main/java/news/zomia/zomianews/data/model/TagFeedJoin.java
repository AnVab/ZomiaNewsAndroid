package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;

/**
 * Created by Andrey on 27.01.2018.
 */

@Entity(
        primaryKeys = { "tag_id", "feed_id" },
        foreignKeys = {
                @ForeignKey(entity = Tag.class,
                        parentColumns = "tid",
                        childColumns = "tag_id"),
                @ForeignKey(entity = Feed.class,
                        parentColumns = "feed_id",
                        childColumns = "feed_id")
        })
public class TagFeedJoin {
    @ColumnInfo(name = "tag_id")
    public final int tagId;
    @ColumnInfo(name = "feed_id")
    public final int feedId;

    public TagFeedJoin(final int tagId, final int feedId) {
        this.tagId = tagId;
        this.feedId = feedId;
    }
}
