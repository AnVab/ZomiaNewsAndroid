package news.zomia.zomianews.fragments;


import android.app.Activity;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import news.zomia.zomianews.R;
import news.zomia.zomianews.customcontrols.OnSwipeTouchListener;
import news.zomia.zomianews.data.viewmodel.StoryViewModel;
import news.zomia.zomianews.data.viewmodel.StoryViewModelFactory;
import news.zomia.zomianews.di.Injectable;

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

    SwipeRefreshLayout swipeRefreshLayout;
    WebView storyPageViewer;
    private Date date;
    private String title;
    private String content;

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
        //storyPageViewer.getSettings().setLoadWithOverviewMode(true);
        //storyPageViewer.getSettings().setUseWideViewPort(true);
        //storyPageViewer.getSettings().setMinimumFontSize(40);

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

        //loadContent();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        storyViewModel = ViewModelProviders.of(getActivity()).get(StoryViewModel.class);

        storyViewModel.getCurrentStory().observe(getActivity(), resource -> {
                // Update the UI.
                if (resource != null && resource.data != null) {

                    date = resource.data.getDate();
                    title = resource.data.getTitle();
                    content = resource.data.getContent();

                    loadContent();

                    swipeRefreshLayout.setRefreshing(false);
                }
                else
                {
                    swipeRefreshLayout.setRefreshing(false);
                }

        });
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    public void loadContent()
    {
        if(storyPageViewer != null) {
            /*SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            Date d = null;
            try {
                d = dateFormat.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String dateToText = "";
            if(d != null)*/
            String dateToText = date.toString();//.toString();

            storyPageViewer.loadDataWithBaseURL("", getStyledFont(title, dateToText, content), "text/html", "UTF-8", "");
        }
    }

    @Override
    public void onRefresh() {
        if(storyPageViewer != null) {
            storyPageViewer.reload();
        }
    }

    public static String getStyledFont(String title, String date, String content) {
        boolean addBodyTagStart = !content.toLowerCase().contains("<body>");
        boolean addBodyTagEnd = !content.toLowerCase().contains("</body");

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
                "<h2>" + title + "</h2>" +
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
}