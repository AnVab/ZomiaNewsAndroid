package news.zomia.zomianews.data.service;

import android.util.Log;

/**
 * Created by Andrey on 18.02.2018.
 */

public enum StoryStatus {
    to_read(0),
    reading(1),
    read(2);

    private Integer value;

    StoryStatus(Integer value) {
        this.value = value;
    }

    public static StoryStatus getValue(Integer id)
    {
        StoryStatus[] As = StoryStatus.values();
        for(int i = 0; i < As.length; i++)
        {
            if(As[i].getValueInt().equals(id))
                return As[i];
        }
        return StoryStatus.to_read;
    }

    public static String getValueName(Integer id)
    {
        return getValue(id.intValue()).name();
    }

    public Integer getValueInt() {
        return value;
    }

}
