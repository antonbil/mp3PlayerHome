package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.orm.SugarContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import examples.quickprogrammingtips.com.tablayout.adapters.ArtistAutoCompleteAdapter;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.model.Server;
import examples.quickprogrammingtips.com.tablayout.tools.ImageLoadTask;
import mpc.DatabaseCommand;
import mpc.MPC;
import mpc.MPCDatabaseListener;
import mpc.MPCListener;
import mpc.MPCSong;
import mpc.MPCStatus;


public class MainActivity extends AppCompatActivity implements MpdInterface, MPCListener, MPCDatabaseListener, OnTaskCompleted{

    //static final int STATIC_RESULT = 3; //positive > 0 integer.
    //static final int NEWALBUMS_RESULT = 15;
    static final int SPOTIFY_PLAYING = 1;
    public static final int MPD_PLAYING = 2;
    public static final int MPDTAB = 3;
    public static final int SMBTAB = 1;
    public static final int SPOTIFYTAB = 4;
    public static final int SPOTIFYPLAYLISTTAB = 6;
    public static final int SELECTTAB = 2;
    public static final int PLAYLISTTAB = 5;
    public static int playingStatus;
    private boolean footerVisible = false;
    private Logic logic;
    public Handler updateBarHandler;
    private int timerTime = 0;
    protected TabLayout tabLayout;
    private MainActivity mainActivity;
    public static HashMap<String, Bitmap> albumPictures = new HashMap<>();
    public String currentArtist;
    public ViewHolder viewHolder = new ViewHolder();


    public static MainActivity getThis;
    public static SpotifyInterface getSpotifyInterface;
    public ProgressDialog dialog;
    public Bitmap albumBitmap;
    public static boolean filterSpotify;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private boolean statusThread=false;
    SelectFragment selectFragment;
    SpotifyPlaylistFragment spotifyPlaylistFragment;

    PlaylistsFragment playlistFragment;
    PlayFragment playFragment;
    ListFragment listFragment;
    static DBFragment         dbFragment;
    SpotifyFragment spotifyFragment;

