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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Feed;
import news.zomia.zomianews.data.model.FeedStoriesCount;
import news.zomia.zomianews.data.model.Tag;

/**
 * Created by Andrey on 02.01.2018.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter
{
    private static final String TAG = "ZomiaExpandableList";
    private LayoutInflater inflater;

    //Header titles - tags
    private List<String> tags;
    private List<String> tagsOriginal;
    //Child items of tags
    private Map<String, List<Feed>> feedsCollections;
    private Map<String, List<Feed>> feedsCollectionsOriginal;
    private Map<Integer, Integer> feedsStoriesCountMap;

    public ExpandableListAdapter(Activity context,
                                 Map<Integer, Integer> feedsStoriesCountMap) {
        this.inflater = LayoutInflater.from(context);

        this.feedsCollections = new HashMap<String, List<Feed>>();
        this.feedsCollectionsOriginal = new HashMap<String, List<Feed>>();
        this.feedsCollectionsOriginal.putAll(feedsCollections);

        this.feedsStoriesCountMap = feedsStoriesCountMap;

        this.tags = new ArrayList<String>();
        this.tagsOriginal = new ArrayList<String>();
        this.tagsOriginal.addAll(this.tags);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

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
        if(this.feedsCollections == null || this.tags == null || this.feedsCollections.get(this.tags.get(groupPosition)) == null)
            return 0;
        else
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

        TextView storiesCountTextView =  (TextView) view.findViewById(R.id.storiesCountTextView);

        if(feedsStoriesCountMap.get(feed.getFeedId()) != null) {
            storiesCountTextView.setText(feedsStoriesCountMap.get(feed.getFeedId()).toString());
        }
        else
            storiesCountTextView.setText("0");

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

        //Set feeds count for the tag
        TextView itemFeedCount = (TextView) view.findViewById(R.id.tagFeedsCountTextView);
        if(itemFeedCount != null && this.feedsCollections != null && this.feedsCollections.get(tag) != null)
        {
            itemFeedCount.setText(String.valueOf(this.feedsCollections.get(tag).size()));
        }
        else
            itemFeedCount.setText(String.valueOf(0));

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

    public void setFeedStoriesCount(List<FeedStoriesCount> feedStoriesCount)
    {
        feedsStoriesCountMap.clear();
        for(FeedStoriesCount count: feedStoriesCount)
        {
            feedsStoriesCountMap.put(count.getFeedId(), count.getStoriesCountTotal());
        }
    }

    public void replaceFeedsCollection(Map<String, List<Feed>> feedsCollections)
    {
        this.feedsCollections.clear();
        this.feedsCollections.putAll(feedsCollections);

        this.feedsCollectionsOriginal.clear();
        this.feedsCollectionsOriginal.putAll(feedsCollections);

        notifyDataSetChanged();
    }

    public void replaceTagsList(List<String> tags)
    {
        this.tags.clear();
        this.tags.addAll(tags);

        this.tagsOriginal.clear();
        this.tagsOriginal.addAll(this.tags);

        notifyDataSetChanged();
    }

    public void filterData(String query){
        query = query.toLowerCase();

        feedsCollections.clear();
        tags.clear();

        if(query.isEmpty()){
            feedsCollections.putAll(feedsCollectionsOriginal);
            tags.addAll(tagsOriginal);
        }
        else {

            for(String tagName: tagsOriginal)
            {
                String key = tagName;
                List<Feed> feedList = feedsCollectionsOriginal.get(tagName);
                List<Feed> newfeedList = new ArrayList<Feed>();

                if(feedList != null) {
                    for (Feed feed : feedList) {
                        if ((feed.getTitle() != null && feed.getTitle().toLowerCase().contains(query))) {
                            newfeedList.add(feed);
                        }
                    }

                    if(newfeedList.size() > 0){
                        feedsCollections.put(key, newfeedList);
                        tags.add(key);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }
}
