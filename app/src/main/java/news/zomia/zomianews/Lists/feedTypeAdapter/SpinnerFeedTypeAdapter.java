package news.zomia.zomianews.Lists.feedTypeAdapter;

import android.content.Context;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import news.zomia.zomianews.R;

public class SpinnerFeedTypeAdapter  extends ArrayAdapter<CharSequence> {
    LayoutInflater inflater;
    ArrayList<CharSequence> items;

    public SpinnerFeedTypeAdapter(Context context, int textViewResourceId, ArrayList<CharSequence> items) {
        super(context, textViewResourceId, items);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position, convertView, parent);
    }

    private View getImageForPosition(int position, View convertView, ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.layout_spinner_feed_type_row, parent,false);

        String text = items.get(position).toString();
        TextView textView = (TextView)itemView.findViewById(R.id.feed_type_text);
        textView.setText(text);

        if(text.compareToIgnoreCase("Youtube") == 0)
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.youtube_logo, 0, 0, 0);

        if(text.compareToIgnoreCase("RSS") == 0)
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rss_logo, 0, 0, 0);

        if(text.compareToIgnoreCase("Telegram") == 0)
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.telegram_logo, 0, 0, 0);

        if(text.compareToIgnoreCase("Facebook") == 0)
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.facebook_logo, 0, 0, 0);

        return itemView;
    }
}
