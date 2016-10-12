package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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

public abstract class LeftDrawerPlaylist implements  HeaderSongInterface,MpdInterface{
    private  int newalbumsdrawer_layout;
    private  int newalbumsdrawer_list;
    private  int newalbumsmpddrawer_list;
    private  int fabswapplaylist;
    private  MpdInterface mpdInterface;
    private Activity activity;
    private ListView spotifyListview;
    private ListView mpdListview;
    private boolean spotifyVisible=true;
    protected DrawerLayout mDrawerLayout;
    private FloatingActionButton swapPlaylist;
    protected boolean drawerActive=false;
    private boolean shouldClick=false;
    private int xcoord=0;
    private PlanetAdapter albumAdapter;
    private ArrayList<String> albumList;
    private ArrayList<PlaylistItem> albumTracks;
    private PlaylistAdapter adapterMpd;
    private TextView timeField;
    private TextView totalField;
    private TextView titleField;
    private TextView artistField;
    private ImageView imageField;
    private int position=-1;
    private ListView drawerListRight;
    public ArrayList<String> itemsArray;

    protected void onStop() {
        for (int i=MainActivity.headers.size()-1;i>=0;i--){
            if (MainActivity.headers.get(i).equals(this)) {
                MainActivity.headers.remove(i);
                break;
            }
        }
    }

    public LeftDrawerPlaylist(Activity activity, /*MpdInterface mpdInterface,*/ int newalbumsdrawer_layout, int newalbumsdrawer_list, int newalbumsmpddrawer_list, int fabswapplaylist) {
        this.activity=activity;this.newalbumsdrawer_layout=newalbumsdrawer_layout;
        this.newalbumsdrawer_list=newalbumsdrawer_list;
        this.newalbumsmpddrawer_list=newalbumsmpddrawer_list;
        this.fabswapplaylist=fabswapplaylist;
        this.mpdInterface=mpdInterface;
        albumList = new ArrayList<>();
        albumTracks = new ArrayList<>();
        albumAdapter = MainActivity.getTracksAdapter(mDrawerLayout, spotifyListview, albumList, albumTracks);
        mDrawerLayout = (DrawerLayout) activity.findViewById(newalbumsdrawer_layout);
        spotifyListview = (ListView) activity.findViewById(newalbumsdrawer_list);
        mpdListview = (ListView) activity.findViewById(newalbumsmpddrawer_list);
        adapterMpd = new PlaylistAdapter(MainActivity.getThis.playFragment, this, MainActivity.getThis.getLogic().getPlaylistFiles(), MainActivity.getThis.getApplicationContext());
        mpdListview.setAdapter(adapterMpd);
        swapPlaylist = (FloatingActionButton) activity.findViewById(fabswapplaylist);
        spotifyListview.setAdapter(albumAdapter);
        SpotifyFragment.checkAddress();
        mpdListview.setOnItemClickListener((parent, view, position, id) -> {
            Log.v("samba","Play:"+position);
            MainActivity.getThis.getLogic().getMpc().play(position);
            updatePlaylistMpd(adapterMpd);
        });
        swapPlaylist.setOnClickListener(view -> {spotifyVisible=!spotifyVisible;
            displayList();
        });
        mDrawerLayout.setOnTouchListener((v, event) -> {

            if (drawerActive) {
                performTouchEvent(event);
                if (drawerListRight!=null)
                drawerListRight.onTouchEvent(event);
                //
                switch (event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_DOWN:
                        shouldClick = true;
                        xcoord=(int)event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (shouldClick) {
                            if ((int)event.getX()>xcoord+100) {
                                mDrawerLayout.closeDrawers();
                            } else {
                                mDrawerLayout.closeDrawers();
                                /*final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Do something after 100ms
                                        performClickOnRightDrawer();
                                    }
                                }, 2000);*/
                                if (drawerListRight!=null)
                                drawerListRight.performClick();
                                performClickOnRightDrawer();
                                shouldClick = false;
                                return true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                return false;
            } else {
                //Log.v("samba", "no event defined for push");
            }
            return false;
        });
        timeField = ((TextView) activity.findViewById(R.id.time_top));
        totalField = (TextView) activity.findViewById(R.id.totaltime_top);
        titleField = ((TextView) activity.findViewById(R.id.title_top));
        artistField = ((TextView) activity.findViewById(R.id.artist_top));
        imageField = ((ImageView) activity.findViewById(R.id.thumbnail_top));
        MainActivity.headers.add(this);
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
        mpdListview.setVisibility(View.VISIBLE);
        updatePlaylistMpd(adapterMpd);


    }

    public void updatePlaylistMpd(PlaylistAdapter adapterMpd) {
        MainActivity.getThis.runOnUiThread(() -> {
            try {
                adapterMpd.setCurrentSong(MainActivity.getThis.getLogic().mpcStatus.song.intValue());
            } catch (Exception e) {
            }
            adapterMpd.notifyDataSetChanged();
        });
    }

    public void getSpotifyPlaylist() {
        SpotifyFragment.refreshPlaylistFromSpotify(1, albumAdapter, activity, albumList, albumTracks);
        MainActivity.getThis.runOnUiThread(() -> {
            try{
                if (mpdListview!=null)mpdListview.setVisibility(View.GONE);
                spotifyListview.setVisibility(View.VISIBLE);
                albumAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
        });
    }
    public abstract void performTouchEvent(MotionEvent event);
    public abstract void performClickOnRightDrawer();
    public void getDrawerSpotifyLayout() {

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                activity,
                mDrawerLayout,
                R.string.hello_world,
                R.string.hello_world
        ) {

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (position>-1){
                    doMenuAction(position);
                    position=-1;
                }
                // Do whatever you want here
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                spotifyVisible=MainActivity.playingStatus==MainActivity.SPOTIFY_PLAYING;
                mpdListview.setVisibility(View.GONE);
                spotifyListview.setVisibility(View.GONE);
                displayList();

            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (mDrawerLayout.isDrawerVisible(Gravity.RIGHT))//if right drawer is opened
                     drawerActive = false;
                } else if (newState == DrawerLayout.STATE_DRAGGING) {
                    drawerActive = false;
                } else if (newState == DrawerLayout.STATE_IDLE) {
                    if (mDrawerLayout.isDrawerVisible(Gravity.RIGHT)) {
                        drawerActive = true;
                    }


                }
            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    protected abstract void doMenuAction(int position);

    @Override
    public void setLogo(Bitmap logo) {
        imageField.setImageBitmap(logo);

    }

    @Override
    public void setData(String time, String totalTime, String title, String artist) {
        timeField.setText(time);
        totalField.setText(totalTime);
        titleField.setText(title);
        artistField.setText(artist);

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

    public void setPosition(int position) {
        this.position = position;
    }

    public void setMenu(ArrayList<String> itemsArray) {
        this.itemsArray=itemsArray;
        drawerListRight = (ListView) activity.findViewById(R.id.DrawerListRight);
        //ArrayAdapter<String> drawerListRightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        MenuAdapter menuAdapter=new MenuAdapter(activity,itemsArray);
        drawerListRight.setAdapter(menuAdapter);
        drawerListRight.setOnItemClickListener((parent, view, position, id) -> {
            Log.v("samba","Select"+position);
            setPosition(position);
        });

    }
}
