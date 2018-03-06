package news.zomia.zomianews.data.service.tasks;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.IOException;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagJson;
import news.zomia.zomianews.data.service.ApiResponse;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.ZomiaService;
import retrofit2.Response;

/**
 * Created by Andrey on 05.03.2018.
 */

public class DeleteTagTask   implements Runnable {
    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final String tagName;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    public DeleteTagTask(String tagName, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        this.tagName = tagName;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            //Get tag by name from the local database
            Tag tag = feedDao.getTagByName(tagName);
            if(tag != null) {
                Integer tagId = tag.getTagId();

                Response<TagJson> response = zomiaService.deleteTag(tagId).execute();

                ApiResponse<TagJson> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    db.beginTransaction();
                    try {
                        feedDao.deleteTagFeedPairsByTagId(tagId);
                        feedDao.deleteTag(tagId);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
                    resultState.postValue(Resource.success(apiResponse.body != null));
                } else {
                    //Received error
                    resultState.postValue(Resource.error(apiResponse.errorMessage, true));
                }
            }
        } catch (IOException e) {
            resultState.postValue(Resource.error(e.getMessage(), true));
        }
    }

    public LiveData<Resource<Boolean>> getResultState() {
        return resultState;
    }
}
