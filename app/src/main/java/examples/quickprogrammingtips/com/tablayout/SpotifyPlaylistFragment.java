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
        currentList=SpotifyList;

        setAdapterForSpotify();
        String hartistName=artistName;

        Log.d("samba", "SpotifyPlayList1b");
        refreshPlaylistFromSpotify(albumAdapter, albumsListview,MainActivity.getThis);
        Log.d("samba", "SpotifyPlayList1c");
        setVisibility(View.GONE);
        albumAdapter.notifyDataSetChanged();
        playButtonsAtBottom();
        fab.setVisibility(View.GONE);
        llview.findViewById(R.id.artist_title).setVisibility(View.GONE);
        //

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
