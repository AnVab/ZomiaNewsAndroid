package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Tag;
import news.zomia.zomianews.data.model.TagFeedPair;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.util.AbsentLiveData;

/**
 * Created by Andrey on 10.01.2018.
 */

public class FeedViewModel extends ViewModel {
    DataRepository dataRepo;

    private final MutableLiveData<Boolean> refreshFeeds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> refreshTags = new MutableLiveData<>();

    private LiveData<Resource<List<Feed>>> feeds;
    private LiveData<Resource<List<FeedStoriesCount>>> feedStoriesCount;
    private LiveData<Resource<List<Tag>>> tags;
    private LiveData<Resource<List<TagFeedPair>>> tagFeedPairs;
    private LiveData<Resource<List<Feed>>> feedsWithNoTag;
    private LiveData<Resource<Boolean>> tagInsertLiveData;

    private MutableLiveData<Feed> selectedCurrentFeed = new MutableLiveData<>();

    @Inject // DataRepository parameter is provided by Dagger 2
    public FeedViewModel(DataRepository dataRepo) {
        this.dataRepo = dataRepo;

        feeds = Transformations.switchMap(refreshFeeds, data -> {
            if (data == null) {
                return AbsentLiveData.create();
            } else {
                return dataRepo.loadFeeds();
            }
        });

        tags = Transformations.switchMap(refreshTags, data -> {
            if (data == null) {
                return AbsentLiveData.create();
            } else {
                return dataRepo.loadTags();
            }
        });

        feedStoriesCount = Transformations.switchMap(refreshFeeds, data -> {
            if (data == null) {
                return AbsentLiveData.create();
            } else {
                return dataRepo.loadFeedStoriesCount();
            }
        });

        tagFeedPairs = dataRepo.getFeedsWithTags();
        feedsWithNoTag = dataRepo.getFeedsWithNoTag();

        //Set flags to receive new data
        refreshFeeds.setValue(true);
        refreshTags.setValue(true);
    }

    public LiveData<Resource<List<Feed>>> getFeeds() {
        return feeds;
    }

    public LiveData<Resource<List<Tag>>> getTags() {
        return tags;
    }

    public void refreshFeeds() {
        if (refreshFeeds.getValue() != null) {
            refreshFeeds.setValue(refreshFeeds.getValue());
        }
    }

    public void refreshTags() {
        if (refreshTags.getValue() != null) {
            refreshTags.setValue(refreshTags.getValue());
        }
    }

    public void refreshAll()
    {
        refreshFeeds();
        refreshTags();
    }

    public LiveData<Resource<List<FeedStoriesCount>>> getFeedStoriesCount() {
        return feedStoriesCount;
    }

    public LiveData<Resource<List<TagFeedPair>>> getFeedsWithTags() {
        return tagFeedPairs;
    }

    public LiveData<Resource<List<Feed>>> getFeedsWithNoTag() {
        return feedsWithNoTag;
    }

    public LiveData<Resource<Boolean>> insertNewTag(String tagName) {
        return dataRepo.insertNewTag(tagName);
    }

    public LiveData<Resource<Boolean>> updateTag(String oldTagName, String newTagName) {
        return dataRepo.updateTag(oldTagName, newTagName);
    }

    public LiveData<Resource<Boolean>> insertNewFeed(String feedUrl, String tag) {
        return dataRepo.insertNewFeed(feedUrl, tag);
    }

    public LiveData<Resource<Boolean>> updateFeed(Feed feed) {
        return dataRepo.updateFeed(feed);
    }

    public LiveData<Resource<Boolean>> deleteFeed(Integer feedId) {
        return dataRepo.deleteFeed(feedId);
    }

    public LiveData<Resource<Boolean>> deleteTag(String tagName) {
        return dataRepo.deleteTag(tagName);
    }

    public void setSelectedFeed(@NonNull Feed feed) {
        selectedCurrentFeed.setValue(feed);
    }

    public LiveData<Feed> getSelectedFeedId() {
        return selectedCurrentFeed;
    }

    public List<Tag> getTagsForCurrentFeed()
    {
        List<Tag> listTags = new ArrayList<>();

        for(TagFeedPair tagFeedPair: tagFeedPairs.getValue().data) {
            if(selectedCurrentFeed != null && selectedCurrentFeed.getValue() != null && tagFeedPair.feed.getFeedId() == selectedCurrentFeed.getValue().getFeedId())
                listTags.add(tagFeedPair.tag);
        }

        return listTags;
    }
}
