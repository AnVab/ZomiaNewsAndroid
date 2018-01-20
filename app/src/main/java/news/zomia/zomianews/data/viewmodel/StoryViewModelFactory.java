package news.zomia.zomianews.data.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import news.zomia.zomianews.data.service.DataRepository;

/**
 * Created by Andrey on 18.01.2018.
 */

@Singleton
public class StoryViewModelFactory  implements ViewModelProvider.Factory {
    private final DataRepository dataRepo;

    @Inject
    public StoryViewModelFactory(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StoryViewModel.class)) {
            return (T) new StoryViewModel(this.dataRepo);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}