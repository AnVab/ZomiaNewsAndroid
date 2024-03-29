package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.ListResponse;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagJson;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.UserInfo;
import news.zomia.zomianews.data.model.Token;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Andrey on 26.12.2017.
 */

public interface ZomiaService {
    @POST("api/registration/")
    Call<User> registerUser(@Body User user);

    @POST("auth/")
    Call<Token> authenticateUser(@Body User user);

    @GET("api/feeds/")
    LiveData<ApiResponse<ListResponse<Feed>>> getFeedsList();

    @POST ("api/feeds/")
    Call<Feed> addNewFeed(@Body Feed feedUrl);

    @PATCH("api/feeds/{feed_id}/")
    Call<Feed> updateFeed(@Path("feed_id") int feed_id, @Body Feed feed);

    @DELETE("api/feeds/{feed_id}/")
    Call<Feed> deleteFeed(@Path("feed_id") int feed_id);

    @GET ("api/feeds/{id}/")
    Call<Feed> getFeedInfo(@Path("id") int id);

    @GET ("api/feeds/{id}/stories/")
    LiveData<ApiResponse<ListResponse<Story>>> getStories(@Path("id") int id);

    @GET ("api/feeds/{id}/stories/")
    Call<ListResponse<Story>> getStoriesCursor(@Path("id") int id, @Query(value = "cursor",encoded = true) String cursor);

    @GET ("api/users/{id}/")
    Call<UserInfo> getUserInfo(@Path("id") int id);

    @GET("api/tags/")
    LiveData<ApiResponse<ListResponse<TagJson>>> getTagsList();

    @POST ("api/tags/")
    Call<TagJson> addNewTag(@Body TagJson tagName);

    @PATCH("api/tags/{tag_id}/")
    Call<TagJson> updateTag(@Path("tag_id") int tagId, @Body Tag tag);

    @DELETE("api/tags/{tag_id}/")
    Call<TagJson> deleteTag(@Path("tag_id") int tag_id);

    @POST ("api/feeds/{feed_id}/stories/{story_id}/{status}/")
    Call<Story> updateStoryStatus(@Path("feed_id") Integer feedId, @Path("story_id") Integer storyId, @Path("status") String status);

    @Multipart
    @POST ("api/opml/")
    Call<ResponseBody> importOpml(@Part MultipartBody.Part file);
}
