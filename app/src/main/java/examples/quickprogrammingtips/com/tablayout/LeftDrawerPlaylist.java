package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.adapters.PlaylistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * Created by anton on 9-10-16.
 */

public class LeftDrawerPlaylist extends Activity implements  HeaderSongInterface,MpdInterface{
    private  int newalbumsdrawer_layout;
    private  int newalbumsdrawer_list;
    private  int newalbumsmpddrawer_list;
    private  int fabswapplaylist;
    private  MpdInterface mpdInterface;
    private Activity activity;
    private ListView spotifyListview;
    private ListView mpdListview;
    private boolean spotifyVisible=true;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton swapPlaylist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.headers.add(this);
    }

    @Override
    protected void onStop() {
        for (int i=MainActivity.headers.size()-1;i>=0;i--){
            if (MainActivity.headers.get(i).equals(this)) {
                MainActivity.headers.remove(i);
                break;
            }
        }
        super.onStop();

    }

    public void initLeftDrawerPlaylist(Activity activity, MpdInterface mpdInterface, int newalbumsdrawer_layout, int newalbumsdrawer_list, int newalbumsmpddrawer_list, int fabswapplaylist) {
        this.activity=activity;this.newalbumsdrawer_layout=newalbumsdrawer_layout;
        this.newalbumsdrawer_list=newalbumsdrawer_list;
        this.newalbumsmpddrawer_list=newalbumsmpddrawer_list;
        this.fabswapplaylist=fabswapplaylist;
        this.mpdInterface=mpdInterface;
        getDrawerSpotifyLayout();
    }

    public void displayList() {
        try{
        if (spotifyVisible)
            getSpotifyPlaylist();
        else getMPDPlaylist();
    }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
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
        try{
            adapterMpd.setCurrentSong(MainActivity.getThis.getLogic().mpcStatus.song.intValue());
    }   catch (Exception e){}
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
    @Override
    public void setLogo(Bitmap logo) {
        ((ImageView) findViewById(R.id.thumbnail_top)).setImageBitmap(logo);

    }

    @Override
    public void setData(String time, String totalTime, String title, String artist) {
        ((TextView) findViewById(R.id.time_top)).setText(time);
        ((TextView) findViewById(R.id.totaltime_top)).setText(totalTime);
        ((TextView) findViewById(R.id.title_top)).setText(title);
        ((TextView) findViewById(R.id.artist_top)).setText(artist);

    }
    @Override
    public void playlistCall(ArrayList<Mp3File> playlist, boolean change) {

    }

    @Override
    public void newMpdCall(Mp3File mp3File, int position, String command) {

        MainActivity.getThis.mpdCall(mp3File, position, command);
    }

    @Override
    public void printCover(Bitmap result, ImageView image, String album) {

    }

}
