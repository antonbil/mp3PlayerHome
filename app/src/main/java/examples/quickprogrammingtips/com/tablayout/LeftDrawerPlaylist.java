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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.adapters.PlaylistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

/**
 * Created by anton on 9-10-16.
 */

public abstract class LeftDrawerPlaylist implements  HeaderSongInterface,MpdInterface, SpotifyPlaylistInterface {
    private  int newalbumsdrawer_layout;
    private  int newalbumsdrawer_list;
    private  int newalbumsmpddrawer_list;
    private  int fabswapplaylist;
    private Activity activity;
    private ListView spotifyListview;
    private ListView mpdListview;
    private boolean spotifyVisible=true;
    protected DrawerLayout mDrawerLayout;
    private FloatingActionButton swapPlaylist;
    protected boolean drawerActive=false;
    private boolean shouldClick=false;
    private int xcoord=0;
    public PlanetAdapter albumAdapter;
    public boolean footerVisible = true;
    private ArrayList<String> albumList;
    public ArrayList<PlaylistItem> albumTracks;
    private PlaylistAdapter adapterMpd;
    private TextView timeField;
    private TextView totalField;
    private TextView titleField;
    private TextView artistField;
    private ImageView imageField;
    private int position=-1;
    private ListView drawerListRight;
    private ArrayList<String> itemsArray;
    private MenuAdapter menuAdapter;

    protected void onStop() {
        MainActivity.headers.removeItem(this);
    }

