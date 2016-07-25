package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.spotify.sdk.android.player.ConnectionStateCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.Utils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static examples.quickprogrammingtips.com.tablayout.model.Mp3File.niceTime;

/*
The most straightforward way to get the access token is to use the Authentication Library from the Spotify Android SDK.(https://github.com/spotify/android-sdk)
explanation:https://developer.spotify.com/technologies/spotify-android-sdk/android-sdk-authentication-guide/#single-sign-on-with-spotify-client-and-a-webview-fallback
working example:https://developer.spotify.com/technologies/spotify-android-sdk/tutorial
and:https://github.com/kaaes/spotify-web-api-android/blob/master/sample-search/src/main/java/kaaes/spotify/webapi/samplesearch/LoginActivity.java
Detailed information how to use it can be found in the Spotify Android SDK Authentication Guide.
 */
/*
get a lot of playlists on:https://open.spotify.com/user/spotify
new album releases:https://open.spotify.com/user/spotify/playlist/3Yrvm5lBgnhzTYTXx2l55x
new album releases: http://everynoise.com/spotify_new_albums.html
 */
public class SpotifyActivity extends AppCompatActivity implements
        ConnectionStateCallback {//AlertDialog
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "89f945f1696e4f389aaed419e51beaad";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "testschema://callback";
    private static ArrayList<String> mainids;
    private SpotifyHeader spotifyHeader;
    public FillListviewWithValues fillListviewWithValues;
    private ArrayList<String> artistList = new ArrayList<>();
    private ArrayList<String> albumIds = new ArrayList<>();
    public static ArrayList<String> albumList = new ArrayList<>();
    public static ArrayList<PlaylistItem> albumTracks = new ArrayList<>();
    public static SpotifyActivity getThis;
    public static SpotifyInterface getSpotifyInterface;
    //private ListView albumsListview, relatedArtistsListView;
    private static int spotifyStartPosition = 0;
    private static HashMap<String, String> spotifyToken = new HashMap<>();
    private static HashMap hm = new HashMap();

    private Handler customHandler = new Handler();
    private static String ipAddress = "";
    private PlanetAdapter albumAdapter;
    private ListView albumsListview;
    private static ProgressDialog dialog1;//
    private static Handler updateBarHandler;
    //AdapterView.OnItemClickListener selectOnPlaylist;
    private boolean nosearch = false;
    private static TextView artistTitleTextView;
    public static ArrayList<Track> tracksPlaylist=new ArrayList<>();
    private static int currentTrack;
    public static String artistName;
    private ArrayAdapter<String> relatedArtistsAdapter;
    private ListView relatedArtistsListView;
    private SpotifyApi api;
    private SpotifyService spotify;
    private AdapterView.OnItemClickListener cl;
    private FloatingActionButton fab;
    public static boolean albumVisible = true;
    static Bitmap bitmap;
    private boolean artistInitiated = false;
    private final float CHECK_MEMORY_FREQ_SECONDS = 3.0f;
    private final float LOW_MEMORY_THRESHOLD_PERCENT = 5.0f; // Available %
    private Handler memoryHandler_;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private SongItems songItems;

    public void checkAppMemory(){
        // Get app memory info
        long available = Runtime.getRuntime().maxMemory();
        long used = Runtime.getRuntime().totalMemory();

        // Check for & and handle low memory state
        float percentAvailable = 100f * (1f - ((float) used / available ));
        if( percentAvailable <= LOW_MEMORY_THRESHOLD_PERCENT )
            handleLowMemory();

        // Repeat after a delay
        memoryHandler_.postDelayed( new Runnable(){ public void run() {
            checkAppMemory();
        }}, (int)(CHECK_MEMORY_FREQ_SECONDS * 1000) );
    }

    public void handleLowMemory(){
        DownLoadImageTask.albumPictures.clear();
    }
    private static void GetSpotifyTokenSync(){
        checkAddress();
        //Log.v("samba", "ask starred:");
        String urlString = ipAddress + "?OpenAddon_plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3Arockin.billy%3Aplaylist%3A03cHQWb5epbCJQsgjwv2dK%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D";
        String data = "{\"jsonrpc\":\"2.0\",\"method\":\"Files.GetDirectory\",\"id\":1,\"params\":[\"plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3Arockin.billy%3Aplaylist%3A03cHQWb5epbCJQsgjwv2dK%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D\",\"music\",[\"title\",\"file\",\"thumbnail\", \"art\",\"duration\"]]}";
        //String data = "{\"jsonrpc\":\"2.0\",\"method\":\"Files.GetDirectory\",\"id\":1,\"params\":[\"plugin://plugin.audio.spotlight/?path=starred&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D\",\"music\",[\"title\",\"file\",\"thumbnail\", \"art\",\"duration\"]]}";
        //String urlString = ipAddress + "?OpenAddon_plugin://plugin.audio.spotlight/?path=starred&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D";
        //Log.v("samba", urlString);

        try {
            String fname = GetJsonFromUrl(data, urlString).optJSONArray("files").getJSONObject(0).optString("file");
            //Log.v("samba", "filenambooleane:"+fname);
            int startIndex = fname.indexOf("Token=") + 6;
            int endIndex = fname.indexOf("&User");
            //Log.v("samba", fname.substring(startIndex, endIndex)); //is your string. do what you want
            spotifyToken.put(ipAddress,fname.substring(startIndex, endIndex));//checkAddress
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    private static String GetSpotifyToken() {

                new AsyncTask<String, Void, String>(){

            @Override
            protected String doInBackground(String... params) {
                GetSpotifyTokenSync();
                return null;
            }
        }.execute();


        //Log.v("samba", "ask starred:");
        /*String data = "{\"jsonrpc\":\"2.0\",\"method\":\"Files.GetDirectory\",\"id\":1,\"params\":[\"plugin://plugin.audio.spotlight/?path=starred&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D\",\"music\",[\"title\",\"file\",\"thumbnail\", \"art\",\"duration\"]]}";
        String urlString = ipAddress + "?OpenAddon_plugin://plugin.audio.spotlight/?path=starred&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D";
        try {
            String fname = GetJsonFromUrl(data, urlString).optJSONArray("files").getJSONObject(0).optString("file");
            //Log.v("samba", "filenambooleane:"+fname);
            int startIndex = fname.indexOf("Token=") + 6;
            int endIndex = fname.indexOf("&User");
            //Log.v("samba", fname.substring(startIndex, endIndex)); //is your string. do what you want
            spotifyToken = fname.substring(startIndex, endIndex);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }*/

        return "";
    }

    public static String checkAddress() {
        String ip = MainActivity.getThis.getLogic().getMpc().getAddress();
        ipAddress = String.format("http://%s:8080/jsonrpc", ip);
        return ipAddress;
    }

    private static void AddSpotifyTrack(Activity getThis, ArrayList<String> ids, final int pos) {
        try {
            if (pos < ids.size()) {
                dialog1.incrementProgressBy(1);
                String data = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Add\", \"params\": { \"playlistid\" : 0 , \"item\" : {\"file\" : \"http://127.0.0.1:8081/track/%s.wav|X-Spotify-Token=%s&User-Agent=Spotlight+1.0\"}}, \"id\": 1}", ids.get(pos), spotifyToken.get(ipAddress));
                String urlString = ipAddress + "?PlaylistAdd";
                GetJsonFromUrl(data, urlString);
                AddSpotifyTrack(getThis, ids, pos + 1);
            } else {
                stopMpd();
                //todo:change http://192.168.2.3 to address of current server
                JSONObject playlist = GetJsonFromUrl(
                        "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.GetItems\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"duration\", \"thumbnail\",\"file\"], \"playlistid\": 0 }, \"id\": 1}\u200B",
                        ipAddress + "?GetPLItemsAudio");

                spotifyStartPosition = playlist.getJSONArray("items").length() - ids.size();
                GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"playlistid\": 0, \"position\": " + spotifyStartPosition + " } }, \"id\": 1}",
                        ipAddress);
                GetJsonFromUrl(String.format("{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": 0, \"to\": %s}, \"id\": 1}\u200B", spotifyStartPosition),
                        ipAddress + "?PlayerGoto");
                getThis.runOnUiThread(new Runnable() {
                    public void run() {
                        if (dialog1.isShowing())
                            dialog1.dismiss();

                    }
                });


            }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    private static JSONObject GetJsonFromUrl(String data, String urlString) {
        JSONObject jsonRootObject = null;

        String sb = getJsonStringFromUrl(data, urlString);
        //Log.v("samba", sb);
        try {
            jsonRootObject = new JSONObject(sb);
            return jsonRootObject.getJSONObject("result");
        } catch (JSONException e) {
           // Log.v("samba", Log.getStackTraceString(e));
        }
        return jsonRootObject;
    }

    @NonNull
    private static String getJsonStringFromUrl(String data, String urlString) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection uc = url.openConnection();
            //uc.setConnectTimeout(1000);//

            uc.setDoOutput(true);// Triggers POST.

            uc.setRequestProperty("User-Agent", "@IT java-tips URLConnection");
            uc.setRequestProperty("Content-Type", "application/json");
            uc.setRequestProperty("Accept-Language", "ja");
            OutputStream os = uc.getOutputStream();

            String postStr = data;
            PrintStream ps = new PrintStream(os);
            ps.print(postStr);
            ps.close();

            InputStream is = uc.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));


            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    //Log.v("samba", "line read:" + line);
                    sb.append(line).append("\n");
                }
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }


        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        return sb.toString();
    }

    public static String LastFMArtist(String artist) {
        //artist.getInfo
        String encArtist = artist;

        String api_key = "07e905eaba54f0d626c2fadcb0fe13f6";//see above; last.fm-key
        String urlString = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&artist=%s&api_key=%s&format=json", encArtist, api_key);
//        String urlString = "http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&artist="
//                + encArtist + "&api_key=07e905eaba54f0d626c2fadcb0fe13f6&format=json";

        try {
            URL url = new URL(urlString);
            URLConnection uc = url.openConnection();
            uc.setDoOutput(true);

            uc.setRequestProperty("User-Agent", "@IT java-tips URLConnection");
            uc.setRequestProperty("Accept-Language", "ja");
            OutputStream os = uc.getOutputStream();

            String postStr = "foo1=bar1&foo2=bar2";
            PrintStream ps = new PrintStream(os);
            ps.print(postStr);
            ps.close();

            InputStream is = uc.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    //Log.v("samba","line read:"+line);
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                Log.v("samba", Log.getStackTraceString(e));
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }
            return sb.toString();

        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        return "";
    }
    // Spotify:Request code that will be used to verify if the result comes from correct activity
