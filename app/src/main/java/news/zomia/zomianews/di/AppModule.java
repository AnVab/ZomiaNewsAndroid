package news.zomia.zomianews.di;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.db.ZomiaDb;
import news.zomia.zomianews.data.service.UserSessionInfo;
import news.zomia.zomianews.data.service.ZomiaService;
import news.zomia.zomianews.data.service.HostSelectionInterceptor;
import news.zomia.zomianews.data.util.LiveDataCallAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Andrey on 13.01.2018.
 */

/* providers for all application scope components is defined here */

@Module
public class AppModule {

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton
    UserSessionInfo provideUserSessionInfo()
    {
        return new UserSessionInfo();
    }

    @Provides
    @Singleton
    Interceptor provideHeaderInterceptor(Application application, SharedPreferences prefs, UserSessionInfo userSessionInfo)
    {
        String token = prefs.getString(application.getString(R.string.preferences_token), "");

        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                //getAccessToken is your own accessToken(retrieve it by saving in shared preference or any other option )
                if(userSessionInfo.isEmpty()){
                    //Authorization header is already present or token is empty
                    return chain.proceed(chain.request());
                }

                Request authorisedRequest = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", userSessionInfo.getTokenAuthValue()).build();

                //Authorization header is added to the url
                return chain.proceed(authorisedRequest);
            }
        };

        return headerInterceptor;
    }

    @Provides
    @Singleton
    HttpLoggingInterceptor provideHttpLoggingInterceptor()
    {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return loggingInterceptor;
    }

    @Provides
    @Singleton
    HostSelectionInterceptor provideHostSelectionInterceptor()
    {
        return new HostSelectionInterceptor();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkhttpClient(Interceptor headerInterceptor, HostSelectionInterceptor urlInterceptor, HttpLoggingInterceptor loggingInterceptor) {

        OkHttpClient.Builder defaultHttpClient = new OkHttpClient.Builder()
                //.addInterceptor(loggingInterceptor)
                .addInterceptor(headerInterceptor)
                .addInterceptor(urlInterceptor);

        return defaultHttpClient.build();
    }

    @Singleton @Provides
    ZomiaService provideZomiaService(Application application, SharedPreferences prefs, OkHttpClient okHttpClient) {

        return new Retrofit.Builder()
                .baseUrl("http://localhost/") // Dummy baseUrl is needed to create instance
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build()
                .create(ZomiaService.class);
    }

    @Singleton @Provides
    ZomiaDb provideDb(Application app) {
        return Room.databaseBuilder(app, ZomiaDb.class,"zomianews.db").build();
    }

    @Singleton @Provides
    FeedDao provideFeedDao(ZomiaDb db) {
        return db.feedDao();
    }
}
