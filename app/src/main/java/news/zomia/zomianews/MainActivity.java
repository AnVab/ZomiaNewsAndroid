package news.zomia.zomianews;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Result;
import  news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import news.zomia.zomianews.fragments.FeedStoriesFragment;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import news.zomia.zomianews.fragments.StoryViewerFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.Token;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener,
        FeedsListFragment.OnFeedSelectedListener,
        FeedStoriesFragment.OnStorySelectedListener {

    private TextView mResponse;
    private APIService apiService;
    public User user;
    public Token userToken;
    private static final String TAG = "ZomiaMainActivity";

    LoginFragment loginFragment;
    FeedsListFragment feedsListFragment;
    FeedStoriesFragment feedStoriesFragment;
    StoryViewerFragment storyViewerFragment;

    Toolbar myToolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        myToolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        loginFragment = new LoginFragment();
        feedsListFragment = new FeedsListFragment();
        feedStoriesFragment = new FeedStoriesFragment();
        storyViewerFragment = new StoryViewerFragment();

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

        LoadFeedsListFragment();
    }

    public void onFeedSelected(Feed feed)
    {
      /*  FeedStoriesFragment fStoriesFrag = (FeedStoriesFragment)
                getSupportFragmentManager().findFragmentById(R.id.feed_stories_fragment);

        if (fStoriesFrag != null) {
            // If article frag is available, we're in two-pane layout
            // Call a method in the FeedStoriesFragment to update its content
            fStoriesFrag.updateStoriesView(feed.getFeedId());
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags
*/
            if (findViewById(R.id.fragment_container) != null) {
                // Create fragment and give it an argument for the selected article
                Bundle args = new Bundle();
                args.putInt("feedId", feed.getFeedId());
                feedStoriesFragment.setArguments(args);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                fragmentTransaction.replace(R.id.fragment_container, feedStoriesFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
       // }
    }
    public void onStorySelected(Result story)
    {
        if (findViewById(R.id.fragment_container) != null) {

            // Create fragment and give it an argument for the selected article
            Bundle args = new Bundle();
            args.putString("date", story.getDate());
            args.putString("title", story.getTitle());
            args.putString("content", story.getContent());

            storyViewerFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, storyViewerFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }
    public void LoadLoginFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, loginFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public void LoadFeedsListFragment()
    {
        if (findViewById(R.id.fragment_container) != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.fragment_container, feedsListFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.menu_search:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
