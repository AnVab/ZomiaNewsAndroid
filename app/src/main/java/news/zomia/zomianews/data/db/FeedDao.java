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

    @Query("SELECT * FROM result where feed_id = :feedId ORDER BY date DESC")
    public LiveData<List<Result>> loadAllStoriesSync(Integer feedId);

    @Query("SELECT * FROM result where id = :storyId")
    public LiveData<Result> findStoryById(Integer storyId);
}
