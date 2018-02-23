package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.NetworkState;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.StoryBoundaryCallback;
import news.zomia.zomianews.data.service.StoryStatus;
import news.zomia.zomianews.data.util.AbsentLiveData;
import news.zomia.zomianews.data.util.Objects;
import okhttp3.Interceptor;

/**
 * Created by Andrey on 17.01.2018.
 */

public class StoryViewModel  extends ViewModel {
    DataRepository dataRepo;

    private MutableLiveData<Integer> selectedFeedId = new MutableLiveData<>();
    private LiveData<PagedList<Story>> stories;
    private MutableLiveData<Integer> selectedCurrentStory = new MutableLiveData<>();
    private LiveData<Resource<Story>> currentStory = null;
    private Story previousStory = null;

    public LiveData<NetworkState> networkState;

    StoryBoundaryCallback storyBoundaryCallback;

    private final UpdateStoryHandler updateCurrentStoryHandler;
    private final UpdateStoryHandler updatePreviousStoryHandler;

    @Inject // DataRepository parameter is provided by Dagger 2
    public StoryViewModel(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
        updateCurrentStoryHandler = new UpdateStoryHandler(this.dataRepo);
        updatePreviousStoryHandler = new UpdateStoryHandler(this.dataRepo);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder()).setEnablePlaceholders(false)
                        .setPrefetchDistance(10)
                        .setPageSize(10).build();

        storyBoundaryCallback = new StoryBoundaryCallback(dataRepo.getZomiaService(), dataRepo.getDb(), dataRepo.getFeedDao(), dataRepo.getAppExecutors());
        networkState = storyBoundaryCallback.getNetworkState();

        stories = Transformations.switchMap(selectedFeedId, result -> {
                storyBoundaryCallback.setSelectedFeedId(result);
                return (new LivePagedListBuilder<>(dataRepo.getFeedDao().loadAllStories2(result), pagedListConfig).setBoundaryCallback(storyBoundaryCallback))
                        .build();
        });

        currentStory = Transformations.switchMap(selectedCurrentStory, result -> {
            if (result == null ) {
                return AbsentLiveData.create();
            } else {

                //Load new story
                Integer index = stories.getValue().get(result).getStoryId();
                if(index >= 0) {
                    LiveData<Resource<Story>> loadedStory = dataRepo.loadStory(index);

                    //Set status of the previoud story to readed
                    if (previousStory != null)
                        updatePreviousStoryHandler.updateStory(previousStory.getFeedId(), previousStory.getStoryId(), StoryStatus.read);

                    //Set status of a new story to reading
                    if (loadedStory.getValue().data != null)
                        updateCurrentStoryHandler.updateStory(loadedStory.getValue().data.getFeedId(), loadedStory.getValue().data.getStoryId(), StoryStatus.reading);

                    return loadedStory;
                }
                else
                    return AbsentLiveData.create();
            }
        });
    }

    public void setCurrentStoryAsRead()
    {
        if(selectedCurrentStory != null && selectedCurrentStory.getValue() != null && selectedCurrentStory.getValue() >= 0)
        {
            Story story = stories.getValue().get(selectedCurrentStory.getValue());
            if (story != null)
                updateCurrentStoryHandler.updateStory(story.getFeedId(), story.getStoryId(), StoryStatus.read);

        }
    }

    public LiveData<PagedList<Story>> getStories() {
        return stories;
    }

    public void setFeedId(@NonNull Integer feedId) {
        selectedFeedId.setValue(feedId);
    }

    public void refresh() {
        if(storyBoundaryCallback != null)
            storyBoundaryCallback.refresh();
    }

    public void setCurrentStoryPosition(@NonNull Integer storyListPosition) {
        //Save id of previous Id
        if(currentStory.getValue() != null && currentStory.getValue().data != null) {
            previousStory = currentStory.getValue().data;
            updateCurrentStoryHandler.reset();
            updatePreviousStoryHandler.reset();
        }
        //Select a new current story
        selectedCurrentStory.setValue(storyListPosition);
    }

    public void goToNextCurrentStoryPosition() {

        Integer newValue = selectedCurrentStory.getValue() - 1;
        if(newValue < stories.getValue().size())
            selectedCurrentStory.setValue(newValue);
        else
            selectedCurrentStory.setValue(0);
    }

    public LiveData<Resource<Story>> getCurrentStory() {
        return currentStory;
    }


    @VisibleForTesting
    static class UpdateStoryHandler implements Observer<Resource<Boolean>> {
        @Nullable
        private LiveData<Resource<Boolean>> updateStoryStatusData;

        private Integer feedId;
        private Integer storyId;
        private StoryStatus status;

        private final DataRepository repository;

        @VisibleForTesting
        UpdateStoryHandler(DataRepository repository) {
            this.repository = repository;
            reset();
        }

        void updateStory(Integer feedId, Integer storyId, StoryStatus status) {
            if (Objects.equals(this.storyId, storyId)) {
                return;
            }
            unregister();
            this.feedId = feedId;
            this.storyId = storyId;
            this.status = status;
            updateStoryStatusData = repository.updateStory(this.feedId, this.storyId, this.status);

            //noinspection ConstantConditions
            updateStoryStatusData.observeForever(this);
        }

        @Override
        public void onChanged(@Nullable Resource<Boolean> result) {
            if (result == null) {
                reset();
            } else {
                switch (result.status) {
                    case SUCCESS:
                        unregister();
                        Log.d("ZOMIA", "Story updated successfully. Id: " + storyId + " status: " + status.name());
                        break;
                    case ERROR:
                        unregister();
                        //show error message
                        Log.d("ZOMIA", "Error while updating story: " + result.message);
                        break;
                }
            }
        }

        private void unregister() {
            if (updateStoryStatusData != null) {
                updateStoryStatusData.removeObserver(this);
                updateStoryStatusData = null;
            }
        }

        private void reset() {
            unregister();
        }
    }
}
