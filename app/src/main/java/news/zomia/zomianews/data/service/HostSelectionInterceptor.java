package news.zomia.zomianews.data.service;

import java.io.IOException;

import javax.inject.Singleton;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Andrey on 16.01.2018.
 */

/**
 * Dynamically change retrofit baseUrl.
 * Use HostSelectionInterceptor.setInterceptor(String) to set a new url
 * */

@Singleton
public final class HostSelectionInterceptor implements Interceptor {
    //private static HostSelectionInterceptor hsInterceptor = null;
    private String host;
    private String scheme;
    private int port;

    public HostSelectionInterceptor(){
        //Intentionally left blank
    }

    /*public static HostSelectionInterceptor get() {
        if (hsInterceptor == null) {

            hsInterceptor = new HostSelectionInterceptor();
        }
        return hsInterceptor;
    }*/

    public void setInterceptor(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        scheme = httpUrl.scheme();
        host = httpUrl.host();
        port = httpUrl.port();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // If new Base URL is properly formatted then replace the old one
        if (scheme != null && host != null) {
            HttpUrl newUrl = original.url().newBuilder()
                    .scheme(scheme)
                    .host(host)
                    .port(port)
                    .build();
            original = original.newBuilder()
                    .url(newUrl)
                    .build();
        }
        return chain.proceed(original);
    }
}
