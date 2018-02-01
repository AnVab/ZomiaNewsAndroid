package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagJson;
import retrofit2.Response;

/**
 * Created by Andrey on 31.01.2018.
 */

public class InsertNewTagTask implements Runnable {
    private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();

    private final String tagName;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    InsertNewTagTask(String tagName, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        this.tagName = tagName;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            TagJson tag = new TagJson();
            tag.setName(tagName);

            //First: try to insert into remote server
            Response<TagJson> response = zomiaService.addNewTag(tag).execute();

            ApiResponse<TagJson> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {
                //Insert new tag to DB
                db.beginTransaction();
                try {
                    feedDao.insertTag(new Tag(apiResponse.body.getId(), apiResponse.body.getName()));
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
