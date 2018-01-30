package news.zomia.zomianews.data.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Result;
import news.zomia.zomianews.data.model.StoriesResponse;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedJoin;
import news.zomia.zomianews.data.model.TagFeedPair;
import news.zomia.zomianews.data.model.TagJson;
import news.zomia.zomianews.data.model.TagsResponse;
import news.zomia.zomianews.data.util.AbsentLiveData;
import news.zomia.zomianews.data.util.AppExecutors;
import news.zomia.zomianews.data.util.RateLimiter;

/**
 * Created by Andrey on 10.01.2018.
 */

@Singleton
public class DataRepository {
    private final ZomiaDb db;

    private final ZomiaService webService;

    private final FeedDao feedDao;

    private final AppExecutors appExecutors;

    private RateLimiter<String> rateLimiter = new RateLimiter<>(10, TimeUnit.MINUTES);

    @Inject
    public DataRepository(ZomiaService webService, ZomiaDb db, FeedDao feedDao, AppExecutors appExecutors) {
        this.db = db;
        this.webService = webService;
        this.feedDao = feedDao;
        this.appExecutors = appExecutors;
    }

    public ZomiaService getZomiaService()
    {
        return webService;
    }
    public LiveData<Resource<List<Feed>>> loadFeeds() {

        Log.d("ZOMIA", "loadFeeds");

        return new NetworkBoundResource<List<Feed>,List<Feed>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Feed> item) {
                feedDao.insertFeeds(item);
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Feed> data) {
                //return data == null || data.isEmpty();
                return true;
            }

            @NonNull @Override
            protected LiveData<List<Feed>> loadFromDb() {
                Log.d("ZOMIA", "feedDao.loadFromDb");
                return feedDao.loadAllFeeds();
            }

            @NonNull @Override
            protected LiveData<ApiResponse<List<Feed>>> createCall() {
                Log.d("ZOMIA", "webService.getFeedsList");
                return webService.getFeedsList();
            }

            @Override
            protected void onFetchFailed() {
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Result>>> loadStories(Integer feedId) {

        return new NetworkBoundResource<List<Result>, StoriesResponse>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull StoriesResponse item) {
                //Set feed id while saving to the database
                for (Result story : item.getResults()) {
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
            protected boolean shouldFetch(@Nullable List<Result> data) {
                //return data == null || data.isEmpty();
                return true;
            }

            @NonNull
            @Override
            protected LiveData<List<Result>> loadFromDb() {
                return feedDao.loadAllStories(feedId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<StoriesResponse>> createCall() {
                return webService.getStories(feedId);
            }

            @Override
            protected StoriesResponse processResponse(ApiResponse<StoriesResponse> response) {
                StoriesResponse body = response.body;
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<Result>> loadStory(Integer storyId) {
        return new NetworkBoundResource<Result,Result>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull Result item) {
                return;
            }

            @Override
            protected boolean shouldFetch(@Nullable Result data) {
                return false;
            }

            @NonNull
            @Override
            protected LiveData<Result> loadFromDb() {
                return feedDao.findStoryById(storyId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<Result>> createCall() {
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

        return new NetworkBoundResource<List<Tag>, TagsResponse>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull TagsResponse item) {

                //Save tags
                for(TagJson tagFeed: item.getResults())
                {
                    db.beginTransaction();
                    try {
                        //Save tag to db
                        feedDao.insertTag(new Tag(tagFeed.getId(), tagFeed.getName()));

                        //Save tags and feeds join data
                        for(int i = 0; i < tagFeed.getFeedsId().size(); i++)
                            feedDao.insertTagFeedJoin(new TagFeedJoin(tagFeed.getId(), tagFeed.getFeedsId().get(i)));

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }
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
            protected LiveData<ApiResponse<TagsResponse>> createCall() {
                return webService.getTagsList();
            }

            @Override
            protected TagsResponse processResponse(ApiResponse<TagsResponse> response) {
                TagsResponse body = response.body;
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
}
