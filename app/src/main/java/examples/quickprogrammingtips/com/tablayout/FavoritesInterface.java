package examples.quickprogrammingtips.com.tablayout;

import examples.quickprogrammingtips.com.tablayout.model.Favorite;

/**
 * Created by anton on 23-1-16.
 * Interface for favorites
 */
public interface FavoritesInterface {
    //void playlistCall(ArrayList<Mp3File> playlist);
    void favoritesCall(Favorite favorite, String id);
}
