package news.zomia.zomianews.data.service.tasks;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import news.zomia.zomianews.data.service.ApiResponse;
import news.zomia.zomianews.data.service.InputStreamRequestBody;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.service.ZomiaService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by Andrey on 13.03.2018.
 */

public class UploadFileTask implements Runnable {

    private final MutableLiveData<Resource<Boolean>> resultState = new MutableLiveData<>();

    private final Uri fileUri;
    private final ZomiaService zomiaService;
    private final ContentResolver contentResolver;

    public UploadFileTask(ContentResolver contentResolver, Uri fileUri, ZomiaService zomiaService) {
        this.fileUri = fileUri;
        this.contentResolver = contentResolver;
        this.zomiaService = zomiaService;
    }

    @Override
    public void run() {
        InputStreamRequestBody inputStreamRequestBody = null;

        try {
            inputStreamRequestBody = new InputStreamRequestBody(
            MediaType.parse("multipart/form-data"), contentResolver, fileUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        File file = new File(fileUri.getPath());

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", file.getName(), inputStreamRequestBody);

        Response<ResponseBody> response = null;
        try {
            response = zomiaService.importOpml(body).execute();

            ApiResponse<ResponseBody> apiResponse = new ApiResponse<>(response);
            if (apiResponse.isSuccessful()) {
                resultState.postValue(Resource.success(apiResponse.body != null));
            } else {
                resultState.postValue(Resource.error(apiResponse.errorMessage, true));
            }

        } catch (IOException e) {
            e.printStackTrace();
            resultState.postValue(Resource.error(e.getMessage(), true));
        }
    }

    public LiveData<Resource<Boolean>> getLiveData() {
        return resultState;
    }
}