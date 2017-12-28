package news.zomia.zomianews;
import  news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.Token;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private TextView mResponse;
    private APIService mAPIService;
    public User mUser;
    public Token mToken;
    private static final String TAG = "Zomia";

    LoginFragment mLoginFragment;
    FeedsListFragment mFeedsListFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }
            mLoginFragment = new LoginFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, mLoginFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            mFeedsListFragment = new FeedsListFragment();
        }

        /*Button submitBtn = (Button) findViewById(R.id.testButton);
        mAPIService = ApiUtils.getAPIService();
        mUser = new User();

        mResponse = (TextView) findViewById(R.id.responseTextView);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String layout_login = "1@1.rr";
                String password = "admin123";
                if(!TextUtils.isEmpty(layout_login) && !TextUtils.isEmpty(password)) {
                    authorizePostRequest(layout_login, password);
                }
            }
        });*/
    }

    public void authorizePostRequest(String login, String password) {
        mUser.setEmail(login);
        mUser.setPassword(password);

        mAPIService.authenticateUser(mUser).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                //To get the status code
                if(response.isSuccessful())
                {
                    switch(response.code())
                    {
                        case 200:
                            //No errors
                            mToken = response.body();
                            showResponse(mToken.toString());
                            Log.i(TAG, "post submitted to API." + mToken.toString());
                            break;
                        default:
                        break;
                    }
                }
                else
                {
                    //Connection problem
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
