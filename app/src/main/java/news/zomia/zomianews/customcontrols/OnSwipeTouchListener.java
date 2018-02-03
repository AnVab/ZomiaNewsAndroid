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

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown() {
    }

         /*@Override
        public void onLongPress(MotionEvent event) {
            Log.d(TAG, "onLongPress: " + event.toString());
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent event) {
            Log.d(TAG, "onShowPress: " + event.toString());
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.d(TAG, "onSingleTapUp: " + event.toString());
            return true;
        }


       @Override
        public boolean onDoubleTap(MotionEvent event) {
            Log.d(TAG, "onDoubleTap: " + event.toString());
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent event) {
            Log.d(TAG, "onDoubleTapEvent: " + event.toString());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(TAG, "onSingleTapConfirmed: " + event.toString());
            return true;
        }*/

    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);

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
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            final int SWIPE_MIN_DISTANCE = 120;
            final int SWIPE_MAX_OFF_PATH = 250;
            final int SWIPE_THRESHOLD_VELOCITY = 200;

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "Right to Left swipe performed");
                onSwipeLeft();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "Left to Right swipe performed");
                onSwipeRight();
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}

