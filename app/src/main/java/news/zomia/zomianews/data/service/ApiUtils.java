package news.zomia.zomianews.data.service;

/**
 * Created by Andrey on 26.12.2017.
 */

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "http://127.0.0.1:8000/api/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}