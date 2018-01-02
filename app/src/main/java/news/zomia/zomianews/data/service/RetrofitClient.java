package news.zomia.zomianews.data.service;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Interceptor;

/**
 * Created by Andrey on 26.12.2017.
 */

public class RetrofitClient {

    private static final String TAG = "ZomiaRetrofitClient";
    private static Retrofit retrofit = null;
    private static String token = "";

    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {

            OkHttpClient defaultHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            //getAccessToken is your own accessToken(retrieve it by saving in shared preference or any other option )
                            if(getAccessToken().isEmpty()){
                                //Authorization header is already present or token is empty
                                return chain.proceed(chain.request());
                            }
                            Request authorisedRequest = chain.request().newBuilder()
                                    .addHeader("Authorization", getAccessToken()).build();
                            //Authorization header is added to the url
                            return chain.proceed(authorisedRequest);
                        }}).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(defaultHttpClient)
                    .build();
        }
        return retrofit;
    }

    public static String getAccessToken()
    {
       return token;
    }

    public static void setAccessToken(String tokenValue)
    {
        token = tokenValue;
    }
}
