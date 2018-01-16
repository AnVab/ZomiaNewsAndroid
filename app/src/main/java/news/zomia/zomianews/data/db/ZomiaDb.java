package news.zomia.zomianews.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Result;

/**
 * Created by Andrey on 10.01.2018.
 */

@Database(entities = {Feed.class, Result.class}, version = 1)
public abstract class ZomiaDb extends RoomDatabase {
    public abstract FeedDao feedDao();
}
