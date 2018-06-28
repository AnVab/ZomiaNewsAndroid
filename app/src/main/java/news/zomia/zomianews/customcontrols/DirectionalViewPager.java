package news.zomia.zomianews.customcontrols;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DirectionalViewPager  extends ViewPager {

    public enum SwipeDirection {
        ALL, LEFT, RIGHT, NONE;
    }

    private float initialXValue;
    private SwipeDirection direction;

    public DirectionalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.direction = SwipeDirection.ALL;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.IsSwipeAllowed(event)) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.IsSwipeAllowed(event)) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setSwipeDirection(SwipeDirection direction) {
        this.direction = direction;
    }

    private boolean IsSwipeAllowed(MotionEvent event) {
        if (this.direction == SwipeDirection.ALL)
            return true;

        //disable any swipe
        if (direction == SwipeDirection.NONE)
            return false;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initialXValue = event.getX();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                float diffX = event.getX() - initialXValue;
                if (diffX > 0 && direction == SwipeDirection.RIGHT) {
                    //left to right swipe
                    return false;
                } else if (diffX < 0 && direction == SwipeDirection.LEFT) {
                    //right to left swipe
                    return false;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return true;
    }
}
