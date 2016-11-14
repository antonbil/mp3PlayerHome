package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.adapters.ArtistAutoCompleteAdapter;
import examples.quickprogrammingtips.com.tablayout.adapters.InstantAutoComplete;
import examples.quickprogrammingtips.com.tablayout.adapters.RelatedArtistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
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
import mpc.DatabaseListThread;
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
public class SpotifyFragment extends Fragment implements
         MPCDatabaseListListener {
    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "89f945f1696e4f389aaed419e51beaad";
    // TODO: Replace with your redirect URI
    //private static final String REDIRECT_URI = "testschema://callback";
    public static final String MPD = "mpd://";
    public static int playingEngine;
    private static boolean busyupdateSongInfo=false;
    public static boolean explicitlyCalled=false;
    private static boolean spotifyPaused=false;
    public static boolean hasBeen=false;
    public static boolean isNewArtist=true;
    protected SpotifyHeader spotifyHeader;
    private static SpotifyFragment instance;
    private SpotifyInterface getSpotifyInterface;
    private static int spotifyStartPosition = 0;
    public static String ipAddress = "";
    public static String nextCommand="";
    public PlanetAdapter albumAdapter;
    protected ListView albumsListview;
    private static ProgressDialog dialog1;//
    protected boolean nosearch = false;
    protected static TextView artistTitleTextView;
    public static int currentTrack;
    public static String artistName="";
    protected RelatedArtistAdapter relatedArtistsAdapter;
    protected ListView relatedArtistsListView;
    private SpotifyApi api;
    private SpotifyService spotify;
    protected AdapterView.OnItemClickListener cl;
    public static boolean albumVisible = true;
    static Bitmap bitmap;
    private final float CHECK_MEMORY_FREQ_SECONDS = 3.0f;
    private final float LOW_MEMORY_THRESHOLD_PERCENT = 5.0f; // Available %
    private Handler memoryHandler_;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public static final ArrayList<String> CATEGORY_IDS = new ArrayList<>(Arrays.asList("electronic", "progressive", "alternative", "rnb", "soul", "singer-songwriter",
            "classical","acoustic", "ambient", "americana", "blues", "country", "techno", "shoegaze", "Hip-Hop", "funk", "jazz", "rock", "folk","instrumental","pop","punk","metal"
            ,"Progressive+rock","indie+rock","indie+pop"));
    private static String searchAlbumString ="";
    private static int totalTime;
    private static int currentTime;
    protected static Activity activityThis;
    View llview;
    public static PopupMenu categoriesMenu;
    protected boolean artist_desc_hidden=true;
    protected boolean relatedartists_hidden=false;
    private static MainActivity.SpotifyData data;
    protected boolean spotifyWorkingOnPlaylist=false;
    private static int scrollY=0;
    private int prevScrollY=0;

    public static MainActivity.SpotifyData getData() {
        return data;
    }

    public static void setData(MainActivity.SpotifyData data) {
        SpotifyFragment.data = data;
    }

    public static SpotifyFragment getInstance() {
        return instance;
    }

    public static void startAtTop() {
        scrollY=0;
        isNewArtist =true;
    }

    public static void setInstance(SpotifyFragment getThis) {
        SpotifyFragment.instance = getThis;
    }

    public static void setSpotifyInterface(SpotifyInterface getSpotifyInterface) {
        getInstance().getSpotifyInterface = getSpotifyInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try{
            setInstance(this);

            spotifyWorkingOnPlaylist=false;
            activityThis = getActivity();
            SpotifyFragment.hasBeen=true;
            getLayout(inflater, container);
            checkAddress();
            memoryHandler_ = new Handler();
            checkAppMemory();

            String ip = MainActivity.getInstance().getLogic().getMpc().getAddress();
            ipAddress = String.format("http://%s:8080/jsonrpc", ip);
            setSpotifyInterface(new SpotifyInterface());
            api = new SpotifyApi();
            spotify = api.getService();
            dialog1 = new ProgressDialog(activityThis);

            onActivityCreated();
            lastOncreateView(llview);
            return llview;
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
                return null;
        }
    }

    public void getLayout(LayoutInflater inflater, ViewGroup container) {
        llview = inflater.inflate(R.layout.activity_spotify, container, false);
    }

    public void lastOncreateView(View llview) {
        if (spotifyWorkingOnPlaylist) return;
        if(!nosearch)
        {
            try{
                new Thread(() -> {
                    listAlbumsForArtist(api, spotify, artistName, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);

                }).start();
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        }else {
            clearAlbums();

        }
        if ((nextCommand.equals("search artist"))) {
            searchArtist();
        }
        nextCommand="";

    }

    public void clearAlbums() {
        getData().albumIds.clear();
        getData().albumList.clear();
        getData().albums.clear();
    }

    @Override
    public void onPause() {
        Log.e("DEBUG", "OnPause of loginFragment");
        super.onPause();

    }

    public void checkAppMemory(){
        // Get app memory info
        long available = Runtime.getRuntime().maxMemory();
        long used = Runtime.getRuntime().totalMemory();

        // Check for & and handle low memory state
        float percentAvailable = 100f * (1f - ((float) used / available ));
        if( percentAvailable <= LOW_MEMORY_THRESHOLD_PERCENT ) {
            Log.v("samba","check for low memory");
            handleLowMemory();
        }

        // Repeat after a delay
        memoryHandler_.postDelayed(this::checkAppMemory, (int)(CHECK_MEMORY_FREQ_SECONDS * 1000) );
    }

    public void handleLowMemory(){
    }

    public static String checkAddress() {
        String ip = MainActivity.getInstance().getLogic().getMpc().getAddress();
        checkAddressIp(ip);
        return ipAddress;
    }

    public static void checkAddressIp(String ip) {
        ipAddress =
                String.format("http://%s:6680/mopidy/rpc", ip);
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

    private static void AddSpotifyTrack(ArrayList<String> ids, final int pos) {
        try {
            if (pos < ids.size()) {
                dialog1.incrementProgressBy(1);
                //add track to playlist
                String prefix="spotify:track:";
                String uri=ids.get(pos);
                if (uri.startsWith("spotify"))prefix="";
                AddSpotifyItemToPlaylist(prefix, uri);

                AddSpotifyTrack(ids, pos + 1);
            } else {
                //all tracks added
                stopMpd();
                //get playlist from server
                JSONArray playlist = getPlaylist();
                if (playlist!=null&&ids!=null) {

                    spotifyStartPosition = playlist.length() - ids.size();
                    playAtPosition(spotifyStartPosition);
                }

                MainActivity.getInstance().runOnUiThread(() -> {
                    if (dialog1.isShowing())
                        dialog1.dismiss();

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

    public static JSONArray getPlaylist() {
        return GetJsonArrayFromUrl(
                "{\"jsonrpc\": \"2.0\", \"method\": \"core.tracklist.get_tl_tracks\", \"id\": 1}",
                ipAddress);
    }

    private static JSONArray GetJsonArrayFromUrl(String data, String urlString) {
        JSONObject jsonRootObject;

        String sb = getJsonStringFromUrl(data, urlString);
        try {
            jsonRootObject = new JSONObject(sb);
            return jsonRootObject.getJSONArray("result");
        } catch (JSONException e) {
           /**/
        }
        return null;
    }

    private static JSONObject GetJsonFromUrl(String data, String urlString) {
        if (busyupdateSongInfo)return null;
        busyupdateSongInfo=true;

        JSONObject jsonRootObject = null;
        try {

            String sb = getJsonStringFromUrl(data, urlString);
            jsonRootObject = new JSONObject(sb);
            busyupdateSongInfo=false;
            return jsonRootObject.getJSONObject("result");
        } catch (Exception e) {
            busyupdateSongInfo=false;
        }
        busyupdateSongInfo=false;
        return jsonRootObject;
    }
    @NonNull
    private static String getJsonStringFromUrl(String data, String urlString) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection uc = url.openConnection();

            uc.setDoOutput(true);// Triggers POST.

            uc.setRequestProperty("User-Agent", "@IT java-tips URLConnection");
            uc.setRequestProperty("Content-Type", "application/json");
            uc.setRequestProperty("Accept-Language", "ja");
            OutputStream os = uc.getOutputStream();

            PrintStream ps = new PrintStream(os);
            ps.print(data);
            ps.close();

            InputStream is = uc.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));


            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (Exception e) {
                /**/
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                   /**/
                }
            }


        } catch (Exception e) {
            /**/
        }
        return sb.toString();
    }

    public static String LastFMArtist(String artist) {
        String api_key = "07e905eaba54f0d626c2fadcb0fe13f6";//see above; last.fm-key
        String urlString = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&artist=%s&api_key=%s&format=json", artist, api_key);

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
            Log.v("samba","LastFMArtist(String artist) error");
        }
        return "";
    }
    // Spotify:Request code that will be used to verify if the result comes from correct activity
// Can be any integer
    //private static final int REQUEST_CODE = 1337;


        public void onActivityCreated() {
            albumsListview = (ListView) llview.findViewById(R.id.albums_listview2);

            try {

                if (artistName == null || artistName.equals("")) {
                    artistName = MainActivity.getInstance().currentArtist;
                }
                if (nosearch) artistName = "The Beatles";

                albumsListview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                setAdapterForSpotify();
                albumsListview.setAdapter(albumAdapter);


                albumsListview.setOnItemClickListener(cl);
                relatedArtistsListView = (ListView) llview.findViewById(R.id.relatedartists_listview);

                relatedArtistsAdapter = new RelatedArtistAdapter<>(activityThis, android.R.layout.simple_list_item_1, getData().artistList);
                relatedArtistsListView.setAdapter(relatedArtistsAdapter);

                artistTitleTextView = (TextView)//

                        llview.findViewById(R.id.artist_title);

                spotifyHeader = new SpotifyHeader(activityThis);

                View artist_description_view = llview.findViewById(R.id.spotifyscrollviewtop);
                artist_description_view.setVisibility(View.GONE);
                llview.findViewById(R.id.artist_title).setOnClickListener(view -> {
                    View albums_scroll_view = llview.findViewById(R.id.spotifyscrollviewmiddle);
                    if (artist_desc_hidden) {
                        artist_description_view.setVisibility(View.VISIBLE);
                        albums_scroll_view.setVisibility(View.GONE);
                    } else{
                        artist_description_view.setVisibility(View.GONE);
                        albums_scroll_view.setVisibility(View.VISIBLE);
                    }
                    artist_desc_hidden=!artist_desc_hidden;
                });
                llview.findViewById(R.id.relatedartists_text).setOnClickListener(view ->
                {
                    if (relatedartists_hidden)
                    llview.findViewById(R.id.relatedartists_listview).setVisibility(View.VISIBLE);
                else
                        llview.findViewById(R.id.relatedartists_listview).setVisibility(View.GONE);
                    relatedartists_hidden=!relatedartists_hidden;});

            } catch (Exception e) {
                Log.getStackTraceString(e);
            }
        }

    public void setScrollviewListenerToGetScrollOffset() {
        ScrollView sc=(ScrollView) llview.findViewById(R.id.spotifyscrollviewmiddle);
        sc.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY1 = sc.getScrollY();
            //workaround: scrolly first gives 0 and after that the actual value;
            if (scrollY1>0||prevScrollY==0) {
                SpotifyFragment.scrollY = scrollY1;
            }
            prevScrollY =scrollY1;
         });
    }

    public static void albumTop100Nl(){
        try {
            MainActivity.getInstance().fillListviewWithValues = new FillListviewWithValues() {

                @Override
                public void generateList(ArrayList<NewAlbum> newAlbums) {

                    Document doc = null;
                    try {
                        String fullString = getContentsOfAddress("http://dutchcharts.nl/weekchart.asp?cat=a");
                        fullString=fullString.replace("<br>", "$$$").replace("<br />", "$$$").replace("<b>", "b-b-b-").replace("</b>", "b+b+b+"); //$$$ instead <br>
                        doc = Jsoup.parse(fullString); //Parse again
                    } catch (IOException e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

                    Elements trackelements = doc.getElementsByClass("charts");
                    int i=0;
                    for (Element element : trackelements) {
                        i++;
                        try {
                            String current=""+i;
                            String previous=element.children().get(1).text().replace("b-b-b-","").replace("b+b+b+","");
                            if (previous.length()==0)previous="*";
                            String nofweeks=element.children().get(3).text();
                            String image1 =//.getElementsByTag("img").get(0)
                            element.children().get(4).getElementsByTag("img").get(0).attr("src");//http://www.spotifynewmusic.com/covers/13903.jpg
                            Elements aelements = element.getElementsByTag("a");
                            String s="";
                            for (Element e : aelements) {
                                s=e.attr("onclick");
                                if (s!=null){
                                    s=s.replace("playSpotify('https://embed.spotify.com/?uri=","").replace("');","").replace("%3A",":");
                                }
                            }
                            if (s.length()==0)current="no Spotify-Album!\n"+current;
                            Elements navbelements = element.getElementsByClass("navb");

                            String div = navbelements.text();
                            String[] list = div.replace("$$$", ";").split(";");
                            String artist = list[0].replace("b-b-b-","").replace("b+b+b+","");
                            String album = "";
                            if (list.length > 1)
                                album = list[1].replace("\"","");
                            newAlbums.add(new NewAlbum(s, artist, String.format("%s-%s(%s)-%s",current,previous,nofweeks,album), image1));
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                            DebugLog.log("albumTop100Nl() error");
                        }

                    }
                }

                @Override
                public void addToFavorites(NewAlbum newAlbum) {
                    newFavorite(Favorite.SPOTIFYALBUM + newAlbum.url.replace("spotify:album:", ""), newAlbum.artist + "-" + newAlbum.album, Favorite.NEWALBUM, newAlbum.getImage());
                    generateLists();
                }

            };


            {
                Intent intent = new Intent(MainActivity.getInstance(), NewAlbumsActivityElectronic.class);
                MainActivity.getInstance().startActivity(intent);
            }
            // }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

}

    @NonNull
    public static String getContentsOfAddress(String address) throws IOException {
        String fullString = "";
        URL url = new URL(address);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            fullString += line;
        }
        reader.close();
        return fullString;
    }

    public static void spotifyAlbumShortcuts(){
        try {
            String url = "http://192.168.2.8/spotify/data";

            showSpotifyAlbumlistDirectory(url, new ArrayList<> ());

            // }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

    }

    public static void showSpotifyAlbumlistDirectory(String url,ArrayList<String> previousDirectoryListing) {
        try{
            MainActivity.getInstance().spotifyShortcutsDoc = null;
        try {
            MainActivity.getInstance().spotifyShortcutsDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
        Elements links = MainActivity.getInstance().spotifyShortcutsDoc.select("body a");
        ArrayList<String> directoryListing=new ArrayList<>();
        for (Element link : links)
        {
            if(link.text().lastIndexOf("/")>0) {
                String s=link.text().replace("//","");
                directoryListing.add(s);
            }
        }
        if (directoryListing.size()>0) {

            MainActivity.getInstance().spotifyShortcutsDoc =null;
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.getInstance());
            builderSingle.setIcon(R.drawable.common_ic_googleplayservices);
            builderSingle.setTitle("Select Directory");

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    MainActivity.getInstance(),
                    android.R.layout.select_dialog_singlechoice);
            for (String cat : directoryListing) {
                arrayAdapter.add(cat);
            }

            builderSingle.setNegativeButton(
                    "cancel",
                    (dialog, which) -> dialog.dismiss());

            builderSingle.setAdapter(
                    arrayAdapter,
                    (dialog, which) -> {
                        final String dir = arrayAdapter.getItem(which);
                        showSpotifyAlbumlistDirectory(url + "/" + dir,directoryListing);
                    });
            builderSingle.show();
        } else{
            try{
                MainActivity.getInstance().trackelements = MainActivity.getInstance().spotifyShortcutsDoc.getElementsByClass("spotifyalbum");
                MainActivity.getInstance().spotifyShortcutsDoc =null;
                MainActivity.getInstance().fillListviewWithValues = new FillListviewWithValues() {

                @Override
                protected void addMenuItems(ArrayList<String> menuItems){
                    menuItems.add("sep");
                    ArrayList<String> menuItemsadd=new ArrayList<>();
                    for (String item:previousDirectoryListing)
                        menuItemsadd.add("http://"+item);
                    menuItems.addAll(menuItemsadd);
                }
                @Override
                public void executeUrl(String s){
                    String hurl=url;
                    while (hurl.endsWith("/"))hurl=hurl.substring(0,hurl.length()-1);
                    int index=hurl.lastIndexOf('/');
                    hurl = hurl.substring(0, index);
                    if (!hurl.endsWith("/"))hurl=hurl+"/";
                    s= hurl +s.replace("http://","");
                    showSpotifyAlbumlistDirectory(s,previousDirectoryListing);
                }

                @Override
                public void generateList(ArrayList<NewAlbum> newAlbums) {
                    try{
                    int i=0;
                        for (Element element : MainActivity.getInstance().trackelements) {
                            //if (i>20) break;;
                            String artist="";
                            String album="";
                            try {
                                String url = element.getElementsByClass("url").get(0).text();
                                artist = element.getElementsByClass("artist").get(0).text();
                                album = element.getElementsByClass("album").get(0).text();
                                String imageurl = element.getElementsByClass("img").get(0).text();
                                if (url.length() > 0&&artist.length() > 0&&album.length() > 0)
                                    newAlbums.add(new NewAlbum(url, artist, album, imageurl));

                            }catch (Exception e){
                                Log.v("samba","Error in "+i+artist+album);
                                }
                            i++;
                        }
                }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
                }

                @Override
                protected ArrayList<String> getChoices() {
                    ArrayList<String>choices= new ArrayList<>();
                    choices.add("delete");
                    return choices;
                }

                @Override
                public boolean processChoice(String choice, NewAlbumsActivity.ListAdapter listAdapter, ArrayList<NewAlbum> items, int position) {
                    if (choice.equals("delete")) {
                        String outputurl=String.format(url+"/index.php?key=%s&deleteitem=%s",items.get(position).url,true).replace(" ","%20");
                        try {

                            URL obj = new URL(outputurl);
                            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                            // optional default is GET
                            con.setRequestMethod("GET");

                            String USER_AGENT = "Mozilla/5.0";
                            //add request header
                            con.setRequestProperty("User-Agent", USER_AGENT);

                            //int responseCode = con.getResponseCode();
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));
                            in.close();
                            items.remove(position);
                            listAdapter.notifyDataSetChanged();
                        }catch (Exception e){/**/}
                        return true;
                    } else {
                        return false;
                    }

                }


                public void addToFavorites(NewAlbum newAlbum) {
                    newFavorite(Favorite.SPOTIFYALBUM + newAlbum.url.replace("spotify:album:", ""), newAlbum.artist + "-" + newAlbum.album, Favorite.NEWALBUM, newAlbum.getImage());
                    generateLists();
                }

            };


            {
                Intent intent = new Intent(MainActivity.getInstance(), NewAlbumsActivityElectronic.class);
                MainActivity.getInstance().startActivity(intent);
            }

        }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
        }
    }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
    }

    public static void newAlbumsCategories() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.getInstance());
        builderSingle.setIcon(R.drawable.common_ic_googleplayservices);
        builderSingle.setTitle("Select Category");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                MainActivity.getInstance(),
                android.R.layout.select_dialog_singlechoice);
        for (String cat : CATEGORY_IDS) {
            arrayAdapter.add(cat);
        }

        builderSingle.setNegativeButton(
                "cancel",
                (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(
                arrayAdapter,
                (dialog, which) -> {
                    final String cat = arrayAdapter.getItem(which);//title1.replace("new albums ", "");
                    spotifyNewMusic(cat);
                });
        builderSingle.show();
    }

    public static void spotifyNewMusic(final String cat) {
        try {
            MainActivity.getInstance().fillListviewWithValues = new FillListviewWithValues() {

                @Override
                protected void addMenuItems(ArrayList<String> menuItems){
                    menuItems.add("sep");
                    ArrayList<String> menuItemsadd=new ArrayList<>();
                    for (String cat : CATEGORY_IDS) {
                        menuItemsadd.add("http://"+cat);
                    }
                    menuItems.addAll(menuItemsadd);
                }
                @Override
                public void executeUrl(String s){
                    s= s.replace("http://","");
                    spotifyNewMusic(s);
                }
                @Override
                public String getText(){
                    return cat;
                }
                @Override
                public void generateList(ArrayList<NewAlbum> newAlbums) {

                    Favorite.NEWALBUM=Favorite.getCategoryId(cat);

                    Document doc = null;
                    try {
                        String address = "http://www.spotifynewmusic.com/tagwall3.php?ans=" + cat;
                        doc = Jsoup.connect(address).get();
                    } catch (IOException e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

                    Elements trackelements = doc.getElementsByClass("album");
                    for (Element element : trackelements) {
                        String image1 = "http://www.spotifynewmusic.com/" + element.getElementsByTag("img").get(0).attr("src");//http://www.spotifynewmusic.com/covers/13903.jpg
                        Elements links = element.getElementsByClass("play").select("a[href]"); // a with href
                        String s = links.get(0).attr("href");
                        String html2 = element.children().get(1).html().replace("<br />", ";").replace("\n", "");
                        try {
                            String[] list = html2.split(";");
                            String artist = list[0];
                            String album = "";
                            if (list.length > 1)
                                album = list[1];
                            newAlbums.add(new NewAlbum(s, artist, album, image1));
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }
                    }
                }

                @Override
                public void addToFavorites(NewAlbum newAlbum) {
                    newFavorite(Favorite.SPOTIFYALBUM + newAlbum.url.replace("spotify:album:", ""), newAlbum.artist + "-" + newAlbum.album, cat, newAlbum.getImage());
                    generateLists();
                }

            };


            {
                Intent intent = new Intent(MainActivity.getInstance(), NewAlbumsActivityElectronic.class);
                MainActivity.getInstance().startActivity(intent);
            }
            // }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }


    protected PlanetAdapter setAdapterForSpotify() {
        albumAdapter = new PlanetAdapter(getData().albumList, activityThis, getData().albums) {
            @Override
            public void removeUp(int counter) {
                removeUplist(albumAdapter, counter,  activityThis);
            }

            @Override
            public void removeAll() {
                clearSpotifyPlaylist();
                refreshPlaylistFromSpotify(albumAdapter,  activityThis);
            }

            @Override
            public void onClickFunc(int counter) {
                currentTrack=counter;
                if (albumVisible)
                    try{
                        String s = getData().albumIds.get(counter);
                        boolean clear=false;
                        boolean ret=false;
                        boolean play=true;
                        ret = playMpdAlbum(s, clear, ret, play);
                        if (!ret) {
                            updateSpotifyList(counter);
                        }



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }
                else {
                    stopMpd();
                    playAtPosition(counter);
                }
            }

            void updateSpotifyList(int counter) {
                try {
                    getAlbumtracksFromSpotify(counter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void removeDown(int counter) {
                removeDownlist(albumAdapter, counter,  activityThis);
            }

            @Override
            public void removeAlbum(int counter) {
                SpotifyFragment.removeAlbum(albumAdapter, counter,  activityThis);
            }

            @Override
            public void addAlbumToFavoritesAlbum(int counter) {
                addAlbumToFavorites(Favorite.SPOTIFYALBUM + getData().albumIds.get(counter), artistName + "-" + getData().albumList.get(counter), getData().albums.get(counter).url);

            }

            @Override
            public void addAlbumToFavoritesTrack(int counter) {
                addAlbumToFavoritesTrackwise(counter);

            }

            @Override
            public void removeTrack(int counter) {
                removeTrackSpotify(counter);
                refreshPlaylistFromSpotify(albumAdapter, activityThis);
            }

            @Override
            public void displayArtist(int counter) {
                try{

                    SpotifyFragment.artistName=getData().tracksPlaylist.get(counter).artists.get(0).name;
                    MainActivity.getInstance().tabLayout.getTabAt(MainActivity.SPOTIFYTAB).select();
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
            }

            @Override
            public void displayArtistWikipedia(int counter) {
                    String s = getData().tracksPlaylist.get(counter).artists.get(0).name;
                    MainActivity.startWikipediaPage(s);
            }

            @Override
            public void replaceAndPlayAlbum(int counter) {
                if (albumVisible)
                    try{
                        if (!playMpdAlbum(getData().albumIds.get(counter), true, false, true)) {
                            clearSpotifyPlaylist();
                            updateSpotifyList(counter);
                        }



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

            }

            @Override
            public void addAndPlayAlbum(int counter) {
                if (albumVisible)
                    try{
                        if (!playMpdAlbum(getData().albumIds.get(counter), false, false, true)) {
                            updateSpotifyList(counter);
                        }
                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

            }

            @Override
            public void albumArtistWikipedia(int counter) {
                    MainActivity.startWikipediaPage(artistName);
            }

            @Override
            public void addAlbum(int counter) {
                if (albumVisible)
                    try{
                        if (!playMpdAlbum(getData().albumIds.get(counter), false, false, false)) {
                            addAlbumStatic(counter);
                            updateSpotifyList(counter);
                        }



                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

            }

            @Override
            public void transferPlaylist() {
            }

            @Override
            public void addAlbumNoplay(int counter) {
                String uri = getData().albumIds.get(counter);
                String prefix="spotify:album:";
                AddSpotifyItemToPlaylist(prefix, uri);
            }
        };
        albumAdapter.setDisplayCurrentTrack(false);
        return albumAdapter;
    }

    public boolean playMpdAlbum(String s, boolean clear, boolean ret, boolean play) {
        if (s.startsWith(MPD)){
            ret=true;
            String path=s.replace(MPD,"");
            ArrayList<String> commands=new ArrayList<>();
            if (clear)
                commands.add("clear");

            path = Logic.removeSlashAtEnd(path);
            String s1 = "add \""+path+"\"";

            commands.add(s1);
            Logic logic = MainActivity.getInstance().getLogic();
            String id= getInstance().getString(R.string.addandplay_filelist);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(activityThis);
            builder.setTitle("Search artist");

            // Set up the input
            ArtistAutoCompleteAdapter adapter = new ArtistAutoCompleteAdapter(activityThis,
                    android.R.layout.simple_spinner_item/*android.R.layout.simple_dropdown_item_1line*/);
            final InstantAutoComplete input = new InstantAutoComplete(activityThis);

            input.setAdapter(adapter);
            input.setThreshold(0);
            input.setDropDownHeight(400);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String artistString = input.getText().toString();
                if (artistString.length()==0){
                    Toast.makeText(activityThis.getApplicationContext(), "Please enter something for artist!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean add=true;
                for (String s: getData().searchArtistString)
                    if (s.equals(artistString))add=false;
                if (add)
                    getData().searchArtistString.add(artistString);
                MainActivity.getInstance().fillListviewWithValues = new FillListviewWithValues() {

                    @Override
                    public void generateListSearch(final ArrayList<SearchItem> newAlbums) {
                        spotify.searchArtists(artistString.trim(), new Callback<ArtistsPager>() {

                            @Override
                            public void success(ArtistsPager artistsPager, Response response) {
                                try{
                                    if (artistsPager.artists.items.size()>0) {
                                        for (Artist artist : artistsPager.artists.items) {
                                            String name = artist.name;
                                            //DebugLog.log("artist found: " + name);
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
                                    } else {
                                        Toast.makeText(activityThis.getApplicationContext(), "No artists found!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                SearchActivity.getThis.notifyChange();
                            } catch (Exception e) {
                                Log.v("samba", Log.getStackTraceString(e));
                            }
                        }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }



                    public void processAlbum(SearchItem album) {
                        Image im = new Image();
                        try {
                            im.url = getImageUrl(album.images);
                        } catch (Exception e) {/**/
                        }
                        listAlbumsForArtistId(album.id, im, album.artist, new SpotifyApi());
                    }


                };

                Intent intent = new Intent(MainActivity.getInstance(), SearchActivity.class);
                startActivity(intent);

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public void searchAlbum() {
        try {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.getInstance());
            builder.setTitle("Search album");

            // Set up the input
            final EditText input = new EditText(activityThis);
            input.setText(searchAlbumString);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                searchAlbumString = input.getText().toString();
                if (searchAlbumString.length()==0){
                    Toast.makeText(activityThis.getApplicationContext(), "Please enter something for album!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                MainActivity.getInstance().fillListviewWithValues = new FillListviewWithValues() {

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
                                    si.imageid = getImageUrl(album.images);
                                    newAlbums.add(si);
                                }
                                SearchActivity.getThis.notifyChange();

                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });
                    }

                    @Override
                    public void processAlbum(SearchItem album) {
                        try {
                            getAlbumtracksFromSpotify(album.id, album.artist, activityThis);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                };

                Intent intent = new Intent(MainActivity.getInstance(), SearchActivity.class);
                startActivity(intent);

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public static void addAlbumStatic(int counter) {
        artistName = getData().tracksPlaylist.get(counter).artists.get(0).name;
        try {
            getAlbumtracksFromSpotify(getData().tracksPlaylist.get(counter).album.id, getData().tracksPlaylist.get(counter).album.name,activityThis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addAlbumToFavoritesTrackwise(int counter) {//
        String url = Favorite.SPOTIFYALBUM + getData().tracksPlaylist.get(counter).album.id;
        String name = getData().tracksPlaylist.get(counter).artists.get(0).name;
        String album = getData().tracksPlaylist.get(counter).album.name;
        String description = name + "-" + album;
        String newalbum = Favorite.NEWALBUM;
        newFavorite(url, description, newalbum, getData().albumTracks.get(counter).url);
    }

    public static void newFavorite(String url, String description, String newalbum, String imageurl) {
        EditFavoriteActivity.editAndSaveFavorite(MainActivity.getInstance(),-1, imageurl, url, "", description, newalbum);
    }

    public static void addAlbumToFavorites(String url, String description, String s) {
        newFavorite(url, description, Favorite.NEWALBUM,s);
    }

    public static void removeAlbum(PlanetAdapter albumAdapter, int counter, Activity getThis) {
        String albumid = getData().tracksPlaylist.get(counter).album.id;
        for (int i = getData().tracksPlaylist.size() - 1; i >= 0; i--) {
            if (getData().tracksPlaylist.get(i).album.id.equals(albumid)) removeTrackSpotify(i);
        }
        refreshPlaylistFromSpotify(albumAdapter,  getThis);
    }

    public static int getVolumeSpotify(String ipAddress) {
        int vol=0;
        try {
            String sb = getJsonStringFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.mixer.get_volume\"}",
                    ipAddress);
            vol = new JSONObject(sb).getInt("result");
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

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
            GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"core.playback.stop\"}",
                    ipAddress);//?StopPause
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public static void playPauseSpotify() {playPauseSpotify(ipAddress);}
    public static void playPauseSpotify(String ipAddress) {
        try {
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void setVisibility(int visibility) {

        relatedArtistsListView.setVisibility(visibility);

        ( llview.findViewById(R.id.relatedartists_text)).setVisibility(visibility);//albumsartist_listview
        ( llview.findViewById(R.id.albumsartist_listview)).setVisibility(visibility);//albumsartist_listview
    }

    public static void playlistGotoPosition(int position) {
        playAtPosition(position);
    }

    public void getAlbumtracksFromSpotify(final int position) throws Exception {
        String s = getData().albumIds.get(position);

        //check if not already in playlist
            getAlbumtracksFromSpotify(s, getData().albumList.get(position), activityThis, false);
            try {
                DebugLog.log("update list");
            } catch (Exception e) {
                Log.getStackTraceString(e);
            }
    }
    public static void getAlbumtracksFromSpotify(final String albumid, final String albumname, final Activity getThis1) throws Exception {
        getAlbumtracksFromSpotify(  albumid,   albumname,   getThis1,true);
    }

    public static void getAlbumtracksFromSpotify(final String albumid, final String albumname, final Activity getThis1,boolean display) throws Exception {
        boolean alreadyThere=false;
        for (Track t: getData().tracksPlaylist) {
            if (t.album.id.equals(albumid)) {
                alreadyThere = true;
            }
        }
        if (alreadyThere)
        new AlertDialog.Builder(getThis1)
                .setTitle("Warning")
                .setMessage("Do you really want to add a duplicate of this album?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) ->
                        addSpotifyAlbumToPlaylist(albumid, albumname, getThis1, display))
                .setNegativeButton(android.R.string.no, (dialog, whichButton) ->
                {}).show();
        if (!alreadyThere) {

            addSpotifyAlbumToPlaylist(albumid, albumname, getThis1, display);
        } else {
            Toast.makeText(getThis1, "Album already in playlist!",
                    Toast.LENGTH_SHORT).show();
            throw new Exception();
        }
    }

    public static void addSpotifyAlbumToPlaylist(final String albumid, final String albumname, final Activity getThis1, final boolean display) {
        new SpotifyApi().getService().getAlbumTracks(albumid, new Callback<Pager<Track>>() {

            @Override
            public void success(Pager<Track> trackPager, Response response) {
                ArrayList<String> ids = new ArrayList<>();
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

                        List<Image> l = new ArrayList<>();
                        l.add(im);
                        alb.images=l;
                        t.album = alb;
                        Artist art = new Artist();
                        art.name = artistName;
                        List<ArtistSimple> a = new ArrayList<>();
                        a.add(art);
                        t.artists = a;
                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }
                    getData().tracksPlaylist.add(t);
                    getData().hm.put(t.id, t);
                    ids.add(t.id);
                }

                new AddTracksToPlaylist(ids, getThis1) {
                    @Override
                    public void atEnd() {
                        if (display)
                        refreshPlaylistFromSpotify(null, getThis1);
                    }

                }.run();

            }


            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void listAlbumsForArtist(String s) {
        listAlbumsForArtist(api, spotify, s, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
    }

    public static void removeDownlist(PlanetAdapter albumAdapter, int counter, Activity getThis) {
        for (int i = getData().tracksPlaylist.size()-1; i>= counter; i--) {
            removeTrackSpotify(i);
        }
        spotifyStartPosition = 0;
        refreshPlaylistFromSpotify(albumAdapter,  getThis);
    }

    public static void removeTrackSpotify(int counter) {

        //curl -d '{"jsonrpc": "2.0", "id": 1, "method": "core.tracklist.remove", "params": {"criteria":{"uri":["spotify:track:%s"]}}}' http://192.168.2.12:6680/mopidy/rpc
        //curl -d '{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.tracklist.remove\", \"params\": {\"criteria\":{\"uri\":\["spotify:track:%s\"]}}}' http://192.168.2.12:6680/mopidy/rpc
        String id="spotify:track:"+ getData().albumTracks.get(counter).id;
        GetJsonFromUrl(
                "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.tracklist.remove\", \"params\": {\"criteria\":{\"uri\":[\""+id+"\"]}}}",
                ipAddress);
    }

    public static void removeUplist(PlanetAdapter albumAdapter, int counter, Activity getThis) {
        for (int i = counter; i >=0; i--)
            removeTrackSpotify(i);
        spotifyStartPosition = 0;
        refreshPlaylistFromSpotify(albumAdapter,  getThis);
    }


    @Override
    public void resultDbCall(ArrayList<String> dblist) {
        String album="";
        String file="";
        String prevFile="";
        int total=0;
        try{
            for (String s1:dblist){
                if (s1.startsWith("Album: ")) {
                    String album1= s1.replace("Album: ", "");
                    if (!album.equals(album1)){
                        if ((album.length()>0) &&(total>1)){
                            PlaylistItem pi=new PlaylistItem();
                            pi.pictureVisible=true;
                            pi.url="http://192.168.2.8:8081/FamilyMusic/"+file+"/folder.jpg";
                            pi.text=album;
                            pi.time=0;

                            getData().albumList.add(album);
                            getData().albumIds.add(MPD+file);
                            getData().albums.add(pi);
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
                PlaylistItem pi=new PlaylistItem();
                pi.pictureVisible=true;
                pi.url="http://192.168.2.8:8081/FamilyMusic/"+file+"/folder.jpg";
                pi.text=album;
                pi.time=0;

                getData().albumList.add(album);
                getData().albumIds.add(MPD+file);
                getData().albums.add(pi);
            }
            albumAdapter.setDisplayCurrentTrack(false);
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

        SpotifyFragment.getInstance().getActivity().runOnUiThread(() -> {
            albumAdapter.notifyDataSetChanged();
            Utils.setDynamicHeight(albumsListview, 0);

        });


    }

    public static void billboardAlbumTop200() {
        String url = "http://www.billboard.com/charts/billboard-200";
        billboardAlbumChart(url);

    }

    public static void billboardAlbumChart(final String url) {
        try {
            MainActivity.getInstance().fillListviewWithValues = new FillListviewWithValues() {

                @Override
                protected void addMenuItems(ArrayList<String> menuItems){
                    menuItems.add("sep");
                    ArrayList<String> billboardList = new ArrayList<>(Arrays.asList("classical-albums","greatest-billboard-200-albums","billboard-200","independent-albums"
                    ,"country-albums","bluegrass-albums","rock-albums","alternative-albums","hard-rock-albums","americana-folk-albums","united-kingdom-albums"
                            ,"reggae-albums","new-age-albums","jazz-albums","blues-albums","dance-electronic-albums","r-b-hip-hop-albums","r-and-b-albums"
                            ,"rap-albums","latin-pop-albums","latin-albums","gospel-albums"));
                    for (String cat : billboardList) {
                        menuItems.add("http://"+cat);
                    }
                }
                @Override
                public void executeUrl(String s){
                    s= s.replace("http://","http://www.billboard.com/charts/");
                    billboardAlbumChart(s);
                }
                @Override
                public void generateList(ArrayList<NewAlbum> newAlbums) {

                    Document doc = null;
                    try {
                        doc = Jsoup.connect(url).get();
                    } catch (IOException e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }

                    Elements trackelements = doc.getElementsByClass("chart-row");
                    int i=0;
                    for (Element element : trackelements) {
                        i++;
                        try {
                            String currentWeek=element.getElementsByClass("chart-row__current-week").get(0).text();
                            String albumTitle=element.getElementsByClass("chart-row__song").get(0).text();
                            String artistTitle=element.getElementsByClass("chart-row__artist").get(0).text();//data-imagesrc
                            String image1=element.getElementsByClass("chart-row__image").get(0).attr("data-imagesrc");
                            String lastweek="";
                            try{
                            lastweek=element.select(".chart-row__last-week>.chart-row__value").first().text();
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));

                        }
                            String incharts=element.getElementsByClass("chart-row__weeks-on-chart").get(0).getElementsByClass("chart-row__value").get(0).text();
                            if (image1.length()==0)
                             image1=element.getElementsByClass("chart-row__image").get(0).attr("style").replace("background-image: url(","").replace(")","");
                            String id=element.attr("data-spotifyid");
                            if (id.length()==0)continue;
                            //todo: check if id valid spotify-link
                            String nr=String.format("%s-%s-(%s)-",currentWeek,lastweek,incharts);

                            newAlbums.add(new NewAlbum(id, artistTitle, nr+albumTitle, image1));
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                        }

                    }
                }

                @Override
                public void addToFavorites(NewAlbum newAlbum) {
                    newFavorite(Favorite.SPOTIFYALBUM + newAlbum.url.replace("spotify:album:", ""), newAlbum.artist + "-" + newAlbum.album, Favorite.NEWALBUM, newAlbum.getImage());
                    generateLists();
                }

            };


            {
                Intent intent = new Intent(MainActivity.getInstance(), NewAlbumsActivityElectronic.class);
                MainActivity.getInstance().startActivity(intent);
            }
            // }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public static class getEntirePlaylistFromSpotify {
        String playlistid;
        Activity getThis;

        getEntirePlaylistFromSpotify(String playlistid, Activity getThis) {
            this.playlistid = playlistid;
            this.getThis = getThis;
        }

        public void run() {

            SpotifyPlaylistFragment.gettingList=true;
            try {
                ArrayList<String> ids = new ArrayList<>();

                String prefix="spotify:user:";
                if (playlistid.startsWith("spotify"))prefix="";
                ids.add(prefix+playlistid);
                DebugLog.log("play "+prefix+playlistid);
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
            SpotifyPlaylistFragment.gettingList=false;
        }
    }

    public static void clearSpotifyPlaylist() {
        //curl -d '{"jsonrpc": "2.0", "id": 1, "method": "core.tracklist.clear"}TracklistController.clear()
        checkAddress();
        GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 0, \"method\": \"core.tracklist.clear\"}",
                ipAddress);//
    }

    public static class addExternalPlaylistToSpotify {
        String url;
        Activity getThis;

        addExternalPlaylistToSpotify(String url, Activity getThis) {
            this.url = url;
            this.getThis = getThis;
        }

        public void run() {
            SpotifyPlaylistFragment.gettingList=true;
            stopMpd();

            Document doc = null;
            try {
                doc = Jsoup.connect(url).get();
            } catch (IOException e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
            Elements trackelements = doc.select("meta[property=music:song]");
            ArrayList<String> ids = new ArrayList<>();
            for (Element element : trackelements) {
                String s = element.attr("content");
                int startIndex = s.indexOf("track/") + 6;
                ids.add(element.attr("content").substring(startIndex));
            }
            new AddTracksToPlaylist(ids, getThis) {
                @Override
                public void atEnd() {
                    atLast();
                }

            }.run();
        }

        public void atLast() {
            SpotifyPlaylistFragment.gettingList=false;
        }
    }

    public static void stopMpd() {
        try {
            if (MainActivity.getInstance().getLogic().mpcStatus.playing) {
                MainActivity.getInstance().getLogic().getMpc().stop();
            }
        } catch (Exception e){/**/}
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

            AddSpotifyTrack(mainids, 0);
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

                checkAddress();
                if (getThis!=null) {
                    dialog1 = new ProgressDialog(getThis);
                    dialog1.setTitle("spotify-playlist");
                    dialog1.setMessage("Adding to list");
                    dialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog1.setProgress(0);
                    dialog1.setMax(mainids.size());
                    dialog1.show();
                }


                new Thread(new Task(mainids, getThis) {
                    public void atEnd2() {
                        MainActivity.getInstance().runOnUiThread(() -> {


                            try{
                                if (getThis!=null) {
                                    dialog1.dismiss();
                                }
                            atEnd();
                        } catch (Exception e) {
                            Log.v("samba", Log.getStackTraceString(e));
                            DebugLog.log("end thread after at end");
                        }

                        });
                    }

                }).start();

            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        }

        public void atEnd() {

        }


    }


    public static void refreshPlaylistFromSpotify(final PlanetAdapter albumAdapter1, Activity getThis) {

        albumVisible = false;
        if (albumAdapter1!=null)
        albumAdapter1.setAlbumVisible(false);
        try {
            TracksSpotifyPlaylist.getInstance().triggerPlaylist((albumList, albumTracks, force) -> {
                if (albumAdapter1!=null) {
                    albumAdapter1.setDisplayCurrentTrack(true);
                    getThis.runOnUiThread(() -> {
                        albumAdapter1.notifyDataSetChanged();
                    });
                }
            });

        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

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
        if (beatles!=null)
            new GetArtistId(spotify, beatles){
                public void doSomethingWithId(String id, Image image){
                    //Log.d("samba", "Text:13");
                    listAlbumsForArtistId(id, image, beatles, api);
                }
            }.invoke();
        else
            Toast.makeText(activityThis, "artist not defined",
                    Toast.LENGTH_SHORT).show();
    }

    public void initArtistLook(String beatles) {

        albumsListview.setOnItemClickListener(cl);
        artistName =beatles;
        albumVisible = true;
        albumAdapter.setAlbumVisible(true);
    }

    public void listAlbumsForArtistId(String id, Image image, String beatles, SpotifyApi api) {
        initArtistLook(beatles);
        artistTitleTextView.setText(beatles);

        spotifyHeader.setArtistText(beatles, image);
        if (isNewArtist) {
            SpotifyService spotify = api.getService();
            getArtistAlbums(id, beatles, spotify);
            getRelatedArtists(id, spotify);
            isNewArtist=false;
        }else{
            MainActivity.getInstance().runOnUiThread(() -> {
                Utils.setDynamicHeight(relatedArtistsListView, 0);
                Utils.setDynamicHeight(albumsListview, 0);
                scrollToPreviousPosition(scrollY);
            });
        }

    }

    public void scrollToPreviousPosition(int newPos) {
        final ScrollView scrollView = (ScrollView) llview.findViewById(R.id.spotifyscrollviewmiddle);
        scrollView.post(() -> {

            try {
                MainActivity.getInstance().runOnUiThread(() -> {
                    scrollView.scrollTo(0, newPos);
                    setScrollviewListenerToGetScrollOffset();
                });
            } catch (Exception e) {/**/}
        });
    }

    public void getRelatedArtists(String id, SpotifyService spotify) {
        spotify.getRelatedArtists(id, new Callback<Artists>() {
            @Override
            public void success(Artists artists, Response response) {
                try{
                    getData().artistList.clear();
                    for (Artist artist : artists.artists) {
                        getData().artistList.add(artist.name);
                    }
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
                MainActivity.getInstance().runOnUiThread(() -> {
                    relatedArtistsAdapter.notifyDataSetChanged();
                    Utils.setDynamicHeight(relatedArtistsListView, 0);
                    scrollToPreviousPosition(0);
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getArtistAlbums(String id, final String beatles, SpotifyService spotify) {
        spotify.getArtistAlbums(id, new Callback<Pager<Album>>() {

            @Override
            public void success(Pager<Album> albumPager, Response response) {
                try{
                    if (albumPager.items.size()==0){
                        Toast.makeText(activityThis, "no albums for "+beatles,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    SpotifyFragment.getData().albumList.clear();
                    SpotifyFragment.getData().albums.clear();
                    SpotifyFragment.getData().albumIds.clear();
                    //albumTracks.clear();
                    String previous = "";
                    for (Album album : albumPager.items)
                        if (!album.name.equals(previous)) {
                            PlaylistItem pi=new PlaylistItem();
                            pi.pictureVisible=true;
                            pi.url=getImageUrl(album.images);
                            pi.text=String.format("%s",album.name);
                            pi.time=0;

                            SpotifyFragment.getData().albumList.add(album.name);
                            SpotifyFragment.getData().albumIds.add(album.id);
                            SpotifyFragment.getData().albums.add(pi);
                            previous = album.name;

                        }
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
                MainActivity.getInstance().runOnUiThread(() -> {
                            albumAdapter.notifyDataSetChanged();
                            Utils.setDynamicHeight(albumsListview, 0);
                });
                DatabaseListThread a=new DatabaseListThread(MainActivity.getInstance().getLogic().getMpc(),String.format("find \"artist\" \"%s\"",beatles), getInstance());
                a.start();

            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
    }

    public static String getImageUrl(List<Image> images) {
        int imNr=0;
        if (images.size()>1)imNr=1;
        return images.get(imNr).url;
    }

    public static int getTime(){
        String s=getJsonStringFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"core.playback.get_time_position\"}",ipAddress);
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
            return new JSONObject(s).getString("result");
        } catch (Exception e) {
            /**/
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

    }

    public static String updateSongInfo(Activity getThis, final SpotifyInterface getSpotifyInterface) {
        String artistReturn="";

        try {
                if (isPlaying()) {
                        playingEngine=1;
                     String[] trid1 = getCurrentTrack();//
                    String trid = "0";
                    try {
                        trid = trid1[0];
                    } catch (Exception e){ /**/}
                    totalTime = Integer.parseInt(trid1[1])/1000;
                    if ((trid.length() > 0)) {
                            try{
                                for (int i = 0; i < SpotifyFragment.getData().tracksPlaylist.size(); i++) {
                                    if (SpotifyFragment.getData().tracksPlaylist.get(i).id.equals(trid)) {
                                        currentTrack = i;
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                /**/
                            }
                        Track t = getTrack(trid);
                        if ((t != null)&&(getSpotifyInterface!=null))
                            if ((getSpotifyInterface.previousTrack == null) || !(t.id.equals(getSpotifyInterface.previousTrack.id))) {
                                getSpotifyInterface.previousTrack = t;
                                    String imageurl = getImageUrl(t.album.images);
                                    if (imageurl.equals("")) {
                                        String urlString = "https://api.spotify.com/v1/tracks/" + trid;
                                        String getResult = getStringFromUrl(urlString);
                                        imageurl = new JSONObject(getResult).getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                                    }

                                    DownLoadImageUrlTask.setAlbumPicture(t.album.id, imageurl);
                                    MainActivity.getAlbumPicturesIds().put(t.album.id, imageurl);
                            }
                        MainActivity.getInstance().imageLoader.DisplayImage(MainActivity.getAlbumPicturesIds().get(t.album.id), bitmap -> {
                            MainActivity.getInstance().runOnUiThread(() -> {
                                for (HeaderSongInterface header: MainActivity.getHeaders()){
                                    if (header!=null)
                                        header.setLogo(bitmap);
                                }
                                SpotifyFragment.bitmap = bitmap;
                            });
                        });
                        /*new DownLoadImageTask() {
                            @Override
                            public void setImage(Bitmap logo) {
                                getThis.runOnUiThread(() -> {
                                    for (HeaderSongInterface header: MainActivity.getHeaders()){
                                        if (header!=null)
                                            header.setLogo(logo);
                                    }
                                    SpotifyFragment.bitmap = logo;
                                });
                            }
                        }.execute(MainActivity.getAlbumPicturesIds().get(t.album.id));
*/
                        currentTime = getTime();
                        artistReturn = t.artists.get(0).name;
                        MainActivity.playingStatus=MainActivity.SPOTIFY_PLAYING;
                        try {
                            if (SpotifyPlaylistFragment.getInstance() != null)
                                (SpotifyPlaylistFragment.getInstance()).setData(niceTime(currentTime), niceTime(totalTime), t.name, t.artists.get(0).name, true, currentTrack);
                            MainActivity.getInstance().runOnUiThread(() -> {

                                for (HeaderSongInterface header : MainActivity.getHeaders()) {
                                    if (header != null)
                                        header.setData(niceTime(currentTime), niceTime(totalTime), t.name, t.artists.get(0).name, true, currentTrack);
                                }
                                MainActivity.playingStatus = MainActivity.SPOTIFY_PLAYING;

                            });
                        } catch (Throwable thr){
                            /*;*/
                        }
                    }
                }

            } catch (Exception e) {
            /**/
            }
        return artistReturn;

    }



    public static Track getTrack(String trackid) {
        Track nt = (Track) getData().hm.get(trackid);
        try {
            if (nt == null) {
                nt = new Track();
                JSONObject o = new JSONObject(getStringFromUrl("https://api.spotify.com/v1/tracks/" + trackid));
                nt.id = trackid;
                List<ArtistSimple> a = new ArrayList<>();
                Artist art = new Artist();
                art.name = o.getJSONArray("artists").getJSONObject(0).getString("name");
                a.add(art);
                nt.artists = a;
                Album alb = new Album();
                alb.name = o.getJSONObject("album").getString("name");//track_number
                alb.id = o.getJSONObject("album").getString("id");
                nt.name = o.getString("name");//duration_ms
                nt.track_number=o.getInt("track_number");
                alb.images=new ArrayList<>();
                Image im=new Image();
                try{
                    im.url= o.getJSONObject("album").getJSONArray("images").getJSONObject(0).getString("url");
                    /*MainActivity.getInstance().imageLoader.DisplayImage(im.url, bitmap -> {
                        MainActivity.getInstance().runOnUiThread(() -> {});
                    });*/
                    /*DownLoadImageUrlTask.albumPictures.put(alb.id, im.url);
                    new DownLoadImageTask() {

                        @Override
                        public void setImage(final Bitmap logo) {
                        }
                    }.execute(im.url);*/

                } catch (Exception e) {
                    im.url="";
                }
                alb.images.add(im);
                nt.duration_ms = o.optLong("duration_ms");//
                nt.album = alb;
                getData().hm.put(nt.id, nt);
            }
        } catch (Exception e) {
            /**/
        }
        return nt;
    }

    @NonNull
    public static  String getStringFromUrl(String urlString) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = new URL(urlString).openStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                sb.append(inputStr);
            return sb.toString();
        } catch (Exception e) {
            /**/
        }
        return "";
    }

    public static abstract class DownLoadImageUrlTask extends AsyncTask<String, Void, String> {
    static void setAlbumPicture(String key, String value) {
        DownLoadImageUrlTask.albumPictures.put(key,value);
    }

    //String input of doInBackground, void input of onProgressUpdate, String input of onPostExecute (and this return value of doInBackground)
    //onPreExecute() is called on the UI thread, before the Non-UI work starts
        private static HashMap<String, String>albumPictures=new HashMap<>();


        DownLoadImageUrlTask() {
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
                        Thread.sleep(1000);
                        n++;
                    }
                    return(albumPictures.get(albumId));
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else             try {
                String urlString = "https://api.spotify.com/v1/albums/" + albumId;
                String getResult = getStringFromUrl(urlString);
                imageUrl = new JSONObject(getResult).getJSONArray("images").getJSONObject(0).getString("url");
                albumPictures.put(albumId, imageUrl);//so image is loaded only once
            } catch (Exception e) { // Catch the download exception
                /**/
            }
            return imageUrl;
        }

        protected void onPostExecute(String result) {
            setUrl(result);
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

    }
    public static void seekPlayMpd(Activity getThis) {
        Logic logic = MainActivity.getInstance().getLogic();
        MPCStatus status = logic.mpcStatus;
        if (!status.playing) return;
        int songnr = status.song;
        int time= status.time;
        Mp3File currentSong = logic.getPlaylistFiles().get(songnr);
        new Seek(getThis, time, currentSong.getTime()) {
            @Override
            void seekPos(int progress) {
                String message = "seekcur " + (progress);
                MainActivity.getInstance().getLogic().getMpc().enqueCommands(new ArrayList<>(Arrays.asList(message)));
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


        alert.setPositiveButton("Ok", (dialog, id) -> dialog.dismiss());

        alert.show();
    }


    public static void showPlayMenu(final Activity getThis1) {

        LayoutInflater inflater = getThis1.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.play_spotify, null);
        View stopbutton = alertLayout.findViewById(R.id.stopspotify);
        View playpausebutton = alertLayout.findViewById(R.id.pausespotify);
        View previousbutton = alertLayout.findViewById(R.id.previousspotify);
        View nextbutton = alertLayout.findViewById(R.id.nextspotify);
        View volumebutton = alertLayout.findViewById(R.id.volumespotify);
        View seekbutton = alertLayout.findViewById(R.id.positionspotify);
        String title = setListenersForButtons(getThis1, stopbutton, playpausebutton, previousbutton, nextbutton, volumebutton, seekbutton);
        AlertDialog.Builder alert = new AlertDialog.Builder(getThis1);
        alert.setTitle(title);
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);

        alert.setPositiveButton("OK", (dialog, which) -> {});
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    @NonNull
    public static String setListenersForButtons(Activity getThis1, View stopbutton, View playpausebutton, View previousbutton, View nextbutton, View volumebutton, View seekbutton) {
        String title = "Spotify Play";

        stopbutton.setOnClickListener(v -> {
            try{
            if (SpotifyFragment.playingEngine==1) stopSpotifyPlaying(ipAddress);
            else {
                MainActivity.getInstance().getLogic().getMpc().pause();
                MainActivity.getInstance().getLogic().setPaused(true);
            }
        }catch (Exception e){
                Log.v("samba","Error starting stop!");
        }
        });
        playpausebutton.setOnClickListener(v -> {
            if (SpotifyFragment.playingEngine==1||spotifyPaused) {
                playPauseSpotify(ipAddress);
                spotifyPaused=!spotifyPaused;
            }
            else MainActivity.getInstance().playPause();
        });
        previousbutton.setOnClickListener(v -> {
            if (SpotifyFragment.playingEngine==1) previousSpotifyPlaying(ipAddress);
            else MainActivity.getInstance().getLogic().getMpc().previous();
        });
        nextbutton.setOnClickListener(v -> {
            if (SpotifyFragment.playingEngine==1) nextSpotifyPlaying(ipAddress);
            else MainActivity.getInstance().getLogic().getMpc().next();
        });
        volumebutton.setOnClickListener(v -> {
            if (SpotifyFragment.playingEngine==1) setVolume(getThis1);
            else MainActivity.getInstance().setVolume(getThis1);
        });
        seekbutton.setOnClickListener(v -> {
            if (SpotifyFragment.playingEngine==1) seekPlay(getThis1);
            else seekPlayMpd(getThis1);
        });

        return title;
    }

    private class GetArtistId {
        private final String beatles;
        private SpotifyService spotify;

        GetArtistId(SpotifyService spotify, String beatles) {
            this.spotify = spotify;
            this.beatles = beatles;
        }

        void invoke() {
            spotify.searchArtists(beatles.trim(), new Callback<ArtistsPager>() {

                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    try{

                    String id = "";
                    int max = 10000;
                    Image image = null;
                    for (Artist artist : artistsPager.artists.items) {
                        String name = artist.name;
                        if (name.startsWith("The ")) name = name.substring(4);
                        if (name.toLowerCase().replace(" ","").contains(beatles.toLowerCase().replace(" ",""))) {

                            if (name.length() < max) {
                                id = artist.id;
                                try{
                                image = artist.images.get(0);
                            } catch (Exception e) {
                                /**/
                            }
                                max = name.length();
                            }
                        }


                    }

                    doSomethingWithId(id,image);
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }
                }

                @Override
                public void failure(RetrofitError error) {

                    Log.v("samba", Log.getStackTraceString(error));
                    error.printStackTrace();
                }
            });
        }
        public void doSomethingWithId(String id, Image image){
        }
    }
}

class PlaylistItem {
    boolean pictureVisible;
    public String url;
    public String text;
    public String id;
    public int time;
    int trackNumber=-1;
}

class SpotifyHeader {
    Activity getThis;
    public ImageView icon;
    private TextView MessageView;

    SpotifyHeader(Activity getThis){
        this.getThis=getThis;
        connectVarsToFront();
    }
    private void connectVarsToFront() {
        icon = (ImageView)

                SpotifyFragment.getInstance().llview.findViewById(R.id.icon2);

        MessageView = (TextView)

                SpotifyFragment.getInstance().llview.findViewById(R.id.artist_content);        //Expose the indent for the first three rows
    }

    void setArtistText(final String artistName, Image image) {
        if (SpotifyFragment.isNewArtist)
            AsyncTask.execute(() -> {
                try{
                    SpotifyFragment.getData().artistText = "";

                    JSONObject artist = (new JSONObject(SpotifyFragment.LastFMArtist(artistName))).getJSONObject("artist");

                    SpotifyFragment.getData().artistText = artist.getJSONObject("bio").getString("content");
                    setArtistTextFromLastFM(image);

                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                }

            });
        else {
            setArtistTextFromLastFM(image);
        }


    }

    private void setArtistTextFromLastFM(Image image) {
        SpannableString SS = new SpannableString(SpotifyFragment.getData().artistText);

        int scale = 250;
        int leftMargin = scale + 10;

        //Set the icon in R.id.icon
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(scale, scale);

        if (image!=null)

        try {
            MainActivity.getInstance().imageLoader.DisplayImage(image.url, bitmap -> {
                MainActivity.getInstance().runOnUiThread(() -> {
                    icon.setLayoutParams(layoutParams);
                    icon.setImageBitmap(bitmap);
                });
            });
            /*new DownLoadImageTask() {
                @Override
                public void setImage(Bitmap logo) {

                    MainActivity.getInstance().runOnUiThread(() -> {
                        icon.setLayoutParams(layoutParams);
                        icon.setImageBitmap(logo);
                    });
                }
            }.execute(image.url);*/
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

        SS.setSpan(new MyLeadingMarginSpan2(scale / 50, leftMargin), 0, SS.length(), 0);
        MainActivity.getInstance().runOnUiThread(() -> MessageView.setText(SS));
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
    private int max;
    Seek(Activity getThis, int position, int max){
        this.getThis=getThis;
        this.position=position;
        this.max=max;
    }
    public void run() {

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


        alert.setPositiveButton("Ok", (dialog, id) -> dialog.dismiss());

        alert.show();
    }

}

