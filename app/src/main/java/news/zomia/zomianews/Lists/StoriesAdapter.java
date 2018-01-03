package news.zomia.zomianews.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

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

        //storyImageView.setImageResource(story.getImageResource());
        storyTitleTextView.setText(story.getTitle());

        return view;
    }
}
