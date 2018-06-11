package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.ListResponse;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedJoin;
import news.zomia.zomianews.data.model.TagFeedPair;
import news.zomia.zomianews.data.model.TagJson;
import news.zomia.zomianews.data.service.tasks.DeleteFeedTask;
import news.zomia.zomianews.data.service.tasks.DeleteTagTask;
import news.zomia.zomianews.data.service.tasks.InsertNewFeedTask;
import news.zomia.zomianews.data.service.tasks.InsertNewTagTask;
import news.zomia.zomianews.data.service.tasks.UpdateFeedTask;
import news.zomia.zomianews.data.service.tasks.UpdateStoryStatusTask;
import news.zomia.zomianews.data.service.tasks.UpdateTagTask;
import news.zomia.zomianews.data.service.tasks.UploadFileTask;
import news.zomia.zomianews.data.util.AppExecutors;
import news.zomia.zomianews.data.util.RateLimiter;

/**
 * Created by Andrey on 10.01.2018.
 */

@Singleton
public class DataRepository {
    private static final String TAG = StoryBoundaryCallback.class.getSimpleName();

    private final ZomiaDb db;
    private final @Named("content_json") ZomiaService webService;
    private final @Named("content_multipart") ZomiaService webServiceUploadMultipart;
    private final FeedDao feedDao;
    private final AppExecutors appExecutors;

    private RateLimiter<String> rateLimiter = new RateLimiter<>(10, TimeUnit.MINUTES);

    @Inject
    public DataRepository(@Named("content_json")ZomiaService webService, @Named("content_multipart")ZomiaService webServiceUploadMultipart, ZomiaDb db, FeedDao feedDao, AppExecutors appExecutors) {
        this.db = db;
        this.webService = webService;
        this.webServiceUploadMultipart = webServiceUploadMultipart;
        this.feedDao = feedDao;
        this.appExecutors = appExecutors;
    }

    public ZomiaService getZomiaService()
    {
        return webService;
    }
    public ZomiaService getZomiaServiceUploadMultipart()
    {
        return webServiceUploadMultipart;
    }
    public ZomiaDb getDb()
    {
        return db;
    }
    public FeedDao getFeedDao()
    {
        return feedDao;
    }
    public AppExecutors getAppExecutors()
    {
        return appExecutors;
    }

