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

    private static final String TAG = StoryBoundaryCallback.class.getSimpleName();

    ZomiaService webService;
    FeedDao feedDao;
    ZomiaDb db;
    AppExecutors appExecutors;
    Integer feedId;
    private MutableLiveData networkState;

    public StoryBoundaryCallback(ZomiaService webService, ZomiaDb db, FeedDao feedDao, AppExecutors appExecutors) {
        super();
        this.webService = webService;
        this.db = db;
        this.feedDao = feedDao;
        this.appExecutors = appExecutors;
        networkState = new MutableLiveData();

        feedId = 0;
    }

    public MutableLiveData getNetworkState() {
        return networkState;
    }

    @Override
    public void onZeroItemsLoaded() {
        fetchFromNetwork(null);
    }

    @Override
    public void onItemAtFrontLoaded(@NonNull Story itemAtFront) {
    }

    @Override
    public void onItemAtEndLoaded(@NonNull Story itemAtEnd) {
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

        networkState.postValue(NetworkState.LOADING);

        webService.getStoriesCursor(_feedId, getCursor(date)).enqueue(new Callback<ListResponse<Story>>() {
            @Override
            public void onResponse(Call<ListResponse<Story>> call, Response<ListResponse<Story>> response) {
                appExecutors.diskIO().execute(()->{
                    if(response.isSuccessful() && (response.code() >= 200 && response.code() < 300) && response.body() != null) {

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
                        networkState.postValue(NetworkState.LOADED);
                    }
                    else
                        networkState.postValue(new NetworkState(Status.ERROR, response.message()));
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
                networkState.postValue(new NetworkState(Status.ERROR, errorMessage));
            }
        });
    }

    //Convert story date to a cursor string for a next page data
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
