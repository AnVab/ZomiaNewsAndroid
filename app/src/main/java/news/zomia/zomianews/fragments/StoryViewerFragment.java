package news.zomia.zomianews.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import news.zomia.zomianews.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StoryViewerFragment extends Fragment {

    private View rootView;
    private String date;
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
        date = getArguments().getString("date");
        title = getArguments().getString("title");
        content = getArguments().getString("content");

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

        TextView storyHeaderTextView = (TextView) view.findViewById(R.id.storyHeaderTextView );
        storyHeaderTextView.setText(title);

        TextView storyDateTextView = (TextView) view.findViewById(R.id.storyDateTextView );
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date d = null;
        try {
            d = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(d != null)
            storyDateTextView.setText(d.toString());

        WebView storyPageViewer = (WebView) view.findViewById(R.id.storyPageViewer );
        storyPageViewer.getSettings().setJavaScriptEnabled(true);
        //storyPageViewer.getSettings().setLoadWithOverviewMode(true);
        //storyPageViewer.getSettings().setUseWideViewPort(true);
        //storyPageViewer.getSettings().setMinimumFontSize(40);

        storyPageViewer.loadDataWithBaseURL("", getStyledFont(content), "text/html", "UTF-8", "");
    }
    public static String getStyledFont(String content) {
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
                "</style>" +
                (addBodyTagStart ? "<body>" : "") + content + (addBodyTagEnd ? "</body>" : "");
    }

}