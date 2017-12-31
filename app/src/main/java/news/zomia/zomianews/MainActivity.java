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


        }
    }

    public void onSuccessAuthorization(Token token) {
        userToken = token;

        if (findViewById(R.id.fragment_container) != null) {

            mFeedsListFragment = new FeedsListFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, mFeedsListFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
