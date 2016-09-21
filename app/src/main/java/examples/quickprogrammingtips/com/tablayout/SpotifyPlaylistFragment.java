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
    public void onStop(){
        MainActivity.getThis.firstTime= 0;
        Log.v("samba","onstop");
        super.onStop();
    }
    @Override
    public void lastOncreateView() {
        //currentList = SpotifyList;

        setAdapterForSpotify();

        if (nextCommand.equals("new_albums_categories")){
            newAlbumsCategories();
            new Thread(() -> {
                setCurrentTracklist();
            }).start();

        }else
        if (nextCommand.equals("dutch_album_top_100")){
            albumTop100Nl();
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

    }

    public void setCurrentTracklist() {
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, MainActivity.getThis);
        MainActivity.getThis.runOnUiThread(() -> {albumAdapter.notifyDataSetChanged();
            setVisibility(View.GONE);
            llview.findViewById(R.id.artist_title).setVisibility(View.GONE);});
    }

    @Override
    public void displayAlbums(){

    }

        @Override
    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {

    }


    }
