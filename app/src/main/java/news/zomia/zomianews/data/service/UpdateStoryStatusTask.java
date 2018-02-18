package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.IOException;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Story;
import retrofit2.Response;

/**
 * Created by Andrey on 18.02.2018.
 */

public class UpdateStoryStatusTask implements Runnable {

    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final int status;
    private final int feedId;
    private final int storyId;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    UpdateStoryStatusTask(int feedId, int storyId, int status, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        this.status = status;
        this.feedId = feedId;
        this.storyId = storyId;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            String statusString = StoryStatus.GetValueName(status);
            //First: try to insert into remote server
            Response<Story> response = zomiaService.updateStoryStatus(feedId, storyId, statusString).execute();

            ApiResponse<Story> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {
                //Insert new tag to DB
                db.beginTransaction();
                try {
                    feedDao.updateStory(storyId, status);
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

    LiveData<Resource<Boolean>> getLiveData() {
        return resultState;
    }
}
