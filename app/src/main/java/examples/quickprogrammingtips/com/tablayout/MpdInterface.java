package examples.quickprogrammingtips.com.tablayout;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * Created by anton on 23-1-16.
 * interface for Mpd
 */
public interface MpdInterface {
    void playlistCall(ArrayList<Mp3File>playlist,boolean change);
    void newMpdCall(Mp3File mp3File,int position, String command);
    void printCover(Bitmap result,  ImageView image, String album);
}
