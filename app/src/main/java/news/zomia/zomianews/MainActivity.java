package news.zomia.zomianews;
import  news.zomia.zomianews.data.service.APIService;
import  news.zomia.zomianews.data.service.ApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Stories;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.UserInfo;
import news.zomia.zomianews.data.model.Token;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.text.TextUtils;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private TextView mResponse;
    private APIService mAPIService;
    public User mUser;
    private static final String TAG = "Zomia";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_stories);

        Button submitBtn = (Button) findViewById(R.id.testButton);
        mAPIService = ApiUtils.getAPIService();
        mUser = new User();

        mResponse = (TextView) findViewById(R.id.responseTextView);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = "1@1.rr";
                String password = "admin123";
                if(!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
                    authorizePostRequest(login, password);
                }
            }
        });
    }

    public void authorizePostRequest(String login, String password) {
        mUser.setEmail(login);
        mUser.setPassword(password);

        mAPIService.authenticateUser(mUser).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                //To get the status code
                //response.code()
                if(response.isSuccessful()) {
                    showResponse(response.body().toString());
                    Log.i(TAG, "post submitted to API." + response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Log.e(TAG, "Unable to submit post to API.");
            }
        });
    }

    public void showResponse(String response) {
        mResponse.setText(response);
    }
}
