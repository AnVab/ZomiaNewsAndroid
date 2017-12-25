package news.zomia.zomianews;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.Call;
import retrofit2.http.Query;
import retrofit2.http.Path;
import retrofit2.http.Body;
import java.util.List;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Stories;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.UserInfo;
import news.zomia.zomianews.data.model.Token;
/**
 * Created by Andrey on 25.12.2017.
 */

public interface ZomiaApi {
    @POST("registration")
    Call<User> registerUser(@Query("email") String email, @Query("password") String password);

    @POST("/api/auth/")
    Call<Token> authentificateUser(@Query("email") String email, @Query("password") String password);

    @GET ("/api/feeds/")
    Call< List<Feed> > getFeedsList();

    @POST ("/api/feeds/")
    Call<Feed> addNewFeed(@Query("url") String feedUrl);

    @GET ("/api/feeds/{id}/")
    Call<Feed> getFeedInfo(@Path("id") int id);

    @PUT ("/api/feeds/{id}/")
    Call<Feed> updateFeedInfo(@Path("id") int id, @Body Feed feed);

    @GET ("/api/feeds/{id}/stories/")
    Call< Stories > getStories(@Path("id") int id);

    @GET ("/api/users/{id}/")
    Call<UserInfo> getUserInfo(@Path("id") int id);
}
