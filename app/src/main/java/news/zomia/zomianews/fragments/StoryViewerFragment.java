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

import news.zomia.zomianews.R;
import news.zomia.zomianews.customcontrols.OnSwipeTouchListener;
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

    SwipeRefreshLayout swipeRefreshLayout;
    WebView storyPageViewer;
    private Long dateTimestamp;
    private String title;
    private String content;
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
        storyPageViewer.getSettings().setLayoutAlgorithm(TEXT_AUTOSIZING);

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

        storyViewModel.getCurrentStory().observe(getActivity(), resource -> {
                // Update the UI.
                if (resource != null && resource.data != null) {

                    dateTimestamp = resource.data.getDate();
                    title = resource.data.getTitle();
                    content = resource.data.getContent();

                    loadContent();
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

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd MMMM yyyy", Locale.getDefault());
            //Convert timestamp to milliseconds format
            Timestamp tmp = new Timestamp(dateTimestamp / 1000);
            Date dateToStr = new Date(tmp.getTime());
            storyDateText = formatter.format(dateToStr);
            String targetUrl = "";
            String serverAddress = sharedPref.getString(getString(R.string.preferences_serverAddress), getString(R.string.preferences_serverAddress_default));
            String serverPort = sharedPref.getString(getString(R.string.preferences_serverPort), getString(R.string.preferences_serverPort_default));

            targetUrl = "http://" + serverAddress + ":" + serverPort + "/storage/" + content;
            Log.d("ZOMIA", "baseurl: " + targetUrl);
            new GetPage(targetUrl).execute();
        }
    }

    @Override
    public void onRefresh() {
        if(storyPageViewer != null) {
            storyPageViewer.reload();
        }
    }

    private void injectCSS() {
        try {
            InputStream inputStream = getActivity().getAssets().open("pagestyles.css");
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            String en = "" +
                    "@font-face {\n" +
                    "font-family: OpenSans-Bold;\n" +
                    "src: url('file:///android_asset/fonts/OpenSans-Bold.ttf')\n" +
                    "}\n" +
                    "\n" +
                    "body {\n" +
                    "font-family: OpenSans-Bold;\n" +
                    "font-size: medium;\n" +
                    "text-align: justify;\n" +
                    "}\n" +
                    "\n" +
                    "img{\n" +
                    "display: inline;\n" +
                    "height: auto;\n" +
                    "max-width: 100%;\n" +
                    "}\n" +
                    "\n" +
                    "h2 {\n" +
                    "text-align: justify;\n" +
                    "}\n" +
                    "\n" +
                    "h6 {\n" +
                    "text-align: left;\n" +
                    "}\n";
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            storyPageViewer.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
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

    private class GetPage extends AsyncTask<Void, Void, Void> {
        private String url;
        private String dataBody;
        public GetPage(String url)
        {
            this.url = url;
            this.dataBody = "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to the web site
                Document document = Jsoup.parse(new URL(url).openStream(), "CP1251", url);
                dataBody = document.body().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Set data
            storyPageViewer.loadDataWithBaseURL("", getStyledFont(title, storyDateText, dataBody), "text/html", "UTF-8", "");
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}