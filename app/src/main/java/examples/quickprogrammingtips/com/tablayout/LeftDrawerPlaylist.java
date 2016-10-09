package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.adapters.PlaylistAdapter;

/**
 * Created by anton on 9-10-16.
 */

public class LeftDrawerPlaylist {
    private final int newalbumsdrawer_layout;
    private final int newalbumsdrawer_list;
    private final int newalbumsmpddrawer_list;
    private final int fabswapplaylist;
    private final MpdInterface mpdInterface;
    private Activity activity;
    private ListView spotifyListview;
    private ListView mpdListview;
    private boolean spotifyVisible=true;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton swapPlaylist;

    public LeftDrawerPlaylist(Activity activity, MpdInterface mpdInterface, int newalbumsdrawer_layout, int newalbumsdrawer_list, int newalbumsmpddrawer_list, int fabswapplaylist) {
        this.activity=activity;this.newalbumsdrawer_layout=newalbumsdrawer_layout;
        this.newalbumsdrawer_list=newalbumsdrawer_list;
        this.newalbumsmpddrawer_list=newalbumsmpddrawer_list;
        this.fabswapplaylist=fabswapplaylist;
        this.mpdInterface=mpdInterface;
    }

    public void displayList() {
        if (spotifyVisible)
            getSpotifyPlaylist();
        else getMPDPlaylist();
    }

    public void getMPDPlaylist() {
        if (spotifyListview!=null)spotifyListview.setVisibility(View.GONE);
        PlaylistAdapter adapterMpd = new PlaylistAdapter(MainActivity.getThis.playFragment, mpdInterface, MainActivity.getThis.getLogic().getPlaylistFiles(), MainActivity.getThis.getApplicationContext());
        mpdListview.setVisibility(View.VISIBLE);
        mpdListview.setAdapter(adapterMpd);
        updatePlaylistMpd(adapterMpd);

        mpdListview.setOnItemClickListener((parent, view, position, id) -> {
            Log.v("samba","Play:"+position);
            MainActivity.getThis.getLogic().getMpc().play(position);
            updatePlaylistMpd(adapterMpd);
        });

    }

    public void updatePlaylistMpd(PlaylistAdapter adapterMpd) {
        adapterMpd.setCurrentSong(MainActivity.getThis.getLogic().mpcStatus.song.intValue());
        adapterMpd.notifyDataSetChanged();
    }

    public void getSpotifyPlaylist() {
        MainActivity.getThis.runOnUiThread(() -> {
            try{
                PlanetAdapter albumAdapter;
                if (mpdListview!=null)mpdListview.setVisibility(View.GONE);
                //spotifyListview = null;
                spotifyListview.setVisibility(View.VISIBLE);
                ArrayList<String> albumList = new ArrayList<>();
                ArrayList<PlaylistItem> albumTracks = new ArrayList<>();
                albumAdapter= MainActivity.getTracksAdapter(mDrawerLayout, spotifyListview, albumList, albumTracks);
                spotifyListview.setAdapter(albumAdapter);
                SpotifyFragment.checkAddress();
                SpotifyFragment.refreshPlaylistFromSpotify(1, albumAdapter, activity, albumList, albumTracks);
                albumAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
        });
    }
    public void getDrawerSpotifyLayout() {
        mDrawerLayout = (DrawerLayout) activity.findViewById(newalbumsdrawer_layout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                activity,
                mDrawerLayout,
                R.string.hello_world,
                R.string.hello_world
        ) {

            public void onDrawerOpened(View drawerView) {
                spotifyVisible=MainActivity.playingStatus==MainActivity.SPOTIFY_PLAYING;
                mpdListview.setVisibility(View.GONE);
                spotifyListview.setVisibility(View.GONE);
                displayList();

            }


        };
        spotifyListview = (ListView) activity.findViewById(newalbumsdrawer_list);
        mpdListview = (ListView) activity.findViewById(newalbumsmpddrawer_list);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        swapPlaylist = (FloatingActionButton) activity.findViewById(fabswapplaylist);
        swapPlaylist.setOnClickListener(view -> {spotifyVisible=!spotifyVisible;
            displayList();
        });
    }
}
