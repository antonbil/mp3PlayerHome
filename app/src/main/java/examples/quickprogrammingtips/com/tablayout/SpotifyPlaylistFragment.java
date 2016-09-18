package examples.quickprogrammingtips.com.tablayout;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by anton on 10-9-16.
 */
public class SpotifyPlaylistFragment extends SpotifyFragment {
    @Override
    public void lastOncreateView() {
        if (categoriesMenu!=null)
            categoriesMenu.getMenu().clear();
        if (nextCommand.equals("new_albums_categories")){
            newAlbumsCategories(MainActivity.getThis.findViewById(R.id.thumbnail_top));
            new Thread(() -> {
                setCurrentTracklist();
            }).start();

        }else
        if (nextCommand.equals("search album")){
            searchAlbum();

        }else {
            setCurrentTracklist();
        }
        nextCommand="";
        setVisibility(View.GONE);
        playButtonsAtBottom();
        //fab.setVisibility(View.GONE);
        llview.findViewById(R.id.artist_title).setVisibility(View.GONE);

    }

    public void setCurrentTracklist() {
        currentList = SpotifyList;

        setAdapterForSpotify();

        refreshPlaylistFromSpotify(albumAdapter, albumsListview, MainActivity.getThis);
        MainActivity.getThis.runOnUiThread(() -> albumAdapter.notifyDataSetChanged());
    }

    @Override
    public void displayAlbums(){

    }
    @Override
    protected void selectList(String title) {

    }

        @Override
    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {

    }


    }
