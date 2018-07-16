package news.zomia.zomianews.Lists.storyadapter;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import news.zomia.zomianews.R;
import news.zomia.zomianews.data.model.Story;
import news.zomia.zomianews.data.service.NetworkState;
import news.zomia.zomianews.data.service.Status;
import news.zomia.zomianews.data.util.ListItemClickListener;
import static news.zomia.zomianews.data.service.StoryStatus.read;
import static news.zomia.zomianews.data.service.StoryStatus.reading;
import static news.zomia.zomianews.data.service.StoryStatus.to_read;

/**
 * Created by Andrey on 03.01.2018.
 */

public class StoriesAdapter extends PagedListAdapter<Story, RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    private Context  context;
    private NetworkState networkState;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private StoryViewHolder.ClickListener clickListener;
    private ListItemClickListener itemClickListener;

    public StoriesAdapter(Context context){//, StoryViewHolder.ClickListener clickListener, ListItemClickListener itemClickListener) {
        super(Story.DIFF_CALLBACK);

        this.clickListener = clickListener;
        this.itemClickListener = itemClickListener;

        this.context = context;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();

        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(getItemCount());
            } else {
                notifyItemInserted(getItemCount());
            }
        } else if (newExtraRow && previousState != newNetworkState) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    private boolean hasExtraRow() {
        if (networkState != null && networkState != NetworkState.LOADED) {
            return true;
        } else {
            return false;
        }
    }

    public Story getStory(int position)
    {
        return getCurrentList().get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view;

        if (viewType == TYPE_ITEM) {
            view = layoutInflater.inflate(R.layout.layout_stories_list_item, viewGroup, false);
            return new StoryViewHolder(view, clickListener);
        } else if (viewType == TYPE_HEADER) {
            view = layoutInflater.inflate(R.layout.layout_network_state_item, viewGroup, false);
            return new NetworkStateItemViewHolder(view, itemClickListener);
        } else {
            throw new IllegalArgumentException("unknown view type " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case TYPE_ITEM: {
                ((StoryViewHolder) holder).bindTo(getItem(position), context);
            }
                break;
            case TYPE_HEADER:
                ((NetworkStateItemViewHolder) holder).bindView(networkState);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (hasExtraRow() && position == getItemCount() - 1) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public long getItemId(int position) {
        if(getCurrentList().get(position) != null)
        return  getCurrentList().get(position).getStoryId();
        else
            return 0;
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        ImageView storyImageView;
        TextView storyTitleTextView;
        TextView storyDateTextView;
        TextView statusTextView;
        TextView storyFirstSentenceTextView;
        ConstraintLayout constraintLayout;
        View readStatusGray;

        private ClickListener listener;

        StoryViewHolder(View itemView, ClickListener listener) {
            super(itemView);

            storyImageView = (ImageView)itemView.findViewById(R.id.storyImageView);
            storyTitleTextView = (TextView)itemView.findViewById(R.id.storyTitleTextView);
            storyDateTextView = (TextView)itemView.findViewById(R.id.storyDateTextView);
            statusTextView = (TextView)itemView.findViewById(R.id.statusTextView);
            storyFirstSentenceTextView = (TextView)itemView.findViewById(R.id.storyFirstSentenceTextView);
            constraintLayout = (ConstraintLayout) itemView.findViewById(R.id.constraintLayout);
            readStatusGray = (View) itemView.findViewById(R.id.readStatusGray);
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

        public void bindTo(Story story, Context context) {

            String storyUrl = "";
            if(story != null) {
                storyUrl = story.getImage();

                //Load img from a story. If image not loaded, show default icon.
                if (storyUrl != null && !storyUrl.isEmpty()) {
                    final int radius = 15;
                    final int margin = 0;
                    final Transformation transformation = new RoundedCornersTransformation(radius, margin);
                    Picasso.with(context)
                            .load(storyUrl)
                            .fit()
                            .centerCrop()
                            .placeholder(R.drawable.progress_animation)
                            .error(R.drawable.error_image)
                            .transform(transformation)
                            .into(storyImageView);
                } else {
                    //storyImageView.setImageResource(R.drawable.image_icon);
                    storyImageView.setVisibility(View.GONE);

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);

                    constraintSet.clear(R.id.statusTextView, ConstraintSet.TOP);
                    constraintSet.connect(R.id.statusTextView, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 8);
                    constraintSet.connect(R.id.statusTextView, ConstraintSet.BOTTOM, R.id.storyTitleTextView, ConstraintSet.TOP, 8);

                    constraintSet.connect(R.id.storyDateTextView,ConstraintSet.START,R.id.statusTextView,ConstraintSet.END,8);
                    constraintSet.connect(R.id.storyDateTextView,ConstraintSet.TOP,R.id.statusTextView,ConstraintSet.TOP,8);

                    constraintSet.connect(R.id.storyFirstSentenceTextView,ConstraintSet.TOP,R.id.storyTitleTextView,ConstraintSet.BOTTOM,8);
                    constraintSet.applyTo(constraintLayout);
                }

                storyTitleTextView.setText(story.getTitle());

                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm, dd MMMM yyyy", Locale.getDefault());
                //Convert timestamp to milliseconds format
                Timestamp tmp = new Timestamp(story.getDate() / 1000);
                Date dateToStr = new Date(tmp.getTime());
                String dateString = formatter.format(dateToStr);
                storyDateTextView.setText(dateString);

                String statusString = context.getString(R.string.status);
                String statusValue = "";
                switch(story.getStatus().intValue())
                {
                    case to_read:
                        statusValue = context.getString(R.string.status_unread);
                        statusTextView.setBackground(context.getResources().getDrawable(R.drawable.new_status, null));

                        //Set default style
                        SetItemStyleDefault(context);
                        break;
                    case reading:
                        statusValue = context.getString(R.string.status_reading);
                        statusTextView.setBackground(context.getResources().getDrawable(R.drawable.reading_status, null));

                        //Set default style
                        SetItemStyleDefault(context);
                        break;
                    case read:
                        statusValue = context.getString(R.string.status_read);
                        statusTextView.setBackground(context.getResources().getDrawable(R.drawable.read_status, null));

                        //Set the style for a read story
                        SetItemStyleRead(context);
                        break;
                    default:
                        Log.d("ZOMIA", "UNKNOWN STATUS");
                        statusValue = context.getString(R.string.status_unread);
                        statusTextView.setBackground(context.getResources().getDrawable(R.drawable.new_status, null));

                        //Set default style
                        SetItemStyleDefault(context);
                        break;
                }
                statusTextView.setText(statusValue);

                //get first sentences from the story text
                String shortText = story.getShortText();
                if(shortText != null)
                    storyFirstSentenceTextView.setText(shortText);
            }
        }

        //Set default style
        private void SetItemStyleDefault(Context context)
        {
            readStatusGray.setVisibility(View.INVISIBLE);
            storyImageView.clearColorFilter();
            statusTextView.setTextColor(context.getResources().getColor(R.color.stories_list_item_normal_text));
            storyDateTextView.setTextColor(context.getResources().getColor(R.color.story_item_date_text));
            storyTitleTextView.setTextColor(context.getResources().getColor(R.color.stories_list_item_normal_text));
            storyFirstSentenceTextView.setTextColor(context.getResources().getColor(R.color.stories_list_item_normal_text));
        }
        //Set the style for a read story
        private void SetItemStyleRead(Context context)
        {
            readStatusGray.setVisibility(View.VISIBLE);
            storyImageView.setColorFilter(Color.argb(200,153,153,153));
            statusTextView.setTextColor(context.getResources().getColor(R.color.read_status_gray_out_text));
            storyDateTextView.setTextColor(context.getResources().getColor(R.color.read_status_gray_out_text));
            storyTitleTextView.setTextColor(context.getResources().getColor(R.color.read_status_gray_out_text));
            storyFirstSentenceTextView.setTextColor(context.getResources().getColor(R.color.read_status_gray_out_text));
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

    static class NetworkStateItemViewHolder extends RecyclerView.ViewHolder {

        private final ProgressBar progressBar;
        private final TextView errorMsg;
        private Button button;

        public NetworkStateItemViewHolder(View itemView, ListItemClickListener listItemClickListener) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar);
            errorMsg = itemView.findViewById(R.id.error_msg);
            /*button = itemView.findViewById(R.id.retry_button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listItemClickListener.onRetryClick(view, getAdapterPosition());
                }
            });*/
        }


        public void bindView(NetworkState networkState) {
            if (networkState != null && networkState.getStatus() == Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }

            if (networkState != null && networkState.getStatus() == Status.ERROR) {
                errorMsg.setVisibility(View.VISIBLE);
                errorMsg.setText(networkState.getMsg());
            } else {
                errorMsg.setVisibility(View.GONE);
            }
        }
    }
}
