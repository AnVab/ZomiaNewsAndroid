package news.zomia.zomianews;
import news.zomia.zomianews.data.model.Feed;
import  news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.Token;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener,
        FeedsListFragment.OnFeedSelectedListener {

    private TextView mResponse;
    private APIService apiService;
    public User user;
    public Token userToken;
    private static final String TAG = "ZomiaMainActivity";

    LoginFragment mLoginFragment;
    FeedsListFragment mFeedsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);

        mLoginFragment = new LoginFragment();
        mFeedsListFragment = new FeedsListFragment();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String token = sharedPref.getString("token", "");
        ApiUtils.setAccessToken(token);

        if (savedInstanceState != null) {
            return;
        }

        if(token.isEmpty())
            LoadLoginFragment();
        else
            LoadFeedsListFragment();
    }

    public void onSuccessAuthorization(Token token) {
        userToken = token;

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", userToken.getToken());
        editor.commit();

        ApiUtils.setAccessToken(userToken.getToken());
    }

    public void onFeedSelected(Feed feed)
    {

    }

    public void LoadLoginFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, mLoginFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public void LoadFeedsListFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, mFeedsListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}
