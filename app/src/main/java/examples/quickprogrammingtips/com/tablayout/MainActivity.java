package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.ImageLoadTask;
import mpc.DatabaseCommand;
import mpc.MPC;
import mpc.MPCDatabaseListener;
import mpc.MPCListener;
import mpc.MPCSong;
import mpc.MPCStatus;


public class MainActivity extends AppCompatActivity  implements MpdInterface,MPCListener , MPCDatabaseListener , OnTaskCompleted{

    static final int STATIC_RESULT=3; //positive > 0 integer.
    private boolean footerVisible=false;
    private int tabSelected=0;
    private ListFragment listFragment;
    protected DBFragment dbFragment;
    private Logic logic;
    public Handler updateBarHandler;
    private int timerTime=0;
    private PlayFragment playFragment;
    protected TabLayout tabLayout;
    private MainActivity mainActivity;
    public static HashMap<String, Bitmap>albumPictures=new HashMap<>();
    private String currentArtist;
    public ViewHolder viewHolder=new ViewHolder();


    public static MainActivity getThis;
    public ProgressDialog dialog;
    public Bitmap albumBitmap;

    public static void panicMessage(final String message){
        //Let this be the code in your n'th level thread from main UI thread
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                Toast.makeText(getThis, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SugarContext.init(this);//init db
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        mainActivity=this;
        getThis=this;
        dialog=new ProgressDialog(this);//keep it hidden until needed
        updateBarHandler = new Handler();

        logic=new Logic(this);
        setContentView(R.layout.activity_main);
        LinearLayout ll=((LinearLayout)findViewById(R.id.time_layout));
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });//android:id="@+id/song_title"
        ll=((LinearLayout)findViewById(R.id.song_title));
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSpotify(currentArtist);
            }
        });
        ll.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                MainScreenDialog msDialog=new MainScreenDialog(getThis);
                msDialog.show();
                return true;
            }
        });
        ImageView im=((ImageView)findViewById(R.id.thumbnail_top));
        im.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                displayLargeImage(MainActivity.this,MainActivity.this.albumBitmap);
                return true;
            }
        });
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVolume();
            }
        });
        tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setTabTextColors(Color.WHITE, R.color.accent_material_dark);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.addTab(tabLayout.newTab().setText("Play"));
        tabLayout.addTab(tabLayout.newTab().setText("List"));
        tabLayout.addTab(tabLayout.newTab().setText("DB"));
        tabLayout.addTab(tabLayout.newTab().setText("Radio"));
        tabLayout.addTab(tabLayout.newTab().setText("Select"));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#00FFFF"));

        this.setTitle("");
        final LinearLayout footerView  = (LinearLayout) findViewById(R.id.footer);
        footerView.setVisibility(View.GONE);
        FloatingActionButton FAB = (FloatingActionButton) findViewById(R.id.fab);
        FAB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                //Toast.makeText(MainActivity.this, "Hello Worl", Toast.LENGTH_SHORT).show();
                if (tabSelected == 1 || (tabSelected == 2)) {
                    //Toast.makeText(MainActivity.this, "Back key", Toast.LENGTH_SHORT).show();
                    if (tabSelected == 1) listFragment.back();
                    if (tabSelected == 2) dbFragment.back();
                } else {
                    setFooterVisibility();
                }


            }
        });

        listFragment = new ListFragment();
        dbFragment = new DBFragment();

        //display tablayout
        displayHome();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //playlistThread.interrupt();
                tabSelected = tab.getPosition();
                if (tab.getPosition() == 0)
                    displayHome();
                if (tabSelected == 1) {

                    getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, listFragment).commit();
                }
                if (tab.getPosition() == 2) {
                    try {

                        getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, dbFragment).commit();
                    }catch(Exception e){}
                }
                if (tab.getPosition() == 4)
                    getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, new SelectFragment()).commit();
                if (tab.getPosition() == 3)
                    getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, new PlaylistsFragment()).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });


        Toolbar tool = (Toolbar)findViewById(R.id.app_bar);//cast it to ToolBar
        setSupportActionBar(tool);
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic.getMpc().play();
                logic.setPaused(false);
            }
        });
        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic.getMpc().pause();
                logic.setPaused(true);
            }
        });
        ImageButton pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
        ImageButton forwardButton = (ImageButton) findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic.getMpc().next();
            }
        });
        ImageButton backButton = (ImageButton) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic.getMpc().previous();
            }
        });

        updateDisplay();

    }

    public void startPlaylistSpotify() {
        try{
            Intent intent = new Intent(MainActivity.getThis, SpotifyActivity.class);
            intent.putExtra("artist", "nosearch");

            MainActivity.getThis.startActivity(intent);
            Log.v("samba", "Spotify");
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //e.printStackTrace();
        }
    }

    public void callSpotify(String currentArtist) {
        try{
        Intent intent = new Intent(getThis, SpotifyActivity.class);
        intent.putExtra("artist", currentArtist);

        getThis.startActivityForResult(intent, 4);
    } catch (Exception e) {
        Log.v("samba", Log.getStackTraceString(e));
        //e.printStackTrace();
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

    private void updateDisplay() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try{
                    MPC mpc = logic.getMpc();
                    if (timerTime>=3) {
                        playlistGetContent(mpc, MainActivity.getThis);
                        timerTime=0;
                    } else timerTime++;
                    mpc.getStatusSynch();
                } catch(Exception e){

                }
            }

        },0,1000);//Update text every second
    }
    private void setFooterVisibility() {
        LinearLayout footerView  = (LinearLayout) findViewById(R.id.footer);
        if (footerVisible)
            footerView.setVisibility(View.GONE);
        else
            footerView.setVisibility(View.VISIBLE);
        footerVisible = !footerVisible;
    }

    public boolean areCrashesEnabled() {
        SharedPreferences preferences;
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getBoolean("are_crashes_enabled", false);
    }
    private void displayHome() {
        playFragment = new PlayFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, playFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void onGroupItemClick (MenuItem item) {

    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.display_footer);
        checkable.setChecked(footerVisible);
        return true;
    }
    @Override
    public void onBackPressed() {
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
    }    @Override
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
        if (id==R.id.set_volume){
            setVolume();
        }
        if (id==R.id.spotify_playlist){
            startPlaylistSpotify();
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

    public void startWikipediaPage(String outsiders) {
        Intent myIntent = new Intent(MainActivity.this,
                WikipediaActivity.class);
        myIntent.putExtra("searchitem", outsiders);
        startActivity(myIntent);
    }
    public void searchTerm(){
        searchTerm("");
    }
    public void searchTerm(String myterm){
        myterm=myterm.trim();
        //selectTab(2);
        //getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, dbFragment).commit();
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectTab(2);
                getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, dbFragment).commit();
                //searchForItem(searchString);
            }
        }, 100);

        View inflate = getLayoutInflater().inflate(R.layout.activity_search, null);
        alert.setView(inflate);
        final EditText searchEditText = (EditText) inflate.findViewById(R.id.search);
        searchEditText.setText(myterm);

        Button save = (Button) inflate.findViewById(R.id.save_search_button);
        save.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                try {
                    final String searchString = searchEditText.getText().toString();
                    searchForItem(searchString);
                    alert.dismiss();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();

                }

            }

        });

        alert.show();

    }

    public void searchForItem(String searchString) {
        //Log.v("samba", "search:" + searchString);
        //selectTab(2);
        new DatabaseCommand(logic.getMpc(), "find any \"" + searchString + "\"", dbFragment, true).run();
    }

    public void setVolume() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Volume");

        LinearLayout linear=new LinearLayout(this);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text=new TextView(this);
        text.setPadding(10, 10, 10, 10);

        Integer volume = logic.mpcStatus.volume;
        text.setText(""+volume);
        SeekBar seek=new SeekBar(this);

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


        alert.setPositiveButton("Ok",new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog,int id)
            {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    public static void displayLargeImage(Context context,Bitmap bitmap) {
        final AlertDialog alert = new AlertDialog.Builder(context).create();

        LinearLayout linear=new LinearLayout(context);

        linear.setOrientation(LinearLayout.VERTICAL);
        ImageView image = new ImageView(context);
        //get width of screen
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        //fit image to width of screen, keep aspect ratio
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width-140, width-140);
        image.setLayoutParams(layoutParams);
        image.setImageBitmap(bitmap);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        linear.addView(image);
        alert.setView(linear);

        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //if (requestCode == STATIC_RESULT) //check if the request code is the one you've sent
        {
            if (resultCode == Activity.RESULT_OK)
            {
                //spotify window asks for search of artist
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selectTab(2);
                        //give the program time to restore saved instance
                        getSupportFragmentManager().beginTransaction().replace(R.id.frLayout, dbFragment).commit();
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

    public Logic getLogic() {
        return logic;
    }
    public void playlistGetContent() {
        playlistGetContent( logic.getMpc(),MainActivity.getThis);
    }
    public void playlistGetContent(final MPC mpc, final MpdInterface mpdInterface){
        AsyncTask thread = new AsyncTask(){
            @Override
            protected Object doInBackground(Object[] params) {
                String address=logic.getMpc().getAddress();
                Socket sock=null;
                BufferedReader in=null;
                PrintWriter out=null;

                List<MPCSong> songs;
                ArrayList<Mp3File>playlist;
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
                    }

                    //see if address has not changed while getting information
                    if (address!=logic.getMpc().getAddress()) return true;
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

                        for (final Mp3File f : logic.getPlaylistFiles()) {
                            if (f.getBitmap() != null) continue;
                            f.setStartAlbum(!f.getAlbum().equals(albumName));

                            albumName = f.getAlbum();
                            final String niceAlbumName = f.niceAlbum();
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
                                            e.printStackTrace();
                                        }

                            } catch (Exception e) {
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

    public void selectTab(int tab){
        tabLayout.getTabAt(tab).select();
    }

    @Override
    public void playlistCall(ArrayList<Mp3File> playlist, boolean change) {
        if (tabSelected == 0)
            playFragment.playlistCall(playlist,change);

    }

    @Override
    public void newMpdCall(Mp3File mp3File, int position, String command) {

    }

    @Override
    public void printCover(final Bitmap result,  final ImageView image, String album) {
        if(result != null){

            albumPictures.put(album,result);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //ImageView thumbnail=(ImageView) findViewById(R.id.thumbnail_top);
                    image.setImageBitmap(result);
                }
        });
        }else{

            Log.v("samba","Image Does Not exist or Network Error");
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
    public void onSaveInstanceState( Bundle outState ) {

    }

    @Override
    public void databaseUpdated() {
    }

    @Override
    public void databaseUpdateProgressChanged(int progress) {

    }

    @Override
    public void statusUpdate(MPCStatus newStatus) {
        final MPCStatus status=newStatus;
        logic.mpcStatus=newStatus;
        if (status.song==null) return;
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                //Log.v("samba","tijd:"+newStatus.time.toString());

                if (status.song.intValue()<logic.getPlaylistFiles().size())
                    runOnUiThread(new Runnable() {


                        @Override
                        public void run() {
                            ViewHolder vh=getThis.viewHolder;
                            try {
                                Mp3File currentSong = logic.getPlaylistFiles().get(status.song.intValue());

                                TextView tvName = (TextView) findViewById(R.id.title_top);
                                final String title = currentSong.getTitle();
                                tvName.setText(title);
                                vh.title=title;
                                //Log.v("samba", currentSong.getTitle());
                                //Log.v("samba", currentSong.getArtist());
                                TextView time = (TextView) findViewById(R.id.time_top);
                                final String time1 = Mp3File.niceTime(status.time.intValue());
                                vh.time=time1;
                                time.setText(time1);
                                TextView totaltime = (TextView) findViewById(R.id.totaltime_top);
                                try{
                                final String timeNice = currentSong.getTimeNice();
                                totaltime.setText(timeNice);
                                vh.totaltime=timeNice;
                            } catch (Exception e) {
                                    totaltime.setText("00:00");
                            }
                                String album="";
                                TextView artist = (TextView) findViewById(R.id.artist_top);
                                try{
                                album = currentSong.niceAlbum();
                                currentArtist=currentSong.getArtist();
                                artist.setText(album);
                                vh.album=album;
                            } catch (Exception e) {
                                    artist.setText("");
                            }
                                final ImageView image = (ImageView) findViewById(R.id.thumbnail_top);
                                String uri = Logic.getUrlFromSongpath(currentSong);

                                if (albumPictures.containsKey(album)) {
                                    final Bitmap b = albumPictures.get(album);
                                    albumBitmap=b;
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


                                }
                                ;
                                //Log.v("samba",uri);
                            } catch (Exception e) {
                                //mpc.connectionFailed("Connection failed, check settings");
                                //t.stop();
                            }
                    }
                });
            }
        });
    }


    @Override public void onDestroy(){

        super.onDestroy();
        SugarContext.terminate();
    }

    public class ViewHolder{
        public String totaltime;
        public String time;
        public String title;
        public String album;

    }

    @Override
    public void onTaskCompleted(String result, String call) {
        Log.v("samba",result);
    }
}
