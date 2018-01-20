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
import news.zomia.zomianews.data.model.Result;

/**
 * Created by Andrey on 09.01.2018.
 */
@Dao
public interface FeedDao {

    //Feeds
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void  insertFeeds(List<Feed> feeds);

    @Query("SELECT * FROM feed")
    public LiveData<List<Feed>> loadAllFeedsSync();

    //Stories
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertStories(List<Result> stories);

    @Query("SELECT * FROM result")// where feed_id = :feedId")
    public LiveData<List<Result>> loadAllStoriesSync();//Integer feedId);


/*  @Query("SELECT * FROM feed")// WHERE feed_id = feed_id")
    public Feed loadFeed();//int feedId);

    @Update
    public void updateStories(Result... stories);

    @Delete
    public void deleteStories(Result... stories);

    @Update
    public void updateFeeds(Feed... feeds);

    @Delete
    public void deleteFeeds(Feed... feeds);

    @Query("SELECT * FROM feed")
    public List<Feed> loadAllFeeds();

    @Query("SELECT * FROM result")// where feedId = feed_id")
    public LiveData<List<Result>> loadAllStoriesForFeedSync();

    @Query("SELECT * FROM result WHERE id = id")
    public Result loadStory();//int id);*/
}
