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
public interface DAOService {

    //Feeds
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertFeeds(Feed... feeds);

    @Update
    public void updateFeeds(Feed... feeds);

    @Delete
    public void deleteFeeds(Feed... feeds);

    @Query("SELECT * FROM feeds")
    public List<Feed> loadAllFeeds();

    @Query("SELECT * FROM feeds")
    public LiveData<List<Feed>> loadAllFeedsSync();

    @Query("SELECT * FROM feeds WHERE feed_id = feedId")
    public Feed loadFeed(int feedId);

    //Stories
    @Insert
    public void insertStories(Result... stories);

    @Update
    public void updateStories(Result... stories);

    @Delete
    public void deleteStories(Result... stories);

    @Query("SELECT * FROM results where feed_id = feedId")
    public List<Result> loadAllStoriesForFeed(int feedId);

    @Query("SELECT * FROM results where feed_id = feedId")
    public LiveData<List<Result>> loadAllStoriesForFeedSync();

    @Query("SELECT * FROM results WHERE id = id")
    public Result loadStory(int id);
}
