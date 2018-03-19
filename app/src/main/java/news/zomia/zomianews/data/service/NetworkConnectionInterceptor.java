package news.zomia.zomianews.data.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Andrey on 19.03.2018.
 */

public abstract class NetworkConnectionInterceptor implements Interceptor {

    public abstract boolean isNetworkAvailable();
    public abstract void onNetworkUnavailable();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!isNetworkAvailable()) {
            onNetworkUnavailable();
        }
        return chain.proceed(request);
    }
}