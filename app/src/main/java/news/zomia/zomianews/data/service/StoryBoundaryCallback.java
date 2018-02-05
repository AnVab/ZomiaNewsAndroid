package news.zomia.zomianews.data.service;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PagedList;
import java.text.SimpleDateFormat;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.ListResponse;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.util.AppExecutors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Andrey on 05.02.2018.
 */

public class StoryBoundaryCallback extends PagedList.BoundaryCallback<Story>  {

    public static final String TAG = "ZOMIAStoryBoundaryCback";

    ZomiaService webService;

    FeedDao feedDao;

    ZomiaDb db;

    Integer feedId;

    AppExecutors appExecutors;
    private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();

    public StoryBoundaryCallback(ZomiaService webService, ZomiaDb db, FeedDao feedDao, AppExecutors appExecutors) {
        super();
        this.webService = webService;
        this.db = db;
        this.feedDao = feedDao;
        this.appExecutors = appExecutors;

        feedId = 0;
    }

    public MutableLiveData getNetworkState() {
        return liveData;
    }

    @Override
    public void onZeroItemsLoaded() {
        //super.onZeroItemsLoaded();
        //fetchFromNetwork(null);
    }

    @Override
    public void onItemAtFrontLoaded(@NonNull Story itemAtFront) {
        //super.onItemAtFrontLoaded(itemAtFront);
    }

    @Override
    public void onItemAtEndLoaded(@NonNull Story itemAtEnd) {
        // super.onItemAtEndLoaded(itemAtEnd);
        fetchFromNetwork(itemAtEnd);
    }

    public void setSelectedFeedId(Integer feedId)
    {
        this.feedId = feedId;
    }

    public void fetchFromNetwork(Story story) {

        Integer _feedId;
        Date date;
        if(story == null) {
            date = Calendar.getInstance().getTime();
            _feedId = this.feedId;
        }
        else {
            date = story.getDate();
            _feedId = story.getFeedId();
        }

        liveData.postValue(Resource.loading(true));

        webService.getStoriesCursor(_feedId, getCursor(date)).enqueue(new Callback<ListResponse<Story>>() {
            @Override
            public void onResponse(Call<ListResponse<Story>> call, Response<ListResponse<Story>> response) {
                appExecutors.diskIO().execute(()->{
                    if((response.code() >= 200 && response.code() < 300) && response.body() != null) {

                        //Set feed id while saving to the database
                        for (Story story : response.body().getResults()) {
                            story.setFeedId(_feedId);
                        }

                        db.beginTransaction();
                        try {
                            feedDao.insertStories(response.body().getResults());
                            db.setTransactionSuccessful();
                        } finally {
                            db.endTransaction();
                        }
                        liveData.postValue(Resource.success(true));
                    }
                    else
                        liveData.postValue(Resource.error(response.message(), true));

                });
            }

            @Override
            public void onFailure(Call<ListResponse<Story>> call, Throwable t) {
                String errorMessage;
                errorMessage = t.getMessage();
                if (t == null) {
                    errorMessage = "unknown error";
                }
                Log.d(TAG, errorMessage);
                //liveData.postValue(new NetworkState(Status.ERROR, errorMessage));
                liveData.postValue(Resource.error(errorMessage, true));
            }
        });
    }

    private String getCursor(Date date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        Log.d(TAG, "dateString: " + date.toString() + " formatted: " + dateString);
        String base64 = "";
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(URLEncoder.encode("p=", "UTF-8"));
            sb.append(URLEncoder.encode(dateString, "UTF-8"));

            byte[] data = sb.toString().getBytes();
            Log.d(TAG, "byte: " + sb.toString());
            base64 = Base64.encodeToString(data, Base64.DEFAULT);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
        }

        return base64;
    }
}
