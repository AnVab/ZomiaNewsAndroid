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
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((velocityX * velocityX) > (velocityY * velocityY)) {
                if (velocityX < 0) {
                    Log.d(TAG, "Right to Left swipe performed");
                    onSwipeLeft();
                } else {
                    Log.d(TAG, "Left to Right swipe performed");
                    onSwipeRight();
                }
            } else {
                if (velocityY < 0) {
                    Log.d(TAG, "Down to Up swipe performed");
                    onSwipeUp();
                } else {
                    Log.d(TAG, "Up to Down swipe performed");
                    onSwipeDown();
                }
            }
            return false;
        }
    }
}