    public LeftDrawerPlaylist(Activity activity, /*MpdInterface mpdInterface,*/ int newalbumsdrawer_layout, int newalbumsdrawer_list, int newalbumsmpddrawer_list, int fabswapplaylist) {
        this.activity=activity;this.newalbumsdrawer_layout=newalbumsdrawer_layout;
        this.newalbumsdrawer_list=newalbumsdrawer_list;
        this.newalbumsmpddrawer_list=newalbumsmpddrawer_list;
        this.fabswapplaylist=fabswapplaylist;
        albumList = new ArrayList<>();
        albumTracks = new ArrayList<>();
        mDrawerLayout = (DrawerLayout) activity.findViewById(newalbumsdrawer_layout);
        albumAdapter = MainActivity.getTracksAdapter(mDrawerLayout, spotifyListview, albumList, albumTracks);
        spotifyListview = (ListView) activity.findViewById(newalbumsdrawer_list);
        mpdListview = (ListView) activity.findViewById(newalbumsmpddrawer_list);
        adapterMpd = new PlaylistAdapter(MainActivity.getThis.playFragment, this, MainActivity.getThis.getLogic().getPlaylistFiles(), MainActivity.getThis.getApplicationContext());
        mpdListview.setAdapter(adapterMpd);
        swapPlaylist = (FloatingActionButton) activity.findViewById(fabswapplaylist);
        spotifyListview.setAdapter(albumAdapter);
        SpotifyFragment.checkAddress();
        mpdListview.setOnItemClickListener((parent, view, position, id) -> {
            //Log.v("samba","Play:"+position);
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
                                //Log.v("samba","close drawers100");
                                mDrawerLayout.closeDrawers();
                            } else {
                                try{
                                if (drawerListRight!=null)
                                drawerListRight.performClick();
                                performClickOnRightDrawer();
                                shouldClick = false;
                                if (position>=0 &&(!(getItemsArray().get(position).equals("sep")))&& getItemsArray().get(position).length()>0) {
                                    //Log.v("samba","close drawers");
                                    //mDrawerLayout.closeDrawers();
                                    //return true;
                                } else {
                                    //Log.v("samba","item<0");
                                    return true;
                                }
                            }catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
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
                return true;
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
        LinearLayout ll = ((LinearLayout) activity.findViewById(R.id.time_layout));
        ll.setOnClickListener(v -> MainActivity.playPauseAll());
        ll = ((LinearLayout) activity.findViewById(R.id.song_title));

        ll.setOnLongClickListener(v -> {
            MainActivity.displayLargeTime(activity);
            return true;
        });
        connectListenersToThumbnail();
        footerVisible=setFooterVisibility(footerVisible);
        setListenersForButtons();
        MainActivity.headers.add(this);
        getDrawerSpotifyLayout();
    }
    void connectListenersToThumbnail() {
        ImageView im = ((ImageView) activity.findViewById(R.id.thumbnail_top));
        im.setOnLongClickListener(v -> {
            footerVisible=setFooterVisibility(footerVisible);
            return true;
        });
        im.setOnClickListener(v -> {
            footerVisible=setFooterVisibility(footerVisible);
        });
    }
    protected boolean setFooterVisibility(boolean footerVisible) {
        LinearLayout footerView = (LinearLayout) activity.findViewById(R.id.footer);
        if (footerVisible)
            footerView.setVisibility(View.GONE);
        else
            footerView.setVisibility(View.VISIBLE);
        footerVisible = !footerVisible;
        return footerVisible;
    }

    private void setListenersForButtons() {
        //View playbutton = findViewById(R.id.playspotify);
        View stopbutton = activity.findViewById(R.id.stopspotify);
        View playpausebutton = activity.findViewById(R.id.pausespotify);
        View previousbutton = activity.findViewById(R.id.previousspotify);
        View nextbutton = activity.findViewById(R.id.nextspotify);
        View volumebutton = activity.findViewById(R.id.volumespotify);
        View seekbutton = activity.findViewById(R.id.positionspotify);
        new Thread(() -> {
            SpotifyFragment.setListenersForButtons(activity, stopbutton, playpausebutton, previousbutton, nextbutton, volumebutton, seekbutton);
        }).start();

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
    public void getDrawerSpotifyPlaylist(GetSpotifyPlaylistClass pc) {
        TracksSpotifyPlaylist.getInstance().triggerPlaylist(this);
    }
    public void getSpotifyPlaylist() {
        TracksSpotifyPlaylist.getInstance().triggerPlaylist(this,true);
        TracksSpotifyPlaylist.getInstance().triggerPlaylist(this,40);
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
                //DebugLog.log("opened");
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
    public void setData(String time, String totalTime, String title, String artist, boolean spotifyList, int currentTrack) {
        titleField.setText(title);
        timeField.setText(time);
        if (!totalTime.equals("00:00")) {
            totalField.setVisibility(View.VISIBLE);
            artistField.setVisibility(View.VISIBLE);
            totalField.setText(totalTime);
            artistField.setText(artist);
        } else{
            MainActivity.getThis.currentArtist=title.split("-")[0].trim();
            totalField.setVisibility(View.GONE);
            artistField.setVisibility(View.GONE);
        }

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
        this.setItemsArray(itemsArray);
        drawerListRight = (ListView) activity.findViewById(R.id.DrawerListRight);
        //ArrayAdapter<String> drawerListRightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        menuAdapter = new MenuAdapter(activity,itemsArray);
        drawerListRight.setAdapter(menuAdapter);
        drawerListRight.setOnItemClickListener((parent, view, position, id) -> {
            if (!(itemsArray.get(position).equals("sep"))&&itemsArray.get(position).length()>0) {
                shouldClick = false;
                setPosition(position);
                mDrawerLayout.closeDrawers();
                //Log.v("samba","Select"+position+itemsArray.get(position));
            } else{
                //Log.v("samba","NOT Select"+position);
            }
        });

    }

    public void addItem(String url) {
        getItemsArray().add(url);
        menuAdapter.notifyDataSetChanged();
    }

    public ArrayList<String> getItemsArray() {
        return itemsArray;
    }

    public void setItemsArray(ArrayList<String> itemsArray) {
        this.itemsArray = itemsArray;
    }

    @Override
    public void spotifyPlaylistReturn(ArrayList<String> albumList1, ArrayList<PlaylistItem> albumTracks1, boolean force) {

        activity.runOnUiThread(() -> {
            //if (albumList1.size()==0)DebugLog.log("empty"); else
            //if (albumList1.size()!=albumList.size()||force)
            try{
                SpotifyPlaylistFragment.generateAdapterLists(SpotifyFragment.getData().tracksPlaylist,albumList,albumTracks);
                /*albumTracks.clear();
                albumList.clear();
                for (int i = 0; i < albumTracks1.size(); i++) {
                    PlaylistItem pi = new PlaylistItem();
                    PlaylistItem pi1 = albumTracks1.get(i);
                    pi.text = pi1.text;
                    pi.id = pi1.id;
                    pi.pictureVisible = pi1.pictureVisible;
                    pi.time = pi1.time;
                    pi.trackNumber = pi1.trackNumber;
                    pi.url = pi1.url;

                    //DebugLog.log(albumTracks.get(i).text);
                    albumTracks.add(pi);
                    albumList.add(albumList1.get(i));
                }*/

                //tracksAdapter.notifyDataSetChanged();
                if (mpdListview!=null)mpdListview.setVisibility(View.GONE);
                spotifyListview.setVisibility(View.VISIBLE);
                albumAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
            try{
            //albumAdapter.notifyDataSetChanged();
        }catch(Exception e){
            Log.v("samba", Log.getStackTraceString(e));}
        });    }
}
