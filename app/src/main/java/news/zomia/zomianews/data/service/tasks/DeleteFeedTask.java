package news.zomia.zomianews.data.service.tasks;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.IOException;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.service.ApiResponse;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.ZomiaService;
import retrofit2.Response;

/**
 * Created by Andrey on 04.03.2018.
 */

public class DeleteFeedTask  implements Runnable {
    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final Integer feedId;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    public DeleteFeedTask(Integer feedId, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        this.feedId = feedId;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            Response<Feed> response = zomiaService.deleteFeed(feedId).execute();

            ApiResponse<Feed> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {
                //Insert new tag to DB
                db.beginTransaction();
                try {
                    feedDao.deleteTagFeedPairs(feedId);
                    feedDao.deleteFeed(feedId);

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                resultState.postValue(Resource.success(apiResponse.body != null));
            } else {
                //Received error
                resultState.postValue(Resource.error(apiResponse.errorMessage, true));
            }

        } catch (IOException e) {
            resultState.postValue(Resource.error(e.getMessage(), true));
        }
    }

    public LiveData<Resource<Boolean>> getResultState() {
        return resultState;
    }
}
