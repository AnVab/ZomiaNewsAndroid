package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import news.zomia.zomianews.data.service.DataRepository;

/**
 * Created by Andrey on 13.01.2018.
 */

@Singleton
public class FeedViewModelFactory implements ViewModelProvider.Factory {
    private final DataRepository dataRepo;

    @Inject
    public FeedViewModelFactory(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(FeedViewModel.class)) {
            return (T) new FeedViewModel(this.dataRepo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