    public static void panicMessage(final String message) {
        //Let this be the code in your n'th level thread from main UI thread
        Handler h = new Handler(Looper.getMainLooper());
        h.post(() -> Toast.makeText(getThis, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //try{
        //Log.d("samba", "Text:1");
        super.onCreate(savedInstanceState);
            Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            final List<String> segments = intent.getData().getPathSegments();
            if (segments.size() > 1) {
                //Log.d("MainActivity", "Text:" + segments.get(1));
                Toast.makeText(getApplicationContext(), "Text:" + segments.get(1),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //Log.d("samba", "Text:2");

        SugarContext.init(this);//init db
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        mainActivity = this;
        getThis = this;
        getSpotifyInterface=new SpotifyInterface();
        dialog = new ProgressDialog(this);//keep it hidden until needed
        updateBarHandler = new Handler();
        //Log.d("samba", "Text:3");

        logic = new Logic(this);
        Handler customHandler = new Handler();

        //Log.v("samba",""+15);
        setContentView(R.layout.activity_main);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Log.d("samba", "Text:4");
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.hello_world,
                R.string.hello_world
        ) {PlanetAdapter albumAdapter;
            public void onDrawerClosed(View view) {
                //customHandler.removeCallbacks(updateTimerThread);
                albumAdapter=null;
            }

            public void onDrawerOpened(View drawerView) {
                ListView albumsListview = (ListView) findViewById(R.id.drawer_list);
                ArrayList<String> albumList = new ArrayList<>();
                ArrayList<PlaylistItem> albumTracks = new ArrayList<>();
                albumAdapter = new PlanetAdapter(albumList, getThis,albumTracks) {
                    @Override
                    public void removeUp(int counter) {
                        duplicateLists();

                        SpotifyFragment.removeUplist(this, albumsListview,counter, getThis);
                    }

                    @Override
                    public void onClickFunc(int counter) {
                        try {
                            getSpotifyInterface.previousTrack.id = "";
                        } catch (Exception e){Log.v("samba","error in starting song");};
                            //Log.v("samba","a");
                            duplicateLists();
                            //Log.v("samba","b");
                            SpotifyFragment.stopMpd();
                            //Log.v("samba","c");
                            SpotifyFragment.playlistGotoPosition(counter);
                    }

                    @Override
                    public void removeDown(int counter) {
                        duplicateLists();
                        SpotifyFragment.removeDownlist(this, albumsListview,counter,getThis);

                    }

                    @Override
                    public void removeAlbum(int counter) {
                        duplicateLists();
                        SpotifyFragment.removeAlbum(this, counter, albumsListview,getThis);

                    }

                    private void duplicateLists() {
                        SpotifyFragment.albumList=albumList;
                        SpotifyFragment.albumTracks=albumTracks;
                    }

                    @Override
                    public void addAlbumToFavoritesAlbum(int counter) {

                    }

                    @Override
                    public void addAlbumToFavoritesTrack(int counter) {
                        duplicateLists();
                        SpotifyFragment.addAlbumToFavoritesTrackwise(counter);

                    }

                    @Override
                    public void removeTrack(int counter) {
                        duplicateLists();
                        SpotifyFragment.removeTrackSpotify(counter);

                    }

                    @Override
                    public void displayArtist(int counter) {
                        mDrawerLayout.closeDrawers();
                        getThis.callSpotify(SpotifyFragment.tracksPlaylist.get(counter).artists.get(0).name);


                    }

                    @Override
                    public void displayArtistWikipedia(int counter) {
                        String s = SpotifyFragment.tracksPlaylist.get(counter).artists.get(0).name;
                        MainActivity.startWikipediaPage(s);
                    }

                    @Override
                    public void replaceAndPlayAlbum(int counter) {

                    }

                    @Override
                    public void addAndPlayAlbum(int counter) {

                    }

                    @Override
                    public void albumArtistWikipedia(int counter) {

                    }

                    @Override
                    public void addAlbum(int counter) {
                        SpotifyFragment.getAlbumtracksFromSpotify(SpotifyFragment.tracksPlaylist.get(counter).album.id, SpotifyFragment.tracksPlaylist.get(counter).artists.get(0).name
                                , getThis, this, albumsListview);

                    }

                    @Override
                    public void addAlbumNoplay(int counter) {
                    }
                };
                albumsListview.setAdapter(albumAdapter);
                SpotifyFragment.checkAddress();
                SpotifyFragment.refreshPlaylistFromSpotify(1,albumAdapter, getThis,albumList,albumTracks);
                LinearLayout viewHeader = (LinearLayout) findViewById(R.id.song_display2);
                final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabspotifydrawerlist);
                fab.setOnClickListener(view -> SpotifyFragment.showPlayMenu(getThis,viewHeader));

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        //Log.v("samba",""+16);

        //Log.d("samba", "Text:5");
        LinearLayout ll = ((LinearLayout) findViewById(R.id.time_layout));
        ll.setOnClickListener(v -> playPauseAll());//android:id="@+id/song_title"
        ll = ((LinearLayout) findViewById(R.id.song_title));
        ll.setOnClickListener(v -> {
            mDrawerLayout.closeDrawer(GravityCompat.START,false);
            callSpotify(currentArtist);
        });
        ll.setOnLongClickListener(v -> {
            MainScreenDialog msDialog = new MainScreenDialog(getThis);
            msDialog.show();
            return true;
        });
        connectListenersToThumbnail();
        //Log.d("samba", "Text:6");
            //TODO: make it for-statement (6 times!)
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabTextColors(Color.WHITE, R.color.accent_material_dark);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        for (int i=0;i<7;i++)
        tabLayout.addTab(tabLayout.newTab());
        int[] imageResId = {
                R.drawable.play,
                R.drawable.smb,
                R.drawable.ic_sync_black_24dp,
                R.drawable.mpd,
                R.drawable.spotifylist,
                R.drawable.swan1,
                R.drawable.spf
        };
        playFragment = new PlayFragment();
        for (int i=0;i<tabLayout.getTabCount();i++){
            tabLayout.getTabAt(i).setIcon(imageResId[i]);
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        new Thread(() -> {
            selectFragment = new SelectFragment();
            TabLayout.Tab tab = tabLayout.getTabAt(SELECTTAB);
            tab.select();
            tab = tabLayout.getTabAt(0);
            tab.select();
            //Log.d("samba", "Text:9");

            try{
                spotifyFragment=new SpotifyFragment();}
            catch (Exception e){Log.v("samba","error spotify create");}
            listFragment = new ListFragment();
            try{
                spotifyPlaylistFragment = new SpotifyPlaylistFragment();
            }
            catch (Exception e){Log.v("samba","error spotify playlist create");}
            try{
                dbFragment = new DBFragment();}
            catch (Exception e){Log.v("samba","error spotify create");}
            playlistFragment = new PlaylistsFragment();

        }).start();

        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#00FFFF"));


            //Log.d("samba", "Text:7");
        //Log.v("samba",""+17);
        this.setTitle("");
        final LinearLayout footerView = (LinearLayout) findViewById(R.id.footer);
        footerView.setVisibility(View.GONE);
        final FloatingActionButton findbutton = (FloatingActionButton) findViewById(R.id.find);
        findbutton.setOnClickListener(v -> {
            SpotifyFragment.nextCommand="search artist";
            startPlaylistSpotify();
        });        FloatingActionButton FAB = (FloatingActionButton) findViewById(R.id.fab);
        findbutton.setVisibility(View.GONE);
        //Log.d("samba", "Text:8");
        FAB.setOnClickListener(v -> {
                        SpotifyFragment.nextCommand="search artist";
            //Log.v("samba","search"+1);
                        startPlaylistSpotify();
        });

            try {
                Toolbar tool = (Toolbar) findViewById(R.id.app_bar);//cast it to ToolBar
                //Log.v("samba", "b" + 19);
                setSupportActionBar(tool);
            }
            catch (Exception e){Log.v("samba","error in setting up tool");}
            //Log.d("samba", "Text:9");


        //Log.v("samba",""+20);
        new Thread(() -> {
            try{
                setListenersForButtons();
            }
            catch (Exception e){Log.v("samba","error setting listeners");}
            updateDisplay();
            SpotifyFragment.getOnlyPlaylistFromSpotify(1, SpotifyFragment.albumList, SpotifyFragment.albumTracks);

        }).start();
        //Log.d("samba", "Text:10");

            try{
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        ArtistAutoCompleteAdapter.getAllFilenames();
        //Log.d("samba", "Text:11");
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(!Logic.hasbeen)
            Toast.makeText(this, "No connection with "+ Server.servers.get(Server.getServer(this)).url, Toast.LENGTH_SHORT).show();
        }, 400);
        }
        catch (Exception e){Log.v("samba","error setting handler");}
            try{
            cleanUp();
        }
        catch (Exception e){Log.v("samba","error cleanup");}

    }

    private void setCurrentTabFragment(int tabPosition)
    {
        switch (tabPosition)
        {

            case 0 :
                replaceFragment(playFragment);
                break;
            case SMBTAB :
                replaceFragment(listFragment);
                break;
            case SELECTTAB:
                replaceFragment(selectFragment);
                break;
            case SPOTIFYTAB :
                //if(spotifyFragment != null)
                //    getSupportFragmentManager().beginTransaction().remove(spotifyFragment).commit();
                replaceFragment(spotifyFragment);
                break;
            case MPDTAB :
                replaceFragment(dbFragment);
                break;
            case PLAYLISTTAB:
                replaceFragment(playlistFragment);
                break;
            case SPOTIFYPLAYLISTTAB :
                replaceFragment(spotifyPlaylistFragment);
                break;
        }
    }
    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragment).commit();
    }
    void connectListenersToThumbnail() {
        ImageView im = ((ImageView) findViewById(R.id.thumbnail_top));
        im.setOnLongClickListener(v -> {
            setFooterVisibility();
            return true;
        });
        im.setOnClickListener(v -> {
            setFooterVisibility();
        });
    }

    static void playPauseAll() {
        if (SpotifyFragment.playingEngine==1){
            SpotifyFragment.playPauseSpotify();
        }else
        MainActivity.getThis.playPause();
    }

    public void startPlaylistSpotify() {
        try {
            /*Intent intent = new Intent(MainActivity.getThis, SpotifyActivity.class);
            intent.putExtra("artist", "nosearch");

            MainActivity.getThis.startActivity(intent);*/
            SpotifyFragment.artistName="nosearch";
            //Log.v("samba","search"+2);

            callSpotify();
            //hiermoetiets
            //Log.v("samba", "Spotify");
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //e.printStackTrace();
        }
    }

    public void callSpotify(String currentArtist) {
        try {
            //Intent intent = new Intent(getThis, SpotifyActivity.class);
            //intent.putExtra("artist", currentArtist);

            //getThis.startActivityForResult(intent, 4);
            SpotifyFragment.artistName=currentArtist;
            //Log.v("samba", "call artist! with:"+currentArtist);
            callSpotify();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //e.printStackTrace();
        }

    }

    public void callSpotify() {
        SpotifyFragment.explicitlyCalled=true;

        getSupportFragmentManager()
                    .beginTransaction()
                    .detach(spotifyFragment)
                    .attach(spotifyFragment)
                    .commit();

        tabLayout.getTabAt(SPOTIFYTAB).select();


    }

    public void callSpotifyPlaylist() {
        SpotifyFragment.explicitlyCalled=true;

        if(tabLayout.getSelectedTabPosition()>=5)
        getSupportFragmentManager()
                .beginTransaction()
                .detach(spotifyPlaylistFragment)
                .attach(spotifyPlaylistFragment)
                .commit();
        tabLayout.getTabAt(SPOTIFYPLAYLISTTAB).select();

    }

    public void callSpotifyPlaylist(String currentArtist) {
        /*
        does not work; libspotify does not support spotify readio-stations
         */
        String id= SpotifyFragment.searchSpotifyArtist(currentArtist);
        if (id.length()>0)
        try {
            id="spotify:station:artist:"+id;
            final ProgressDialog loadingdialog;
            loadingdialog = ProgressDialog.show(getThis,
                    "","Loading, please wait",true);
            SpotifyFragment.clearSpotifyPlaylist();
            new SpotifyFragment.getEntirePlaylistFromSpotify(id,MainActivity.getThis){
                @Override
                public void atLast() {
                    loadingdialog.dismiss();
                    SpotifyFragment.playAtPosition(0);
                    MainActivity.getThis.startPlaylistSpotify();
                }
            }.run();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //Log.v("samba", Log.getStackTraceString(e));
        }


    }
    public void playPause() {
        if (logic.getPaused()) {
            logic.getMpc().play();
            logic.setPaused(false);
        } else {
            logic.getMpc().pause();
            logic.setPaused(true);

        }
    }

    private void cleanUp(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    if (statusThread) {
                        statusThread = false;
                        statusUpdate(logic.mpcStatus);
                    }
                } catch (Exception e) {

                    Log.v("samba","cleanup");
                }
            }

        }, 0, 10000);//Update text every second

    }

    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    MPC mpc = logic.getMpc();
                    if (timerTime >= 3) {
                        playlistGetContent(mpc, MainActivity.getThis);
                        timerTime = 0;
                    } else timerTime++;
                    mpc.getStatusSynch();
                } catch (Exception e) {
                    Log.v("samba","updateDisplay");

                }
            }

        }, 0, 1000);//Update text every second
    }

    protected void setFooterVisibility() {
        LinearLayout footerView = (LinearLayout) findViewById(R.id.footer);
        if (footerVisible)
            footerView.setVisibility(View.GONE);
        else
            footerView.setVisibility(View.VISIBLE);
        footerVisible = !footerVisible;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void onGroupItemClick(MenuItem item) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.display_footer);
        checkable.setChecked(footerVisible);

        return true;
    }

    @Override
    public void onBackPressed() {
        int tabSelected=tabLayout.getSelectedTabPosition();;
        if ((tabSelected== SMBTAB)||(tabSelected==MPDTAB)) {
            if (tabSelected == SMBTAB)
                listFragment.back();
            if (tabSelected == MPDTAB) dbFragment.back();
        }
        else
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a logic activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(MainActivity.this,
                    SettingsActivity.class);
            startActivity(myIntent);
            return true;
        }
        if (id == R.id.set_volume) {
            setVolume(getThis);
        }
        if (id == R.id.new_albums_categories) {
            SpotifyFragment.nextCommand="new_albums_categories";
            callSpotifyPlaylist();
            return true;
        }
        if (id == R.id.search_album) {
            SpotifyFragment.nextCommand="search album";
            callSpotifyPlaylist();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.display_footer) {
            boolean isChecked = !item.isChecked();
            item.setChecked(isChecked);
            setFooterVisibility();
            return true;
        }
        if ((id == R.id.search_option)) {//playlists_option
            searchTerm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void startWikipediaPage(String outsiders) {
        Intent myIntent = new Intent(getThis,//MainActivity.this,
                WikipediaActivity.class);
        myIntent.putExtra("searchitem", outsiders);
        getThis.startActivity(myIntent);
    }

    public void searchTerm() {
        searchTerm("");
    }

    public void searchTerm(String myterm) {
        myterm = myterm.trim();
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectTab(2);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, dbFragment).commit();
            }
        }, 100);

        View inflate = getLayoutInflater().inflate(R.layout.activity_search, null);
        alert.setView(inflate);
        final EditText searchEditText = (EditText) inflate.findViewById(R.id.search);
        searchEditText.setText(myterm);

        Button save = (Button) inflate.findViewById(R.id.save_search_button);
        save.setOnClickListener(v -> {
            try {
                final String searchString = searchEditText.getText().toString();
                searchForItem(searchString);
                alert.dismiss();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();

            }

        });

        alert.show();

    }

    public void searchForItem(String searchString) {
        //Log.v("samba", "search:" + searchString);
        //selectTab(2);
        new DatabaseCommand(logic.getMpc(), "find any \"" + searchString + "\"", dbFragment, true).run();
    }

    public void setVolume(Activity activity) {
        if (SpotifyFragment.isPlaying()){
            SpotifyFragment.setVolume(activity);
            return;
        }

            final AlertDialog.Builder alert = new AlertDialog.Builder(activity);

        alert.setTitle("Volume");

        LinearLayout linear = new LinearLayout(this);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(this);
        text.setPadding(10, 10, 10, 10);

        Integer volume = logic.mpcStatus.volume;
        text.setText("" + volume);
        SeekBar seek = new SeekBar(this);

        seek.setProgress(volume);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText("" + progress);
                logic.getMpc().setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        linear.addView(seek);
        linear.addView(text);

        alert.setView(linear);


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public static void displayLargeImage(Context context, Bitmap bitmap) {
        final AlertDialog alert = new AlertDialog.Builder(context).create();

        LinearLayout linear = new LinearLayout(context);

        linear.setOrientation(LinearLayout.VERTICAL);
        ImageView image = new ImageView(context);
        //get width of screen
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        //fit image to width of screen, keep aspect ratio
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width - 140, width - 140);
        image.setLayoutParams(layoutParams);
        image.setImageBitmap(bitmap);
        image.setOnClickListener(v -> alert.dismiss());
        linear.addView(image);
        alert.setView(linear);

        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == 441) {
            Bundle extras = data.getExtras();


            String urlString = extras.getString("url");
            //Log.v("samba", "return:" + urlString);
            Toast.makeText(MainActivity.this, "return:" + urlString, Toast.LENGTH_SHORT).show();

        }
        //Log.v("samba", "in hoofd-activity");
        //Log.v("samba", "requestcode:" + resultCode);
        if (resultCode == 23) {Log.v("samba", "get favorites");
            SelectFragment.getThis.getFavorites();return;}
        //Log.v("samba", "in hoofd-activity erna");
        {
            if (resultCode == Activity.RESULT_OK) {
                //spotify window asks for search of artist
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selectTab(2);
                        //give the program time to restore saved instance
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, dbFragment).commit();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //start searching if right tab is selected
                                searchForItem(data.getExtras().getString("artist"));
                            }
                        }, 100);
                    }
                }, 100);


            } else {
                // the result code is different from the one you've finished with, do something else.
            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible=true;
    @Override
    protected void onResume() {
        super.onResume();
        activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused();
    }
    public Logic getLogic() {
        return logic;
    }

    public void playlistGetContent() {
        playlistGetContent(logic.getMpc(), MainActivity.getThis);
    }

    public void playlistGetContent(final MPC mpc, final MpdInterface mpdInterface) {
        AsyncTask thread = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                if (!MainActivity.activityVisible)return true;
                String address = logic.getMpc().getAddress();
                Socket sock = null;
                BufferedReader in = null;
                PrintWriter out = null;

                List<MPCSong> songs;
                ArrayList<Mp3File> playlist;
                // Establish socket connection
                try {
                    sock = new Socket();
                    sock.connect(new InetSocketAddress(mpc.getAddress(), mpc.getPort()), mpc.timeout);
                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    out = new PrintWriter(sock.getOutputStream(), true);
                    out.println("playlistinfo");
                    // Clear version number from buffer
                    in.readLine();
                    playlist = new ArrayList<Mp3File>();
                    String response;
                    ArrayList<String> list = new ArrayList<String>();
                    while (((response = in.readLine()) != null)) {
                        if (response.startsWith("OK")) break;
                        list.add(response);
                        if (response.startsWith("Id:")) {
                            playlist.add(new Mp3File("", list));
                        }
                    }
                    try {
                        sock.close();
                        if (in != null) in.close();
                        if (out != null) out.close();
                    } catch (Exception e) {
                        Log.v("samba","playlist get content");
                    }

                    //see if address has not changed while getting information
                    if (address != logic.getMpc().getAddress()) return true;
                    boolean change = false;
                    int max = playlist.size();
                    if (max == 0) {
                        logic.getPlaylistFiles().clear();
                        change = true;

                    } else {

                        if (logic.getPlaylistFiles().size() < max) {
                            max = logic.getPlaylistFiles().size();
                            change = true;
                        }

                        for (int i = 0; i < max; i++) {
                            if (!(playlist.get(i).getTitle() == logic.getPlaylistFiles().get(i).getTitle()))
                                change = true;
                        }
                        if (change) {
                            logic.getPlaylistFiles().clear();
                            logic.getPlaylistFiles().addAll(playlist);
                        }
                        String albumName = "";
                        String previousAlbumName = "";

                        for (final Mp3File f : logic.getPlaylistFiles()) {
                            if (f.getBitmap() != null) continue;
                            f.setStartAlbum(!f.getAlbum().equals(albumName));

                            albumName = f.getAlbum();
                            final String niceAlbumName = f.niceAlbum();
                            if (previousAlbumName.equals(niceAlbumName))continue;
                            previousAlbumName=niceAlbumName;
                            if (albumPictures.containsKey(niceAlbumName)) {
                                if (albumPictures.get(niceAlbumName) != null)
                                    f.setBitmap(albumPictures.get(niceAlbumName));
                            } else try {
                                albumPictures.put(niceAlbumName, null);//so image is loaded only once
                                try {
                                    URL urlConnection = new URL(Logic.getUrlFromSongpath(f).replace(" ", "%20"));
                                    //Log.v("samba","get:"+url);
                                    HttpURLConnection connection = (HttpURLConnection) urlConnection
                                            .openConnection();
                                    connection.setInstanceFollowRedirects(false);
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                                    albumPictures.put(niceAlbumName, bitmap);
                                    for (Mp3File f1 : logic.getPlaylistFiles()) {
                                        if (f1.niceAlbum().equals(niceAlbumName))
                                            f1.setBitmap(bitmap);
                                    }
                                    connection.disconnect();
                                } catch (Exception e) {

                                    albumPictures.remove(niceAlbumName);
                                    Log.v("samba", "error connect " + Logic.getUrlFromSongpath(f));
                                }

                            } catch (Exception e) {
                                Log.v("samba","playlistgetcontent");
                            }

                        }
                    }
                    mpdInterface.playlistCall(playlist, change);
                } catch (Exception e) {
                    mpc.connectionFailed("Connection failed, check settings");
                }


                return true;
            }
        };
        thread.execute();
    }

    public void selectTab(int tab) {
        tabLayout.getTabAt(tab).select();
    }

    @Override
    public void playlistCall(ArrayList<Mp3File> playlist, boolean change) {
        if (tabLayout.getSelectedTabPosition() == 0)
            playFragment.playlistCall(playlist, change);

    }

    @Override
    public void newMpdCall(Mp3File mp3File, int position, String command) {

    }

    @Override
    public void printCover(final Bitmap result, final ImageView image, String album) {
        if (result != null) {

            albumPictures.put(album, result);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //ImageView thumbnail=(ImageView) findViewById(R.id.thumbnail_top);
                    image.setImageBitmap(result);
                }
            });
        } else {

            Log.v("samba", "Image Does Not exist or Network Error");
            Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void connectionFailed(String message) {

    }

    @Override
    public void databaseCallCompleted(ArrayList<File> files) {

        dbFragment.databaseCallCompleted(files);


    }

    @Override
    public void databaseFindCompleted(ArrayList<File> files) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void databaseUpdated() {
    }

    @Override
    public void databaseUpdateProgressChanged(int progress) {

    }

    private void checkButtons(int prev){
        if (prev!= SpotifyFragment.playingEngine) {
            setListenersForButtons();
        }
    }

    private void setListenersForButtons() {
        //View playbutton = findViewById(R.id.playspotify);
        View stopbutton = findViewById(R.id.stopspotify);
        View playpausebutton = findViewById(R.id.pausespotify);
        View previousbutton = findViewById(R.id.previousspotify);
        View nextbutton = findViewById(R.id.nextspotify);
        View volumebutton = findViewById(R.id.volumespotify);
        View seekbutton = findViewById(R.id.positionspotify);
        new Thread(() -> {
            SpotifyFragment.setListenersForButtons(this, stopbutton, playpausebutton, previousbutton, nextbutton, volumebutton, seekbutton);
        }).start();

    }

    @Override
    public void statusUpdate(MPCStatus newStatus) {

        if (statusThread) return;

        new Thread(() -> {
            statusThread=true;

            int prev= SpotifyFragment.playingEngine;

            if (SpotifyFragment.isPlaying()){
                try{
                if (SpotifyFragment.getThis.albumAdapter!=null)
                    SpotifyFragment.playingEngine=1;
            } catch (Exception e) {
            }

                //(findViewById(R.id.time_top2)).setOnClickListener(v -> SpotifyActivity.playPauseSpotify());
                //(findViewById(R.id.totaltime_top2)).setOnClickListener(v -> SpotifyActivity.playPauseSpotify());
                ImageView viewById = (ImageView) findViewById(R.id.thumbnail_top);
                //viewById.setOnClickListener(v -> SpotifyActivity.setVolume(getThis));
                 if (SpotifyFragment.busyupdateSongInfo) {
                     try {
                         Log.v("samba", "nu binnen");
                    String[] trid1 = SpotifyFragment.getCurrentTrack();//
                    String trid = "0";
                        trid = trid1[0];
                    if (trid.length() > 0) {
                        //currentTrack=0;
                        if (SpotifyFragment.getThis.albumAdapter != null)
                            for (int i = 0; i < SpotifyFragment.tracksPlaylist.size(); i++) {
                                if (SpotifyFragment.tracksPlaylist.get(i).id.equals(trid)) {
                                    if (SpotifyFragment.currentTrack != i)
                                        SpotifyFragment.getThis.albumsListview.setItemChecked(SpotifyFragment.currentTrack, false);
                                    SpotifyFragment.currentTrack = i;
                                    //Log.v("samba", "current track:" + i + "," + tracksPlaylist.get(i).name);
                                    break;
                                }
                            }

                        if (SpotifyFragment.getThis.albumAdapter != null) {
                            SpotifyFragment.getThis.albumAdapter.setCurrentItem(SpotifyFragment.currentTrack);
                            MainActivity.getThis.runOnUiThread(() -> {
                                SpotifyFragment.getThis.albumAdapter.notifyDataSetChanged();
                            });
                        }
                    }
                     } catch (Exception e) {
                         Log.v("samba", Log.getStackTraceString(e));
                     }
                } else {
                     PlanetAdapter albumAdapter = null;
                     if (SpotifyFragment.getThis!=null)
                         albumAdapter = SpotifyFragment.getThis.albumAdapter;
                     ListView albumsListview = null;
                     if (SpotifyFragment.getThis!=null)
                         albumsListview = SpotifyFragment.getThis.albumsListview;
                     currentArtist = SpotifyFragment.updateSongInfo((TextView) findViewById(R.id.time_top),
                             (TextView) findViewById(R.id.totaltime_top),
                             (TextView) findViewById(R.id.title_top),
                             (TextView) findViewById(R.id.artist_top),
                             viewById,
                             albumAdapter, albumsListview,getThis,getSpotifyInterface);
                 }

                checkButtons(prev);
                MainActivity.playingStatus=MainActivity.SPOTIFY_PLAYING;
                statusThread=false;

                return;

            }
        //        if (SpotifyActivity.playingEngine==1){SpotifyActivity.getThis.playButtonsAtBottom();}
            //checkButtons(prev);

            SpotifyFragment.playingEngine=2;
            final MPCStatus status = newStatus;
            logic.mpcStatus = newStatus;
            if (status.song == null) {

                statusThread=false;
                return;}
            if (status.playing){
                if (SpotifyFragment.playingEngine==1){setListenersForButtons();}
                SpotifyFragment.playingEngine=2;
            }
            //if (MainActivity.activityVisible)
            {
                //Handler h = new Handler(Looper.getMainLooper());
                //h.post(() -> {
                    //Log.v("samba","tijd:"+newStatus.time.toString());

                    if (status.song.intValue() < logic.getPlaylistFiles().size())

                        runOnUiThread(() -> {
                            ViewHolder vh = getThis.viewHolder;
                            try {
                                Mp3File currentSong = logic.getPlaylistFiles().get(status.song.intValue());

                                TextView tvName = (TextView) findViewById(R.id.title_top);
                                final String title = currentSong.getTitle();
                                tvName.setText(title);
                                vh.title = title;
                                //Log.v("samba", currentSong.getTitle());
                                //Log.v("samba", currentSong.getArtist());
                                TextView time = (TextView) findViewById(R.id.time_top);
                                final String time1 = Mp3File.niceTime(status.time.intValue());
                                vh.time = time1;
                                time.setText(time1);
                                TextView totaltime = (TextView) findViewById(R.id.totaltime_top);
                                try {
                                    final String timeNice = currentSong.getTimeNice();
                                    totaltime.setText(timeNice);
                                    vh.totaltime = timeNice;
                                } catch (Exception e) {
                                    totaltime.setText("00:00");
                                }
                                String album = "";
                                TextView artist = (TextView) findViewById(R.id.artist_top);
                                try {
                                    album = currentSong.niceAlbum();
                                    currentArtist = currentSong.getArtist();
                                    artist.setText(album);
                                    vh.album = album;
                                } catch (Exception e) {
                                    artist.setText("statusupdate");
                                }
                                final ImageView image = (ImageView) findViewById(R.id.thumbnail_top);
                                String uri = Logic.getUrlFromSongpath(currentSong);

                                if (albumPictures.containsKey(album)) {
                                    final Bitmap b = albumPictures.get(album);
                                    albumBitmap = b;
                                    currentSong.setBitmap(b);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            image.setImageBitmap(b);
                                        }
                                    });
                                } else {
                                    albumPictures.put(album, null);


                                    new ImageLoadTask(uri, album, mainActivity, image).execute();


                                }//
                                if (playFragment!=null)playFragment.updateCurrentSong();
                                MainActivity.playingStatus = MainActivity.MPD_PLAYING;
                                //Log.v("samba",uri);
                            } catch (Exception e) {
                                statusThread=false;
                                Log.v("samba","statusupdate");

                                //mpc.connectionFailed("Connection failed, check settings");
                                //t.stop();
                            }
                        });
                //});
            }
            statusThread=false;

        }).start();
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        SugarContext.terminate();
        try {
            trimCache(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.v("samba","ondestroy");

            e.printStackTrace();
        }
    }

    public static void trimCache(Context context) {
        try {
            java.io.File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            // TODO: handle exception
            Log.v("samba","trimcache");

        }
    }

    public static boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new java.io.File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://examples.quickprogrammingtips.com.tablayout/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://examples.quickprogrammingtips.com.tablayout/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public class ViewHolder {
        public String totaltime;
        public String time;
        public String title;
        public String album;

    }

    @Override
    public void onTaskCompleted(String result, String call) {
        Log.v("samba", result);
    }
}
