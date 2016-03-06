package examples.quickprogrammingtips.com.tablayout.model;

import android.os.Parcelable;
import android.util.Log;

/**
 * Created by anton on 5-3-16.
 */
public class HistoryListview {
    public int position;

    public HistoryListview(String path, Parcelable state, int firstVisiblePosition) {
        //todo: state can be removed
        this.path = path;
        this.state = state;
        this.position=firstVisiblePosition;
        Log.v("samba", "set state to:" + path+":"+position);
    }

    public String path;
    public Parcelable state;
}
