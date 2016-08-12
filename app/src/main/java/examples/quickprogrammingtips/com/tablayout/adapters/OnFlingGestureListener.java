package examples.quickprogrammingtips.com.tablayout.adapters;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by anton on 11-8-16.
 */
public abstract class OnFlingGestureListener implements View.OnTouchListener {

    private GestureDetector gdt;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Log.v("samba","righttoleft1");
        if (gdt == null) gdt = new GestureDetector(v.getContext(), new GestureListener());
        return gdt.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 50;
        private static final int SWIPE_THRESHOLD_VELOCITY = 50;

        @Override
        public boolean onDown(MotionEvent event) {
            // triggers first for both single tap and long press
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            // triggers after onDown only for single tap
            onTapUp();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            // triggers after onDown only for long press
            onLongTapUp();
            //super.onLongPress(event);
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.v("samba","righttoleft");
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE ){// && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onRightToLeft();
                return true;
            }
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE){// && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onLeftToRight();
                return true;
            }

            /*if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                onBottomToTop();
                return true;
            }
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                onTopToBottom();
                return true;
            }*/
            return false;
        }
    }

    public abstract void onRightToLeft();

    public abstract void onLeftToRight();


    public abstract void onTapUp();
    public abstract void onLongTapUp();
}