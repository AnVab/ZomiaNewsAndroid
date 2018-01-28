package news.zomia.zomianews.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Result;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedJoin;
import news.zomia.zomianews.data.model.TagFeedPair;

/**
 * Created by Andrey on 09.01.2018.
 */
@Dao
public interface FeedDao {
    //Tags
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertTag(Tag tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertTags(List<Tag> tags);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertTagFeedJoin(TagFeedJoin tagFeedJoin);

    @Query("SELECT * FROM tag  ORDER BY name ASC")
    public LiveData<List<Tag>> getTags();

    @Query("SELECT * FROM tag INNER JOIN TagFeedJoin ON tag.tid=TagFeedJoin.tag_id WHERE  TagFeedJoin.feed_id=:feedId")
    public LiveData<List<Tag>> getTagsForFeed(Integer feedId);

    @Query("SELECT * FROM feed INNER JOIN TagFeedJoin ON feed.feed_id=TagFeedJoin.feed_id WHERE TagFeedJoin.tag_id=:tagId")
    public LiveData<List<Feed>> getFeedsForTag(Integer tagId);

    @Query("SELECT * FROM feed INNER JOIN TagFeedJoin ON feed.feed_id=TagFeedJoin.feed_id WHERE TagFeedJoin.tag_id is null")
    public LiveData<List<Feed>> getFeedsWithNoTag();

    @Query("SELECT * FROM feed, tag INNER JOIN TagFeedJoin ON feed.feed_id=TagFeedJoin.feed_id WHERE TagFeedJoin.tag_id=tid AND TagFeedJoin.feed_id =fid ORDER BY name, title ASC")
    public LiveData<List<TagFeedPair>> getFeedsWithTags();


    //Feeds
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void  insertFeeds(List<Feed> feeds);

    @Query("SELECT * FROM feed")
    public LiveData<List<Feed>> loadAllFeeds();

    //Stories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertStories(List<Result> stories);

    @Query("SELECT * FROM result WHERE feed_id = :feedId ORDER BY date DESC")
    public LiveData<List<Result>> loadAllStories(Integer feedId);

    @Query("SELECT * FROM result WHERE id = :storyId")
    public LiveData<Result> findStoryById(Integer storyId);

    @Query("SELECT feed_id, COUNT(*) FROM result GROUP BY feed_id")
    public abstract LiveData<List<FeedStoriesCount>> countFeedStoriesTotal();
}
