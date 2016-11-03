package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;

/**
 * Created by anton on 9-10-16.
 * interface to display song at top of screen
 */

public interface HeaderSongInterface {
    void setLogo(Bitmap logo);
    void setData(String time, String totalTime, String title, String artist, boolean spotifyList, int currentTrack);
}
