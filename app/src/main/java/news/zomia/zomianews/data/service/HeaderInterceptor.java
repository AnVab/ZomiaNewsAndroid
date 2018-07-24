package news.zomia.zomianews.data.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HeaderInterceptor implements Interceptor {
    private static HeaderInterceptor instance = null;
    UserSessionInfo userSessionInfo;
    public HeaderInterceptor(){
        userSessionInfo = UserSessionInfo.getInstance();
    }

    public static HeaderInterceptor getInstance() {
        if (instance == null) {
            instance = new HeaderInterceptor();
        }
        return instance;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
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
}
