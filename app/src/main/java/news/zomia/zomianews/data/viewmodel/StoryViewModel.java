package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Result;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.util.AbsentLiveData;
import news.zomia.zomianews.data.util.Objects;

/**
 * Created by Andrey on 17.01.2018.
 */

public class StoryViewModel  extends ViewModel {
    DataRepository dataRepo;

    private MutableLiveData<Integer> selectedFeedId = new MutableLiveData<>();
    private LiveData<Resource<List<Result>>> stories;
    private MutableLiveData<Resource<Result>> currentStory = null;

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

        //stories = dataRepo.loadStories(selectedFeedId.getValue());
    }

    public LiveData<Resource<List<Result>>> getStories() {
        return stories;
    }

    public void setFeedId(@NonNull int feedId) {
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
}
