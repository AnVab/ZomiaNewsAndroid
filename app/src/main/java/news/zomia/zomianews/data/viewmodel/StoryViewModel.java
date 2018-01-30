package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.util.AbsentLiveData;

/**
 * Created by Andrey on 17.01.2018.
 */

public class StoryViewModel  extends ViewModel {
    DataRepository dataRepo;

    private MutableLiveData<Integer> selectedFeedId = new MutableLiveData<>();
    private LiveData<Resource<List<Story>>> stories;
    private MutableLiveData<Integer> selectedCurrentStory = new MutableLiveData<>();
    private LiveData<Resource<Story>> currentStory = null;

    @Inject // DataRepository parameter is provided by Dagger 2
    public StoryViewModel(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
        stories = Transformations.switchMap(selectedFeedId, results -> {
            if (results == null ) {
                return AbsentLiveData.create();
            } else {
                return dataRepo.loadStories(selectedFeedId.getValue());
            }
        });

        currentStory = Transformations.switchMap(selectedCurrentStory, result -> {
            if (result == null ) {
                return AbsentLiveData.create();
            } else {
                return dataRepo.loadStory(stories.getValue().data.get(selectedCurrentStory.getValue()).getId());
            }
        });
    }

    public LiveData<Resource<List<Story>>> getStories() {
        return stories;
    }

    public void setFeedId(@NonNull Integer feedId) {
        /*if (Objects.equals(feedId, selectedFeedId.getValue())) {
            return;
        }*/
        selectedFeedId.setValue(feedId);
    }

    public void refresh() {
        if (selectedFeedId.getValue() != null) {
            selectedFeedId.setValue(selectedFeedId.getValue());
        }
    }


    public void setCurrentStoryPosition(@NonNull Integer storyListPosition) {
        selectedCurrentStory.setValue(storyListPosition);
    }

    public void goToNextCurrentStoryPosition() {

        Integer newValue = selectedCurrentStory.getValue() + 1;
        if(newValue < stories.getValue().data.size())
            selectedCurrentStory.setValue(newValue);
        else
            selectedCurrentStory.setValue(0);
    }

    public LiveData<Resource<Story>> getCurrentStory() {
        return currentStory;
    }
}
