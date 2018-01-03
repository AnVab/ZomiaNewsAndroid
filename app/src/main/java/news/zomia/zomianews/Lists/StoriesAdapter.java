package news.zomia.zomianews.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Result;

/**
 * Created by Andrey on 03.01.2018.
 */

public class StoriesAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private Context  context;
    private List<Result> stories;

    public StoriesAdapter(Context context, List<Result> stories) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.stories = stories;
    }

    @Override
    public int getCount() {
        try {
            int size = stories.size();
            return size;
        } catch(NullPointerException ex) {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return stories.get(position).getId();
    }

    @Override
    public Object getItem(int position) {
        if(position < stories.size())
            return stories.get(position);
        else
            return null;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        final Result story = stories.get(position);

        if (view == null) {
            view = inflater.inflate(R.layout.layout_stories_list_item, null);
        }

        ImageView storyImageView = (ImageView)view.findViewById(R.id.storyImageView);
        TextView storyTitleTextView = (TextView)view.findViewById(R.id.storyTitleTextView);
        TextView storyDateTextView = (TextView)view.findViewById(R.id.storyDateTextView);

        String storyUrl = GetStoryUrl(story.getContent());
        int imgWidth = 250;
        int imgHeight = 250;

        //Load img from a story. If image not loaded, show default icon.
       // if(!storyUrl.isEmpty())
            Picasso.with(context)
                    .load(storyUrl)
                    //.resize(imgWidth, imgHeight)
                    .fit()
                    .centerCrop()
                    //.onlyScaleDown()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(storyImageView);
       /* else {
            storyImageView.getLayoutParams().width = imgWidth;
            storyImageView.getLayoutParams().height = imgHeight;
            storyImageView.setImageResource(R.mipmap.ic_launcher);
        }*/

        storyTitleTextView.setText(story.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date d = null;
        try {
            d = dateFormat.parse(story.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(d != null)
            storyDateTextView.setText(d.toString());

        return view;
    }

    private String GetStoryUrl(String content)
    {
        String imgRegex = "<[iI][mM][gG][^>]+[sS][rR][cC]\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern p = Pattern.compile(imgRegex);
        Matcher m = p.matcher(content);

        if (m.find()) {
            String imgSrc = m.group(1);
            return imgSrc;
        }
        else
            return "";
    }
}
