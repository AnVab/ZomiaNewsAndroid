package news.zomia.zomianews.data.service;

/**
 * Created by Andrey on 18.02.2018.
 */

public enum StoryStatus {
    to_read(0),
    reading(1),
    read(2);

    private final int value;

    private StoryStatus(int value) {
        this.value = value;
    }

    public int getStatus(){
        return this.value;
    }

    public static StoryStatus GetValue(int id)
    {
        StoryStatus[] As = StoryStatus.values();
        for(int i = 0; i < As.length; i++)
        {
            if(As[i].equals(id))
                return As[i];
        }
        return StoryStatus.to_read;
    }

    public static String GetValueName(int id)
    {
        return GetValue(id).name();
    }
}