// Can be any integer
    //private static final int REQUEST_CODE = 1337;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
            dialog1=new ProgressDialog(this);
            updateBarHandler = MainActivity.getThis.updateBarHandler;
        // Code called from an activity
        /*final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private"})
                .build();

        AuthenticationClient.openLoginInBrowser(this, request);*/

            memoryHandler_ = new Handler();
            checkAppMemory();


        tracksPlaylist = new ArrayList<Track>();

        String ip = MainActivity.getThis.getLogic().getMpc().getAddress();
        ipAddress = String.format("http://%s:8080/jsonrpc", ip);

        //Log.v("samba", "ip:" + ip);
            /*selectOnPlaylist = new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    //Log.v("samba", "click on "+position); //is your string. do what you want
                    Log.v("samba", "got position:" + (position));
                    playlistGotoPosition(position);
                }

                ;
            };*/
        super.onCreate(savedInstanceState);
        getThis = this;
            getSpotifyInterface=new SpotifyInterface();
        setContentView(R.layout.activity_spotify);
        //Log.v("samba", "nosearch1");
        api = new SpotifyApi();
        spotify = api.getService();
        GetSpotifyToken();


        Bundle extras = getIntent().getExtras();
        String temp = extras.getString("artist");
        nosearch = (temp.startsWith("nosearch"));
        if (nosearch) temp = "The Beatles";
        artistName = temp;

        //Log.v("samba", "nosearch2");

        albumsListview = (ListView) findViewById(R.id.albums_listview);
        albumsListview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        albumAdapter = new PlanetAdapter(albumList, this,albumTracks) {
            @Override
            public void removeUp(int counter) {
                removeUplist(albumAdapter, albumsListview,counter,getThis);
            }

            @Override
            public void onClickFunc(int counter) {
                Log.v("samba","spotifyToken:"+spotifyToken);
                currentTrack=counter;
                if (albumVisible)
                    try{
                    getAlbumtracksFromSpotify(counter);
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
                else {
                    stopMpd();
                    playlistGotoPosition(counter);
                }
            }

            @Override
            public void removeDown(int counter) {
                removeDownlist(albumAdapter, albumsListview,counter, getThis);
            }

            @Override
            public void removeAlbum(int counter) {
                SpotifyActivity.removeAlbum(albumAdapter, counter, albumsListview,getThis);
            }

            @Override
            public void addAlbumToFavoritesAlbum(int counter) {
                addAlbumToFavorites(Favorite.SPOTIFYALBUM + albumIds.get(counter), artistName + "-" + albumList.get(counter));

            }

            @Override
            public void addAlbumToFavoritesTrack(int counter) {
                addAlbumToFavoritesTrackwise(counter);

            }

            @Override
            public void removeTrack(int counter) {
                removeTrackSpotify(counter);
                refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
            }

            @Override
            public void displayArtist(int counter) {
                try{
                    String s = tracksPlaylist.get(counter).artists.get(0).name;
                    setVisibility(View.VISIBLE);
                    listAlbumsForArtist(s);
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }

            @Override
            public void replaceAndPlayAlbum(int counter) {
                clearSpotifyPlaylist();
                //Log.v("samba","end removing");
                getAlbumtracksFromSpotify(counter);
            }

            @Override
            public void addAndPlayAlbum(int counter) {

                getAlbumtracksFromSpotify(counter);
            }

            @Override
            public void addAlbum(int counter) {
                addAlbumStatic(counter,albumAdapter, albumsListview);
            }
        };
        albumAdapter.setDisplayCurrentTrack(false);
        //albumAdapter.setDisplayCurrentTrack(true);
        albumsListview.setAdapter(albumAdapter);
            /*final AdapterView.OnItemClickListener cl=   new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {

                    getAlbumtracksFromSpotify(position);

                }
            };*/
        albumsListview.setOnItemClickListener(cl);
        relatedArtistsListView = (ListView) findViewById(R.id.relatedartists_listview);
        //Log.v("samba", "nosearch3");

        relatedArtistsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, artistList);
        relatedArtistsListView.setAdapter(relatedArtistsAdapter);
        relatedArtistsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                final String selectedItem = artistList.get(pos);

                //Log.v("long clicked", "pos: " + pos + "artist: " + selectedItem);
                PopupMenu menu = new PopupMenu(arg1.getContext(), arg1);
                menu.getMenu().add("search");
                menu.getMenu().add("wikipedia");
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        try{

                            String title = item.getTitle().toString();
                            if ((title.equals("search"))) {
                                    /*MainActivity.getThis.selectTab(2);
                                    try{ Thread.sleep(1000); MainActivity.getThis.searchTerm(selectedItem);}catch(InterruptedException e){ }
                                    */
                                final Intent intent = getThis.getIntent();
                                intent.putExtra("artist", selectedItem);
                                setResult(Activity.RESULT_OK, intent);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode
                                getThis.finish(); //finish the activity




                            }
                            if ((title.equals("wikipedia"))) {
                                MainActivity.getThis.startWikipediaPage(selectedItem);
                            }
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }

                        return true;
                    }

                });

                return true;
            }
        });
        //Log.v("samba", "nosearch4");
        relatedArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                try{
                String s = artistList.get(position);
                listAlbumsForArtist(s);
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
            }
        });
        //Log.v("samba", "nosearch5");
        ImageButton stopButton = (ImageButton) findViewById(R.id.stopspotifyButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Player.GoTo", "params": { "playerid": 0, "to": 20}, "id": 1}​
                stopSpotifyPlaying(ipAddress);
            }
        });
        //jsonrpc /jsonrpc?PlaylistClear {"jsonrpc": "2.0", "id": 0, "method": "Playlist.Clear", "params": {"playlistid": 0}}
        //Log.v("samba", "nosearch6");
        ImageButton clearButton = (ImageButton) findViewById(R.id.clearspotifyButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //yarc.js:906 jsonrpc /jsonrpc?GetRemoteInfos [{"jsonrpc":"2.0","method":"Application.GetProperties","id":1,"params":[["muted"]]},{"jsonrpc":"2.0","method":"Player.GetProperties","id":2,"params":[0,["time", "totaltime", "percentage", "shuffled","repeat"]]},{ "jsonrpc": "2.0", "method": "Player.GetItem", "params": { "playerid": 0, "properties": [ "title", "showtitle", "artist", "thumbnail", "streamdetails", "file", "season", "episode"] }, "id": 3 }
                    // "Player.GoTo", "params": { "playerid": 0, "to": 20}, "id": 1}​
                    clearSpotifyPlaylist();
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }
        });
        //jsonrpc /jsonrpc?PlaylistClear {"jsonrpc": "2.0", "id": 0, "method": "Playlist.Clear", "params": {"playlistid": 0}}
        ImageButton playButton = (ImageButton) findViewById(R.id.playspotifyButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //yarc.js:906 jsonrpc /jsonrpc?GetRemoteInfos [{"jsonrpc":"2.0","method":"Application.GetProperties","id":1,"params":[["muted"]]},{"jsonrpc":"2.0","method":"Player.GetProperties","id":2,"params":[0,["time", "totaltime", "percentage", "shuffled","repeat"]]},{ "jsonrpc": "2.0", "method": "Player.GetItem", "params": { "playerid": 0, "properties": [ "title", "showtitle", "artist", "thumbnail", "streamdetails", "file", "season", "episode"] }, "id": 3 }
                    // "Player.GoTo", "params": { "playerid": 0, "to": 20}, "id": 1}​
                    stopMpd();
                    GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": 0 }, \"id\": 1}",
                            ipAddress + "?StopPause");//
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }
        });
            artistTitleTextView = (TextView)

                    findViewById(R.id.artist_title);//relatedartists_text

            songItems=new SongItems(this);
            songItems.setOnClick(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //MainActivity.displayLargeImage(getThis, bitmap);
                    //Log.v("samba", "clicked!");
                    // Toast.makeText(getApplicationContext(), "Clicked Image",
                    //         Toast.LENGTH_SHORT).show();
                    PopupMenu menu = new PopupMenu(songItems.image.getContext(), songItems.image);
                    //jsonrpc /jsonrpc?OpenAddon_plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3Aredactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D
                    // {"jsonrpc":"2.0","method":"Files.GetDirectory","id":1,"params":["plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3Aredactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D","music",["title","file","thumbnail", "art","duration"]]}

                    //plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3Aredactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D
                    //{"jsonrpc":"2.0","method":"Files.GetDirectory","id":1,"params":["plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%…A3N9rTO6YG7kjWETJGOEvQY%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D","music",["title","file","thumbnail", "art","duration"]]
                    //menu.getMenu().add("oor11");
                    menu.getMenu().add("hide/show");
                    menu.getMenu().add("search artist");
                    menu.getMenu().add("search album");
                    menu.getMenu().add("enlarge cover");
                    menu.getMenu().add("refresh token");
                    menu.getMenu().add("new albums");
                    menu.getMenu().add("new albums categories");

                    menu.show();
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                                        @Override/*
                    rnb
                    soul
                    singer-songwriter
                    */
                                                        public boolean onMenuItemClick(MenuItem item) {
                                                            String title = item.getTitle().toString();
                                                            if ((title.equals("new albums categories"))) {
                                                                PopupMenu menu = new PopupMenu(songItems.image.getContext(), songItems.image);
                                                                ;
                                                                ArrayList<String> categoryIds = new ArrayList<>(Arrays.asList("electronic", "progressive", "alternative", "rnb", "soul", "singer-songwriter",
                                                                        "acoustic", "ambient", "americana", "blues", "country", "techno", "shoegaze", "Hip-Hop", "funk", "jazz", "rock", "folk"));
                                                                for (String cat : categoryIds) {
                                                                    menu.getMenu().add(cat);
                                                                }
                                                                menu.show();
                                                                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                                                    @Override
                                                                    public boolean onMenuItemClick(MenuItem item) {
                                                                        String title = item.getTitle().toString();
                                                                        try {
                                                                            //if (title.startsWith("new albums ")) {
                                                                            final String cat = title.replace("new albums ", "");
                                                                            fillListviewWithValues = new FillListviewWithValues() {

                                                                                @Override
                                                                                public void generateList(ArrayList<NewAlbum> newAlbums) {
                                                                                    //Bundle extras = getIntent().getExtras();


                                                                                    String url = "http://www.spotifynewmusic.com/tagwall3.php?ans=" + cat;
                                                                                    //String act = getIntent().getStringExtra("from");

                                                                                    //try {
                                                                                    //    Class callerClass = Class.forName(act);
                                                                                    //} catch (ClassNotFoundException e) {
                                                                                    //    e.printStackTrace();
                                                                                    //}
                                                                                    Document doc = null;
                                                                                    try {
                                                                                        doc = Jsoup.connect(url).get();
                                                                                        String temp = doc.html().replace("<br>", "$$$").replace("<br />", "$$$"); //$$$ instead <br>
                                                                                        doc = Jsoup.parse(temp); //Parse again
                                                                                    } catch (IOException e) {
                                                                                        Log.v("samba", Log.getStackTraceString(e));
                                                                                    }

                                                                                    Elements trackelements = doc.getElementsByClass("album");
                                                                                    //;
                                                                                    //ArrayList<String> ids = new ArrayList<String>();
                                                                                    for (Element element : trackelements) {
                                                                                        String image = "http://www.spotifynewmusic.com/" + element.select("img").attr("src");//http://www.spotifynewmusic.com/covers/13903.jpg
                                                                                        Elements links = element.getElementsByClass("play").select("a[href]"); // a with href
                                                                                        String s = links.get(0).attr("href");
                                                                                        Log.v("samba", s);

                                                                                        String div = element.children().get(1).text();
                                                                                        Log.v("samba", div);
                                                                                        try {
                                                                                            String[] list = div.replace("$$$", ";").split(";");
                                                                                            String artist = list[0];
                                                                                            String album = "";
                                                                                            if (list.length > 1)
                                                                                                album = list[1];
                                                                                            //ids.add(artist + "-" + album);
                                                                                            newAlbums.add(new NewAlbum(s, artist, album, image));
                                                                                        } catch (Exception e) {
                                                                                            Log.v("samba", Log.getStackTraceString(e));
                                                                                        }
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void addToFavorites(NewAlbum newAlbum) {
                                                                                    FavoriteRecord fv = new FavoriteRecord(Favorite.SPOTIFYALBUM + newAlbum.url.replace("spotify:album:", ""),
                                                                                            newAlbum.artist + "-" + newAlbum.album, Favorite.NEWALBUM);
                                                                                    fv.save();
                                                                                }

                                                                            };


                                                                            {
                                                                                Intent intent = new Intent(MainActivity.getThis, NewAlbumsActivityElectronic.class);
                                                                                startActivity(intent);
                                                                            }
                                                                            // }
                                                                        } catch (Exception e) {
                                                                            Log.v("samba", Log.getStackTraceString(e));
                                                                        }
                                                                        return true;
                                                                    }
                                                                });

                                                            }
                                                            try {

                                                                if ((title.equals("new albums"))) {
                                                                    Intent intent = new Intent(MainActivity.getThis, NewAlbumsActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                            } catch (Exception e) {
                                                                Log.v("samba", Log.getStackTraceString(e));
                                                            }

                                                            if ((title.equals("enlarge cover"))) {
                                                                MainActivity.displayLargeImage(getThis, bitmap);
                                                            }

                                                            if ((title.equals("refresh token"))) {
                                                                GetSpotifyTokenSync();
                                                            }
                                                            if ((title.equals("search album"))) {
                                                                try {

                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getThis);
                                                                    builder.setTitle("Search album");

                                                                    // Set up the input
                                                                    final EditText input = new EditText(getThis);
                                                                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                                                    builder.setView(input);

                                                                    // Set up the buttons
                                                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            final String artist = input.getText().toString();
                                                                            //SearchActivity.artistName=artist;
                                                                            fillListviewWithValues = new FillListviewWithValues() {

                                                                                @Override
                                                                                public void generateListSearch(final ArrayList<SearchItem> newAlbums) {
                                                                                    spotify.searchAlbums(artist.trim(), new Callback<AlbumsPager>() {

                                                                                        @Override
                                                                                        public void success(AlbumsPager albumsPager, Response response) {
                                                                                            for (AlbumSimple album : albumsPager.albums.items) {
                                                                                                String name = album.name;

                                                                                                SearchItem si = new SearchItem();
                                                                                                si.artist = name;
                                                                                                si.title = "";
                                                                                                si.id = album.id;
                                                                                                si.imageid = album.images.get(0).url;
                                                                                                newAlbums.add(si);
                                                                                            }
                                                                                            SearchActivity.getThis.notifyChange();

                                                                                        }

                                                                                        @Override
                                                                                        public void failure(RetrofitError error) {

                                                                                        }
                                                                                    });
                                                                                }

                                                                                ;

                                                                                @Override
                                                                                public void processAlbum(SearchItem album) {
                                                                                    getAlbumtracksFromSpotify(album.id, album.artist, getThis, albumAdapter, albumsListview);
                                                                                }

                                                                                ;

                                                                            };

                                                                            Intent intent = new Intent(MainActivity.getThis, SearchActivity.class);
                                                                            startActivity(intent);

                                                                        }
                                                                    });
                                                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.cancel();
                                                                        }
                                                                    });

                                                                    builder.show();
                                                                } catch (Exception e) {
                                                                    Log.v("samba", Log.getStackTraceString(e));
                                                                }
                                                            }
                                                            if ((title.equals("search artist"))) {
                                                                try {

                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getThis);
                                                                    builder.setTitle("Search artist");

                                                                    // Set up the input
                                                                    final EditText input = new EditText(getThis);
                                                                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                                                                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                                                                    builder.setView(input);

                                                                    // Set up the buttons
                                                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            final String artist = input.getText().toString();
                                                                            //SearchActivity.artistName=artist;
                                                                            fillListviewWithValues = new FillListviewWithValues() {

                                                                                @Override
                                                                                public void generateListSearch(final ArrayList<SearchItem> newAlbums) {
                                                                                    spotify.searchArtists(artist.trim(), new Callback<ArtistsPager>() {

                                                                                        @Override
                                                                                        public void success(ArtistsPager artistsPager, Response response) {
                                                                                            String id = "";
                                                                                            int max = 10000;
                                                                                            Image image = null;
                                                                                            for (Artist artist : artistsPager.artists.items) {
                                                                                                String name = artist.name;
                                                                                                Log.v("samba", "artist found: " + name);
                                                                                                if (name.startsWith("The "))
                                                                                                    name = name.substring(4);

                                                                                                SearchItem si = new SearchItem();
                                                                                                si.artist = name;
                                                                                                si.title = "";
                                                                                                si.id = artist.id;
                                                                                                if (artist.images.size() > 0)
                                                                                                    si.imageid = artist.images.get(0).url;
                                                                                                else
                                                                                                    si.imageid = "";
                                                                                                newAlbums.add(si);


                                                                                            }
                                                                                            SearchActivity.getThis.notifyChange();
                                                                                        }

                                                                                        @Override
                                                                                        public void failure(RetrofitError error) {

                                                                                        }
                                                                                    });
                                                                                }

                                                                                ;

                                                                                public void processAlbum(SearchItem album) {
                                                                                    //listAlbumsForArtist(album.artist);
                                                                                    Image im = new Image();
                                                                                    try {
                                                                                        im.url = album.images.get(0).url;
                                                                                    } catch (Exception e) {
                                                                                    }
                                                                                    listAlbumsForArtistId(album.id, im, album.artist, new SpotifyApi());
                                                                                }

                                                                                ;
                                                                            };

                                                                            Intent intent = new Intent(MainActivity.getThis, SearchActivity.class);
                                                                            startActivity(intent);

                                                                        }
                                                                    });
                                                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            dialog.cancel();
                                                                        }
                                                                    });

                                                                    builder.show();
                                                                } catch (Exception e) {
                                                                    Log.v("samba", Log.getStackTraceString(e));
                                                                }
                                                            }

                                                            if ((title.equals("oor11"))) {
                                                                String playlistid = "redactie_oor%3Aplaylist%3A3N9rTO6YG7kjWETJGOEvQY";
                                                                new getEntirePlaylistFromSpotify(playlistid, getThis) {
                                                                    @Override
                                                                    public void atLast() {
                                                                        refreshPlaylistFromSpotify(albumAdapter, albumsListview,SpotifyActivity.getThis);
                                                                    }
                                                                }.run();

                                                            }
                                                            if ((title.equals("hide/show"))) {
                                                                if (!nosearch) {
                                                                    refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
                                                                    setVisibility(View.GONE);
                                                                } else {
                                                                    try {
                                                                        if (!artistInitiated)
                                                                            initArtistlist(tracksPlaylist.get(0).artists.get(0).name);
                                                                        setVisibility(View.VISIBLE);
                                                                    } catch (Exception e) {
                                                                        Log.v("samba", Log.getStackTraceString(e));
                                                                        //Log.v("samba", Log.getStackTraceString(e));
                                                                    }
                                                                }
                                                                nosearch = !nosearch;
                                                            }

                                                            return true;
                                                        }

                                                    }

                    );

                }
            });



            spotifyHeader=new SpotifyHeader(this,artistTitleTextView);
            spotifyHeader.connectVarsToFront();

            fab=(FloatingActionButton)

            findViewById(R.id.fab);

            //Log.v("samba","nosearch9");
            if(!nosearch)

            {
                initArtistlist(artistName);
            }

            else

            {
                //Log.v("samba", "nosearch");


                try {
                    //refreshPlaylistFromSpotify(albumAdapter, albumsListview);
                    //mainLayout.setVisibility(View.GONE);//spotifyscrollviewtop
                    int visibility = View.GONE;
                    setVisibility(visibility);//
                    ((LinearLayout) findViewById(R.id.song_display)).setVisibility(View.VISIBLE);
                    //startPlaylistThread
                    customHandler.postDelayed(startPlaylistThread, 1000);
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                    //Log.v("samba", Log.getStackTraceString(e));
                }

            }

            customHandler.postDelayed(updateTimerThread,0);
        }

    public static void addAlbumStatic(int counter, PlanetAdapter albumAdapter, ListView albumsListview) {
        artistName = tracksPlaylist.get(counter).artists.get(0).name;
        getAlbumtracksFromSpotify(tracksPlaylist.get(counter).album.id, tracksPlaylist.get(counter).album.name,getThis,albumAdapter, albumsListview);
    }

    public static void addAlbumToFavoritesTrackwise(int counter) {
        String url = Favorite.SPOTIFYALBUM + tracksPlaylist.get(counter).album.id;
        String name = tracksPlaylist.get(counter).artists.get(0).name;
        String album = tracksPlaylist.get(counter).album.name;
        //Log.v("samba","add "+url+name+"-"+album);
        FavoriteRecord fv=new FavoriteRecord(url,
                name +"-"+ album,Favorite.NEWALBUM);
        fv.save();
    }

    public static void addAlbumToFavorites(String url, String description) {
        FavoriteRecord fv = new FavoriteRecord(url,
                description, Favorite.NEWALBUM);
        fv.save();
    }

    public static void removeAlbum(PlanetAdapter albumAdapter, int counter, ListView albumsListview, AppCompatActivity getThis) {
        String albumid = tracksPlaylist.get(counter).album.id;
        for (int i = tracksPlaylist.size() - 1; i >= 0; i--) {
            if (tracksPlaylist.get(i).album.id == albumid) removeTrackSpotify(i);
            //Log.v("samba","remove "+i);
            //removeTrackSpotify(counter);
        }
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, getThis);
    }


    public static void stopSpotifyPlaying(String ipAddress) {
        try {
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.stop\", \"params\": { \"playerid\": 0 }, \"id\": 1}",
                    ipAddress + "?StopPause");//?StopPause
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v("samba","return:");
        if (resultCode==441){
        }

        super.onActivityResult(requestCode, resultCode, intent);
        Log.v("samba","callback");
        Log.v("samba","callback2");

        //Log.v("samba","ja maar!");
    }
    private void initArtistlist(final String atistName) {
        artistInitiated = true;
        Utils.setDynamicHeight(albumsListview, 0);
        Utils.setDynamicHeight(relatedArtistsListView, 0);

        listAlbumsForArtist(api, spotify, atistName, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                albumsListview.setOnItemClickListener(cl);
                listAlbumsForArtist(api, spotify, atistName, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
            }
        });
    }

    private void setVisibility(int visibility) {
        int opposite=View.GONE;
        if (visibility==opposite)opposite=View.VISIBLE;
        relatedArtistsListView.setVisibility(visibility);

        artistTitleTextView.setVisibility(visibility);
        spotifyHeader.icon.setVisibility(visibility);
        fab.setVisibility(opposite);//spotifyscrollviewtop
        ((TextView) findViewById(R.id.relatedartists_text)).setVisibility(visibility);//albumsartist_listview
        ((TextView) findViewById(R.id.albumsartist_listview)).setVisibility(visibility);//albumsartist_listview

        spotifyHeader.MessageView.setVisibility(visibility);
        ((ScrollView) findViewById(R.id.spotifyscrollviewtop)).setVisibility(visibility);//albumsartist_listview
    }

    public static void playlistGotoPosition(int position) {
        GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"playlistid\": 0, \"position\": " + (/*spotifyStartPosition + */position) + " } }, \"id\": 1}",
                ipAddress + "?PlayerOpen");
    }

    public void getAlbumtracksFromSpotify(final int position) {
        String s = albumIds.get(position);
        getAlbumtracksFromSpotify(s, albumList.get(position),getThis, albumAdapter, albumsListview);
    }

    public static void getAlbumtracksFromSpotify(final String albumid, final String albumname, final AppCompatActivity getThis1, PlanetAdapter albumAdapter, ListView albumsListview) {
        if (albumAdapter==null)albumAdapter=SpotifyActivity.getThis.albumAdapter;
        if (albumsListview==null)albumsListview=SpotifyActivity.getThis.albumsListview;
        final PlanetAdapter albumAdapter1=albumAdapter;
        final ListView albumsListview1=albumsListview;
        AppCompatActivity getThis=getThis1;
        //int position = ;
        new SpotifyApi().getService().getAlbumTracks(albumid, new Callback<Pager<Track>>() {

            @Override
            public void success(Pager<Track> trackPager, Response response) {
                //albumList.clear();
                ArrayList<String> ids = new ArrayList<String>();
                for (Track t : trackPager.items) {
                    try {
                        Album alb = new Album();
                        alb.name = albumname;
                        alb.id = albumid;
                        final Image im=new Image();
                        im.url="";
                        new DownLoadImageUrlTask() {
                            @Override
                            public void setUrl(String logo) {
                                im.url=logo;
                            }
                        }.execute(albumid);

                        List<Image> l = new ArrayList();
                        l.add(im);
                        alb.images=l;
                        t.album = alb;
                        Artist art = new Artist();
                        art.name = artistName;
                        List<ArtistSimple> a = new ArrayList();
                        a.add(art);
                        t.artists = a;
                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }
                    hm.put(t.id, t);
                    ids.add(t.id);
                    //albumList.add(t.name+String.format("(%s)", Mp3File.niceString(new Double(t.duration_ms / 1000).intValue())));
                }
                //albumAdapter.notifyDataSetChanged();
                //Utils.setDynamicHeight(albumsListview, 0);
                new AddTracksToPlaylist(ids, getThis) {
                    @Override
                    public void atEnd() {
                        //Log.v("samba", "einde taak");
                        refreshPlaylistFromSpotify(albumAdapter1, albumsListview1,getThis1);
                    }

                }.run();

                //addTracksToPlaylist(ids);

            }


            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void listAlbumsForArtist(String s) {
        listAlbumsForArtist(api, spotify, s, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
    }

    public static void removeDownlist(PlanetAdapter albumAdapter, ListView albumsListview, int counter, AppCompatActivity getThis) {
        for (int i = counter; i < tracksPlaylist.size(); i++) {
            //Log.v("samba", "remove " + i);
            removeTrackSpotify(counter);
        }
        spotifyStartPosition = 0;
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, getThis);
    }

    public static void removeTrackSpotify(int counter) {
        GetJsonFromUrl(
                "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Remove\", \"params\": { \"playlistid\": 0, \"position\": " + counter + "}, \"id\": 1}",
                ipAddress + "?PlayerRemove");
    }

    public static void removeUplist(PlanetAdapter albumAdapter, ListView albumsListview, int counter, AppCompatActivity getThis) {
        for (int i = 0; i < counter + 1; i++)
            removeTrackSpotify(0);
        spotifyStartPosition = 0;
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, getThis);
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");        Toast.makeText(getApplicationContext(), "User logged in",
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

        Toast.makeText(getApplicationContext(), "User login failed",
                         Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "User login failed");
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    public static class getEntirePlaylistFromSpotify {
        String playlistid;
        Activity getThis;

        getEntirePlaylistFromSpotify(String playlistid, Activity getThis) {
            this.playlistid = playlistid;
            this.getThis = getThis;
        }

        public void run() {
            JSONObject playlist = GetJsonFromUrl(
                    "{\"jsonrpc\":\"2.0\",\"method\":\"Files.GetDirectory\",\"id\":1,\"params\":[\"plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3A" + playlistid + "%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D\",\"music\",[\"title\",\"file\",\"thumbnail\", \"art\",\"duration\"]]}",
                    ipAddress + "?OpenAddon_plugin://plugin.audio.spotlight/?path=GetPlaylist&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22spotify%3Auser%3A" + playlistid + "%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D");

            //line read:{"id":1,"jsonrpc":"2.0","result":{"files":[{"art":{"thumb":"image://http%3a%2f%2f127.0.0.1%3a8081%2fimage%2fae346f2c03ddf26e39ee5111b068c6b1e41543f0.jpg/"},"duration":247,"file":"http://127.0.0.1:8081
            //Log.v("samba", "clicked3!");
            JSONArray items = null;
            try {
                items = playlist.getJSONArray("files");
                ArrayList<String> ids = new ArrayList<String>();
                for (int i = 0; i < items.length(); i++) {
                    String file = "";
                    JSONObject o = items.getJSONObject(i);
                    file = o.getString("file");
                    String fileid = getTrackId(file);
                    //Log.v("samba", fileid);
                    if (fileid.length()>0)
                    ids.add(fileid);
                }
                new AddTracksToPlaylist(ids, getThis) {
                    @Override
                    public void atEnd() {
                        atLast();
                        //refreshPlaylistFromSpotify(albumAdapter, albumsListview);
                    }

                }.run();
                //addTracksToPlaylist(ids);
                //if (ids.size() > 0)
                //    if (spotifyToken.length() == 0) GetSpotifyToken();
                //AddSpotifyTrack(getThis, ids, 0);
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        }

        public void atLast() {

        }
    }

    public static void clearSpotifyPlaylist() {
        checkAddress();
        GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 0, \"method\": \"Playlist.Clear\", \"params\": {\"playlistid\": 0}}",
                ipAddress + "?PlaylistClear");//
    }

    public abstract static class addAlbumWithIdToSpotify {
        String id;
        String artist;
        String album;
        Activity getThis;

        public abstract void atLast();

        addAlbumWithIdToSpotify(String id, String artist, String album, Activity getThis) {
            this.id = id;
            this.artist = artist;
            this.album = album;
            this.getThis = getThis;
        }

        public void run() {
            new SpotifyApi().getService().getAlbumTracks(id, new Callback<Pager<Track>>() {

                        @Override
                        public void success(Pager<Track> trackPager, Response response) {
                            //albumList.clear();
                            ArrayList<String> ids = new ArrayList<String>();
                            for (Track t : trackPager.items) {
                                try {
                                    Album alb = new Album();
                                    alb.name = album;
                                    alb.id = id;
                                    final Image im = new Image();
                                    im.url = "";
                                    new DownLoadImageUrlTask() {
                                        @Override
                                        public void setUrl(String logo) {
                                            im.url = logo;
                                        }
                                    }.execute(id);

                                    List<Image> l = new ArrayList();
                                    l.add(im);
                                    alb.images = l;
                                    t.album = alb;
                                    Artist art = new Artist();
                                    art.name = artist;
                                    List<ArtistSimple> a = new ArrayList();
                                    a.add(art);
                                    t.artists = a;
                                } catch (Exception e) {
                                    Log.v("samba", Log.getStackTraceString(e));
                                }
                                hm.put(t.id, t);
                                ids.add(t.id);
                                //albumList.add(t.name+String.format("(%s)", Mp3File.niceString(new Double(t.duration_ms / 1000).intValue())));
                            }

                            //albumAdapter.notifyDataSetChanged();
                            //Utils.setDynamicHeight(albumsListview, 0);
                            new AddTracksToPlaylist(ids, getThis) {
                                @Override
                                public void atEnd() {
                                    //Log.v("samba", "einde taak");
                                    atLast();
                                }

                            }.run();

                            //addTracksToPlaylist(ids);
                        }


                        @Override
                        public void failure(RetrofitError error) {

                        }
                    }
            );


        }
    }

    public static class addExternalPlaylistToSpotify {
        String url;
        Activity getThis;

        addExternalPlaylistToSpotify(String url, Activity getThis) {
            this.url = url;
            this.getThis = getThis;
        }

        public void run() {
            stopMpd();

            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
            Elements trackelements = doc.select("meta[property=music:song]");
            ArrayList<String> ids = new ArrayList<String>();
            for (Element element : trackelements) {
                String s = element.attr("content");
                int startIndex = s.indexOf("track/") + 6;
                ids.add(element.attr("content").substring(startIndex));

                //Log.v("samba", element.attr("content").substring(startIndex));
            }
            new AddTracksToPlaylist(ids, getThis) {
                @Override
                public void atEnd() {
                    atLast();
                }

            }.run();
            //addTracksToPlaylist(ids);
        }

        public void atLast() {

        }
    }

    public static void stopMpd() {
        if (MainActivity.getThis.getLogic().mpcStatus.playing) {
            MainActivity.getThis.getLogic().getMpc().stop();
        }
    }

    static class Task implements Runnable {
        ArrayList<String> mainids;
        Activity getThis;

        Task(ArrayList<String> ids, Activity getThis) {
            mainids = ids;
            this.getThis = getThis;

        }

        @Override

        public void run() {

            AddSpotifyTrack(getThis, mainids, 0);
            atEnd2();

        }

        public void atEnd2() {

        }


    }

    static class AddTracksToPlaylist {
        ArrayList<String> mainids;
        Activity getThis;

        AddTracksToPlaylist(ArrayList<String> ids, Activity getThis) {
            mainids = ids;
            this.getThis = getThis;

        }

        public void run() {
            try {

                //mainids=ids;
                //spotifyToken.put(ipAddress,fname.substring(startIndex, endIndex));//checkAddress
                checkAddress();
                if (mainids.size() > 0)
                    if (spotifyToken.get(ipAddress) == null) GetSpotifyTokenSync();
                dialog1 = new ProgressDialog(getThis);
                dialog1.setTitle("spotify-playlist");
                dialog1.setMessage("Adding to list");
                dialog1.setProgressStyle(dialog1.STYLE_HORIZONTAL);
                dialog1.setProgress(0);
                dialog1.setMax(mainids.size());
                dialog1.show();


                new Thread(new Task(mainids, getThis) {
                    public void atEnd2() {
                        //Log.v("samba", "einde taak");
                        MainActivity.getThis.runOnUiThread(new Runnable() {
                            public void run() {

                                dialog1.dismiss();
                                atEnd();

                            }
                        });
                    }

                }).start();

            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
                //Log.v("samba", Log.getStackTraceString(e));
            }
        }

        public void atEnd() {

        }


    }

    public static void addTracksToPlaylist(ArrayList<String> ids) {
        try {
            new AddTracksToPlaylist(ids, getThis) {
                @Override
                public void atEnd() {
                    //Log.v("samba", "einde taak");
                }

            }.run();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //Log.v("samba", Log.getStackTraceString(e));
        }

    }

    public static void refreshPlaylistFromSpotify(PlanetAdapter albumAdapter, ListView albumsListview, AppCompatActivity getThis) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getThis);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Get playlist...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();
        albumVisible = false;
        albumAdapter.setAlbumVisible(false);
        try {
            JSONObject playlist = GetJsonFromUrl(
                    "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.GetItems\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"duration\", \"thumbnail\",\"file\"], \"playlistid\": 0 }, \"id\": 1}\u200B",
                    ipAddress + "?GetPLItemsAudio");
            //Log.v("samba", "refresh");

            albumList.clear();
            albumTracks.clear();
            JSONArray items = null;
            items = playlist.getJSONArray("items");
            String prevAlbum = "";
            tracksPlaylist.clear();
            for (int i = 0; i < items.length(); i++) {
                final PlaylistItem pi=new PlaylistItem();
                pi.pictureVisible=false;
                String trackid = "";
                JSONObject o = items.getJSONObject(i);
                trackid = getTrackId(o.getString("file"));
                if (trackid.length()>0) {
                    Track t = getTrack(trackid);
                    //tracksPlaylist.add(t);
                    Log.v("samba", t.name);
                    //ArrayList<String> ids=new ArrayList<String>();
                    String extra = "";
                    try {
                        String name = t.album.name;
                        if (!prevAlbum.startsWith(name)) {
                            extra = String.format("(%s-%s)", t.artists.get(0).name, name);
                            prevAlbum = name;
                        }
                        pi.pictureVisible = true;
                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }
                    pi.text = t.name + extra + String.format("(%s)", Mp3File.niceString(new Double(t.duration_ms / 1000).intValue()));
                    new DownLoadImageUrlTask() {
                        @Override
                        public void setUrl(String logo) {
                            pi.url = logo;
                        }
                    }.execute(t.album.id);


                    pi.url = t.album.images.get(0).url;
                    albumList.add(pi.text);
                    albumTracks.add(pi);
                    tracksPlaylist.add(t);
                }
            }
            //spotifyStartPosition=0;
            albumAdapter.setDisplayCurrentTrack(true);
            albumAdapter.notifyDataSetChanged();
            try{
            Utils.setDynamicHeight(albumsListview, 0);
        } catch (Exception e) {
            //Log.v("samba", Log.getStackTraceString(e));
        }

        //albumsListview.setOnItemClickListener(selectOnPlaylist);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        progressDialog.dismiss();

    }


    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {
        initArtistLook(beatles);
        spotify.searchArtists(beatles.trim(), new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                String id = "";
                int max = 10000;
                Image image = null;
                for (Artist artist : artistsPager.artists.items) {
                    String name = artist.name;
                    //Log.v("samba","artist found: "+name);
                    if (name.startsWith("The ")) name = name.substring(4);
                    if (name.toLowerCase().replace(" ","").contains(beatles.toLowerCase().replace(" ",""))) {


                        //Log.v("samba","artist found: "+name);
                        if (name.length() < max) {
                            id = artist.id;
                            try{
                            image = artist.images.get(0);
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }
                            max = name.length();
                        }
                    }


                }

                listAlbumsForArtistId(id, image, beatles, api);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void initArtistLook(String beatles) {
        albumsListview.setOnItemClickListener(cl);
        //artistName = s;
        this.artistName =beatles;
        albumVisible = true;
        albumAdapter.setAlbumVisible(true);
    }

    public void listAlbumsForArtistId(String id, Image image, String beatles, SpotifyApi api) {
        initArtistLook(beatles);

        spotifyHeader.setArtistText(beatles, image);
        SpotifyService spotify = api.getService();
        spotify.getArtistAlbums(id, new Callback<Pager<Album>>() {

            @Override
            public void success(Pager<Album> albumPager, Response response) {
                albumList.clear();
                albumTracks.clear();
                albumIds.clear();
                //albumTracks.clear();
                String previous = "";
                for (Album album : albumPager.items)
                    if (!album.name.equals(previous)) {
                        PlaylistItem pi=new PlaylistItem();
                        pi.pictureVisible=true;
                        pi.url=album.images.get(0).url;
                        pi.text=album.name;

                        albumList.add(album.name);
                        albumIds.add(album.id);
                        albumTracks.add(pi);
                        previous = album.name;

                    }
                albumAdapter.setDisplayCurrentTrack(false);
                //albumAdapter.setDisplayCurrentTrack(true);
                albumAdapter.notifyDataSetChanged();
                Utils.setDynamicHeight(albumsListview, 0);

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
        spotify.getRelatedArtists(id, new Callback<Artists>() {
            @Override
            public void success(Artists artists, Response response) {
                artistList.clear();
                for (Artist artist : artists.artists) {
                    artistList.add(artist.name);
                }
                relatedArtistsAdapter.notifyDataSetChanged();
                Utils.setDynamicHeight(relatedArtistsListView, 0);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            updateSongInfo(songItems.time,songItems.totaltime,songItems.tvName,songItems.artist,songItems.image,albumAdapter,albumsListview, getThis,getSpotifyInterface);

            customHandler.postDelayed(this, 1000);
        }

    };

    private Runnable startPlaylistThread = new Runnable() {

        public void run() {
            refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
        }

    };

    public static void updateSongInfo(TextView time,TextView totaltime,TextView tvName,TextView artist,
                                      ImageView image, PlanetAdapter albumAdapter, ListView albumsListview, AppCompatActivity getThis,final SpotifyInterface getSpotifyInterface) {
        try {
            String s = getJsonStringFromUrl("[{\"jsonrpc\":\"2.0\",\"method\":\"Application.GetProperties\",\"id\":1,\"params\":[[\"muted\"]]},{\"jsonrpc\":\"2.0\",\"method\":\"Player.GetProperties\",\"id\":2,\"params\":[0,[\"time\", \"totaltime\", \"percentage\", \"shuffled\",\"repeat\",\"speed\"]]}," +
                            "{ \"jsonrpc\": \"2.0\", \"method\": \"Player.GetItem\", \"params\": { \"playerid\": 0, \"properties\": [ \"title\", \"showtitle\", \"artist\", \"thumbnail\", \"streamdetails\", \"file\", \"season\", \"episode\"] }, \"id\": 3 }]",
                    ipAddress + "?GetRemoteInfos");//
            //result:[{"id":1,"jsonrpc":"2.0","result":{"muted":false}},{"id":2,"jsonrpc":"2.0","result":{"percentage":85.810699462890625,"repeat":"off","shuffled":false,"speed":1,"time":{"hours":0,"milliseconds":493,"minutes":3,"seconds":4},"totaltime":{"hours":0,"milliseconds":0,"minutes":3,"seconds":35}}},{"id":3,"jsonrpc":"2.0","result":{"item":{"artist":[],"file":"http://127.0.0.1:8081/track/4btX6FOX75h0D40m8eQPAe.wav|X-Spotify-Token=d46cdae3fb885e0c3bd450a053c95916028f9790&User-Agent=Spotlight+1.0","label":"4btX6FOX75h0D40m8eQPAe.wav","thumbnail":"image://DefaultAlbumCover.png/","title":"4btX6FOX75h0D40m8eQPAe.wav","type":"song"}}}]
            //speed=0:paused or not playing
            JSONArray jsonRootObject = null;
            try {
                jsonRootObject = new JSONArray(s);
                Double speed = jsonRootObject.getJSONObject(1).getJSONObject("result").getDouble("speed");
                if (speed.doubleValue() > 0) {
                    String fname = jsonRootObject.getJSONObject(2).getJSONObject("result").getJSONObject("item").getString("file");
                    String trid = getTrackId(fname);
                    if (trid.length() > 0) {
                        //currentTrack=0;
                        for (int i = 0; i < tracksPlaylist.size(); i++) {
                            if (tracksPlaylist.get(i).id.equals(trid)) {
                                if (currentTrack != i)
                                    albumsListview.setItemChecked(currentTrack, false);
                                currentTrack = i;
                                //Log.v("samba", "current track:" + i + "," + tracksPlaylist.get(i).name);
                                break;
                            }
                        }

                        //albumsListview.setItemChecked(currentTrack,true);
                        albumAdapter.setCurrentItem(currentTrack);
                        albumAdapter.notifyDataSetChanged();
                        final String trackid = trid;

                        Track t = getTrack(trackid);
                        if (t != null)
                            if ((getSpotifyInterface.previousTrack == null) || !(t.id == getSpotifyInterface.previousTrack.id)) {
                                //Log.v("samba", trackid);
                                getSpotifyInterface.previousTrack = t;
                                try {
                                    String imageurl = t.album.images.get(0).url;
                                    if (imageurl == "") {
                                        String urlString = "https://api.spotify.com/v1/tracks/" + trackid;
                                        String getResult = getStringFromUrl(urlString);
                                        imageurl = new JSONObject(getResult).getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                                    }

                                    DownLoadImageUrlTask.setAlbumPicture(t.album.id, imageurl);
                                    new DownLoadImageTask() {
                                        @Override
                                        public void setImage(Bitmap logo) {
                                            getThis.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    image.setImageBitmap(logo);
                                                    SpotifyActivity.bitmap = logo;
                                                }
                                            });
                                        }
                                    }.execute(imageurl);
                                } catch (Exception e) {
                                    Log.v("samba", Log.getStackTraceString(e));
                                }
                            }

                        Double d = jsonRootObject.getJSONObject(1).getJSONObject("result").getDouble("percentage");
                        JSONObject timeObject = jsonRootObject.getJSONObject(1).getJSONObject("result").getJSONObject("time");
                        int hours = timeObject.getInt("hours");
                        int mins = timeObject.getInt("minutes");
                        int secs = timeObject.getInt("seconds");
                        JSONObject ttimeObject = jsonRootObject.getJSONObject(1).getJSONObject("result").getJSONObject("totaltime");
                        int thours = ttimeObject.getInt("hours");
                        int tmins = ttimeObject.getInt("minutes");
                        int tsecs = ttimeObject.getInt("seconds");
                        int timeint = hours * 60 * 60 + mins * 60 + secs;
                        getThis.runOnUiThread(new Runnable() {
                            public void run() {
                                time.setText(niceTime(timeint));
                                int ttimeint = thours * 60 * 60 + tmins * 60 + tsecs;
                                totaltime.setText(niceTime(ttimeint));
                                tvName.setText(t.name);
                                artist.setText(t.artists.get(0).name);

                            }
                        });
                    }
                }

            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }



    public static Track getTrack(String trackid) {
        Track nt = (Track) hm.get(trackid);
        try {
            if (nt == null) {
                nt = new Track();
                JSONObject o = new JSONObject(getStringFromUrl("https://api.spotify.com/v1/tracks/" + trackid));
                nt.id = trackid;
                List<ArtistSimple> a = new ArrayList();
                Artist art = new Artist();
                art.name = o.getJSONArray("artists").getJSONObject(0).getString("name");
                a.add(art);
                nt.artists = a;
                Album alb = new Album();
                alb.name = o.getJSONObject("album").getString("name");
                alb.id = o.getJSONObject("album").getString("id");
                nt.name = o.getString("name");//duration_ms
                alb.images=new ArrayList<Image>();
                Image im=new Image();
                try{
                    im.url= o.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                } catch (Exception e) {
                    im.url="";
                }
                alb.images.add(im);
                nt.duration_ms = o.optLong("duration_ms");//
                nt.album = alb;
                hm.put(nt.id, nt);
            }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        return nt;
    }


    @NonNull
    public static String getTrackId(String fname) {
        try{
        int startIndex = fname.indexOf("track/") + 6;
        int endIndex = fname.indexOf(".wav");
        return fname.substring(startIndex, endIndex);
    } catch (Exception e) {
        return "";
    }
    }

    @NonNull
    public static  String getStringFromUrl(String urlString) {
        try {
            //Log.v("samba", urlString);
            StringBuilder sb = new StringBuilder();
            InputStream is = new URL(urlString).openStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                sb.append(inputStr);
            return sb.toString();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        return "";
    }

/*
                                new DownLoadImageUrlTask() {
                                    @Override
                                    public void setUrl(Bitmap logo) {
                                        image.setImageBitmap(logo);
                                        getThis.bitmap = logo;
                                    }
                                }.execute(albumId);

 */
    private static abstract class DownLoadImageUrlTask extends AsyncTask<String, Void, String> {
    public static void setAlbumPicture(String key, String value) {
        DownLoadImageUrlTask.albumPictures.put(key,value);
    }

    //String input of doInBackground, void input of onProgressUpdate, String input of onPostExecute (and this return value of doInBackground)
    //onPreExecute() is called on the UI thread, before the Non-UI work starts
        private static HashMap<String, String>albumPictures=new HashMap<>();


        public DownLoadImageUrlTask() {
        }

        public abstract void setUrl(String logo);

        /*
            doInBackground(Params... params)
                Override this method to perform a computation on a background thread.
         */
        protected String doInBackground(String... urls) {
            String albumId = urls[0];
            String imageUrl = null;
            if (albumPictures.containsKey(albumId)) {
                //if (albumPictures.get(niceAlbumName) != null)

                try {
                    int n=0;
                    while ((albumPictures.get(albumId) == null)&&(n<30)) {
                        //Log.v("samba","wait....."+n+" iteration");
                        Thread.sleep(1000);
                        n++;
                    }
                    return(albumPictures.get(albumId));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //setBitmap(albumPictures.get(niceAlbumName));

            } else             try {
                String urlString = "https://api.spotify.com/v1/albums/" + albumId;
                String getResult = getStringFromUrl(urlString);
                imageUrl = new JSONObject(getResult).getJSONArray("images").getJSONObject(0).getString("url");
                albumPictures.put(albumId, imageUrl);//so image is loaded only once
            } catch (Exception e) { // Catch the download exception
                Log.v("samba", Log.getStackTraceString(e));
            }
            return imageUrl;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(String result) {
            setUrl(result);
        }
    }

    public abstract class WaitClass extends AsyncTask<String,Void,String>{
        private final String message;
        private Context context;
        private ProgressDialog progressDialog;
        public WaitClass(Context context, String message){
            this.message=message;
            this.context=context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(true);
            progressDialog.setMessage(message+"...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
            progressDialog.dismiss();
        }


        }
    ;


}

class PlaylistItem {
    public boolean pictureVisible;
    public String url;
    public String text;
}

class SpotifyHeader {
    AppCompatActivity getThis;
    TextView artistTitleTextView;

    public ImageView icon;
    public TextView MessageView;

    public SpotifyHeader(AppCompatActivity getThis, TextView artistTitleTextView){
        this.getThis=getThis;
        this.artistTitleTextView=artistTitleTextView;
    }
    public void connectVarsToFront() {
        icon = (ImageView)

                getThis.findViewById(R.id.icon2);


        //RelativeLayout mainLayout = (RelativeLayout) getThis.findViewById(R.id.spotifylayouttop);
        MessageView = (TextView)

                getThis.findViewById(R.id.artist_content);        //Expose the indent for the first three rows
    }

    public void setArtistText(final String artistName, Image image) {

        artistTitleTextView.setText(artistName);

        String artistText = "";

        try {
            JSONObject artist = (new JSONObject(SpotifyActivity.LastFMArtist(artistName))).getJSONObject("artist");

            artistText = artist.getJSONObject("bio").getString("content");
        } catch (JSONException e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        SpannableString SS = new SpannableString(artistText);

        int scale = 250;
        int leftMargin = scale + 10;

        //Set the icon in R.id.icon
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(scale, scale);
        icon.setLayoutParams(layoutParams);


        try {
            new DownLoadImageTask() {
                @Override
                public void setImage(Bitmap logo) {
                    ImageView i = (ImageView) getThis.findViewById(R.id.image);
                    //Log.v("samba", "image loaded");
                    icon.setImageBitmap(logo);
                }
            }.execute(image.url);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
/*        try {
            //sometimes no album-image available
            ImageView i = (ImageView) findViewById(R.id.image);
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(image.url).getContent());
            Log.v("samba", "image loaded");
            icon.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }*/


        SS.setSpan(new MyLeadingMarginSpan2(scale / 50, leftMargin), 0, SS.length(), 0);
        MessageView.setText(SS);
    }
}

class MyLeadingMarginSpan2 implements LeadingMarginSpan.LeadingMarginSpan2 {
    private int margin;
    private int lines;

    MyLeadingMarginSpan2(int lines, int margin) {
        this.margin = margin;
        this.lines = lines;
    }

    /*Returns the value to which must be added indentation*/
    @Override
    public int getLeadingMargin(boolean first) {
        if (first) {
                /*This * indentation is applied to the number of                    rows returned * getLeadingMarginLineCount ()*/

            return margin;
        } else {
            //Offset for all other Layout layout ) { }
       /*Returns * the number of rows which should be  applied *         indent returned by getLeadingMargin (true)
        * Note:* Indent only applies to N lines of the first paragraph.*/

            return 0;
        }
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom, CharSequence text,
                                  int start, int end, boolean first, Layout layout) {
    }

    @Override
    public int getLeadingMarginLineCount() {
        return lines;
    }
}
