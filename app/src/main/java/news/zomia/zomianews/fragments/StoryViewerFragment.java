package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.internal.GwtIncompatible;
import news.zomia.zomianews.R;
import news.zomia.zomianews.customcontrols.OnSwipeTouchListener;
import news.zomia.zomianews.data.db.FeedDao;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.model.StoryCache;
import news.zomia.zomianews.data.service.DataRepository;
import news.zomia.zomianews.data.service.Resource;
import news.zomia.zomianews.data.viewmodel.StoryViewModel;
import news.zomia.zomianews.data.viewmodel.StoryViewModelFactory;
import news.zomia.zomianews.di.Injectable;

import static android.webkit.WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoryViewerFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
        LifecycleRegistryOwner,
        Injectable {

    private static final String TAG = "StoryViewerFragment";

    OnStoryViewerListener onStoryViewerListenerCallback;

    private View rootView;
    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Inject
    StoryViewModelFactory storyViewModelFactory;
    private StoryViewModel storyViewModel;
    @Inject
    SharedPreferences sharedPref;
    @Inject
    DataRepository dataRepo;

    SwipeRefreshLayout swipeRefreshLayout;
    WebView storyPageViewer;
    private Story currentStory;
    private String storyDateText;
    public StoryViewerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.layout_news_viewer, container, false);
        return rootView;
    }

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

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        storyPageViewer = (WebView) view.findViewById(R.id.storyPageViewer );
        storyPageViewer.getSettings().setJavaScriptEnabled(true);
        storyPageViewer.getSettings().setDomStorageEnabled(true);
        storyPageViewer.getSettings().setAllowFileAccess(true);
        storyPageViewer.getSettings().setAllowFileAccessFromFileURLs(true);
        storyPageViewer.getSettings().setAllowUniversalAccessFromFileURLs(true);
        //storyPageViewer.getSettings().setLayoutAlgorithm(TEXT_AUTOSIZING);

        storyPageViewer.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        storyPageViewer.setScrollbarFadingEnabled(true);
        storyPageViewer.getSettings().setLoadsImagesAutomatically(true);


        storyPageViewer.setOnTouchListener(new OnSwipeTouchListener(getActivity())
        {
            @Override
            public void onSwipeLeft() {
                storyViewModel.goToNextCurrentStoryPosition();
            }

            @Override
            public void onSwipeRight() {
                onStoryViewerListenerCallback.goBackToStoriesList();
            }
        });

        storyPageViewer.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(storyPageViewer, url);

                if(swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), getString(R.string.page_loading_error) + ": " + description, Toast.LENGTH_SHORT).show();
            }
        });

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

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd MMMM yyyy", Locale.getDefault());
            //Convert timestamp to milliseconds format
            Timestamp tmp = new Timestamp(currentStory.getDate() / 1000);
            Date dateToStr = new Date(tmp.getTime());
            storyDateText = formatter.format(dateToStr);

            String serverUrl = "";
            String serverAddress = sharedPref.getString(getString(R.string.preferences_serverAddress), getString(R.string.preferences_serverAddress_default));
            String serverPort = sharedPref.getString(getString(R.string.preferences_serverPort), getString(R.string.preferences_serverPort_default));

            serverUrl = "http://" + serverAddress + ":" + serverPort;
            new GetPage(serverUrl, currentStory, dataRepo.getFeedDao(), forceUpdate).execute();
        }
    }

    @Override
    public void onRefresh() {
        if(storyPageViewer != null) {
            loadContent(true);
        }
    }

    public static String getStyledFont(String title,String link, String date, String content) {
        boolean addBodyTagStart = !content.toLowerCase().contains("<body>");
        boolean addBodyTagEnd = !content.toLowerCase().contains("</body");
        boolean linkEnd = (link != null && !link.isEmpty());

        return "<style type=\"text/css\">" +
                "@font-face {" +
                "font-family: CustomFont;" +
                "src: url(\"file:///android_asset/fonts/OpenSans-SemiBold.ttf\")}" +
                "body {" +
                "font-family: CustomFont;" +
                "font-size: medium;" +
                "text-align: justify;" +
                "}" +
                "img{display: inline;height: auto;max-width: 100%;}"+
                "h2 {" +
                "text-align: justify;" +
                "}" +
                "h6 {" +
                "text-align: left;" +
                "}" +
                "</style>" +
                (addBodyTagStart ? "<body>" : "") +
                "<h2>" +
                (linkEnd ? "<a href=" + link + ">" : "")+
                title +
                (linkEnd ? "</a>" : "") +
                "</h2>" +
                "<h6>" + date + "</h6>" +
                content +
                (addBodyTagEnd ? "</body>" : "");
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
        public void nextStoryRequest(Integer currentStoryId);
        public void goBackToStoriesList();
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