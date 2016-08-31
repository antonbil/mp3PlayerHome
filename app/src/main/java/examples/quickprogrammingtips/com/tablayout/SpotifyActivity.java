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
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

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
import java.util.concurrent.CopyOnWriteArrayList;

import examples.quickprogrammingtips.com.tablayout.adapters.ArtistAutoCompleteAdapter;
import examples.quickprogrammingtips.com.tablayout.adapters.InstantAutoComplete;
import examples.quickprogrammingtips.com.tablayout.adapters.RelatedArtistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.ImageLoadTask;
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
import mpc.DatabaseListThread;
import mpc.MPC;
import mpc.MPCDatabaseListListener;
import mpc.MPCStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static examples.quickprogrammingtips.com.tablayout.model.Favorite.generateLists;
import static examples.quickprogrammingtips.com.tablayout.model.Mp3File.niceTime;

//import com.spotify.sdk.android.player.ConnectionStateCallback;
//import org.apache.commons.lang3.StringUtils;

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
/*
Een api-call doen op poort 6680:

curl -d '{"jsonrpc": "2.0", "id": 1, "method": "core.tracklist.add", "params": {"uris":["spotify:track:7gbHxCG82lvHla9q3nMXtQ","spotify:track:5kJQpjwBeWXzQQmKnrQa5L"]}}' http://192.168.2.12:6680/mopidy/rpc
curl -d '{"jsonrpc": "2.0", "id": 1, "method": "core.playback.play"}' http://192.168.2.12:6680/mopidy/rpc tl_track
TracklistController.clear()
wordt dus: core.tracklist.clear
TracklistController.get_tl_tracks()
PlaybackController.play(tl_track=None, tlid=None)
PlaybackController.next()
PlaybackController.previous()
PlaybackController.stop() etc.
PlaybackController.get_current_tl_track()
PlaybackController.get_time_position()
PlaybackController.get_state()

 */
