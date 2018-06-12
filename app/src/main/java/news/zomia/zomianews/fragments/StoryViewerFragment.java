package news.zomia.zomianews.fragments;

import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.squareup.picasso.Picasso;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import news.zomia.zomianews.R;
import news.zomia.zomianews.customcontrols.NestedScrollViewTouched;
import news.zomia.zomianews.customcontrols.OnSwipeTouchListener;
import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.model.StoryCache;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.viewmodel.StoryViewModel;
import news.zomia.zomianews.data.viewmodel.StoryViewModelFactory;
import news.zomia.zomianews.di.Injectable;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoryViewerFragment extends Fragment
        implements

        LifecycleRegistryOwner,
        Injectable {

    private static final String TAG = "StoryViewerFragment";

    OnStoryViewerListener onStoryViewerListenerCallback;
    OnSwipeTouchListener onSwipeTouchListener;

    private View rootView;
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    StoryViewModelFactory storyViewModelFactory;
    private StoryViewModel storyViewModel;
    @Inject
    SharedPreferences sharedPref;
    @Inject
    DataRepository dataRepo;

    SwipyRefreshLayout swipeRefreshLayout;
    WebView storyPageViewer;
    ImageView expandedImageAppBar;
    TextView appBarStoryTitle;
    TextView appBarStoryDate;
    CollapsingToolbarLayout collapsingToolbar;
    //Web view scrolling states
    public static final int WEBVIEW_SCROLLING = 0;
    public static final int WEBVIEW_AT_BOTTOM = 1;
    public int WEBVIEW_SCROLLING_STATE = WEBVIEW_AT_BOTTOM;

    private Story currentStory;
    private String storyDateText;
    private float bottomRefreshLayoutValue = 0;
    public StoryViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.layout_news_viewer, container, false);

        Bundle arguments = getArguments();
        boolean showArrow = arguments.getBoolean("showArrow", false);
        boolean showBurger = arguments.getBoolean("showBurger", false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.action_toolbar);
        if(showArrow) {
            toolbar.setNavigationIcon(R.drawable.ic_action_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        //Add menu for the toolbar
        toolbar.inflateMenu(R.menu.story_viewer_action_menu);
        toolbar.setOnMenuItemClickListener(onMenuItemClickListener);

        if(!showBurger){
            toolbar.getMenu().findItem(R.id.action_settings).setVisible(false);
            toolbar.getMenu().findItem(R.id.logout).setVisible(false);
        }
        return rootView;
    }

    Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId()){
                case R.id.menu_refresh:
                    loadContent(true);
                    return true;

                case android.R.id.home:
                    getActivity().onBackPressed();
                    return true;

                case R.id.action_settings:
                    onStoryViewerListenerCallback.onSettings();
                    return true;

                case R.id.logout:
                    onStoryViewerListenerCallback.onLogOut();
                    return true;
            }
            return true;
        }
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        expandedImageAppBar = (ImageView) getActivity().findViewById(R.id.expandedImage );
        collapsingToolbar = (CollapsingToolbarLayout) getActivity().findViewById(R.id.collapsing_toolbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            collapsingToolbar.setExpandedTitleTextAppearance(R.style.TransparentText);
        };

        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.TextAppearance_Zomia_Title_Collapsed);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.TextAppearance_Zomia_Title_Expanded);

        collapsingToolbar.setTitle(" ");

        appBarStoryTitle = (TextView) getActivity().findViewById(R.id.appBarStoryTitle );
        appBarStoryDate = (TextView) getActivity().findViewById(R.id.appBarStoryDate );
        /*final Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "font/firasans_regular.otf");
        collapsingToolbar.setCollapsedTitleTypeface(tf);
        collapsingToolbar.setExpandedTitleTypeface(tf);*/

        storyPageViewer = (WebView) view.findViewById(R.id.storyPageViewer );
        swipeRefreshLayout = (SwipyRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(direction == SwipyRefreshLayoutDirection.TOP)
                {
                    if(storyPageViewer != null) {
                        loadContent(true);
                    }
                }
                else
                {
                    //if(WEBVIEW_SCROLLING_STATE == WEBVIEW_AT_BOTTOM) {
                        goToNextNews();
                    //}
                }
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        storyPageViewer.getSettings().setJavaScriptEnabled(true);
        storyPageViewer.getSettings().setDomStorageEnabled(true);
        storyPageViewer.getSettings().setAllowFileAccess(true);
        storyPageViewer.getSettings().setAllowFileAccessFromFileURLs(true);
        storyPageViewer.getSettings().setAllowUniversalAccessFromFileURLs(true);
        //storyPageViewer.getSettings().setLayoutAlgorithm(TEXT_AUTOSIZING);

        storyPageViewer.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        storyPageViewer.setScrollbarFadingEnabled(true);
        storyPageViewer.getSettings().setLoadsImagesAutomatically(true);

        //loadNextGuideline = (Guideline) view.findViewById(R.id.loadNextGuideline );
        //nextStoryLoadProgressBar = (ProgressBar)  view.findViewById(R.id.nextStoryLoadProgressBar);

        onSwipeTouchListener = new OnSwipeTouchListener(getActivity()) {
            @Override
            public void onSwipeLeft() {
                goToNextNews();
            }

            @Override
            public void onSwipeRight() {
                onStoryViewerListenerCallback.goBackToStoriesList();
            }

            @Override
            public void onSwipeUp() {
                /*if(WEBVIEW_SCROLLING_STATE == WEBVIEW_AT_BOTTOM) {
                    goToNextNews();
                }*/
            }

            /*@Override
            public void onScrollValue(float yValue) {
                if(yValue < 0)
                    collapseNextPageBottomProgress();

                if(WEBVIEW_SCROLLING_STATE == WEBVIEW_AT_BOTTOM)
                {
                    if(nextStoryLoadProgressBar.getVisibility() == View.GONE)
                        nextStoryLoadProgressBar.setVisibility(View.VISIBLE);

                    if(yValue < 0)
                        bottomRefreshLayoutValue -= 1;
                    else
                        bottomRefreshLayoutValue += 1;

                    float value = loadNextGuidelineDefaultPercentage - bottomRefreshLayoutValue * 100 / 200/ 30;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) loadNextGuideline.getLayoutParams();
                    params.guidePercent = value;

                    //Log.d(TAG, "onSwipeUpValue: " + yValue + " " + value);
                    loadNextGuideline.setLayoutParams(params);

                    if(value < 0.8f)
                        goToNextNews();
                }
            }*/
        };

        //Get scroll view to detect when we reach end of the webview
        NestedScrollViewTouched nestedScrollView =  (NestedScrollViewTouched) view.findViewById(R.id.nestedScrollView );
        nestedScrollView.setOnTouchListener(onSwipeTouchListener);

        storyPageViewer.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(storyPageViewer, url);

                if(swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                //Scroll to the top
                nestedScrollView.scrollTo(0, 0);

                //Disable scrolling in the middle of action. Or new story will be scrolling after loading.
                nestedScrollView.smoothScrollBy(0,0);

                WEBVIEW_SCROLLING_STATE = WEBVIEW_AT_BOTTOM;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), getString(R.string.page_loading_error) + ": " + description, Toast.LENGTH_SHORT).show();
            }
        });
        storyPageViewer.setOverScrollMode(View.OVER_SCROLL_ALWAYS);

        nestedScrollView.setOnScrollChangeListener((NestedScrollViewTouched.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(v.getChildAt(v.getChildCount() - 1) != null) {

                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    switch(WEBVIEW_SCROLLING_STATE)
                    {
                        case WEBVIEW_SCROLLING:
                            WEBVIEW_SCROLLING_STATE = WEBVIEW_AT_BOTTOM;
                            break;
                        case WEBVIEW_AT_BOTTOM:
                            //Do nothing
                            break;
                        default:
                            WEBVIEW_SCROLLING_STATE = WEBVIEW_SCROLLING;
                            break;
                    }
                }
                else {
                    WEBVIEW_SCROLLING_STATE = WEBVIEW_SCROLLING;

                    //Hide the next page loading indicator
                    //collapseNextPageBottomProgress();
                }
            }
        });

        storyPageViewer.addJavascriptInterface(this, "Android");

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                swipeRefreshLayout.setRefreshing(true);

                if(storyPageViewer != null) {
                    storyPageViewer.reload();
                }
            }
        });
    }

    /*private void collapseNextPageBottomProgress()
    {
        bottomRefreshLayoutValue = 0.0f;
        nextStoryLoadProgressBar.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) loadNextGuideline.getLayoutParams();
        params.guidePercent = loadNextGuidelineDefaultPercentage;
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        storyViewModel = ViewModelProviders.of(getActivity()).get(StoryViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        storyViewModel.getCurrentStory().observe(this, this::ongetCurrentStory);
    }

    @Override
    public void onStop() {
        //Unsubscribe all livedata observers
        storyViewModel.getCurrentStory().removeObservers(this);

        super.onStop();
    }

    @JavascriptInterface
    public void goToNextNews()
    {
        //onStoryViewerListenerCallback.showNextStoryFragment();
        storyViewModel.goToNextCurrentStoryPosition();

       // collapseNextPageBottomProgress();
    }

    private void ongetCurrentStory(Resource<Story> resource) {
        // Update the UI.
        if (resource != null && resource.data != null) {
            currentStory = resource.data;

            loadContent(false);
        }
        else
        {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    public void loadContent(boolean forceUpdate)
    {
        if(storyPageViewer != null && currentStory != null) {

            String storyUrl = currentStory.getImage();
            //Load img from a story. If image not loaded, show default icon.
            if (storyUrl != null && !storyUrl.isEmpty()) {
                //final int radius = 15;
                //final int margin = 0;
                //final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                Picasso.with(getActivity())
                        .load(storyUrl)
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.progress_animation)
                        .error(R.drawable.error_image)
                        //.transform(transformation)
                        .into(expandedImageAppBar);
            }
            //Set image color filter
            expandedImageAppBar.setColorFilter(Color.argb(180,45,74,161));

            //storyPageViewer.startAnimation(slideLeftAnimation);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd MMMM yyyy", Locale.getDefault());
            //Convert timestamp to milliseconds format
            Timestamp tmp = new Timestamp(currentStory.getDate() / 1000);
            Date dateToStr = new Date(tmp.getTime());
            storyDateText = formatter.format(dateToStr);

            appBarStoryTitle.setText(currentStory.getTitle());
            appBarStoryDate.setText(storyDateText);

            String serverUrl = "";
            String serverAddress = sharedPref.getString(getString(R.string.preferences_serverAddress), getString(R.string.preferences_serverAddress_default));
            String serverPort = sharedPref.getString(getString(R.string.preferences_serverPort), getString(R.string.preferences_serverPort_default));

            serverUrl = "http://" + serverAddress + ":" + serverPort;
            new GetPage(serverUrl, currentStory, dataRepo.getFeedDao(), forceUpdate).execute();

        }
    }

    public String getStyledFont(String title,String link, String date, String content) {
        boolean addBodyTagStart = (content != null && !content.toLowerCase().contains("<body>"));
        boolean addBodyTagEnd = (content != null && !content.toLowerCase().contains("</body"));
        boolean linkEnd = (link != null && !link.isEmpty());

        String videoFrame = "";
        if(link != null && !link.isEmpty()) {
            String youtube = "youtube";
            boolean hasYoutubeLink = link.toLowerCase().contains(youtube.toLowerCase());
            //manage youtube link for embedding
            if(hasYoutubeLink) {
                String pattern = "watch\\?v=([^&]*)";
                Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher m = r.matcher(link);
                String youtubeId = "";
                if(m.find() && m.group(1) != null) {
                    youtubeId = m.group(1);
                }
                videoFrame = "<iframe width=\"100%\" height=\"315\"" +
                                "src=\"https://www.youtube.com/embed/" + youtubeId + "\"?autoplay=1>" +
                                "</iframe>";
            }
        }

        return "<style type=\"text/css\">" +
                "@font-face {" +
                "font-family: firasans;" +
                "src: url(\"file:///android_asset/fonts/firasans_regular.ttf\")}" +
                "body {" +
                "font-family: firasans;" +
                "font-size: medium;" +
                "text-align: justify;" +
                "margin: 8px;" +
                "padding: 8px" +
                "}" +
                "a:hover, a:visited, a:link, a:active {" +
                "text-decoration: none;" +
                "}" +
                "img{display: inline;height: auto;max-width: 100%;}"+
                "h2 {" +
                "text-align: left;" +
                "}" +
                "h6 {" +
                "text-align: left;" +
                "}" +
                "</style>" +
                (addBodyTagStart ? "<body>" : "") +
                //"<h2>" +
                //(linkEnd ? "<a href=" + link + ">" : "")+
                //title +
                //(linkEnd ? "</a>" : "") +
                //"</h2>" +
                //"<h6>" + date + "</h6>" +
                (!videoFrame.isEmpty() ? "<div style=\"text-align: center;\">" : "") +
                videoFrame +
                (!videoFrame.isEmpty() ? "</div>" : "") +
                content +
                (addBodyTagEnd ? "</body>" : "");/* +
                "<br />" +
                "<div style=\"text-align: center; padding-top: 30px;\">" +
                "<button onclick=\"Android.goToNextNews();\">" + getString(R.string.go_to_next_story) + "</button>" +
                "</div>";*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity = getActivity();

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        if(activity != null) {
            try {
                onStoryViewerListenerCallback = (OnStoryViewerListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnStoryViewerListener");
            }
        }
    }

    // Container Activity must implement this interface
    public interface OnStoryViewerListener {
        //public void showNextStoryFragment();
        //public void showNextStoryFragmentAnimationRight();
        public void goBackToStoriesList();
        public void onSettings();
        public void onLogOut();
    }

    public class GetPage extends AsyncTask<Void, Void, Void> {
        private String serverUrl;
        private Story currentStory;
        private String dataBody;
        private FeedDao feedDao;
        private boolean forceUpdate;

        public GetPage(String serverUrl, Story currentStory, FeedDao feedDao, boolean forceUpdate)
        {
            this.serverUrl = serverUrl;
            this.currentStory = currentStory;
            this.dataBody = "";
            this.feedDao = feedDao;
            this.forceUpdate = forceUpdate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String contentUrl = "/storage/" + currentStory.getContent();

                //Check if we have a story in cache already. If not, send request to the remote server
                StoryCache storyCache = feedDao.findStoryInCacheByLink(contentUrl);
                if(!forceUpdate && storyCache != null) {
                    dataBody = storyCache.getContent();
                    Log.d("Zomia", "WebView load content from cache");
                }
                else{
                    // Connect to the web site
                    //Document document = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
                    Document document = Jsoup.connect(serverUrl + contentUrl)
                            .timeout(5000)
                            .get();
                    dataBody = document.body().toString();
                    Log.d("Zomia", "WebView load content from web");
                    //Insert page to the cache
                    feedDao.insertStoryToCache(new StoryCache(contentUrl,dataBody, currentStory.getFeedId(), currentStory.getDate()));
                }
            } catch (IOException e) {
               dataBody = e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Set data
            storyPageViewer.loadDataWithBaseURL("", getStyledFont(
                    currentStory.getTitle(),
                    currentStory.getLink(),
                    storyDateText,
                    dataBody), "text/html", "UTF-8", "");
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}