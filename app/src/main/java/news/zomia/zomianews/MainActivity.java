package news.zomia.zomianews;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.Result;
import  news.zomia.zomianews.data.service.APIService;
import news.zomia.zomianews.data.service.ApiUtils;
import news.zomia.zomianews.fragments.FeedStoriesFragment;
import news.zomia.zomianews.fragments.FeedsListFragment;
import news.zomia.zomianews.fragments.LoginFragment;
import news.zomia.zomianews.fragments.NewFeedFragment;
import news.zomia.zomianews.fragments.SettingsFragment;
import news.zomia.zomianews.fragments.StoryViewerFragment;

import news.zomia.zomianews.data.model.User;
import news.zomia.zomianews.data.model.Token;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnSuccessAuthorizationListener,
        FeedsListFragment.OnFeedsListListener,
        FeedStoriesFragment.OnStorySelectedListener,
        NewFeedFragment.OnFeedAddedListener,
        GestureDetector.OnGestureListener,
        SettingsFragment.OnSettingsChangedListener
        //,GestureDetector.OnDoubleTapListener
{

    private TextView mResponse;
    private APIService apiService;
    public User user;
    public Token userToken;
    private static final String TAG = "ZomiaMainActivity";

    LoginFragment loginFragment;
    private GestureDetectorCompat gestureDetector;

    FeedStoriesFragment feedStoriesFragment;
    StoryViewerFragment storyViewerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();
        // Enable the Up button
        actionBar.setDisplayHomeAsUpEnabled(true);

        gestureDetector = new GestureDetectorCompat(this,this);

        loginFragment = new LoginFragment();

        feedStoriesFragment = new FeedStoriesFragment();
        storyViewerFragment = new StoryViewerFragment();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String token = sharedPref.getString(getString(R.string.preferences_token), "");
        ApiUtils.setAccessToken(token);

        updateZomiaUrl();

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

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.preferences_serverAddress), userToken.getToken());
        editor.commit();

        ApiUtils.setAccessToken(userToken.getToken());

        LoadFeedsListFragment();
    }

    public void onFeedAdded()
    {
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

    public void onNewFeedAddAction()
    {
        if (findViewById(R.id.fragment_container) != null) {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            NewFeedFragment newFeedFragment = new NewFeedFragment();
            fragmentTransaction.replace(R.id.fragment_container, newFeedFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    public void onStorySelected(Result story)
    {
        if (findViewById(R.id.fragment_container) != null) {

            ShowToolbar();

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

    public void onSettingsUpdated(String key)
    {
        if (key.equals(getString(R.string.preferences_serverAddress)) ||
                key.equals(getString(R.string.preferences_serverPort)))
        {
            updateZomiaUrl();
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

            FeedsListFragment feedsListFragment = new FeedsListFragment();
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
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                SettingsFragment settingsFragment = new SettingsFragment();
                fragmentTransaction.replace(R.id.fragment_container, settingsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
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

    private void updateZomiaUrl()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String serverAddress = sharedPref.getString(getString(R.string.preferences_serverAddress), getString(R.string.preferences_serverAddress_default));
        String serverPort = sharedPref.getString(getString(R.string.preferences_serverPort), getString(R.string.preferences_serverPort_default));

        String url = "http://" + serverAddress + ":" + serverPort + "/";
        ApiUtils.updateBaseUrl(url);

        Log.d(TAG, "Updated url: " + url);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ShowToolbar();
    }

    private void ShowToolbar()
    {
        AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
        appBar.setExpanded(true, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gestureDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(TAG, "onFling: " + event1.toString() + event2.toString());

        if (event1.getX() < event2.getX()) {
            Log.d(TAG, "Left to Right swipe performed");
        }

        if (event1.getX() > event2.getX()) {
            Log.d(TAG, "Right to Left swipe performed");

            if (findViewById(R.id.fragment_container) != null) {

                FeedsListFragment feedsListFragment = new FeedsListFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);

                fragmentTransaction.replace(R.id.fragment_container, feedsListFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }

        if (event1.getY() < event2.getY()) {
            Log.d(TAG, "Up to Down swipe performed");
        }

        if (event1.getY() > event2.getY()) {
            Log.d(TAG, "Down to Up swipe performed");
        }

        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        Log.d(TAG, "onScroll: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(TAG, "onSingleTapUp: " + event.toString());
        return true;
    }


    /*@Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }*/

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }
}
