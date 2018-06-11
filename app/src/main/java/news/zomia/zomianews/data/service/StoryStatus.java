package news.zomia.zomianews.data.service;

import android.util.Log;

/**
 * Created by Andrey on 18.02.2018.
 */

public final class StoryStatus {
    public static final int to_read = 0;
    public static final int reading = 1;
    public static final int read = 2;

    private StoryStatus() { }

    public static String getName(int id)
    {
        switch(id)
        {
            case to_read:
                return "to_read";
            case reading:
                return "reading";
            case read:
                return "read";
            default:
                return "to_read";
        }
    }
}
