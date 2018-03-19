package news.zomia.zomianews.data.service;

import android.util.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Andrey on 19.03.2018.
 */

public abstract class UnauthorizedInterceptor implements Interceptor {
    private static final String TAG = UnauthorizedInterceptor.class.getSimpleName();

    public abstract void onUnauthorizedEvent();

    public UnauthorizedInterceptor(){
        //Intentionally left blank
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Response response = chain.proceed(request);
        boolean unauthorized = response.code() == 401;
        if (unauthorized) {
            onUnauthorizedEvent();
            return response;
        }

        return response;
    }
}