public class SpotifyActivity extends AppCompatActivity implements
         MPCDatabaseListListener {
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "89f945f1696e4f389aaed419e51beaad";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "testschema://callback";
    public static final String MPD = "mpd://";
    public static final int SpotifyList = 1;
    public static final int AlbumList = 0;
    public static final int MpdList = 2;
    //private static ArrayList<String> mainids;
    public static int playingEngine;
    //public static boolean longClickHasbeenCalled=false;
    private SpotifyHeader spotifyHeader;
    public FillListviewWithValues fillListviewWithValues;
    public ArrayList<String> artistList = new ArrayList<>();
    public static  ArrayList<String> albumIds = new ArrayList<>();
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
    public static String nextCommand="";
    public PlanetAdapter albumAdapter;
    protected ListView albumsListview;
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
    public static final ArrayList<String> CATEGORY_IDS = new ArrayList<>(Arrays.asList("electronic", "progressive", "alternative", "rnb", "soul", "singer-songwriter",
            "classical","acoustic", "ambient", "americana", "blues", "country", "techno", "shoegaze", "Hip-Hop", "funk", "jazz", "rock", "folk"));
    public static ArrayList<String> searchArtistString =new ArrayList<>();
    private static String searchAlbumString ="";
    private static int totalTime;
    private static int currentTime;
    //OnFlingGestureListener flingListener;
    private boolean displayMpd;
    public static int currentList=SpotifyList+1;
    private String[] lists = new String[]{"albumlist","spotifylist","mpdlist"};;

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
        spotifyToken.put(ipAddress,"something");
        /*
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
        }*/
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
        ipAddress = //String.format("http://%s:8080/jsonrpc", ip);
                String.format("http://%s:6680/mopidy/rpc", ip);
        return ipAddress;
    }

    public static void playSpotify(){
        try {
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.play\" }",
                    ipAddress);
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.tracklist.set_repeat\", \"params\": {\"value\":true} }",
                    ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void playAtPosition(int position){
        JSONArray playlist = getPlaylist();
        try {
            int plid=playlist.getJSONObject(position).getInt("tlid");
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.play\", \"params\": { \"tlid\":"  + plid + " } }",
                    ipAddress);
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.tracklist.set_repeat\", \"params\": {\"value\":true} }",
                    ipAddress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void AddSpotifyTrack(Activity getThis, ArrayList<String> ids, final int pos) {
        try {
            if (pos < ids.size()) {
                dialog1.incrementProgressBy(1);
                //add track to playlist
                String prefix="spotify:track:";
                String uri=ids.get(pos);
                if (uri.startsWith("spotify"))prefix="";
                AddSpotifyItemToPlaylist(prefix, uri);

                //String data = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Add\", \"params\": { \"playlistid\" : 0 , \"item\" : {\"file\" : \"http://127.0.0.1:8081/track/%s.wav|X-Spotify-Token=%s&User-Agent=Spotlight+1.0\"}}, \"id\": 1}", ids.get(pos), spotifyToken.get(ipAddress));
                //String urlString = ipAddress;// + "?PlaylistAdd";
                //GetJsonFromUrl(data, urlString);
                AddSpotifyTrack(getThis, ids, pos + 1);
            } else {
                //all tracks added
                stopMpd();
                //get playlist from server
                JSONArray playlist = getPlaylist();

                spotifyStartPosition = playlist.length() - ids.size();
                playAtPosition(spotifyStartPosition);
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

    public static JSONObject AddSpotifyItemToPlaylist(String prefix, String uri) {
        String data= String.format("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.tracklist.add\", \"params\": {\"uris\":[\"%s%s\"]}}", prefix,uri);
        String urlString = ipAddress;// + "?PlaylistAdd";
        return GetJsonFromUrl(data, urlString);

    }

    private static JSONArray getPlaylist() {
        return GetJsonArrayFromUrl(
                            "{\"jsonrpc\": \"2.0\", \"method\": \"core.tracklist.get_tl_tracks\", \"id\": 1}",
                            ipAddress);
    }

    private static JSONArray GetJsonArrayFromUrl(String data, String urlString) {
        JSONObject jsonRootObject = null;

        String sb = getJsonStringFromUrl(data, urlString);
        //Log.v("samba", sb);
        try {
            jsonRootObject = new JSONObject(sb);
            return jsonRootObject.getJSONArray("result");
        } catch (JSONException e) {
           // Log.v("samba", Log.getStackTraceString(e));
        }
        return null;
    }

    private static JSONObject GetJsonFromUrl(String data, String urlString) {
        JSONObject jsonRootObject = null;

        //Log.v("samba", data);
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
        //Log.v("samba", "command:" + data);
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
            try{
            dialog1=new ProgressDialog(this);
            updateBarHandler = MainActivity.getThis.updateBarHandler;
        // Code called from an activity
        /*final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private"})
                .build();

        AuthenticationClient.openLoginInBrowser(this, request);*/

            memoryHandler_ = new Handler();
            checkAppMemory();


            /*flingListener = new OnFlingGestureListener() {
                @Override
                public void onRightToLeft() {
                    Log.v("samba","righttoleft");
                }

                @Override
                public void onLeftToRight() {
                    Log.v("samba","lefttoright");
                }

                @Override
                public void onTapUp() {

                }

                @Override
                public void onLongTapUp() {

                }


            };*/
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

            SmartLinkSwipeDetector swipeDetector = new SmartLinkSwipeDetector();
            albumsListview.setOnTouchListener(swipeDetector);
            setAdapterForSpotify();

        albumsListview.setOnItemClickListener(cl);
        relatedArtistsListView = (ListView) findViewById(R.id.relatedartists_listview);
            relatedArtistsListView.setOnTouchListener(swipeDetector);
            findViewById(R.id.spotifylayouttop).setOnTouchListener(swipeDetector);
            //
        //Log.v("samba", "nosearch3");

        relatedArtistsAdapter = new RelatedArtistAdapter<String>(this, android.R.layout.simple_list_item_1, artistList);
        relatedArtistsListView.setAdapter(relatedArtistsAdapter);

            playButtonsAtBottom();

            artistTitleTextView = (TextView)

                    findViewById(R.id.artist_title);//relatedartists_text

            songItems=new SongItems(this);
            findViewById(R.id.time_layout).setOnClickListener(v -> MainActivity.playPauseAll());
            songItems.setOnClickTitles(v -> getThis.finish());
            songItems.setOnClick(arg0 -> {

                View image = songItems.image;
                PopupMenu menu = new PopupMenu(songItems.image.getContext(), image);
                menu.getMenu().add("play");
                menu.getMenu().add("search artist");
                menu.getMenu().add("search album");
                menu.getMenu().add("new albums");
                menu.getMenu().add("new albums categories");

                menu.show();
                menu.setOnMenuItemClickListener(item -> {
                    String title = item.getTitle().toString();
                    if ((title.equals("play"))) {
                        SpotifyActivity.showPlayMenu(getThis,songItems.image);
                    } else
                    if ((title.equals("new albums categories"))) {
                        PopupMenu menu1 = new PopupMenu(songItems.image.getContext(), songItems.image);
                        ;
                        for (String cat : CATEGORY_IDS) {
                            menu1.getMenu().add(cat);
                        }
                        menu1.show();
                        generateLists();
                        menu1.setOnMenuItemClickListener(item1 -> {
                            String title1 = item1.getTitle().toString();

                            try {
                                //if (title.startsWith("new albums ")) {
                                final String cat = title1.replace("new albums ", "");
                                fillListviewWithValues = new FillListviewWithValues() {

                                    @Override
                                    public void generateList(ArrayList<NewAlbum> newAlbums) {
                                        //Bundle extras = getIntent().getExtras();

                                        String url = "http://www.spotifynewmusic.com/tagwall3.php?ans=" + cat;

                                        Favorite.NEWALBUM=Favorite.getCategoryId(cat);

                                        Document doc = null;
                                        try {
                                            doc = Jsoup.connect(url).get();
                                            String temp1 = doc.html().replace("<br>", "$$$").replace("<br />", "$$$"); //$$$ instead <br>
                                            doc = Jsoup.parse(temp1); //Parse again
                                        } catch (IOException e) {
                                            Log.v("samba", Log.getStackTraceString(e));
                                        }

                                        Elements trackelements = doc.getElementsByClass("album");
                                        //;
                                        //ArrayList<String> ids = new ArrayList<String>();
                                        for (Element element : trackelements) {
                                            String image1 = "http://www.spotifynewmusic.com/" + element.select("img").attr("src");//http://www.spotifynewmusic.com/covers/13903.jpg
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
                                                newAlbums.add(new NewAlbum(s, artist, album, image1));
                                            } catch (Exception e) {
                                                Log.v("samba", Log.getStackTraceString(e));
                                            }
                                        }
                                    }

                                    @Override
                                    public void addToFavorites(NewAlbum newAlbum) {
                                        newFavorite(Favorite.SPOTIFYALBUM + newAlbum.url.replace("spotify:album:", ""), newAlbum.artist + "-" + newAlbum.album, Favorite.NEWALBUM);
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

                    if ((title.equals("search album"))) {
                        searchAlbum();
                    }
                    if ((title.equals("search artist"))) {
                        searchArtist();
                    }

                    return true;
                }

                );

            });



            spotifyHeader=new SpotifyHeader(this,artistTitleTextView);
            spotifyHeader.connectVarsToFront();

            fab=(FloatingActionButton)

            findViewById(R.id.fab);
            fab.setOnClickListener(view ->{nextList();});

            fab.setOnLongClickListener(view -> {
                PopupMenu menu = new PopupMenu(fab.getContext(), fab);
                menu.getMenu().add(lists[SpotifyList]);
                menu.getMenu().add(lists[AlbumList]);
                menu.getMenu().add(lists[MpdList]);
                menu.getMenu().add("return to main");

                menu.show();
                menu.setOnMenuItemClickListener(item -> {
                            String title = item.getTitle().toString();
                    selectList(title);
                    if ((title.equals("return to main"))) {
                                getThis.finish();

                            }

                            return true;
                        }

                );
                return true;

                //albumsListview.setOnItemClickListener(cl);
                //listAlbumsForArtist(api, spotify, atistName, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
            });

            if(!nosearch)
            {
                initArtistlist(artistName);
                currentList=AlbumList+1;
            }

            else

            {
                currentList=SpotifyList+1;
                //Log.v("samba", "nosearch");


                try {
                    //refreshPlaylistFromSpotify(albumAdapter, albumsListview);
                    //mainLayout.setVisibility(View.GONE);//spotifyscrollviewtop
                    int visibility = View.GONE;
                    setVisibility(visibility);//
                    findViewById(R.id.song_display).setVisibility(View.VISIBLE);
                    //startPlaylistThread
                    customHandler.postDelayed(startPlaylistThread, 1000);
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                    //Log.v("samba", Log.getStackTraceString(e));
                }

            }

            customHandler.postDelayed(updateTimerThread,0);
            if (nextCommand.equals("search album")){
                    searchAlbum();

            }
            if ((nextCommand.equals("search artist"))) {
                searchArtist();
            }
            nextCommand="";
        } catch (Exception e){Log.getStackTraceString(e);}
        }

    public void nextList(){

        currentList++;
        if (currentList>3)currentList=1;
        selectList(lists[currentList-1]);
    }
    public void previousList() {

        currentList--;
        if (currentList < 1) currentList = 3;
        selectList(lists[currentList - 1]);
    }

    private void selectList(String title) {
        if ((title.equals(lists[SpotifyList]))) {
            currentList=SpotifyList+1;
            displayMpd=false;
            setAdapterForSpotify();
            String hartistName=artistName;
            refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
            setVisibility(View.GONE);
            artistName=hartistName;
            playButtonsAtBottom();
        } else
        if ((title.equals(lists[MpdList]))) {
            currentList=MpdList+1;
            displayMpd(albumsListview);
            playButtonsAtBottom();
        }else
        if ((title.equals(lists[AlbumList]))) {
            currentList=AlbumList+1;
            if (displayMpd){
                if (artistName.equals("The Beatles")) {
                    CopyOnWriteArrayList<Mp3File> playlistFiles = MainActivity.getThis.getLogic().getPlaylistFiles();
                    if (playlistFiles.size() > 0)
                        artistName = playlistFiles.get(0).getArtist();
                }
            }
            setAdapterForSpotify();
            displayAlbums();
            displayMpd=false;
            playButtonsAtBottom();
        }
    }

    public void playButtonsAtBottom() {
        View playbutton = findViewById(R.id.playspotify);
        View stopbutton = findViewById(R.id.stopspotify);
        View playpausebutton = findViewById(R.id.pausespotify);
        View previousbutton = findViewById(R.id.previousspotify);
        View nextbutton = findViewById(R.id.nextspotify);
        View volumebutton = findViewById(R.id.volumespotify);
        View seekbutton = findViewById(R.id.positionspotify);
        setListenersForButtons(this, playbutton, stopbutton, playpausebutton, previousbutton, nextbutton, volumebutton, seekbutton);
    }

    private void setAdapterForMpd() {
        albumAdapter = new PlanetAdapter(albumList, this,albumTracks) {
            @Override
            public void removeUp(int counter) {
                String message = "delete 0:" + (counter + 1);
                //Log.v("samba", message);
                enqueuecommand((message));
            }

            @Override
            public void onClickFunc(int counter) {
                MPC mpc = getLogic().getMpc();
                mpc.play(counter);
                mpc.play();
            }

            private Logic getLogic() {
                return MainActivity.getThis.getLogic();
            }

            @Override
            public void removeDown(int counter) {
                String message = "delete " + (counter) + ":" + (getLogic().getPlaylistFiles().size() + 1);
                enqueuecommand((message));

            }

            @Override
            public void removeAlbum(int counter) {
                Logic logic= getLogic();
                String album = logic.getPlaylistFiles().get(counter).getAlbum();
                String artist = logic.getPlaylistFiles().get(counter).getArtist();
                logic.removeAlbum(album, artist);
            }

            @Override
            public void addAlbumToFavoritesAlbum(int counter) {

            }

            @Override
            public void addAlbumToFavoritesTrack(int counter) {

            }

            @Override
            public void removeTrack(int counter) {
                String message = "delete " + (counter);
                enqueuecommand(message);
            }

            private void enqueuecommand(String message) {
                getLogic().getMpc().enqueCommands(new ArrayList<String>(Arrays.asList(message)));
            }

            @Override
            public void displayArtist(int counter) {
                try{
                    displayMpd=false;
                    String s = getLogic().getPlaylistFiles().get(counter).getArtist();
                    setVisibility(View.VISIBLE);
                    listAlbumsForArtist(s);
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }

            @Override
            public void displayArtistWikipedia(int counter) {
                String s = getLogic().getPlaylistFiles().get(counter).getArtist();
                MainActivity.startWikipediaPage(s);
            }

            @Override
            public void replaceAndPlayAlbum(int counter) {

            }

            @Override
            public void addAndPlayAlbum(int counter) {
                MPC mpc = getLogic().getMpc();
                mpc.play(counter);
                mpc.play();
            }

            @Override
            public void albumArtistWikipedia(int counter) {
            }

            @Override
            public void addAlbum(int counter) {

            }

            @Override
            public void addAlbumNoplay(int counter) {

            }
        };
        albumAdapter.setDisplayCurrentTrack(false);
        albumsListview.setAdapter(albumAdapter);
        }

    private void setAdapterForSpotify() {
        albumAdapter = new PlanetAdapter(albumList, this,albumTracks) {
            @Override
            public void removeUp(int counter) {
                removeUplist(albumAdapter, albumsListview,counter,getThis);
            }
            private void add(String path,boolean toplay,boolean clear){
                /*

                    ArrayList<String>commands=new ArrayList<>();
                    if (clear)
                        commands.add("clear");
                    path = Logic.removeSlashAtEnd(path);
                    String s = "add \""+path+"\"";
                    //Log.v("samba","command:"+s);

                    commands.add(s);
                    logic.getMpc().enqueCommands(commands);
                    logic.playWithDelay(id, toplay);

 */
            }

            @Override
            public void onClickFunc(int counter) {
                currentTrack=counter;
                if (albumVisible)
                    try{
                        String s = albumIds.get(counter);
                        boolean clear=false;
                        boolean ret=false;
                        boolean play=true;
                        ret = playMpdAlbum(s, clear, ret, play);
                        if (!ret)
                            getAlbumtracksFromSpotify(counter);



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }
                else {
                    stopMpd();
                    //playlistGotoPosition(counter);
                    playAtPosition(counter);
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
            public void displayArtistWikipedia(int counter) {
                    String s = tracksPlaylist.get(counter).artists.get(0).name;
                    MainActivity.startWikipediaPage(s);
            }

            @Override
            public void replaceAndPlayAlbum(int counter) {
                if (albumVisible)
                    try{
                        if (!playMpdAlbum(albumIds.get(counter), true, false, true)) {
                            clearSpotifyPlaylist();
                            //Log.v("samba","end removing");
                            getAlbumtracksFromSpotify(counter);
                        }



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

            }

            @Override
            public void addAndPlayAlbum(int counter) {
                if (albumVisible)
                    try{
                        if (!playMpdAlbum(albumIds.get(counter), false, false, true))
                            getAlbumtracksFromSpotify(counter);



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

                //getAlbumtracksFromSpotify(counter);
            }

            @Override
            public void albumArtistWikipedia(int counter) {
                    MainActivity.startWikipediaPage(artistName);
            }

            @Override
            public void addAlbum(int counter) {
                if (albumVisible)
                    try{
                        if (!playMpdAlbum(albumIds.get(counter), false, false, false))
                            addAlbumStatic(counter,albumAdapter, albumsListview);



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

            }

            @Override
            public void addAlbumNoplay(int counter) {
                String uri = albumIds.get(counter);
                Log.v("samba","add"+uri);
                String prefix="spotify:album:";
                AddSpotifyItemToPlaylist(prefix, uri);
                //refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
            }
        };
        albumAdapter.setDisplayCurrentTrack(false);
        albumsListview.setAdapter(albumAdapter);
    }

    private void displayMpd(ListView albumsListview) {
        displayMpd=true;
        setAdapterForMpd();
        albumAdapter.setAlbumVisible(false);
            try{
                albumList.clear();
                albumTracks.clear();
                JSONArray items = null;
                tracksPlaylist.clear();
                String prevartist="";
                String prevalbum="";
                for (Mp3File s:MainActivity.getThis.getLogic().getPlaylistFiles()){
                    final PlaylistItem pi=new PlaylistItem();
                    String artist=s.getArtist();
                    String naalbum=s.getAlbum();
                    String text="";
                    if (!artist.equals(prevartist)) {
                        text=String.format("(%s)",s.getArtist());
                    }
                    pi.pictureVisible=!naalbum.equals(prevalbum);
                    prevartist=artist;
                    prevalbum=naalbum;
                    pi.id=s.getFile();
                    String file=s.getFile();
                    file=file.substring(0,file.lastIndexOf("/"));
                    file="http://192.168.2.8:8081/FamilyMusic/"+file+"/folder.jpg";
                    //Log.v("samba",file);
                    pi.url=file;
                    pi.text=String.format("%s(%s)",s.getTitle()+text,s.getTimeNice());
                    albumList.add(pi.text);
                    albumTracks.add(pi);
                }

                Utils.setDynamicHeight(albumsListview, 0);
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
            //spotifyStartPosition=0;
            albumAdapter.setDisplayCurrentTrack(true);
            albumAdapter.notifyDataSetChanged();

        setVisibility(View.GONE);
    }

    public void changeScreen() {
        if (!nosearch) {
            refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
            setVisibility(View.GONE);
        } else {
            displayAlbums();
        }
        nosearch = !nosearch;
    }

    private void displayAlbums() {
        try {
            if (artistName.equals("The Beatles"))
                initArtistlist(tracksPlaylist.get(0).artists.get(0).name);
            else
                initArtistlist(artistName);
            setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public boolean playMpdAlbum(String s, boolean clear, boolean ret, boolean play) {
        if (s.startsWith(MPD)){
            ret=true;
            String path=s.replace(MPD,"");
            Log.v("samba","id:"+path);
            ArrayList<String> commands=new ArrayList<>();
            if (clear)
                commands.add("clear");

            path = Logic.removeSlashAtEnd(path);
            String s1 = "add \""+path+"\"";
            //Log.v("samba","command:"+s);

            commands.add(s1);
            Logic logic = MainActivity.getThis.getLogic();
            String id=getThis.getString(R.string.addandplay_filelist);
            logic.getMpc().enqueCommands(commands);
            if (play) {
                int toplay = logic.getToplay(id);
                logic.playWithDelay(id, toplay);
            }
       }
        return ret;
    }

    public void searchArtist() {
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(getThis);
            builder.setTitle("Search artist");

            // Set up the input
            ArtistAutoCompleteAdapter adapter = new ArtistAutoCompleteAdapter(this,
                    android.R.layout.simple_spinner_item/*android.R.layout.simple_dropdown_item_1line*/);
            final InstantAutoComplete input = new InstantAutoComplete(getThis);
            //input.setText(searchArtistString);
            input.setAdapter(adapter);
            input.setThreshold(0);
            input.setDropDownHeight(400);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String artistString = input.getText().toString();
                if (artistString.length()==0){
                    Toast.makeText(getApplicationContext(), "Please enter something for artist!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean add=true;
                for (String s:searchArtistString)
                    if (s.equals(artistString))add=false;
                if (add)
                    searchArtistString.add(artistString);
                fillListviewWithValues = new FillListviewWithValues() {

                    @Override
                    public void generateListSearch(final ArrayList<SearchItem> newAlbums) {
                        spotify.searchArtists(artistString.trim(), new Callback<ArtistsPager>() {

                            @Override
                            public void success(ArtistsPager artistsPager, Response response) {
                                String id = "";
                                int max = 10000;
                                Image image1 = null;
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
    @Override
    public boolean onTouchEvent(MotionEvent event){
        //Log.v("samba","righttoleft2");

        int action = MotionEventCompat.getActionMasked(event);
        /*if (flingListener.onTouch(this.getCurrentFocus(), event)) {
            // if gesture detected, ignore other touch events
            return false;
        }*/

        if (action == MotionEvent.ACTION_DOWN) {
            // normal touch events
            return true;
        }
        return true;
    }

    public void searchAlbum() {
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(getThis);
            builder.setTitle("Search album");

            // Set up the input
            final EditText input = new EditText(getThis);
            input.setText(searchAlbumString);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                searchAlbumString = input.getText().toString();
                if (searchAlbumString.length()==0){
                    Toast.makeText(getApplicationContext(), "Please enter something for album!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //SearchActivity.artistName=artist;
                fillListviewWithValues = new FillListviewWithValues() {

                    @Override
                    public void generateListSearch(final ArrayList<SearchItem> newAlbums) {
                        spotify.searchAlbums(searchAlbumString.trim(), new Callback<AlbumsPager>() {

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

    public static void addAlbumStatic(int counter, PlanetAdapter albumAdapter, ListView albumsListview) {
        artistName = tracksPlaylist.get(counter).artists.get(0).name;
        getAlbumtracksFromSpotify(tracksPlaylist.get(counter).album.id, tracksPlaylist.get(counter).album.name,getThis,albumAdapter, albumsListview);
    }

    public static void addAlbumToFavoritesTrackwise(int counter) {
        String url = Favorite.SPOTIFYALBUM + tracksPlaylist.get(counter).album.id;
        String name = tracksPlaylist.get(counter).artists.get(0).name;
        String album = tracksPlaylist.get(counter).album.name;
        //Log.v("samba","add "+url+name+"-"+album);
        String description = name + "-" + album;
        String newalbum = Favorite.NEWALBUM;
        newFavorite(url, description, newalbum);
    }

    public static void newFavorite(String url, String description, String newalbum) {
        FavoriteRecord fv=new FavoriteRecord(url,
                description, newalbum);
        long a = fv.save();
        Log.v("samba","added to favorites."+description);
        EditFavoriteActivity.editFavorite(MainActivity.getThis, new Favorite(fv.url,description,description,""),a);
    }

    public static void addAlbumToFavorites(String url, String description) {
        newFavorite(url, description, Favorite.NEWALBUM);
    }

    public static void removeAlbum(PlanetAdapter albumAdapter, int counter, ListView albumsListview, AppCompatActivity getThis) {
        String albumid = tracksPlaylist.get(counter).album.id;
        for (int i = tracksPlaylist.size() - 1; i >= 0; i--) {
            if (tracksPlaylist.get(i).album.id.equals(albumid)) removeTrackSpotify(i);
            //Log.v("samba","remove "+i);
            //removeTrackSpotify(counter);
        }
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, getThis);
    }

    public static int getVolumeSpotify(String ipAddress) {
        int vol=0;
        try {
            String sb = getJsonStringFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.mixer.get_volume\"}",
                    ipAddress);
            Log.v("samba","return:"+sb);
            vol = new JSONObject(sb).getInt("result");
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        Log.v("samba","old volume:"+vol);

        return vol;
    }


    public static void seekPositionSpotify(String ipAddress, int position) {
        try {
            getJsonStringFromUrl(String.format("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.seek\", \"params\": {\"time_position\":%s}}",position),
                    ipAddress);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }
    public static void setVolumeSpotify(String ipAddress, int vol) {

        try {
            getJsonStringFromUrl(String.format("{\"jsonrpc\": \"2.0\", \"method\": \"core.mixer.set_volume\", \"params\": {\"volume\":%s}}",vol),
                    ipAddress);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }
    public static void nextSpotifyPlaying(String ipAddress) {
        try {
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.next\"}",
                    ipAddress);//?StopPause
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public static void previousSpotifyPlaying(String ipAddress) {
        try {
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.previous\"}",
                    ipAddress);//?StopPause
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public static void stopSpotifyPlaying(String ipAddress) {
        try {
            //PlaybackController.stop()
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.stop\"}",
                    ipAddress);//?StopPause
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public static void playPauseSpotify() {playPauseSpotify(ipAddress);}
    public static void playPauseSpotify(String ipAddress) {
        try {
            //PlaybackController.stop()
            if (getState().equals("playing"))
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.pause\"}",
                    ipAddress);//?StopPause
            else
                GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.resume\"}",
                        ipAddress);//?StopPause
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
    }

    public void setVisibility(int visibility) {
        int opposite=View.GONE;
        if (visibility==opposite)opposite=View.VISIBLE;
        relatedArtistsListView.setVisibility(visibility);

        artistTitleTextView.setVisibility(visibility);
        spotifyHeader.icon.setVisibility(visibility);
        //fab.setVisibility(opposite);//spotifyscrollviewtop
        ((TextView) findViewById(R.id.relatedartists_text)).setVisibility(visibility);//albumsartist_listview
        ((TextView) findViewById(R.id.albumsartist_listview)).setVisibility(visibility);//albumsartist_listview

        spotifyHeader.MessageView.setVisibility(visibility);
        ((ScrollView) findViewById(R.id.spotifyscrollviewtop)).setVisibility(visibility);//albumsartist_listview
    }

    public static void playlistGotoPosition(int position) {
        playAtPosition(position);
        //GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.play\", \"params\": { \"tlid\":"  + position + " } }",
         //       ipAddress);

        //GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"playlistid\": 0, \"position\": " + (/*spotifyStartPosition + */position) + " } }, \"id\": 1}",
        //        ipAddress + "?PlayerOpen");
    }

    public void getAlbumtracksFromSpotify(final int position) {
        String s = albumIds.get(position);
        getAlbumtracksFromSpotify(s, albumList.get(position),getThis, albumAdapter, albumsListview,false);
    }
    public static void getAlbumtracksFromSpotify(final String albumid, final String albumname, final Activity getThis1, PlanetAdapter albumAdapter, ListView albumsListview) {
        getAlbumtracksFromSpotify(  albumid,   albumname,   getThis1,  albumAdapter,  albumsListview,true);
    }

    public static void getAlbumtracksFromSpotify(final String albumid, final String albumname, final Activity getThis1, PlanetAdapter albumAdapter, ListView albumsListview,boolean display) {
        if (albumAdapter==null)albumAdapter=SpotifyActivity.getThis.albumAdapter;
        if (albumsListview==null)albumsListview=SpotifyActivity.getThis.albumsListview;
        final PlanetAdapter albumAdapter1=albumAdapter;
        final ListView albumsListview1=albumsListview;
        Activity getThis=getThis1;
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
                        if (display)
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
        for (int i = tracksPlaylist.size()-1;i>= counter;i--) {
            //Log.v("samba", "remove " + i);
            removeTrackSpotify(i);
        }
        spotifyStartPosition = 0;
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, getThis);
    }

    public static void removeTrackSpotify(int counter) {
        ;
        //curl -d '{"jsonrpc": "2.0", "id": 1, "method": "core.tracklist.remove", "params": {"criteria":{"uri":["spotify:track:%s"]}}}' http://192.168.2.12:6680/mopidy/rpc
        //curl -d '{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.tracklist.remove\", \"params\": {\"criteria\":{\"uri\":\["spotify:track:%s\"]}}}' http://192.168.2.12:6680/mopidy/rpc
        String id="spotify:track:"+albumTracks.get(counter).id;
        //Log.v("samba","remove:"+id);
        GetJsonFromUrl(
                "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.tracklist.remove\", \"params\": {\"criteria\":{\"uri\":[\""+id+"\"]}}}",
                ipAddress);
    }

    public static void removeUplist(PlanetAdapter albumAdapter, ListView albumsListview, int counter, AppCompatActivity getThis) {
        for (int i = counter; i >=0; i--)
            removeTrackSpotify(i);
        spotifyStartPosition = 0;
        refreshPlaylistFromSpotify(albumAdapter, albumsListview, getThis);
    }

    /*@Override
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

    }*/

    @Override
    public void resultDbCall(ArrayList<String> dblist) {
        String album="";
        String file="";
        String prevFile="";
        //String s="";
        int total=0;
        //StringUtils.getLevenshteinDistance()
        for (String s1:dblist){
            if (s1.startsWith("Album: ")) {
                String album1= s1.replace("Album: ", "");
                if (!album.equals(album1)){
                    if ((album.length()>0) &&(total>1)){
                        Log.v("samba",album+file);
                        PlaylistItem pi=new PlaylistItem();
                        pi.pictureVisible=true;
                        pi.url="http://192.168.2.8:8081/FamilyMusic/"+file+"/folder.jpg";
                        pi.text=album;

                        albumList.add(album);
                        albumIds.add(MPD+file);
                        albumTracks.add(pi);
                    }
                    album=album1;
                    file=prevFile;
                    total=1;
                }
            }
            if (s1.startsWith("file: ")) {
                total++;
                prevFile= s1.replace("file: ", "");
                int p=prevFile.lastIndexOf("/");
                prevFile=prevFile.substring(0,p);
            }
        }
        if ((album.length()>0) &&(total>1)){
            Log.v("samba",album+file);
            PlaylistItem pi=new PlaylistItem();
            pi.pictureVisible=true;
            pi.url="http://192.168.2.8:8081/FamilyMusic/"+file+"/folder.jpg";
            pi.text=album;

            albumList.add(album);
            albumIds.add(MPD+file);
            albumTracks.add(pi);
        }
        MainActivity.getThis.runOnUiThread(() -> {
            albumAdapter.setDisplayCurrentTrack(false);

            albumAdapter.notifyDataSetChanged();
            Utils.setDynamicHeight(albumsListview, 0);

        });


    }

    public static class getEntirePlaylistFromSpotify {
        String playlistid;
        Activity getThis;

        getEntirePlaylistFromSpotify(String playlistid, Activity getThis) {
            this.playlistid = playlistid;
            this.getThis = getThis;
        }

        public void run() {

            try {
                ArrayList<String> ids = new ArrayList<String>();

                String prefix="spotify:user:";
                if (playlistid.startsWith("spotify"))prefix="";
                ids.add(prefix+playlistid);
                new AddTracksToPlaylist(ids, getThis) {
                    @Override
                    public void atEnd() {
                        atLast();

                    }

                }.run();

            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        }

        public void atLast() {

        }
    }

    public static void clearSpotifyPlaylist() {
        //curl -d '{"jsonrpc": "2.0", "id": 1, "method": "core.tracklist.clear"}TracklistController.clear()
        checkAddress();
        GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 0, \"method\": \"core.tracklist.clear\"}",
                ipAddress);//
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
                            }


                            new AddTracksToPlaylist(ids, getThis) {
                                @Override
                                public void atEnd() {

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
        try {
            if (MainActivity.getThis.getLogic().mpcStatus.playing) {
                MainActivity.getThis.getLogic().getMpc().stop();
            }
        } catch (Exception e){}
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

    public static void refreshPlaylistFromSpotify(final PlanetAdapter albumAdapter1, ListView albumsListview, Activity getThis) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(getThis);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Get playlist...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setProgress(0);
        progressDialog.show();
        albumVisible = false;
        albumAdapter1.setAlbumVisible(false);
        try {
            //JSONObject playlist = GetJsonFromUrl(
             //       "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.GetItems\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"duration\", \"thumbnail\",\"file\"], \"playlistid\": 0 }, \"id\": 1}\u200B",
             //       ipAddress + "?GetPLItemsAudio");
            //Log.v("samba", "refresh");
            refreshPlaylistFromSpotify(1,albumAdapter1,getThis,albumList,albumTracks);
            //spotifyStartPosition=0;
            albumAdapter1.setDisplayCurrentTrack(true);
            //getThis.runOnUiThread(() -> albumAdapter1.notifyDataSetChanged());
            //albumAdapter.notifyDataSetChanged();
            try{
            Utils.setDynamicHeight(albumsListview, 0);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

        //albumsListview.setOnItemClickListener(selectOnPlaylist);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        progressDialog.dismiss();

    }

    public static void refreshPlaylistFromSpotify(int nr, final PlanetAdapter albumAdapter1, Activity getThis,ArrayList<String> albumList,ArrayList<PlaylistItem> albumTracks) {
        try{
        JSONArray playlist = getPlaylist();

        albumList.clear();
        albumTracks.clear();
        tracksPlaylist.clear();
        JSONArray items = null;
        items = playlist;
        String prevAlbum = "";
            if ((items==null)&&(nr<3)){
                Handler handler = new Handler();
                handler.postDelayed(() -> refreshPlaylistFromSpotify(nr+1, albumAdapter1,getThis,albumList,albumTracks), 1000);
            }
            else
        for (int i = 0; i < items.length(); i++) {
            final PlaylistItem pi=new PlaylistItem();
            pi.pictureVisible=false;
            String trackid = "";
            JSONObject o = items.getJSONObject(i);
            trackid=o.getJSONObject("track").getString("uri").replace("spotify:track:","");
            if (trackid.length()>0) {
                Track t = getTrack(trackid);
                //Log.v("samba", t.name);
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
                pi.id=t.id;
                albumList.add(pi.text);
                albumTracks.add(pi);
                tracksPlaylist.add(t);


            }
        }
            getThis.runOnUiThread(() -> albumAdapter1.notifyDataSetChanged());
    } catch (Exception e) {
        Log.v("samba", Log.getStackTraceString(e));
    }
    }

    @Override
    protected void onDestroy() {
        if (currentList!=1){
            selectList((lists[SpotifyList]));
        }
        super.onDestroy();
    }


    public static String searchSpotifyArtist(String artist){
        SpotifyApi api=new SpotifyApi();
        SpotifyService spotify = api.getService();
        String artistid1 = "";
        ArtistsPager artistsPager = spotify.searchArtists(artist.trim());
        for (Artist artist1 : artistsPager.artists.items) {
            artistid1 = artist1.id;
            break;
        }
        return artistid1;

    }


    public void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final PlanetAdapter albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {
        initArtistLook(beatles);
        spotify.searchArtists(beatles.trim(), new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                Log.v("samba","get 1");

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
                Log.v("samba","get 2 for "+beatles);

                listAlbumsForArtistId(id, image, beatles, api);
                Log.v("samba","get 3");
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
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
                if (albumPager.items.size()==0){
                    Toast.makeText(getThis, "no albums for "+beatles,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
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
                        Log.v("samba",album.name);

                        albumList.add(album.name);
                        albumIds.add(album.id);
                        albumTracks.add(pi);
                        previous = album.name;

                    }
                //albumAdapter.setDisplayCurrentTrack(false);
                //albumAdapter.notifyDataSetChanged();
                Utils.setDynamicHeight(albumsListview, 0);
                //DatabaseListThread a=new DatabaseListThread(MainActivity.getThis.getLogic().getMpc(),"list \"file\" artist \"Rolling Stones\" group album",getThis);//"album"
                DatabaseListThread a=new DatabaseListThread(MainActivity.getThis.getLogic().getMpc(),String.format("find \"artist\" \"%s\"",beatles),getThis);//"album"
                a.start();

            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
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
           MainActivity.getThis.currentArtist= updateSongInfo(songItems.time,songItems.totaltime,songItems.tvName,songItems.artist,songItems.image,albumAdapter,albumsListview, getThis,getSpotifyInterface);

            customHandler.postDelayed(this, 1000);
        }

    };

    private Runnable startPlaylistThread = new Runnable() {

        public void run() {
            refreshPlaylistFromSpotify(albumAdapter, albumsListview,getThis);
        }

    };

    public static int getTime(){
        String s=getJsonStringFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.get_time_position\"}",ipAddress);
        //Log.v("samba",s);
        try {
            int t= new JSONObject(s).getInt("result");
            return t/1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getState(){
        String s = getState("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.get_state\"}", ipAddress);
        try {
            String s1=new JSONObject(s).getString("result");
            return s1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "stopped";
    }

    public static boolean isPlaying(){
        checkAddress();
        try {
            return getState().equals("playing");
        } catch (Exception e) {return false;}
    }

    @NonNull
    public static String getState(String data, String ipAddress) {
        return getJsonStringFromUrl(data, ipAddress);
    }

    public static String[] getCurrentTrack(){
        JSONObject o=GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.get_current_tl_track\"}",ipAddress);
        try {
            final String[] a= {o.getJSONObject("track").getString("uri").replace("spotify:track:",""), ""+o.getJSONObject("track").getInt("length")};
            return a;
        } catch (Exception e) {
            return null;
        }
        //return null;

    }

    public static String updateSongInfo(TextView time, TextView totaltime, TextView tvName, TextView artist,
                                        ImageView image, PlanetAdapter albumAdapter, ListView albumsListview, AppCompatActivity getThis, final SpotifyInterface getSpotifyInterface) {
        String artistReturn="";
        try {
                if (isPlaying()) {//(speed.doubleValue() > 0) {
                    if (albumAdapter!=null)
                        if (albumAdapter!=null)
                            try {
                                if (playingEngine == 2) {
                                    SpotifyActivity.getThis.playButtonsAtBottom();
                                }
                            } catch (Exception e){}
                        playingEngine=1;
                     String[] trid1 = getCurrentTrack();//
                    String trid = "0";
                    try {
                        trid = trid1[0];
                    } catch (Exception e){}
                    totalTime = Integer.parseInt(trid1[1])/1000;
                    if (trid.length() > 0) {
                        //currentTrack=0;
                        if (albumAdapter!=null)
                            for (int i = 0; i < tracksPlaylist.size(); i++) {
                                if (tracksPlaylist.get(i).id.equals(trid)) {
                                    if (currentTrack != i)
                                        albumsListview.setItemChecked(currentTrack, false);
                                    currentTrack = i;
                                    //Log.v("samba", "current track:" + i + "," + tracksPlaylist.get(i).name);
                                    break;
                                }
                            }

                        if (albumAdapter!=null) {
                            albumAdapter.setCurrentItem(currentTrack);
                            albumAdapter.notifyDataSetChanged();
                        }
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
                                            getThis.runOnUiThread(() -> {
                                                image.setImageBitmap(logo);
                                                SpotifyActivity.bitmap = logo;
                                            });
                                        }
                                    }.execute(imageurl);
                                } catch (Exception e) {
                                    Log.v("samba", Log.getStackTraceString(e));
                                }
                            }

                        //hours * 60 * 60 + mins * 60 + secs;
                        currentTime = getTime();
                        artistReturn = t.artists.get(0).name;
                        MainActivity.playingStatus=MainActivity.SPOTIFY_PLAYING;
                        getThis.runOnUiThread(() -> {
                            time.setText(niceTime(currentTime));
                            int ttimeint = totalTime;// thours * 60 * 60 + tmins * 60 + tsecs;
                            totaltime.setText(niceTime(ttimeint));
                            tvName.setText(t.name);
                            artist.setText(t.artists.get(0).name);
                            MainActivity.playingStatus=MainActivity.SPOTIFY_PLAYING;

                        });
                    }
                } else{//spotify not playing
                    try {
                        Logic logic = MainActivity.getThis.getLogic();
                        MPCStatus status = logic.mpcStatus;
                        if (!status.playing) return artistReturn;
                        if (playingEngine==1){SpotifyActivity.getThis.playButtonsAtBottom();}
                        playingEngine=2;
                        int songnr = status.song.intValue();
                        Mp3File currentSong = logic.getPlaylistFiles().get(songnr);
                        albumAdapter.setCurrentItem(songnr);


                        String title = currentSong.getTitle();
                        artistReturn = currentSong.getArtist();
                        tvName.setText(title);

                        String time1 = Mp3File.niceTime(status.time.intValue());

                        time.setText(time1);
                        try {
                            final String timeNice = currentSong.getTimeNice();
                            totaltime.setText(timeNice);
                        } catch (Exception e) {
                            totaltime.setText("00:00");
                        }
                        String album = "";
                        try {
                            album = currentSong.niceAlbum();
                            artist.setText(album);
                        } catch (Exception e) {
                            artist.setText("");
                        }
                        String uri = Logic.getUrlFromSongpath(currentSong);

                        MainActivity.playingStatus=MainActivity.MPD_PLAYING;
                        if (MainActivity.getThis.albumPictures.containsKey(album)) {
                            final Bitmap b = MainActivity.getThis.albumPictures.get(album);
                            MainActivity.getThis.albumBitmap = b;
                            currentSong.setBitmap(b);
                            MainActivity.getThis.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageBitmap(b);
                                }
                            });
                        } else {
                            MainActivity.getThis.albumPictures.put(album, null);


                            new ImageLoadTask(uri, album, MainActivity.getThis, image).execute();


                        }
                        if (SpotifyActivity.getThis.displayMpd)
                            if (albumTracks.size()!=logic.getPlaylistFiles().size())
                                SpotifyActivity.getThis.displayMpd(albumsListview);
                        albumAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        //mpc.connectionFailed("Connection failed, check settings");
                        //t.stop();
                    }

                }

            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        return artistReturn;

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
    public static void seekPlay(Activity getThis) {
        if (!isPlaying()){
            Toast.makeText(getThis, "spotify not playing!",
                    Toast.LENGTH_SHORT).show();
            seekPlayMpd(getThis);
            return;
        }
        new Seek(getThis, currentTime, totalTime) {
            @Override
            void seekPos(int progress) {
                seekPositionSpotify(ipAddress,progress*1000);
            }
        }.run();


        /*final android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getThis);

        alert.setTitle("Seek");

        LinearLayout linear = new LinearLayout(getThis);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(getThis);
        text.setPadding(10, 10, 10, 10);

        Integer position = currentTime;
        text.setText(niceTime(position));
        SeekBar seek = new SeekBar(getThis);

        seek.setMax(totalTime);
        seek.setProgress(position);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText(niceTime(progress));
                seekPositionSpotify(ipAddress,progress*1000);
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


        alert.setPositiveButton("Ok", (dialog, id) -> {
            dialog.dismiss();
        });

        alert.show();*/
    }
    public static void seekPlayMpd(Activity getThis) {
        Logic logic = MainActivity.getThis.getLogic();
        MPCStatus status = logic.mpcStatus;
        if (!status.playing) return;
        int songnr = status.song.intValue();
        int time=status.time.intValue();
        Mp3File currentSong = logic.getPlaylistFiles().get(songnr);
        new Seek(getThis, time, currentSong.getTime()) {
            @Override
            void seekPos(int progress) {
                String message = "seekcur " + (progress);
                MainActivity.getThis.getLogic().getMpc().enqueCommands(new ArrayList<String>(Arrays.asList(message)));
            }
        }.run();
    }

    public static void setVolume(Activity getThis) {
        final android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getThis);

        alert.setTitle("Volume");

        LinearLayout linear = new LinearLayout(getThis);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(getThis);
        text.setPadding(10, 10, 10, 10);

        //setVolumeSpotify(String ipAddress, int vol) {
            //int vol=getVolumeSpotify(ipAddress)-10;
        Integer volume = getVolumeSpotify(ipAddress);
        text.setText("" + volume);
        SeekBar seek = new SeekBar(getThis);

        seek.setProgress(volume);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText("" + progress);
                setVolumeSpotify(ipAddress,progress);
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


        alert.setPositiveButton("Ok", (dialog, id) -> {
            dialog.dismiss();
        });

        alert.show();
    }


    public static void showPlayMenu(final Activity getThis1,View view) {

        LayoutInflater inflater = getThis1.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.play_spotify, null);
        View playbutton = alertLayout.findViewById(R.id.playspotify);
        View stopbutton = alertLayout.findViewById(R.id.stopspotify);
        View playpausebutton = alertLayout.findViewById(R.id.pausespotify);
        View previousbutton = alertLayout.findViewById(R.id.previousspotify);
        View nextbutton = alertLayout.findViewById(R.id.nextspotify);
        View volumebutton = alertLayout.findViewById(R.id.volumespotify);
        View seekbutton = alertLayout.findViewById(R.id.positionspotify);
        String title = setListenersForButtons(getThis1, playbutton, stopbutton, playpausebutton, previousbutton, nextbutton, volumebutton, seekbutton);
        AlertDialog.Builder alert = new AlertDialog.Builder(getThis1);
        alert.setTitle(title);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        alert.setPositiveButton("OK", (dialog, which) -> {});
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @NonNull
    public static String setListenersForButtons(Activity getThis1, View playbutton, View stopbutton, View playpausebutton, View previousbutton, View nextbutton, View volumebutton, View seekbutton) {
        String title = "Spotify Play";
        MpcStatus mpcStatus = new MpcStatus().invoke();
        Logic logic1=null;
        //boolean mpdPlaying=false;
        try {
            //mpdPlaying = mpcStatus.isMpdPlaying();
            logic1 = mpcStatus.getLogic();
        } catch (Exception e){

        }
        Logic logic=logic1;
        //isPlaying()
        if (isPlaying()) {
            playbutton.setOnClickListener(v -> playSpotify());
            stopbutton.setOnClickListener(v -> stopSpotifyPlaying(ipAddress));
            playpausebutton.setOnClickListener(v -> playPauseSpotify(ipAddress));
            previousbutton.setOnClickListener(v -> previousSpotifyPlaying(ipAddress));
            nextbutton.setOnClickListener(v -> nextSpotifyPlaying(ipAddress));
            volumebutton.setOnClickListener(v -> setVolume(getThis1));
            seekbutton.setOnClickListener(v -> seekPlay(getThis1));
        } else
        if (logic !=null){
            title = "Mpd Play";
            playbutton.setOnClickListener(v -> {logic.getMpc().play();
                logic.setPaused(false);});
            stopbutton.setOnClickListener(v -> {logic.getMpc().pause();
            logic.setPaused(true);});
            playpausebutton.setOnClickListener(v -> MainActivity.getThis.playPause());
            previousbutton.setOnClickListener(v -> logic.getMpc().previous());
            nextbutton.setOnClickListener(v -> logic.getMpc().next());
            volumebutton.setOnClickListener(v -> MainActivity.getThis.setVolume(getThis1));
            seekbutton.setOnClickListener(v -> seekPlayMpd(getThis1));
            //seekPlayMpd(
            //alertLayout.findViewById(R.id.positionspotify).setOnClickListener(v -> seekPlay(getThis1));
        }
        return title;
    }

    private static class MpcStatus {
        private Logic logic;
        private boolean mpdPlaying;

        public Logic getLogic() {
            return logic;
        }

        public boolean isMpdPlaying() {
            return mpdPlaying;
        }

        public MpcStatus invoke() {
            try {
                logic = MainActivity.getThis.getLogic();
                MPCStatus status = logic.mpcStatus;
                mpdPlaying = status.playing;
                return this;
            } catch (Exception e){
                return null;
            }
        }
    }
}

class PlaylistItem {
    public boolean pictureVisible;
    public String url;
    public String text;
    public String id;
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
        AsyncTask.execute(() -> {
            getThis.runOnUiThread(() -> {
                artistTitleTextView.setText(artistName);
            });


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


            try {
                new DownLoadImageTask() {
                    @Override
                    public void setImage(Bitmap logo) {
                        ImageView i = (ImageView) getThis.findViewById(R.id.image);
                        //Log.v("samba", "image loaded");
                        getThis.runOnUiThread(() -> {
                            icon.setLayoutParams(layoutParams);
                            icon.setImageBitmap(logo);
                        });
                    }
                }.execute(image.url);
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }

            SS.setSpan(new MyLeadingMarginSpan2(scale / 50, leftMargin), 0, SS.length(), 0);
            getThis.runOnUiThread(() -> {
                MessageView.setText(SS);
            });

        });


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
abstract class Seek{
    abstract void seekPos(int progress);
    Activity getThis;
    int position;
    int max;
    public Seek(Activity getThis, int position, int max){
        this.getThis=getThis;
        this.position=position;
        this.max=max;
    }
    public void run() {
        Log.v("samba","trying to create alert");
        /*Logic logic = MainActivity.getThis.getLogic();
        MPCStatus status = logic.mpcStatus;
        if (!status.playing) return;
        int songnr = status.song.intValue();
        int time=status.time.intValue();
        Mp3File currentSong = logic.getPlaylistFiles().get(songnr);*/
        AlertDialog.Builder alert = new AlertDialog.Builder(getThis/*.getSupportActionBar().getThemedContext()*/);

        alert.setTitle("Seek");

        LinearLayout linear = new LinearLayout(getThis);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(getThis);
        text.setPadding(10, 10, 10, 10);

        //Integer position = time;
        text.setText(String.format("%s(%s)",niceTime(position),niceTime(max)));
        SeekBar seek = new SeekBar(getThis);

        seek.setMax(max);
        seek.setProgress(position);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText(String.format("%s(%s)",niceTime(progress),niceTime(max)));
                seekPos(progress);
                //MainActivity.getThis.getLogic().getMpc().enqueCommands(new ArrayList<String>(Arrays.asList(message)));

                //seekPositionSpotify(ipAddress,progress*1000);
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


        alert.setPositiveButton("Ok", (dialog, id) -> {
            dialog.dismiss();
        });

        alert.show();
    }

}

class SmartLinkSwipeDetector implements View.OnTouchListener {

    private final int MIN_DISTANCE = 100;
    private float downX, upX;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        /*Observable
                .just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .filter(integer -> integer % 2 == 0)
                .subscribe(System.out::println);*/

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                return true;

            case MotionEvent.ACTION_UP:
                if (PlanetAdapter.longclicked||RelatedArtistAdapter.longclicked){
                    PlanetAdapter.longclicked=false;
                    RelatedArtistAdapter.longclicked=false;
                    return true;
                }
                upX = event.getX();
                float deltaX = downX - upX;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (deltaX < 0)
                    {
                        SpotifyActivity.getThis.nextList();

                    }
                    else if (deltaX > 0)
                    {
                        SpotifyActivity.getThis.previousList();

                    }
                } else {
/*                    {
                        final int position = SpotifyActivity.getThis.albumsListview.pointToPosition(
                                Math.round(event.getX()), Math.round(event.getY()));

                        String countryName = (String) SpotifyActivity.getThis.albumsListview.getItemAtPosition(position);
                        SpotifyActivity.getThis.albumAdapter.onClickFunc(position);

                    }*/
                }

                return true;

            default:
                return true;
        }
    }
}
