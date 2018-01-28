package news.zomia.zomianews.data.model;

import android.arch.persistence.room.Embedded;

/**
 * Created by Andrey on 28.01.2018.
 */

public class TagFeedPair {

    @Embedded
    public Tag tag;

    @Embedded
    public Feed feed;

    public TagFeedPair(Tag tag, Feed feed)
    {
        this.tag = tag;
        this.feed = feed;
    }
}
