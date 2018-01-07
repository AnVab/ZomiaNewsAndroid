package news.zomia.zomianews.customcontrols;

/**
 * Created by Andrey on 08.01.2018.
 */

import android.content.Context;
import android.util.AttributeSet;

public class EditTextPreference extends android.support.v7.preference.EditTextPreference{
    public EditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        return getText();
    }
}
