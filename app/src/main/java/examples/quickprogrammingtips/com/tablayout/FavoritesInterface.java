package examples.quickprogrammingtips.com.tablayout;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * Created by anton on 23-1-16.
 */
public interface FavoritesInterface {
    //void playlistCall(ArrayList<Mp3File> playlist);
    void favoritesCall(Favorite favorite, String id);
}
