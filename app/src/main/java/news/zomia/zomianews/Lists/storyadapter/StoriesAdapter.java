package news.zomia.zomianews.Lists.storyadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class StoriesAdapter extends SelectableAdapter<StoriesAdapter.StoryViewHolder>{

    private LayoutInflater inflater;
    private Context  context;

    private static final int TYPE_INACTIVE = 0;
    private static final int TYPE_ACTIVE = 1;

    private List<Result> stories;

    private StoryViewHolder.ClickListener clickListener;

    public StoriesAdapter(Context context, List<Result> stories, StoryViewHolder.ClickListener clickListener) {
        super();

        this.clickListener = clickListener;

        //this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.stories = stories;
    }

    public void removeItem(int position) {
        stories.remove(position);
        notifyItemRemoved(position);
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            stories.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public Result getItem(int position)
    {
        return stories.get(position);
    }

    @Override
    public int getItemCount() {
        try {
            int size = stories.size();
            return size;
        } catch(NullPointerException ex) {
            return 0;
        }
    }

    @Override
    public StoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final int layout = viewType == TYPE_INACTIVE ? R.layout.layout_stories_list_item : R.layout.layout_stories_list_item /* inactive if false */;

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(layout, viewGroup, false);
        StoryViewHolder pvh = new StoryViewHolder(v, clickListener);
        return pvh;
    }

    @Override
    public void onBindViewHolder(StoryViewHolder personViewHolder, int position) {
        String storyUrl = GetStoryUrl(stories.get(position).getContent());
        int imgWidth = 250;
        int imgHeight = 250;

        //Load img from a story. If image not loaded, show default icon.
        if(!storyUrl.isEmpty())
            Picasso.with(context)
                    .load(storyUrl)
                    //.resize(imgWidth, imgHeight)
                    .fit()
                    .centerCrop()
                    //.onlyScaleDown()
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(personViewHolder.storyImageView);
        else {
            personViewHolder.storyImageView.getLayoutParams().width = imgWidth;
            personViewHolder.storyImageView.getLayoutParams().height = imgHeight;
            personViewHolder.storyImageView.setImageResource(R.mipmap.ic_launcher);
        }

        personViewHolder.storyTitleTextView.setText(stories.get(position).getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date d = null;
        try {
            d = dateFormat.parse(stories.get(position).getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(d != null)
            personViewHolder.storyDateTextView.setText(d.toString());

        personViewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemViewType(int position) {
        final Result item = stories.get(position);
        //return item.isFresh() ? TYPE_ACTIVE : TYPE_INACTIVE;

        return TYPE_ACTIVE;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
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

    public static class StoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        ImageView storyImageView;
        TextView storyTitleTextView;
        TextView storyDateTextView;
        View selectedOverlay;

        private ClickListener listener;

        StoryViewHolder(View itemView, ClickListener listener) {
            super(itemView);

            storyImageView = (ImageView)itemView.findViewById(R.id.storyImageView);
            storyTitleTextView = (TextView)itemView.findViewById(R.id.storyTitleTextView);
            storyDateTextView = (TextView)itemView.findViewById(R.id.storyDateTextView);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);

            this.listener = listener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Item clicked at position
            if (listener != null) {
                listener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            //Item long-clicked at position
            if (listener != null) {
                return listener.onItemLongClicked(getLayoutPosition());
            }

            return false;
        }

        public interface ClickListener {
            void onItemClicked(int position);
            boolean onItemLongClicked(int position);
        }
    }
}
