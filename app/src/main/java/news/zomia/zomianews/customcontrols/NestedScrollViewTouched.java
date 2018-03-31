package news.zomia.zomianews.customcontrols;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NestedScrollViewTouched  extends NestedScrollView {
    private OnSwipeTouchListener gestureListener;

    public NestedScrollViewTouched(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    public void setOnTouchListener(OnSwipeTouchListener l){
        gestureListener = l;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        gestureListener.getGestureDetector().onTouchEvent(motionEvent);
        return super.dispatchTouchEvent(motionEvent);
    }
}