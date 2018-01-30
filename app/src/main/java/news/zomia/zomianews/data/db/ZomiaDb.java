package news.zomia.zomianews.data.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Result;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedJoin;

/**
 * Created by Andrey on 10.01.2018.
 */

@Database(entities = {Feed.class, Result.class, Tag.class, TagFeedJoin.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class ZomiaDb extends RoomDatabase {
    public abstract FeedDao feedDao();
}
