package news.zomia.zomianews.data.db;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Story;
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

    @Query("SELECT * FROM tag WHERE name=:name ORDER BY name ASC")
    public Tag getTagByName(String name);

    @Query("UPDATE tag SET name = :name WHERE tag_id = :tagId")
    public abstract int updateTagName(Integer tagId, String name);

    @Query("SELECT * FROM tag INNER JOIN TagFeedJoin ON tag.tag_id=TagFeedJoin.tag_id WHERE  TagFeedJoin.feed_id=:feedId")
    public LiveData<List<Tag>> getTagsForFeed(Integer feedId);

    @Query("SELECT * FROM feed INNER JOIN TagFeedJoin ON feed.feed_id=TagFeedJoin.feed_id WHERE TagFeedJoin.tag_id=:tagId")
    public LiveData<List<Feed>> getFeedsForTag(Integer tagId);

    @Query("SELECT feed.* FROM feed LEFT JOIN TagFeedJoin ON feed.feed_id=TagFeedJoin.feed_id WHERE TagFeedJoin.tag_id is null")
    public LiveData<List<Feed>> getFeedsWithNoTag();

    @Query("SELECT * FROM feed, tag INNER JOIN TagFeedJoin ON feed.feed_id=TagFeedJoin.feed_id WHERE TagFeedJoin.tag_id=tag.tag_id AND TagFeedJoin.feed_id =feed.feed_id ORDER BY name, title ASC")
    public LiveData<List<TagFeedPair>> getFeedsWithTags();

    @Query("DELETE FROM tag WHERE tag_id=:tagId")
    public void  deleteTag(Integer tagId);

    //Feeds
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void  insertFeed(Feed feed);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void  insertFeeds(List<Feed> feeds);

    @Query("SELECT * FROM feed")
    public LiveData<List<Feed>> loadAllFeeds();

    @Query("DELETE FROM feed WHERE feed_id=:feedId")
    public void  deleteFeed(Integer feedId);

    @Query("DELETE FROM TagFeedJoin WHERE feed_id=:feedId")
    public void  deleteTagFeedPairsByFeedId(Integer feedId);

    @Query("DELETE FROM TagFeedJoin WHERE tag_id=:tagId")
    public void  deleteTagFeedPairsByTagId(Integer tagId);

    //Stories
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void insertStories(List<Story> stories);

    @Query("SELECT * FROM Story WHERE feed_id = :feedId ORDER BY date DESC")
    public LiveData<List<Story>> loadAllStories(Integer feedId);

    @Query("SELECT * FROM Story WHERE feed_id = :feedId ORDER BY date DESC")
    public abstract DataSource.Factory<Integer, Story> loadAllStories2(Integer feedId);

    @Query("SELECT * FROM Story WHERE story_id = :storyId")
    public LiveData<Story> findStoryById(Integer storyId);

    @Query("SELECT feed_id, COUNT(*) FROM Story GROUP BY feed_id")
    public abstract LiveData<List<FeedStoriesCount>> countFeedStoriesTotal();

    @Query("UPDATE Story SET status = :status WHERE story_id = :storyId")
    public abstract int updateStory(Integer storyId, Integer status);

    @Query("DELETE FROM Story WHERE feed_id=:feedId")
    public void deleteStoriesByFeedId(Integer feedId);
}
