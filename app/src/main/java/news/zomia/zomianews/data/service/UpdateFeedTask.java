package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.IOException;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Feed;
import retrofit2.Response;

/**
 * Created by Andrey on 28.02.2018.
 */

public class UpdateFeedTask implements Runnable {
    private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();

    private final Feed feed;
    //private final String feedUrl;
    //private final String tag;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    UpdateFeedTask(Feed feed, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        //this.feedUrl = feedUrl;
        //this.tag = tag;
        this.feed = feed;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            //Feed feed = new Feed();
            //feed.setUrl(feedUrl);
            //feed.setTag(tag);
            //First: try to update into remote server
            Response<Feed> response = zomiaService.updateFeed(feed.getFeedId(), feed).execute();

            ApiResponse<Feed> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {
                //Insert new tag to DB
                db.beginTransaction();
                try {
                    feedDao.insertFeed(apiResponse.body);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                liveData.postValue(Resource.success(apiResponse.body != null));
            } else {
                //Received error
                liveData.postValue(Resource.error(apiResponse.errorMessage, true));
            }

        } catch (IOException e) {
            liveData.postValue(Resource.error(e.getMessage(), true));
        }
    }

    LiveData<Resource<Boolean>> getLiveData() {
        return liveData;
    }
}
