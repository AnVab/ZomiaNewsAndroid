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

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener{

    private TextView mResponse;
    private APIService mAPIService;
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

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, mLoginFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public void onSuccessAuthorization(Token token) {
        userToken = token;

        if (findViewById(R.id.fragment_container) != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, mFeedsListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
}
