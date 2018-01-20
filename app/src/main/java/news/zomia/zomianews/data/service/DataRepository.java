package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Result;
import news.zomia.zomianews.data.model.Stories;
import news.zomia.zomianews.data.util.AbsentLiveData;
import news.zomia.zomianews.data.util.AppExecutors;
import news.zomia.zomianews.data.util.RateLimiter;

/**
 * Created by Andrey on 10.01.2018.
 */

@Singleton
public class DataRepository {
    private final ZomiaDb db;

    private final ZomiaService webService;

    private final FeedDao feedDao;

    private final AppExecutors appExecutors;

    private RateLimiter<String> rateLimiter = new RateLimiter<>(10, TimeUnit.MINUTES);

    @Inject
    public DataRepository(ZomiaService webService, ZomiaDb db, FeedDao feedDao, AppExecutors appExecutors) {
        this.db = db;
        this.webService = webService;
        this.feedDao = feedDao;
        this.appExecutors = appExecutors;
    }

    public ZomiaService getZomiaService()
    {
        return webService;
    }
    public LiveData<Resource<List<Feed>>> loadFeeds() {

        Log.d("ZOMIA", "loadFeeds");

        return new NetworkBoundResource<List<Feed>,List<Feed>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Feed> item) {
                feedDao.insertFeeds(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Feed> data) {
                return data == null || data.isEmpty();
            }

            @NonNull @Override
            protected LiveData<List<Feed>> loadFromDb() {
                Log.d("ZOMIA", "feedDao.loadFromDb");
                return feedDao.loadAllFeedsSync();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<List<Feed>>> createCall() {
                Log.d("ZOMIA", "webService.getFeedsList");
                return webService.getFeedsList();
            }

            @Override
            protected void onFetchFailed() {
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Result>>> loadStories(Integer feedId) {

        Log.d("ZOMIA", "loadStories feedId: " + feedId);
        return new NetworkBoundResource<List<Result>, Stories>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull Stories item) {
                Log.d("ZOMIA", "saveCallResult feedId: " + feedId);
                //Set feed id while saving to the database
                for (Result story : item.getResults()) {
                    story.setFeedId(feedId);
                }

                /*RepoSearchResult repoSearchResult = new RepoSearchResult(
                        query, storiesResult, item.getTotal(), item.getNextPage());*/

                db.beginTransaction();
                try {
                    feedDao.insertStories(item.getResults());
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Result> data) {
                //return data == null || data.isEmpty();
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Result>> loadFromDb() {
                //LiveData<List<Result>> res = feedDao.loadAllStoriesSync();//feedId);
                //return res;
                Log.d("ZOMIA", "loadFromDb feedId: " + feedId);
                return feedDao.loadAllStoriesSync(feedId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Stories>> createCall() {
                Log.d("ZOMIA", "Result.getStories");
                return webService.getStories(feedId);
            }

            @Override
            protected Stories processResponse(ApiResponse<Stories> response) {
                Stories body = response.body;
                Log.d("ZOMIA", "processResponse " + body.getResults().size());
                /*if (body != null) {
                    body.setNext(response.getNextPage());
                }*/
                return body;
            }
        }.asLiveData();
    }
}