    public LiveData<Resource<List<Feed>>> loadFeeds() {

        Log.d(TAG, "loadFeeds");

        return new NetworkBoundResource<List<Feed>,ListResponse<Feed>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull ListResponse<Feed> item) {
                //Get list of current feeds in the DB
                List<Feed> feedsInDb = feedDao.getFeedsList();

                //Get list of feeds on the remoteserver
                List<Feed> feedsInRemoteServer = item.getResults();

                //Get feeds to remove
                List<Feed> feedsToRemove = new ArrayList<Feed>(feedsInDb);
                feedsToRemove.removeAll(feedsInRemoteServer);

                db.beginTransaction();
                try {
                    //Delete feed-tags and stories for feeds that are not presented in the remote server
                    for(Feed feed: feedsToRemove) {
                        feedDao.deleteTagFeedPairsByFeedId(feed.getFeedId());
                        feedDao.deleteStoriesInCacheByFeedId(feed.getFeedId());
                        feedDao.deleteStoriesByFeedId(feed.getFeedId());
                    }
                    //Delete feeds that are not presented in the remote server
                    int i = feedDao.deleteFeeds(feedsToRemove);

                    //Insert feeds from server
                    feedDao.insertFeeds(item.getResults());

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Feed> data) {
                //return data == null || data.isEmpty();
                return true;
            }

            @NonNull @Override
            protected LiveData<List<Feed>> loadFromDb() {
                Log.d(TAG, "feedDao.loadFromDb");
                return feedDao.loadAllFeeds();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<ListResponse<Feed>>> createCall() {
                Log.d(TAG, "webService.getFeedsList");
                return webService.getFeedsList();
            }

            @Override
            protected ListResponse<Feed> processResponse(ApiResponse<ListResponse<Feed>> response) {
                ListResponse<Feed> body = response.body;
                return body;
            }

            @Override
            protected void onFetchFailed() {
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Story>>> loadStories(Integer feedId) {

        return new NetworkBoundResource<List<Story>, ListResponse<Story>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull ListResponse<Story> item) {
                //Set feed id while saving to the database
                for (Story story : item.getResults()) {
                    story.setFeedId(feedId);
                }

                db.beginTransaction();
                try {
                    feedDao.insertStories(item.getResults());
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Story> data) {
                //return data == null || data.isEmpty();
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Story>> loadFromDb() {
                return feedDao.loadAllStories(feedId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ListResponse<Story>>> createCall() {
                return webService.getStories(feedId);
            }

            @Override
            protected ListResponse<Story> processResponse(ApiResponse<ListResponse<Story>> response) {
                ListResponse<Story> body = response.body;
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<Story>> loadStory(Integer storyId) {
        return new NetworkBoundResource<Story,Story>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull Story item) {
                return;
            }

            @Override
            protected boolean shouldFetch(@Nullable Story data) {
                return false;
            }

            @NonNull
            @Override
            protected LiveData<Story> loadFromDb() {
                return feedDao.findStoryById(storyId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Story>> createCall() {
                return null;
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<FeedStoriesCount>>> loadFeedStoriesCount() {
        return new NetworkBoundResource<List<FeedStoriesCount>,List<FeedStoriesCount>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<FeedStoriesCount> item) {
                return;
            }

            @Override
            protected boolean shouldFetch(@Nullable List<FeedStoriesCount> data) {
                return false;
            }

            @NonNull
            @Override
            protected LiveData<List<FeedStoriesCount>> loadFromDb() {
                return feedDao.countFeedStoriesTotal();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<FeedStoriesCount>>> createCall() {
                return null;
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Tag>>> loadTags() {

        return new NetworkBoundResource<List<Tag>, ListResponse<TagJson>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull ListResponse<TagJson> item) {

                //Get list of current tags in the DB
                List<Tag> tagsInDb = feedDao.getTagsList();

                //Get list of getTagsList on the remoteserver
                List<Tag> tagsInRemoteServer = new ArrayList<>();
                for(TagJson tagFeed: item.getResults())
                    tagsInRemoteServer.add(new Tag(tagFeed.getId(), tagFeed.getName()));

                //Get tags to remove
                List<Tag> tagsToRemove = new ArrayList<Tag>(tagsInDb);
                tagsToRemove.removeAll(tagsInRemoteServer);

                db.beginTransaction();
                try {
                    //Delete tag-feed pairs for removed tags
                    for(Tag tag: tagsToRemove)
                        feedDao.deleteTagFeedPairsByTagId(tag.getTagId());

                    //Delete removed tags
                    feedDao.deleteTags(tagsToRemove);

                    //Save tags from remote server to db
                    feedDao.insertTags(tagsInRemoteServer);

                    //Save tags and feeds join data
                    for(TagJson tagFeed: item.getResults()) {
                        //Save tags and feeds join data
                        for (int i = 0; i < tagFeed.getFeedsId().size(); i++)
                            feedDao.insertTagFeedJoin(new TagFeedJoin(tagFeed.getId(), tagFeed.getFeedsId().get(i)));
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Tag> data) {
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Tag>> loadFromDb() {
                return feedDao.getTags();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ListResponse<TagJson>>> createCall() {
                return webService.getTagsList();
            }

            @Override
            protected ListResponse<TagJson> processResponse(ApiResponse<ListResponse<TagJson>> response) {
                ListResponse<TagJson> body = response.body;
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<TagFeedPair>>> getFeedsWithTags() {
        return new NetworkBoundResource<List<TagFeedPair>,List<TagFeedPair>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<TagFeedPair> item) {
                return;
            }

            @Override
            protected boolean shouldFetch(@Nullable List<TagFeedPair> data) {
                return false;
            }

            @NonNull
            @Override
            protected LiveData<List<TagFeedPair>> loadFromDb() {
                return feedDao.getFeedsWithTags();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<TagFeedPair>>> createCall() {
                return null;
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Feed>>> getFeedsWithNoTag() {
        return new NetworkBoundResource<List<Feed>,List<Feed>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Feed> item) {
                return;
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Feed> data) {
                return false;
            }

            @NonNull
            @Override
            protected LiveData<List<Feed>> loadFromDb() {
                return feedDao.getFeedsWithNoTag();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Feed>>> createCall() {
                return null;
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> insertNewTag(String tagName) {
        InsertNewTagTask insertNewTask = new InsertNewTagTask(
                tagName, webService, feedDao, db);

        appExecutors.networkIO().execute(insertNewTask);
        return insertNewTask.getLiveData();
    }

    public LiveData<Resource<Boolean>> updateTag(String oldTagName, String newTagName) {
        UpdateTagTask updateTagTask = new UpdateTagTask(
                oldTagName, newTagName, webService, feedDao, db);

        appExecutors.networkIO().execute(updateTagTask);
        return updateTagTask.getLiveData();
    }

    public LiveData<Resource<Boolean>> insertNewFeed(String feedUrl, String tag) {
        InsertNewFeedTask insertNeweFeedTask = new InsertNewFeedTask(
                feedUrl, tag, webService, feedDao, db);

        appExecutors.networkIO().execute(insertNeweFeedTask);
        return insertNeweFeedTask.getLiveData();
    }

    public LiveData<Resource<Boolean>> updateFeed(Feed feed) {
        UpdateFeedTask updateFeedTask = new UpdateFeedTask(
                feed, webService, feedDao, db);

        appExecutors.networkIO().execute(updateFeedTask);
        return updateFeedTask.getLiveData();
    }

    public LiveData<Resource<Boolean>> deleteFeed(Integer feedId) {
        DeleteFeedTask task = new DeleteFeedTask(
                feedId, webService, feedDao, db);

        appExecutors.networkIO().execute(task);
        return task.getResultState();
    }

    public LiveData<Resource<Boolean>> deleteTag(String tagName) {
        DeleteTagTask task = new DeleteTagTask(
                tagName, webService, feedDao, db);

        appExecutors.networkIO().execute(task);
        return task.getResultState();
    }

    public LiveData<Resource<Boolean>> updateStory(int feedId, int storyId, int status) {
        UpdateStoryStatusTask updateStoryTask = new UpdateStoryStatusTask(
                feedId, storyId, status, webService, feedDao, db);

        appExecutors.networkIO().execute(updateStoryTask);
        return updateStoryTask.getLiveData();
    }

    public LiveData<Resource<Boolean>> importOpml(ContentResolver contentResolver, Uri fileUri) {
        UploadFileTask uploadOpmlFile = new UploadFileTask(
                contentResolver, fileUri, webServiceUploadMultipart);

        appExecutors.networkIO().execute(uploadOpmlFile);
        return uploadOpmlFile.getLiveData();
    }

    public void deleteAllData()
    {
        appExecutors.diskIO().execute(new Runnable(){
            @Override
            public void run() {
                db.beginTransaction();
                try {
                    feedDao.deleteTableStoryCache();
                    feedDao.deleteTableStories();
                    feedDao.deleteTableTagFeedJoin();
                    feedDao.deleteTableTag();
                    feedDao.deleteTableFeed();
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        });
    }
}
