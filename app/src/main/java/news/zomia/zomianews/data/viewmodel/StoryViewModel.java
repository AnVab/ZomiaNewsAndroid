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
import javax.inject.Inject;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.NetworkState;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.StoryBoundaryCallback;
import news.zomia.zomianews.data.service.StoryStatus;
import news.zomia.zomianews.data.util.AbsentLiveData;
import news.zomia.zomianews.data.util.Objects;

import static news.zomia.zomianews.data.service.StoryStatus.read;
import static news.zomia.zomianews.data.service.StoryStatus.reading;

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
                        .setEnablePlaceholders(true)
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
                if(stories.getValue().get(selectedCurrentStory.getValue()) == null)
                    return AbsentLiveData.create();
                //Load new story
                Integer index = stories.getValue().get(selectedCurrentStory.getValue()).getStoryId();
                if(index >= 0)
                    return dataRepo.loadStory(index);
                else
                    return AbsentLiveData.create();
            }
        });
    }

    public void setCurrentStoryAsRead()
    {
        if(selectedCurrentStory != null &&
                selectedCurrentStory.getValue() != null &&
                selectedCurrentStory.getValue() >= 0 &&
                stories != null &&
                stories.getValue() != null &&
                stories.getValue().size() > 0)
        {
            Story story = stories.getValue().get(selectedCurrentStory.getValue());
            setStoryStatus(story, read, true);
        }
    }

    private void setStoryStatus(Story story, int status, boolean useCurrentStoryHandler)
    {
        //Set status of the story
        if (story != null){
            if(useCurrentStoryHandler){
                updateCurrentStoryHandler.reset();
                updateCurrentStoryHandler.updateStory(story.getFeedId(), story.getStoryId(), status);
            }
            else {
                updatePreviousStoryHandler.reset();
                updatePreviousStoryHandler.updateStory(story.getFeedId(), story.getStoryId(), status);
            }
            Log.d("ZOMIA", "Story updated setStoryStatus " + story.getStoryId() + " " + status);
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
        }
        //Select a new current story
        selectedCurrentStory.setValue(storyListPosition);

        //Set status of the previous story to read
        setStoryStatus(previousStory, read, false);

        //Set status of a new story to reading
        Story story = stories.getValue().get(selectedCurrentStory.getValue());
        setStoryStatus(story, reading, true);
    }

    public void goToNextCurrentStoryPosition() {

        //Save id of previous Id
        if(currentStory.getValue() != null && currentStory.getValue().data != null) {
            previousStory = currentStory.getValue().data;
        }

        //Set new story id
        Integer newValue = selectedCurrentStory.getValue() + 1;
        if(newValue < stories.getValue().size()){
            selectedCurrentStory.setValue(newValue);
        }

        //Set status of the previous story to read
        setStoryStatus(previousStory, read, false);

        //Set status of a new story to reading
        Story story = stories.getValue().get(selectedCurrentStory.getValue());
        setStoryStatus(story, reading, true);
    }

    public void goToPrevCurrentStoryPosition() {

        //Save id of previous Id
        if(currentStory.getValue() != null && currentStory.getValue().data != null) {
            previousStory = currentStory.getValue().data;
            updateCurrentStoryHandler.reset();
            updatePreviousStoryHandler.reset();
        }

        //Set new story id
        Integer newValue = selectedCurrentStory.getValue() - 1;
        if( 0 < newValue)
            selectedCurrentStory.setValue(newValue);

        //Set status of the previous story to read
        setStoryStatus(previousStory, read, false);

        //Set status of a new story to reading
        Story story = stories.getValue().get(selectedCurrentStory.getValue());
        setStoryStatus(story, reading, true);
    }

    public LiveData<Resource<Story>> getCurrentStory() {
        return currentStory;
    }

    public LiveData<Integer> getCurrentStoryListPosition() {
        return selectedCurrentStory;
    }

    @VisibleForTesting
    static class UpdateStoryHandler implements Observer<Resource<Boolean>> {
        @Nullable
        private LiveData<Resource<Boolean>> updateStoryStatusData;

        private Integer feedId;
        private Integer storyId;
        private int status;

        private final DataRepository repository;

        @VisibleForTesting
        UpdateStoryHandler(DataRepository repository) {
            this.repository = repository;
            reset();
        }

        void updateStory(Integer feedId, Integer storyId, int status) {
            /*if (Objects.equals(this.storyId, storyId)) {
                return;
            }*/
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
                        Log.d("ZOMIA", "Story updated successfully. Id: " + storyId + " status: " + StoryStatus.getName(status));
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
