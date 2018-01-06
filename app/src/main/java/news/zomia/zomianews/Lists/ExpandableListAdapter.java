package news.zomia.zomianews.Lists;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;

/**
 * Created by Andrey on 02.01.2018.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter
       // implements Filterable
{
    private static final String TAG = "ZomiaExpandableList";
    private LayoutInflater inflater;

    //Header titles - tags
    private List<String> tags;
    //Child items of tags
    private Map<String, List<Feed>> feedsCollections;

    public ExpandableListAdapter(Activity context, List<String> tags,
                                 Map<String, List<Feed>> feedsCollections) {
        this.inflater = LayoutInflater.from(context);
        this.feedsCollections = feedsCollections;
        this.tags = tags;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Log.d(TAG, "groupPosition = " + groupPosition + " childPosition = " + childPosition + " feedsCollections.size = " + feedsCollections.size() + " tags.size = " +tags.size());

        return this.feedsCollections.get(this.tags.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.tags.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.tags.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
            return this.feedsCollections.get(this.tags.get(groupPosition)).size();
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View view, ViewGroup viewGroup) {
        final Feed feed = (Feed) getChild(groupPosition, childPosition);

        if (view == null) {
            view = inflater.inflate(R.layout.layout_feeds_list_item, null);
        }

        TextView item = (TextView) view.findViewById(R.id.titleTextView);
        item.setText(feed.getTitle());

        TextView channelType = (TextView) view.findViewById(R.id.descriptionTextView);
        channelType.setText("rss");

        return view;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View view, ViewGroup viewGroup) {
        String tag = (String) getGroup(groupPosition);
        if (view == null) {
            view = inflater.inflate(R.layout.layout_feeds_list_group_item, null);
        }
        TextView item = (TextView) view.findViewById(R.id.tagNameTextView);
        //item.setTypeface(null, Typeface.BOLD);
        item.setText(tag);
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
