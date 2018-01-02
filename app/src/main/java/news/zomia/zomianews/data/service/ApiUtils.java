package news.zomia.zomianews.data.service;

/**
 * Created by Andrey on 26.12.2017.
 */

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "http://10.0.2.2:8000/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String getAccessToken()
    {
        return RetrofitClient.getAccessToken();
    }

    public static void setAccessToken(String tokenValue)
    {
        RetrofitClient.setAccessToken(tokenValue);
    }
}