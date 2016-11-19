package examples.quickprogrammingtips.com.tablayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import com.orm.SugarContext;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import examples.quickprogrammingtips.com.tablayout.adapters.ArtistAutoCompleteAdapter;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.HeaderHandler;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.model.Server;
import examples.quickprogrammingtips.com.tablayout.tools.ImageLoadTask;
import kaaes.spotify.webapi.android.models.Track;
import mpc.DatabaseCommand;
import mpc.MPC;
import mpc.MPCDatabaseListener;
import mpc.MPCListener;
import mpc.MPCStatus;


public class MainActivity extends AppCompatActivity implements MpdInterface, MPCListener, MPCDatabaseListener, OnTaskCompleted,HeaderSongInterface {

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
    public static int playingStatus= MPD_PLAYING;

    private Logic logic;
    private HeaderHandler headers;

    public Handler updateBarHandler;
    private int timerTime = 0;
    protected TabLayout tabLayout;
    private MainActivity mainActivity;
    private HashMap<String, Bitmap> albumPictures = new HashMap<>();
    private HashMap<String, String> albumPicturesIds = new HashMap<>();
    public String currentArtist;
    public ViewHolder viewHolder = new ViewHolder();
    public FillListviewWithValues fillListviewWithValues;
    public Document spotifyShortcutsDoc;
    public Elements trackelements;

    private static MainActivity instance;
    private SpotifyInterface getSpotifyInterface;
    public ProgressDialog dialog;
    public Bitmap albumBitmap;
    public static boolean filterSpotify;
    private static final int REQUEST_WRITE_STORAGE = 112;

    private boolean statusThread=false;
    SelectFragment selectFragment;
    SpotifyPlaylistFragment spotifyPlaylistFragment;

    PlaylistsFragment playlistFragment;
    PlayFragment playFragment;
    ListFragment listFragment;
    private DBFragment         dbFragment;
    private SpotifyFragment spotifyFragment;
    public LeftDrawerPlaylist leftDrawerPlaylist;
    private Timer secondTimer;
    public ImageLoader imageLoader;

