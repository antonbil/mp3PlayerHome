package examples.quickprogrammingtips.com.tablayout.model;

/**
 * Created by anton on 5-3-16.
 */
public class HistoryListview {
    public int position;
    public String path;

    public HistoryListview(String path,int firstVisiblePosition) {
        this.path = path;
        this.position=firstVisiblePosition;
    }

}
