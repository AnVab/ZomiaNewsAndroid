package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;

import java.util.List;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Stories;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.UserInfo;
import news.zomia.zomianews.data.model.Token;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Andrey on 26.12.2017.
 */

public interface ZomiaService {
    @POST("registration/")
    Call<User> registerUser(@Query("email") String email, @Query("password") String password);

    @POST("auth/")
    Call<Token> authenticateUser(@Body User user);

    @GET("api/feeds/")
    LiveData<ApiResponse<List<Feed>>> getFeedsList();

    @POST ("api/feeds/")
    Call<Feed> addNewFeed(@Body Feed feedUrl);

    @GET ("api/feeds/{id}/")
    Call<Feed> getFeedInfo(@Path("id") int id);

    @PUT("api/feeds/{id}/")
    Call<Feed> updateFeedInfo(@Path("id") int id, @Body Feed feed);

    @GET ("api/feeds/{id}/stories/")
    Call< Stories > getStories(@Path("id") int id);

    @GET ("api/users/{id}/")
    Call<UserInfo> getUserInfo(@Path("id") int id);
}