    public static void panicMessage(final String message) {
        //Let this be the code in your n'th level thread from main UI thread
        Handler h = new Handler(Looper.getMainLooper());
        h.post(() -> Toast.makeText(getInstance(), message, Toast.LENGTH_SHORT).show());
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public static HashMap<String, Bitmap> getAlbumPictures() {
        return getInstance().albumPictures;
    }

    public static HashMap<String, String> getAlbumPicturesIds() {
        return getInstance().albumPicturesIds;
    }

    public static HeaderHandler getHeaders() {
        return getInstance().headers;
    }

    public static void setHeaders(HeaderHandler headers) {
        getInstance().headers = headers;
    }

    public static DBFragment getDbFragment() {
        return getInstance().dbFragment;
    }

    public static void setDbFragment(DBFragment dbFragment) {
        getInstance().dbFragment = dbFragment;
    }

    public static SpotifyInterface getSpotifyInterface() {
        return getInstance().getSpotifyInterface;
    }

    public static void setSpotifyInterface(SpotifyInterface getSpotifyInterface) {
        getInstance().getSpotifyInterface = getSpotifyInterface;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //reload my activity with permission granted or use the features what required the permission
                } else
                {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
    @SuppressLint("PrivateResource")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            mainActivity = this;
            instance = this;
            imageLoader =new ImageLoader(getApplicationContext());
            imageLoader.setActivity(this);
            ShutDownReceiver shutDownReceiver = new ShutDownReceiver();
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(shutDownReceiver, filter);//shutDownReceiver.wasScreenOn
            //DebugLog.log("Text:1");
            //headers.add(this);
            super.onCreate(savedInstanceState);
            setHeaders(new HeaderHandler());
            Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this
            ));
            trimCache(this);
            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }            final Intent intent = getIntent();
            final String action = intent.getAction();

            if (Intent.ACTION_VIEW.equals(action)) {
                final List<String> segments = intent.getData().getPathSegments();
                if (segments.size() > 1) {
                    //Log.d("MainActivity", "Text:" + segments.get(1));
                    Toast.makeText(getApplicationContext(), "Text:" + segments.get(1),
                            Toast.LENGTH_SHORT).show();
                }
            }
            //DebugLog.log("Text:2");

            SugarContext.init(this);//init db
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
            setSpotifyInterface(new SpotifyInterface());
            SpotifyData data = new SpotifyData();
            SpotifyFragment.setData(data);
            dialog = new ProgressDialog(this);//keep it hidden until needed
            updateBarHandler = new Handler();
            //DebugLog.log("Text:3");

            logic = new Logic(this);
            //DebugLog.log(""+15);
            setContentView(R.layout.activity_main);
            //DebugLog.log(""+16);

            //DebugLog.log("Text:5");
            ArrayList<String> menuItemsArray = new ArrayList<>(
                    Arrays.asList("Settings", "Large Display",
                            "sep", "Search mpd", "Search album", "sep", "New albums categories", "Dutch album top 100", "Billboard top albums", "Spotify Album Shortcuts", "sep", "Volume", "Refresh Spotify",
                            "sep", "Playlists", "sep", "Close"));
            leftDrawerPlaylist=new LeftDrawerPlaylist(this, /*this,*/ R.id.newalbumsdrawer_layout, R.id.newalbumsdrawer_list,
                    R.id.newalbumsmpddrawer_list, R.id.fabswapplaylist) {
                @Override
                public void performTouchEvent(MotionEvent event){

                }
                @Override
                public void performClickOnRightDrawer(){

                }

                @Override
                protected void doMenuAction(int position) {
                    switch (menuItemsArray.get(position)) {
                        case "Settings":
                            doSettings();
                            break;
                        case "Large Display":
                            displayLargeTime(MainActivity.getInstance());
                            break;
                        case "Search mpd":
                            searchTerm();
                            break;
                        case "Search album":
                            doSearchAlbum();
                            break;
                        case "New albums categories":
                            doNewAlbumsCategories();
                            break;//
                        case "Dutch album top 100":
                            doDutchAlbumTop40();
                            break;
                        //

                        case "Billboard top albums":
                            doBillboardAlbumTop200();
                            break;

                        case "Spotify Album Shortcuts":
                            doSpotifyAlbumShortcuts();
                            break;
                        case "Volume":
                            setVolume(getInstance());
                            break;
                        case "Playlists":
                            //DebugLog.log("miner");
                            Intent myIntent = new Intent(getInstance(), PlaylistsSpotifyActivity.class);
                            getInstance().startActivity(myIntent);
                            //DebugLog.log("miner");

                            break;
                        case "Refresh Spotify":
                            try{SpotifyPlaylistFragment.getInstance().setCurrentTracklist();} catch (Exception e) { /*empty*/       }
                            break;
                        case "Close":
                            getInstance().finish();
                            break;
                    }
                }
            };
            leftDrawerPlaylist.setMenu(menuItemsArray);
            LinearLayout ll = ((LinearLayout) findViewById(R.id.song_title));
            ll.setOnClickListener(v -> callSpotify(currentArtist));
            tabLayout = (TabLayout) findViewById(R.id.tabLayout);
            tabLayout.setTabTextColors(Color.WHITE, R.color.accent_material_dark);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            for (int i = 0; i < 7; i++)
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
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                //noinspection ConstantConditions
                tabLayout.getTabAt(i).setIcon(imageResId[i]);
            }
            //noinspection deprecation
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
                //DebugLog.log("Text:9");

                try {
                    spotifyFragment = new SpotifyFragment();
                } catch (Exception e) {
                    DebugLog.log("error spotify create");
                }
                listFragment = new ListFragment();
                try {
                    spotifyPlaylistFragment = new SpotifyPlaylistFragment();
                } catch (Exception e) {
                    DebugLog.log("error spotify playlist create");
                }
                try {
                    setDbFragment(new DBFragment());
                } catch (Exception e) {
                    DebugLog.log("error spotify create");
                }
                playlistFragment = new PlaylistsFragment();

                try {

                    //noinspection ConstantConditions
                    MainActivity.getInstance().runOnUiThread(() -> tabLayout.getTabAt(SELECTTAB).select());

                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }

            }).start();

            tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#00FFFF"));


            //DebugLog.log("Text:7");
            //DebugLog.log(""+17);
            this.setTitle("");
            final LinearLayout footerView = (LinearLayout) findViewById(R.id.footer);
            footerView.setVisibility(View.GONE);
            final FloatingActionButton findbutton = (FloatingActionButton) findViewById(R.id.find);
            findbutton.setOnClickListener(v -> {
                SpotifyFragment.nextCommand = "search artist";
                startPlaylistSpotify();
            });
            FloatingActionButton FAB = (FloatingActionButton) findViewById(R.id.fab);
            findbutton.setVisibility(View.GONE);
            //DebugLog.log("Text:8");
            FAB.setOnClickListener(v -> {
                SpotifyFragment.nextCommand = "search artist";
                //DebugLog.log("search"+1);
                startPlaylistSpotify();
            });

            try {
                Toolbar tool = (Toolbar) findViewById(R.id.app_bar);//cast it to ToolBar
                //DebugLog.log("b" + 19);
                setSupportActionBar(tool);
            } catch (Exception e) {
                DebugLog.log("error in setting up tool");
            }

            setTimerToUpdateDisplay();

            try {
                ArtistAutoCompleteAdapter.getAllFilenames();
                //DebugLog.log("Text:11");
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (!Logic.hasbeen)
                        Toast.makeText(this, "No connection with " + Server.servers.get(Server.getServer(this)).url, Toast.LENGTH_SHORT).show();
                }, 400);
            } catch (Exception e) {
                DebugLog.log("error setting handler");
            }
            try {
                setCleanupTimer();
            } catch (Exception e) {
                DebugLog.log("error cleanup");
            }

        }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
    }


    public static void displayLargeTime(Activity activity) {
        MainScreenDialog msDialog = new MainScreenDialog(activity);
        msDialog.show();
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
                replaceFragment(spotifyFragment);
                break;
            case MPDTAB :
                replaceFragment(getDbFragment());
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

    static void playPauseAll() {
        if (SpotifyFragment.playingEngine==1){
            SpotifyFragment.playPauseSpotify();
        }else
        MainActivity.getInstance().playPause();
    }

    public void startPlaylistSpotify() {
        try {
            SpotifyFragment.artistName="nosearch";
            callSpotify();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //e.printStackTrace();
        }
    }

    public void callSpotify(String currentArtist) {
        try {

            SpotifyFragment.startAtTop();
            SpotifyFragment.artistName=currentArtist;
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

        //noinspection ConstantConditions
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
        //noinspection ConstantConditions
        tabLayout.getTabAt(SPOTIFYPLAYLISTTAB).select();

    }

    public void callSpotifyPlaylist(String currentArtist) {
        /*
        does not work; libspotify does not support spotify radio-stations
         */
        String id= SpotifyFragment.searchSpotifyArtist(currentArtist);
        if (id.length()>0)
        try {
            id="spotify:station:artist:"+id;
            final ProgressDialog loadingdialog;
            loadingdialog = ProgressDialog.show(getInstance(),
                    "","Loading, please wait",true);
            SpotifyFragment.clearSpotifyPlaylist();
            new SpotifyFragment.getEntirePlaylistFromSpotify(id, MainActivity.getInstance()){
                @Override
                public void atLast() {
                    loadingdialog.dismiss();
                    SpotifyFragment.playAtPosition(0);
                    MainActivity.getInstance().startPlaylistSpotify();
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

    private void setCleanupTimer(){
        Timer cleanupTimer = new Timer();
        cleanupTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                //Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(getThis,
                //        MainActivity.class));
                try {
                    if (statusThread) {
                        statusThread = false;
                        statusUpdate(logic.mpcStatus);
                    }
                    SpotifyPlaylistFragment.gettingList=false;
                } catch (Exception e) {

                    DebugLog.log("cleanup");
                }
            }

        }, 0, 10000);//Update text every second

    }

    private void setTimerToUpdateDisplay() {
        secondTimer = new Timer();
        secondTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    MPC mpc = logic.getMpc();
                    if (timerTime >= 3) {
                        playlistGetContent(mpc, MainActivity.getInstance());
                        timerTime = 0;
                    } else timerTime++;
                    mpc.getStatusSynch();
                } catch (Exception e) {
                    DebugLog.log("updateDisplay");

                }
            }

        }, 0, 800);//Update text every second
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
        checkable.setChecked(leftDrawerPlaylist.footerVisible);

        return true;
    }

    @Override
    public void onBackPressed() {
        int tabSelected=tabLayout.getSelectedTabPosition();
        if ((tabSelected== SMBTAB)||(tabSelected==MPDTAB)) {
            if (tabSelected == SMBTAB)
                listFragment.back();
            if (tabSelected == MPDTAB) getDbFragment().back();
        }
        else
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog1, id) -> MainActivity.this.finish())
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
            doSettings();
            return true;
        }
        if (id == R.id.set_volume) {
            setVolume(getInstance());
        }
        if (id == R.id.new_albums_categories) {
            doNewAlbumsCategories();
            return true;
        }
        if (id == R.id.dutch_album_top_100) {
            doDutchAlbumTop40();
            return true;
        }
        if (id == R.id.spotify_album_shortcuts) {
            doSpotifyAlbumShortcuts();
            return true;
        }
        if (id == R.id.search_album) {
            doSearchAlbum();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.display_footer) {
            doDisplayFooter(item);
            return true;
        }
        if ((id == R.id.search_option)) {//playlists_option
            searchTerm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void doDisplayFooter(MenuItem item) {
        boolean isChecked = !item.isChecked();
        item.setChecked(isChecked);
        leftDrawerPlaylist.footerVisible=leftDrawerPlaylist.setFooterVisibility(leftDrawerPlaylist.footerVisible);
    }

    public void doSearchAlbum() {
        SpotifyFragment.nextCommand="search album";
        callSpotifyPlaylist();
    }

    public void doSpotifyAlbumShortcuts() {
        SpotifyFragment.spotifyAlbumShortcuts();
    }

    public void doBillboardAlbumTop200() {
        SpotifyFragment.billboardAlbumTop200();
    }

    public void doDutchAlbumTop40() {
        SpotifyFragment.albumTop100Nl();
    }

    public void doNewAlbumsCategories() {
        SpotifyFragment.newAlbumsCategories();
    }

    public void doSettings() {
        Intent myIntent = new Intent(MainActivity.this,
                SettingsActivity.class);
        startActivity(myIntent);
    }

    public static void startWikipediaPage(String outsiders) {
        Intent myIntent = new Intent(getInstance(),//MainActivity.this,
                WikipediaActivity.class);
        myIntent.putExtra("searchitem", outsiders);
        getInstance().startActivity(myIntent);
    }

    public void searchTerm() {
        searchTerm("");
    }

    public void searchTerm(String myterm) {
        //start search from other tab than mpd-tab
        //todo find why this is necessary
        selectTab(1);
        myterm = myterm.trim();
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            selectTab(2);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, getDbFragment()).commit();
        }, 100);

        @SuppressLint("InflateParams") View inflate = getLayoutInflater().inflate(R.layout.activity_search, null);
        alert.setView(inflate);
        final EditText searchEditText = (EditText) inflate.findViewById(R.id.search);
        searchEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
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
        //DebugLog.log("search:" + searchString);
        selectTab(MainActivity.MPDTAB);

        new DatabaseCommand(logic.getMpc(), "search any \"" + searchString + "\"", getDbFragment(), true).run();
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
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText(String.format("%d", progress));
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


        alert.setPositiveButton("Ok", (dialog1, id) -> dialog1.dismiss());

        alert.show();
    }

    public static void displayLargeImage(Context context, String url) {
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
        try {
            InputStream is = new URL(url.replace(" ", "%20")).openStream();

            image.setImageBitmap(BitmapFactory.decodeStream(is));
        } catch (Exception e){}

        /*MainActivity.getInstance().imageLoader.DisplayImage(url, bitmap -> {
            image.setImageBitmap(bitmap);
        });*/
        /*new DownLoadImageTask() {

            @Override
            public void setImage(final Bitmap logo) {
                image.setImageBitmap(logo);

            }
        }.execute(url+"original");*/
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
            //DebugLog.log("return:" + urlString);
            Toast.makeText(MainActivity.this, "return:" + urlString, Toast.LENGTH_SHORT).show();

        }
        //DebugLog.log("in hoofd-activity");
        //DebugLog.log("requestcode:" + resultCode);
        if (resultCode == 23) {
            //DebugLog.log("get favorites");
            SelectFragment.getThis.getFavorites();return;}
        //DebugLog.log("in hoofd-activity erna");
        {
            if (resultCode == Activity.RESULT_OK) {
                //spotify window asks for search of artist
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    selectTab(MainActivity.MPDTAB);
                    //give the program time to restore saved instance
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, getDbFragment()).commit();
                    handler.postDelayed(() -> {
                        //start searching if right tab is selected
                        searchForItem(data.getExtras().getString("artist"));
                    }, 100);
                }, 100);


            }
        }


        super.onActivityResult(requestCode, resultCode, data);

    }

    public static void activityResumed() {
        //getThis.shutDownReceiver.isdown=false;
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
        //getThis.shutDownReceiver.isdown=true;
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
        playlistGetContent(logic.getMpc(), MainActivity.getInstance());
    }

    public void playlistGetContent(final MPC mpc, final MpdInterface mpdInterface) {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                if (!MainActivity.activityVisible)return true;
                String address = logic.getMpc().getAddress();
                Socket sock;
                BufferedReader in;
                PrintWriter out;

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
                    playlist = new ArrayList<>();
                    String response;
                    ArrayList<String> list = new ArrayList<>();
                    while (((response = in.readLine()) != null)) {
                        if (response.startsWith("OK")) break;
                        list.add(response);
                        if (response.startsWith("Id:")) {
                            //Log.v("samba", response);
                            playlist.add(new Mp3File("", list));
                            list.clear();
                        }
                    }
                    try {
                        sock.close();
                        in.close();
                        out.close();
                    } catch (Exception e) {
                        DebugLog.log("playlist get content");
                    }

                    //see if address has not changed while getting information
                    if (!Objects.equals(address, logic.getMpc().getAddress())) return true;
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
                            if (!Objects.equals(playlist.get(i).getTitle(), logic.getPlaylistFiles().get(i).getTitle())) {
                                change = true;
                            }
                        }
                        if (change) {
                            logic.getPlaylistFiles().clear();
                            logic.getPlaylistFiles().addAll(playlist);
                        }
                        String albumName = "";
                        String previousAlbumName = "";

                        for (final Mp3File f : logic.getPlaylistFiles()) {
                            //if (f.getBitmap() != null) continue;
                            f.setBitmap(null);
                            f.setStartAlbum(!f.getAlbum().equals(albumName));

                            albumName = f.getAlbum();
                            final String niceAlbumName = f.niceAlbum();
                            if (previousAlbumName.equals(niceAlbumName))continue;
                            previousAlbumName=niceAlbumName;
                            if (getAlbumPictures().containsKey(niceAlbumName)) {
                                if (getAlbumPictures().get(niceAlbumName) != null)
                                    f.setBitmap(getAlbumPictures().get(niceAlbumName));
                            } else try {
                                getAlbumPictures().put(niceAlbumName, null);//so image is loaded only once
                                try {
                                    URL urlConnection = new URL(Logic.getUrlFromSongpath(f).replace(" ", "%20"));
                                    //DebugLog.log("get:"+url);
                                    HttpURLConnection connection = (HttpURLConnection) urlConnection
                                            .openConnection();
                                    connection.setInstanceFollowRedirects(false);
                                    connection.setDoInput(true);
                                    connection.connect();
                                    InputStream input = connection.getInputStream();
                                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                                    bitmap=DownLoadImageTask.setBitmapsizeToDefault(bitmap);
                                    getAlbumPictures().put(niceAlbumName, bitmap);
                                    imageLoader.memoryCache.put(niceAlbumName, bitmap);
                                    //noinspection Convert2streamapi
                                    for (Mp3File f1 : logic.getPlaylistFiles())
                                        if (f1.niceAlbum().equals(niceAlbumName))
                                            f1.setBitmap(bitmap);
                                    connection.disconnect();
                                } catch (Exception e) {

                                    getAlbumPictures().remove(niceAlbumName);
                                    DebugLog.log("error connect " + Logic.getUrlFromSongpath(f));
                                }

                            } catch (Exception e) {
                                DebugLog.log("playlistgetcontent");
                            }

                        }
                    }
                    mpdInterface.playlistCall(playlist, change);
                } catch (Exception e) {
                    mpc.connectionFailed("Connection failed, check settings");
                }


                return true;
            }
        }.execute();
    }

    public void selectTab(int tab) {
        //noinspection ConstantConditions
        tabLayout.getTabAt(tab).select();
    }

    @Override
    public void playlistCall(ArrayList<Mp3File> playlist, boolean change) {
        if (tabLayout.getSelectedTabPosition() == 0)
            playFragment.playlistCall(playlist, change);

    }

    @Override
    public void newMpdCall(Mp3File mp3File, int position, String command) {
        //DebugLog.log("mewMpdCall within mainactivity");

    }
    public void enqueueSingleCommand(String message) {
        logic.getMpc().enqueCommands(new ArrayList<>(Collections.singletonList(message)));
    }

    private void export(int position) {
        //save current playlist
        final CopyOnWriteArrayList<Mp3File> copyPlaylist =new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Mp3File> playlist = logic.getPlaylistFiles();
        copyPlaylist.addAll(playlist);
        final int currentSong= logic.mpcStatus.song;
        final int currentTime=logic.mpcStatus.time;
        //stop playing of first server
        logic.getMpc().pause();
        //select new server
        logic.openServer(Server.servers.get(position).url);
        logic.getMpc().setMPCListener(MainActivity.getInstance());
        Server.setServer(position, MainActivity.getInstance());

        //do some commands with delay
        //create handler to do background task
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Change playlist after 1 second
            ArrayList<String> commands = new ArrayList<>();
            commands.add("clear");
            for (Mp3File mp : copyPlaylist) {
                String s = "add \"" + mp.getMpcSong().file + "\"";
                commands.add(s);
            }
            String s = "play " + currentSong;
            commands.add(s);
            s = "seek " + currentSong + " "+currentTime;
            commands.add(s);
            logic.getMpc().enqueCommands(commands);
        }, 1000);
    }

    public void mpdCall(Mp3File mp3File, int position, String command) {
        if (command.equals("export"))
            export(position);
        if (command.equals(getString(R.string.command_play)))
            logic.getMpc().play(position);
        if (command.equals(getString(R.string.playlist_removeall))){
            logic.getMpc().clearPlaylist();
        }
        if (command.equals(getString(R.string.playlist_removetop))){
            String message = "delete 0:" + (position + 1);
            //Log.v("samba", message);
            enqueueSingleCommand(message);
        }
        if (command.equals(getString(R.string.playlist_removebottom))){
            String message = "delete " + (position) + ":" + (logic.getPlaylistFiles().size() + 1);
            //Log.v("samba", message);
            enqueueSingleCommand(message);
            //logic.getMpc().sendSingleMessage(message);
        }
        if (command.equals(getString(R.string.playlist_removesong))){
            enqueueSingleCommand("delete " + (position));
        }
        if (command.equals(getString(R.string.playlist_movebottom))){
            enqueueSingleCommand("move " + (position) + " " + (logic.getPlaylistFiles().size() - 1));
        }
        if (command.equals(getString(R.string.playlist_down))){
            enqueueSingleCommand("move " + (position) + " " + (position + 1));
        }
        if (command.equals(getString(R.string.playlist_removeabum))){
            String album = mp3File.getAlbum();
            String artist = mp3File.getArtist();
            logic.removeAlbum(album, artist);
        }
    }

    @Override
    public void printCover(final Bitmap result, final ImageView image, String album) {
        if (result != null) {
            final Bitmap result1=DownLoadImageTask.setBitmapsizeToDefault(result);

            getAlbumPictures().put(album, result1);


            runOnUiThread(() -> {
                //ImageView thumbnail=(ImageView) findViewById(R.id.thumbnail_top);
                image.setImageBitmap(result1);
            });
        } else {

            DebugLog.log("Image Does Not exist or Network Error");
            Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void connectionFailed(String message) {

    }

    @Override
    public void databaseCallCompleted(ArrayList<File> files) {

        DebugLog.log("databaseCallCompleted");
        getDbFragment().databaseCallCompleted(files);


    }

    @Override
    public void databaseFindCompleted(ArrayList<File> files) {

        DebugLog.log("call completed");
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

    public static PlanetAdapter getTracksAdapter(final DrawerLayout mDrawerLayout, final ListView albumsListview, final ArrayList<String> albumList, final ArrayList<PlaylistItem> albumTracks) {
        return new SpotifyPlaylistAdapter(albumList, MainActivity.getInstance(), albumTracks,albumsListview){
            @Override
            public void displayArtist(int counter) {
                try{
                mDrawerLayout.closeDrawers();
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
                MainActivity.getInstance().callSpotify(SpotifyFragment.getData().tracksPlaylist.get(counter).artists.get(0).name);
            }
        };
    }

    @Override
    public void statusUpdate(MPCStatus newStatus) {

        if (statusThread || !ShutDownReceiver.wasScreenOn) return;

        //new Thread(() -> {
            statusThread = true;

            //int prev = SpotifyFragment.playingEngine;

            if (SpotifyFragment.isPlaying()) {
                if (SpotifyFragment.getInstance() != null)
                    try {
                        SpotifyFragment.playingEngine = 1;
                    } catch (Exception e) {
                        /**/
                    }

                    currentArtist = SpotifyFragment.updateSongInfo(getInstance(), getSpotifyInterface());

                //checkButtons(prev);
                MainActivity.playingStatus = MainActivity.SPOTIFY_PLAYING;
                statusThread = false;

                return;

            }

            final MPCStatus status = newStatus;
            logic.mpcStatus = newStatus;
            if (status.song == null) {

                statusThread = false;
                return;
            }
            if (status.playing) {
                SpotifyFragment.playingEngine = 2;
            }
            if (status.song < logic.getPlaylistFiles().size())

                runOnUiThread(() -> {
                    ViewHolder vh = getInstance().viewHolder;
                    try {
                        Mp3File currentSong = logic.getPlaylistFiles().get(status.song);
                        final String title = currentSong.getTitle();
                        vh.title = title;

                        final String time1 = Mp3File.niceTime(status.time);
                        vh.time = time1;
                        String timeNice;
                        try {
                            timeNice = currentSong.getTimeNice();
                            vh.totaltime = timeNice;
                        } catch (Exception e) {
                            timeNice = "00:00";
                        }
                        String album = "";
                        TextView artist = (TextView) findViewById(R.id.artist_top);
                        try {
                            album = currentSong.niceAlbum();
                            currentArtist = currentSong.getArtist();
                            artist.setText(album);
                            vh.album = album;
                        } catch (Exception e) {
                            album = "statusupdate";
                            artist.setText(album);
                        }
                        final ImageView image = (ImageView) findViewById(R.id.thumbnail_top);
                        String uri = Logic.getUrlFromSongpath(currentSong);
                        for (HeaderSongInterface header : MainActivity.getHeaders()) {
                            if (header != null)
                                header.setData(time1, timeNice, title, album, false, status.song);
                        }
                        MainActivity.playingStatus = MainActivity.SPOTIFY_PLAYING;

                        if (getAlbumPictures().containsKey(album)) {
                            final Bitmap b = getAlbumPictures().get(album);
                            albumBitmap = b;
                            currentSong.setBitmap(b);
                            runOnUiThread(() -> {
                                for (HeaderSongInterface header : MainActivity.getHeaders()) {
                                    if (header != null)
                                        header.setLogo(b);
                                }
                            });
                        } else {
                            getAlbumPictures().put(album, null);
                            new ImageLoadTask(uri, album, mainActivity, image).execute();
                        }//
                        if (playFragment != null) playFragment.updateCurrentSong();
                        MainActivity.playingStatus = MainActivity.MPD_PLAYING;
                        //Log.v("samba",uri);
                    } catch (Exception e) {
                        statusThread = false;
                    }
                });
            statusThread = false;

        //}).start();
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        SugarContext.terminate();
        try {
            trimCache(this);
            secondTimer.cancel();
        } catch (Exception e) {
            DebugLog.log("ondestroy");

            e.printStackTrace();
        }
        instance=null;
        //now really exit; clear all static variables too!
        System.exit(0);
    }

    public static void trimCache(Context context) {
        try {
            java.io.File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            DebugLog.log("trimcache");

        }
    }

    public static boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new java.io.File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        assert dir != null;
        return dir.delete();
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void setLogo(Bitmap logo) {
        ((ImageView) findViewById(R.id.thumbnail_top)).setImageBitmap(logo);

    }

    @Override
    public void setData(String time, String totalTime, String title, String artist, boolean spotifyList, int currentTrack) {
        ((TextView) findViewById(R.id.time_top)).setText(time);
        ((TextView) findViewById(R.id.totaltime_top)).setText(totalTime);
        ((TextView) findViewById(R.id.title_top)).setText(title);
        ((TextView) findViewById(R.id.artist_top)).setText(artist);

    }

    public class ViewHolder {
        String totaltime;
        public String time;
        public String title;
        public String album;

    }

    @Override
    public void onTaskCompleted(String result, String call) {
        //Log.v("samba", result);
    }
    public class SpotifyData{
        ArrayList<String> albumIds = new ArrayList<>();
        ArrayList<String> albumList = new ArrayList<>();
        ArrayList<PlaylistItem> albumTracks = new ArrayList<>();
        ArrayList<PlaylistItem> previousAlbumTracks=new ArrayList<>();
        ArrayList<Track> previousTracksPlaylist= new ArrayList<>();
        public ArrayList<String> artistList = new ArrayList<>();
        List<Track> tracksPlaylist= Collections.synchronizedList(new ArrayList<>());
        HashMap hm = new HashMap();
        public ArrayList<String> searchArtistString =new ArrayList<>();
        public ArrayList<PlaylistItem> albums = new ArrayList<>();
        public String artistText="";
    }
}
