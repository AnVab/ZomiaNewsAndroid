package news.zomia.zomianews.data.service.tasks;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.StoryStatus;
import news.zomia.zomianews.data.service.ZomiaService;

/**
 * Created by Andrey on 18.02.2018.
 */

public class UpdateStoryStatusDBTask implements Runnable {

    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final int status;
    private final Integer feedId;
    private final Integer storyId;
    private final ZomiaService zomiaService;
    private final FeedDao feedDao;
    private final ZomiaDb db;

    public UpdateStoryStatusDBTask(Integer feedId, Integer storyId, int status, ZomiaService zomiaService, FeedDao feedDao, ZomiaDb db) {
        this.status = status;
        this.feedId = feedId;
        this.storyId = storyId;
        this.zomiaService = zomiaService;
        this.feedDao = feedDao;
        this.db = db;
    }

    @Override
    public void run() {
        String statusString = StoryStatus.getName(status);

        //Insert new tag to DB locally
        db.beginTransaction();
        try {
            int i = feedDao.updateStory(storyId, status);
            db.setTransactionSuccessful();
            Log.d("Zomia", "updateStory " + i + " storyId: " + storyId + " status: " + status);
        } finally {
            db.endTransaction();
        }
    }
}
