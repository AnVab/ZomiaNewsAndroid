package news.zomia.zomianews.data.service.tasks;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.IOException;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedJoin;
import news.zomia.zomianews.data.model.TagJson;
import news.zomia.zomianews.data.service.ApiResponse;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.ZomiaService;
import retrofit2.Response;

/**
 * Created by Andrey on 18.02.2018.
 */

public class UpdateTagTask implements Runnable {

    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final String oldTagName;
    private final  String newTagName;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    public UpdateTagTask(String oldTagName, String newTagName, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        this.oldTagName = oldTagName;
        this.newTagName = newTagName;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            //First: try to insert into remote server
            Tag tag = feedDao.getTagByName(oldTagName);
            if(tag != null) {
                //Set new name
                tag.setName(newTagName);
                //Update tag on the server
                Response<TagJson> response = zomiaService.updateTag(tag.getTagId(), tag).execute();

                ApiResponse<TagJson> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    //Insert new tag to DB
                    db.beginTransaction();
                    try {
                        //Update tag in the local database
                        feedDao.updateTagName(apiResponse.body.getId(), apiResponse.body.getName());

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

    public LiveData<Resource<Boolean>> getLiveData() {
        return resultState;
    }
}
