package news.zomia.zomianews.data.service.tasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import androidx.work.Worker;
import news.zomia.zomianews.ZomiaApp;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.ApiResponse;
import news.zomia.zomianews.data.service.StoryStatus;
import news.zomia.zomianews.data.service.ZomiaService;
import news.zomia.zomianews.di.AppComponent;
import news.zomia.zomianews.di.DaggerAppComponent;
import news.zomia.zomianews.di.Injectable;
import retrofit2.Response;

import static news.zomia.zomianews.data.service.StoryStatus.to_read;

public class UpdateStoryStatusNetworkWorker extends Worker  implements Injectable {

    public static final String key_feedId = "feedId";
    public static final String key_storyId = "storyId";
    public static final String key_status = "status";

    @Inject @Named("content_json") ZomiaService zomiaService;

    @NonNull @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if(context instanceof ZomiaApp) {
            AppComponent daggerComponent = DaggerAppComponent.builder().application((ZomiaApp) context).build();
            daggerComponent.inject(this);
        }

        Integer feedId = getInputData().getInt(key_feedId, 0);
        Integer storyId = getInputData().getInt(key_storyId, 0);
        String statusString = StoryStatus.getName(getInputData().getInt(key_status, to_read));

        //Try to insert into remote server
        Response<Story> response = null;
        try {
            response = zomiaService.updateStoryStatus(feedId, storyId, statusString).execute();

            ApiResponse<Story> apiResponse = new ApiResponse<>(response);

            if (apiResponse.isSuccessful()) {
                //Log.d("Zomia", "updateStory network Result.SUCCESS " + " storyId " + storyId + " status: " + statusString);
                return Result.SUCCESS;
            } else {
                //Log.d("Zomia", "updateStory network Result.RETRY " + " storyId " + storyId + " status: " + statusString);
                //Received error
                return Result.RETRY;
            }
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d("Zomia", "updateStory network Result.FAILURE " + " storyId " + storyId + " status: " + statusString);
            return Result.FAILURE;
        }
    }
}
