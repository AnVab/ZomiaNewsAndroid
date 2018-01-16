package news.zomia.zomianews.data.service;

/**
 * Created by Andrey on 26.12.2017.
 */

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "http://10.0.2.2:8000/";

    public static ZomiaService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(ZomiaService.class);
    }

    public static String getAccessToken()
    {
        return RetrofitClient.getAccessToken();
    }

    public static void setAccessToken(String tokenValue)
    {
        RetrofitClient.setAccessToken(tokenValue);
    }

    public static void updateBaseUrl(String url)
    {
        RetrofitClient.updateUrl(url);
    }
}