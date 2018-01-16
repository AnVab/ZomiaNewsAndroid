package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;

/**
 * Created by Andrey on 10.01.2018.
 */

public class FeedViewModel extends ViewModel {
    DataRepository dataRepo;
    private String feedId;
    private LiveData<Resource<List<Feed>>> feeds;


    @Inject // DataRepository parameter is provided by Dagger 2
    public FeedViewModel(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
        feeds = dataRepo.loadFeeds();
    }

    public LiveData<Resource<List<Feed>>> getFeeds() {
       /* if(feeds == null)
        {
            feeds = new MutableLiveData<Resource<List<Feed>>>();
            feeds = dataRepo.loadFeeds();
        }*/
        return feeds;
    }
}
