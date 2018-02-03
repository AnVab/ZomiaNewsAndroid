package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedPair;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;

/**
 * Created by Andrey on 10.01.2018.
 */

public class FeedViewModel extends ViewModel {
    DataRepository dataRepo;
    private LiveData<Resource<List<Feed>>> feeds;

    private LiveData<Resource<List<FeedStoriesCount>>> feedStoriesCount;

    private LiveData<Resource<List<Tag>>> tags;

    private LiveData<Resource<Boolean>> tagInsertLiveData;

    @Inject // DataRepository parameter is provided by Dagger 2
    public FeedViewModel(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
        feeds = dataRepo.loadFeeds();
        tags = dataRepo.loadTags();

        feedStoriesCount = dataRepo.loadFeedStoriesCount();
    }

    public LiveData<Resource<List<Feed>>> getFeeds() {
        return feeds;
    }

    public LiveData<Resource<List<Tag>>> getTags() {
        return tags;
    }

    public void refresh() {
        feeds = dataRepo.loadFeeds();
        tags = dataRepo.loadTags();
    }

    public LiveData<Resource<List<FeedStoriesCount>>> getFeedStoriesCount() {
        return feedStoriesCount;
    }

    public LiveData<Resource<List<TagFeedPair>>> getFeedsWithTags() {
        return dataRepo.getFeedsWithTags();
    }

    public LiveData<Resource<List<Feed>>> getFeedsWithNoTag() {
        return dataRepo.getFeedsWithNoTag();
    }

    public LiveData<Resource<Boolean>> insertNewTag(String tagName) {
        return dataRepo.insertNewTag(tagName);
    }

    public LiveData<Resource<Boolean>> insertNewFeed(String feedUrl, String tag) {
        return dataRepo.insertNewFeed(feedUrl, tag);
    }
}
