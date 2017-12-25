package news.zomia.zomianews.data.service;

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

public interface APIService {
    @POST("registration")
    Call<User> registerUser(@Query("email") String email, @Query("password") String password);

    @POST("auth/")
    Call<Token> authenticateUser(@Query("email") String email, @Query("password") String password);

    @GET("feeds/")
    Call<List<Feed>> getFeedsList();

    @POST ("feeds/")
    Call<Feed> addNewFeed(@Query("url") String feedUrl);

    @GET ("feeds/{id}/")
    Call<Feed> getFeedInfo(@Path("id") int id);

    @PUT("feeds/{id}/")
    Call<Feed> updateFeedInfo(@Path("id") int id, @Body Feed feed);

    @GET ("feeds/{id}/stories/")
    Call< Stories > getStories(@Path("id") int id);

    @GET ("users/{id}/")
    Call<UserInfo> getUserInfo(@Path("id") int id);
}
