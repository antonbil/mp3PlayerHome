package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.support.v4.view.ViewPager;

/**
 * Created by anton on 5-9-16.
 */
public class NoAnimateViewPager extends ViewPager {
    public NoAnimateViewPager(Context context) {
        super(context);
    }
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }
}
