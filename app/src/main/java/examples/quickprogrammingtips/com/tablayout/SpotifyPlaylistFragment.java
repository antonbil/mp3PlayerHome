package examples.quickprogrammingtips.com.tablayout;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
        //Log.v("samba","onstop");
        super.onStop();
    }
    @Override
    public void lastOncreateView(View llview) {
        ListView tracksListview = (ListView) llview.findViewById(R.id.tracks_listview);
        tracksListview.setAdapter(albumAdapter);
        LinearLayout ll = ((LinearLayout) llview.findViewById(R.id.toplinearlayout));
        ll.setVisibility(View.GONE);
        Log.d("samba", "Text:11");

        //setAdapterForSpotify();
        Log.d("samba", "Text:12");

        if (nextCommand.equals("search album")){
            searchAlbum();

        }else {
            Log.d("samba", "Text:12");
            setCurrentTracklist();
            Log.d("samba", "Text:13");
        }
        nextCommand="";

    }

    @Override
    public void onActivityCreated() {

        try {

            Log.d("samba", "Text:5");


            albumsListview = (ListView) llview.findViewById(R.id.albums_listview2);
            albumsListview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            setAdapterForSpotify();

            Log.d("samba", "Text:6");


        } catch (Exception e) {
            Log.getStackTraceString(e);
        }
    }



    public void setCurrentTracklist() {
            refreshPlaylistFromSpotify(albumAdapter,  getThis.getActivity());
        Log.v("samba","ik heb alles opgehaald....");    }

    @Override
    public void displayAlbums(){

    }

        @Override
    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {

    }


    }
