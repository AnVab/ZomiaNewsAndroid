package news.zomia.zomianews.Lists;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import news.zomia.zomianews.R;

/**
 * Created by Andrey on 02.01.2018.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter
       // implements Filterable
{

    private LayoutInflater inflater;
    private Map<String, List<String>> feedsCollections;
    private List<String> tags;

    public ExpandableListAdapter(Activity context, List<String> tags,
                                 Map<String, List<String>> feedsCollections) {
        this.inflater = LayoutInflater.from(context);
        this.feedsCollections = feedsCollections;
        this.tags = tags;
    }

    public Object getChild(int groupPosition, int childPosition) {
        return feedsCollections.get(tags.get(groupPosition)).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public int getChildrenCount(int groupPosition) {
        return feedsCollections.get(tags.get(groupPosition)).size();
    }

    public Object getGroup(int groupPosition) {
        return tags.get(groupPosition);
    }

    public int getGroupCount() {
        return tags.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View view, ViewGroup viewGroup) {
        final String tag = (String) getChild(groupPosition, childPosition);

        if (view == null) {
            view = inflater.inflate(R.layout.layout_feeds_list_item, null);
        }

        TextView item = (TextView) view.findViewById(R.id.titleTextView);
        item.setText(tag);

        return view;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View view, ViewGroup viewGroup) {
        String feedName = (String) getGroup(groupPosition);
        if (view == null) {
            view = inflater.inflate(R.layout.layout_feeds_list_group_item, null);
        }
        TextView item = (TextView) view.findViewById(R.id.tagNameTextView);
        //item.setTypeface(null, Typeface.BOLD);
        item.setText(feedName);
        return view;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
