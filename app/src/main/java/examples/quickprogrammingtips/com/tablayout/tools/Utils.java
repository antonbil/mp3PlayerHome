package examples.quickprogrammingtips.com.tablayout.tools;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by anton on 4-2-16.
 * Utils to be useed overall program
 */
public class Utils {
    public static void setDynamicHeight(ListView mListView, int comp) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }

        //ListAdapter adapter = listView.getAdapter();

        int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        /*int grossElementHeight = 0;
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View childView = mListAdapter.getView(i, null, mListView);
            childView.measure(UNBOUNDED, UNBOUNDED);
            grossElementHeight += childView.getMeasuredHeight();
        }*/
        //Log.v("samba", "gross:" + grossElementHeight);

        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight()+comp;
        }
        //Log.v("samba", "desired:" + height);
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }
}
