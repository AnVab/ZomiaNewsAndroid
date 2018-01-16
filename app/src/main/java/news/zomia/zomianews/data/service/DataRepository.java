package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
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

    /*public LiveData<List<Feed>> getFeeds() {
        refreshFeeds();
        // return a LiveData directly from the database.
        return feedDao.loadAllFeedsSync();
    }

    private void refreshFeeds() {
        executor.execute(() -> {
            // running in a background thread
            // check if feeds were fetched recently
            boolean feedsExist = feedDao.hasFeeds(FRESH_TIMEOUT);
            if (!feedsExist) {
                // refresh the data
                Response response = null;
                try {
                    response = webService.getFeedsList().execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // TODO check for error etc.
                // Update the database.The LiveData will automatically refresh so
                // we don't need to do anything else here besides updating the database
                feedDao.insertFeeds(response.body());
            }
        });
    }*/

    /*public LiveData<List<Feed>> getFeeds()
    {
        final MutableLiveData<List<Feed>> data = new MutableLiveData<>();
        webService.getFeedsList().enqueue(new Callback<List<Feed>>() {
            @Override
            public void onResponse(Call<List<Feed>> call, Response<List<Feed>> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            data.setValue(response.body());
                            break;
                        default:

                            break;
                    }
                }
                else
                {
                    //Connection problem
                }
            }

            @Override
            public void onFailure(Call<List<Feed>> call, Throwable t) {
            }
        });
        return data;
    }*/
}
