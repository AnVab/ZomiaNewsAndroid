package news.zomia.zomianews;

import android.app.Activity;
import android.app.Application;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import news.zomia.zomianews.data.service.UnauthorizedInterceptorListener;
import news.zomia.zomianews.di.AppInjector;

/**
 * Created by Andrey on 13.01.2018.
 */

public class ZomiaApp extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    public UnauthorizedInterceptorListener unauthorizedInterceptorListener;

    @Override
    public void onCreate() {
        super.onCreate();

        AppInjector.init(this);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    public void setUnauthorizedInterceptorListener(UnauthorizedInterceptorListener listener) {
        unauthorizedInterceptorListener = listener;
    }

    public void removeUnauthorizedInterceptorListener() {
        unauthorizedInterceptorListener = null;
    }
}
