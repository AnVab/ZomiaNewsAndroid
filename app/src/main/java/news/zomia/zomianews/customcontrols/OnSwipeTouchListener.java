package news.zomia.zomianews.customcontrols;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Andrey on 08.01.2018.
 */
public class OnSwipeTouchListener  implements View.OnTouchListener
{
    private static final String TAG = "Gestures";
    private final GestureDetectorCompat gestureDetector;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetectorCompat(context, new GestureListener());
    }

    public GestureDetectorCompat getGestureDetector(){
        return  gestureDetector;
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

    public void onTouchEnd() {
    }

    public void onScrollValue(float yValue) {
    }

    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            //case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouchEnd();

                break;
        }

        //stop default scroll action
        if(event.getAction()== MotionEvent.ACTION_MOVE) {
            return true;
        } else {
            return v.onTouchEvent(event);
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY){
            onScrollValue(distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            final int SWIPE_MIN_DISTANCE = 120;
            final int SWIPE_MAX_OFF_PATH = 250;
            final int SWIPE_THRESHOLD_VELOCITY = 200;

            /*if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY){
                onSwipeUp();
                return super.onFling(e1, e2, velocityX, velocityY);
            }*/

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onSwipeLeft();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onSwipeRight();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}

