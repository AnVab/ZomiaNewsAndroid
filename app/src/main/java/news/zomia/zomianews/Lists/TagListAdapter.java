package news.zomia.zomianews.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Tag;

/**
 * Created by Andrey on 01.02.2018.
 */

public class TagListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    List<Tag> items = new ArrayList<Tag>();

    public TagListAdapter(Context context) {

        this.context = context;
        lInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //Items count
    @Override
    public int getCount() {
        if(items != null)
            return items.size();
        else
            return 0;
    }

    // Item on position
    @Override
    public Object getItem(int position) {
        if(items != null)
            return items.get(position);
        else
            return null;
    }

    // Item Id on position
    @Override
    public long getItemId(int position) {
        return position;
    }

    // List item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);
        }

        Tag tag = getTag(position);

        TextView name = ((TextView) view.findViewById(android.R.id.text1));
        if(tag != null) {
            name.setText(tag.getName());
        }

        return view;
    }

    //Tag on position
    Tag getTag(int position) {
        return ((Tag) getItem(position));
    }

    public void replace(List<Tag> items)
    {
        this.items.clear();
        this.items.addAll(items);
    }
}
