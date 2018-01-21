package news.zomia.zomianews.data.model;

import android.arch.persistence.room.ColumnInfo;

/**
 * Created by Andrey on 21.01.2018.
 */

public class FeedStoriesCount {
    @ColumnInfo(name = "feed_id")
    private Integer feedId;
    @ColumnInfo(name = "COUNT(*)")
    private Integer storiesCountTotal;

    public Integer getFeedId() {
        return feedId;
    }

    public void setFeedId(Integer feedId) {
        this.feedId = feedId;
    }

    public Integer getStoriesCountTotal() {
        return storiesCountTotal;
    }

    public void setStoriesCountTotal(Integer storiesCountTotal) {
        this.storiesCountTotal = storiesCountTotal;
    }
}
