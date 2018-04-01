package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Token;
import news.zomia.zomianews.data.model.User;
import retrofit2.Response;

/**
 * Created by Andrey on 15.02.2018.
 */

public class AuthorizeTask implements Runnable {
    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final String login;
    private final String password;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;
    private final SharedPreferences sharedPref;
    private static Context context;
    AuthorizeTask(Context context, String login, String password, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db, SharedPreferences sharedPref) {
        this.login = login;
        this.password = password;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
        this.sharedPref = sharedPref;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            User user = new User();
            user.setEmail(login);
            user.setPassword(password);
            //First: try to insert into remote server
            Response<Token> response = zomiaService.authenticateUser(user).execute();

            ApiResponse<Token> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {

                //Save token
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(context.getString(R.string.preferences_token), response.body().getToken());
                editor.commit();

                resultState.postValue(Resource.success(apiResponse.body != null));
            } else {
                //Received error
                resultState.postValue(Resource.error(apiResponse.errorMessage, true));
            }

        } catch (IOException e) {
            resultState.postValue(Resource.error(e.getMessage(), true));
        }
    }

    LiveData<Resource<Boolean>> getResultState() {
        return resultState;
    }
}