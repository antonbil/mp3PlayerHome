package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.Utils;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
/*
The most straightforward way to get the access token is to use the Authentication Library from the Spotify Android SDK.(https://github.com/spotify/android-sdk)
explanation:https://developer.spotify.com/technologies/spotify-android-sdk/android-sdk-authentication-guide/#single-sign-on-with-spotify-client-and-a-webview-fallback
working example:https://developer.spotify.com/technologies/spotify-android-sdk/tutorial
and:https://github.com/kaaes/spotify-web-api-android/blob/master/sample-search/src/main/java/kaaes/spotify/webapi/samplesearch/LoginActivity.java
Detailed information how to use it can be found in the Spotify Android SDK Authentication Guide.
 */
public class SpotifyActivity extends AppCompatActivity {
    private ArrayList<String>artistList=new ArrayList<>();
    private ArrayList<String>albumIds=new ArrayList<>();
    private ArrayList<String>albumList=new ArrayList<>();
    private TextView MessageView;
    private SpotifyActivity getThis;
    //private ListView albumsListview, relatedArtistsListView;
    private static int spotifyStartPosition=0;
    private static String spotifyToken="";
    private static HashMap hm = new HashMap();
    private static String GetSpotifyToken(){
        Log.v("samba", "ask starred:");
        String data="{\"jsonrpc\":\"2.0\",\"method\":\"Files.GetDirectory\",\"id\":1,\"params\":[\"plugin://plugin.audio.spotlight/?path=starred&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D\",\"music\",[\"title\",\"file\",\"thumbnail\", \"art\",\"duration\"]]}";
        String urlString = "http://192.168.2.3:8080/jsonrpc?OpenAddon_plugin://plugin.audio.spotlight/?path=starred&args=%7B%22start%22%3A+0%2C+%22identifier%22%3A+%22%22%2C+%22max_items%22%3A+0%2C+%22offset%22%3A+0%7D";
        try {
            String fname = GetJsonFromUrl(data, urlString).optJSONArray("files").getJSONObject(0).optString("file");
            Log.v("samba", "filename:"+fname);
            int startIndex = fname.indexOf("Token=")+6;
            int endIndex = fname.indexOf("&User");
            Log.v("samba", fname.substring(startIndex, endIndex)); //is your string. do what you want
            spotifyToken=fname.substring(startIndex, endIndex);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        /*try {
            URL url = new URL(urlString);
            URLConnection uc = url.openConnection();
            uc.setDoOutput(true);// Triggers POST.

            uc.setRequestProperty("User-Agent", "@IT java-tips URLConnection");
            uc.setRequestProperty("Content-Type","application/json");
            uc.setRequestProperty("Accept-Language", "ja");
            OutputStream os = uc.getOutputStream();

            String postStr = data;
            PrintStream ps = new PrintStream(os);
            ps.print(postStr);
            ps.close();

            InputStream is = uc.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    Log.v("samba", "line read:" + line);
                    int startIndex = line.indexOf("Token=")+6;
                    int endIndex = line.indexOf("&User");
                        Log.v("samba", line.substring(startIndex, endIndex)); //is your string. do what you want
                    spotifyToken=line.substring(startIndex, endIndex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();

        }catch (Exception e){e.printStackTrace();}*/
        return "";
    }
    private static void AddSpotifyTrack(SpotifyActivity getThis,ArrayList<String> ids, final int pos){
        try {
            if (pos<ids.size()){
                String data=String.format("{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Add\", \"params\": { \"playlistid\" : 0 , \"item\" : {\"file\" : \"http://127.0.0.1:8081/track/%s.wav|X-Spotify-Token=%s&User-Agent=Spotlight+1.0\"}}, \"id\": 1}",ids.get(pos),spotifyToken);
                String urlString = "http://192.168.2.3:8080/jsonrpc?PlaylistAdd";
                GetJsonFromUrl(data, urlString);
                AddSpotifyTrack(getThis,ids,pos+1);
            }
        else{
                if (MainActivity.getThis.getLogic().mpcStatus.playing) {
                    MainActivity.getThis.getLogic().getMpc().stop();
                }
                //todo:change http://192.168.2.3 to address of current server
                JSONObject playlist = GetJsonFromUrl(
                        "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.GetItems\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"duration\", \"thumbnail\",\"file\"], \"playlistid\": 0 }, \"id\": 1}\u200B",
                        "http://192.168.2.3:8080/jsonrpc?GetPLItemsAudio");

                spotifyStartPosition=playlist.getJSONArray("items").length()-ids.size();
                GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"playlistid\": 0, \"position\": "+spotifyStartPosition+" } }, \"id\": 1}", "http://192.168.2.3:8080/jsonrpc");
                GetJsonFromUrl(String.format("{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": 0, \"to\": %s}, \"id\": 1}\u200B",spotifyStartPosition), "http://192.168.2.3:8080/jsonrpc?PlayerGoto");
                //todo: following does NOT work!
                Intent myIntent = new Intent(getThis,
                        WebActivity.class);
                myIntent.putExtra("url", "http://192.168.2.3:8080/#pl");
                getThis.startActivity(myIntent);

        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject GetJsonFromUrl(String data, String urlString) {
        JSONObject  jsonRootObject=null;

        String sb = getJsonStringFromUrl(data, urlString);
        try {
            jsonRootObject = new JSONObject(sb);
            return jsonRootObject.getJSONObject("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            uc.setRequestProperty("Content-Type","application/json");
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
                    Log.v("samba", "line read:" + line);
                    sb.append(line).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }catch (Exception e){e.printStackTrace();}
        return sb.toString();
    }

    public static String LastFMArtist(String artist) {
        //artist.getInfo
        String encArtist = artist;

        String api_key = "07e905eaba54f0d626c2fadcb0fe13f6";//see above; last.fm-key
        String urlString = String.format("http://ws.audioscrobbler.com/2.0/?method=artist.getInfo&artist=%s&api_key=%s&format=json",encArtist, api_key);
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
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();

        }catch (Exception e){e.printStackTrace();}
        return "";
    }
        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            getThis=this;
            setContentView(R.layout.activity_spotify);
        final SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();
            GetSpotifyToken();



            Bundle extras = getIntent().getExtras();
            final String   beatles= extras.getString("artist");


            final ListView albumsListview = (ListView)findViewById(R.id.albums_listview);
        final ArrayAdapter<String> albumAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumList);
        albumsListview.setAdapter(albumAdapter);
            final AdapterView.OnItemClickListener cl=   new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    String s = albumIds.get(position);
                    spotify.getAlbumTracks(s, new Callback<Pager<Track>>() {

                        @Override
                        public void success(Pager<Track> trackPager, Response response) {
                            albumList.clear();
                            ArrayList<String>ids=new ArrayList<String>();
                            for (Track t : trackPager.items) {
                                hm.put(t.id,t);
                                ids.add(t.id);
                                albumList.add(t.name+String.format("(%s)", Mp3File.niceString(new Double(t.duration_ms / 1000).intValue())));
                            }
                            albumAdapter.notifyDataSetChanged();
                            albumsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {

                                    Log.v("samba", "click on "+position); //is your string. do what you want
                                    GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"playlistid\": 0, \"position\": " + (spotifyStartPosition + position) + " } }, \"id\": 1}", "http://192.168.2.3:8080/jsonrpc");
                                };
                            });
                            Utils.setDynamicHeight(albumsListview, 0);
                            if (ids.size()>0)
                                if(spotifyToken.length()==0)GetSpotifyToken();
                            AddSpotifyTrack(getThis,ids,0);
                        }



                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });

                }
            };
        albumsListview.setOnItemClickListener(cl);
        final ListView relatedArtistsListView = (ListView)findViewById(R.id.relatedartists_listview);

        final ArrayAdapter<String> relatedArtistsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, artistList);
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

                            return true;
                        }

                    });

                    return true;
                }
            });
            relatedArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    String s = artistList.get(position);
                    listAlbumsForArtist(api, spotify, s, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
                }
            });
            ImageButton stopButton = (ImageButton) findViewById(R.id.stopspotifyButton);
            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // "Player.GoTo", "params": { "playerid": 0, "to": 20}, "id": 1}​
                    try {
                        GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.stop\", \"params\": { \"playerid\": 0 }, \"id\": 1}", "http://192.168.2.3:8080/jsonrpc?StopPause");//?StopPause
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            //jsonrpc /jsonrpc?PlaylistClear {"jsonrpc": "2.0", "id": 0, "method": "Playlist.Clear", "params": {"playlistid": 0}}
            ImageButton clearButton = (ImageButton) findViewById(R.id.clearspotifyButton);
            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        //yarc.js:906 jsonrpc /jsonrpc?GetRemoteInfos [{"jsonrpc":"2.0","method":"Application.GetProperties","id":1,"params":[["muted"]]},{"jsonrpc":"2.0","method":"Player.GetProperties","id":2,"params":[0,["time", "totaltime", "percentage", "shuffled","repeat"]]},{ "jsonrpc": "2.0", "method": "Player.GetItem", "params": { "playerid": 0, "properties": [ "title", "showtitle", "artist", "thumbnail", "streamdetails", "file", "season", "episode"] }, "id": 3 }
                        // "Player.GoTo", "params": { "playerid": 0, "to": 20}, "id": 1}​
                        GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"id\": 0, \"method\": \"Playlist.Clear\", \"params\": {\"playlistid\": 0}}", "http://192.168.2.3:8080/jsonrpc?PlaylistClear");//
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            //jsonrpc /jsonrpc?PlaylistClear {"jsonrpc": "2.0", "id": 0, "method": "Playlist.Clear", "params": {"playlistid": 0}}
            ImageButton playButton = (ImageButton) findViewById(R.id.playspotifyButton);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        //yarc.js:906 jsonrpc /jsonrpc?GetRemoteInfos [{"jsonrpc":"2.0","method":"Application.GetProperties","id":1,"params":[["muted"]]},{"jsonrpc":"2.0","method":"Player.GetProperties","id":2,"params":[0,["time", "totaltime", "percentage", "shuffled","repeat"]]},{ "jsonrpc": "2.0", "method": "Player.GetItem", "params": { "playerid": 0, "properties": [ "title", "showtitle", "artist", "thumbnail", "streamdetails", "file", "season", "episode"] }, "id": 3 }
                    // "Player.GoTo", "params": { "playerid": 0, "to": 20}, "id": 1}​
                    GetJsonFromUrl("{\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": 0 }, \"id\": 1}", "http://192.168.2.3:8080/jsonrpc?StopPause");//
                } catch (Exception e) {
                    e.printStackTrace();
                }
                }
            });
            ImageButton infoButton = (ImageButton) findViewById(R.id.infospotifyButton);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        String s=getJsonStringFromUrl("[{\"jsonrpc\":\"2.0\",\"method\":\"Application.GetProperties\",\"id\":1,\"params\":[[\"muted\"]]},{\"jsonrpc\":\"2.0\",\"method\":\"Player.GetProperties\",\"id\":2,\"params\":[0,[\"time\", \"totaltime\", \"percentage\", \"shuffled\",\"repeat\",\"speed\"]]}," +
                                "{ \"jsonrpc\": \"2.0\", \"method\": \"Player.GetItem\", \"params\": { \"playerid\": 0, \"properties\": [ \"title\", \"showtitle\", \"artist\", \"thumbnail\", \"streamdetails\", \"file\", \"season\", \"episode\"] }, \"id\": 3 }]", "http://192.168.2.3:8080/jsonrpc?GetRemoteInfos");//
                        //result:[{"id":1,"jsonrpc":"2.0","result":{"muted":false}},{"id":2,"jsonrpc":"2.0","result":{"percentage":85.810699462890625,"repeat":"off","shuffled":false,"speed":1,"time":{"hours":0,"milliseconds":493,"minutes":3,"seconds":4},"totaltime":{"hours":0,"milliseconds":0,"minutes":3,"seconds":35}}},{"id":3,"jsonrpc":"2.0","result":{"item":{"artist":[],"file":"http://127.0.0.1:8081/track/4btX6FOX75h0D40m8eQPAe.wav|X-Spotify-Token=d46cdae3fb885e0c3bd450a053c95916028f9790&User-Agent=Spotlight+1.0","label":"4btX6FOX75h0D40m8eQPAe.wav","thumbnail":"image://DefaultAlbumCover.png/","title":"4btX6FOX75h0D40m8eQPAe.wav","type":"song"}}}]
                        //speed=0:paused or not playing
                        JSONArray jsonRootObject=null;
                        try {
                            jsonRootObject = new JSONArray(s);
                            String fname=jsonRootObject.getJSONObject(2).getJSONObject("result").getJSONObject("item").getString("file");
                            int startIndex = fname.indexOf("track/")+6;
                            int endIndex = fname.indexOf(".wav");
                            Log.v("samba",fname.substring(startIndex, endIndex));
                            Track t= (Track)hm.get(fname.substring(startIndex, endIndex));

                            Double d=jsonRootObject.getJSONObject(1).getJSONObject("result").getDouble("percentage");
                            AlertDialog.Builder builder = new AlertDialog.Builder(getThis);
                            builder.setMessage("percentage:"+d.toString()+"-"+t.artists.get(0).name+"-"+t.name).setCancelable(//
                                    false).setPositiveButton("Yes",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).setNegativeButton("No",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        Utils.setDynamicHeight(albumsListview,0);
        Utils.setDynamicHeight(relatedArtistsListView,0);

        listAlbumsForArtist(api, spotify, beatles, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                albumsListview.setOnItemClickListener(cl);
                listAlbumsForArtist(api, spotify, beatles, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
            }
        });

    }

    private void setArtistText(final String beatles, Image image) {
        final TextView artistTitleTextView = (TextView)findViewById(R.id.artist_title);

        artistTitleTextView.setText(beatles);

        String artistText="";

        try {
            JSONObject artist = (new JSONObject(LastFMArtist(beatles))).getJSONObject("artist");

            artistText = artist.getJSONObject("bio").getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SpannableString SS = new SpannableString (artistText);

        int scale=250;
        int leftMargin= scale + 10 ;

        //Set the icon in R.id.icon
        ImageView icon =(ImageView)findViewById(R.id.icon2 ) ;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(scale,scale);
        icon.setLayoutParams(layoutParams);
        try {
            ImageView i = (ImageView)findViewById(R.id.image);
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(image.url).getContent());
            icon.setImageBitmap(bitmap);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        MessageView = ( TextView ) findViewById ( R.id.artist_content ) ;        //Expose the indent for the first three rows
        SS.setSpan(new MyLeadingMarginSpan2(scale/50,leftMargin),0,SS.length(),0);
        MessageView.setText(SS ) ;
    }

    private void listAlbumsForArtist(final SpotifyApi api, SpotifyService spotify, final String beatles, final ListView albumsListview, final ListView relatedArtistsListView, final ArrayAdapter<String> albumAdapter, final ArrayAdapter<String> relatedArtistsAdapter) {
        spotify.searchArtists(beatles, new Callback<ArtistsPager>() {

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                String id = "";
                int max = 10000;
                Image image = null;
                for (Artist artist : artistsPager.artists.items) {
                    String name = artist.name;
                    if (name.startsWith("The ")) name = name.substring(4);
                    if (name.toLowerCase().contains(beatles.toLowerCase())) {

                        if (name.length() < max) {
                            id = artist.id;
                            image=artist.images.get(0);
                            max = name.length();
                        }
                    }


                }

                setArtistText(beatles,image);
                SpotifyService spotify = api.getService();
                spotify.getArtistAlbums(id, new Callback<Pager<Album>>() {

                    @Override
                    public void success(Pager<Album> albumPager, Response response) {
                        albumList.clear();
                        albumIds.clear();
                        String previous = "";
                        for (Album album : albumPager.items)
                            if (!album.name.equals(previous)) {

                                albumList.add(album.name);
                                albumIds.add(album.id);
                                previous = album.name;

                            }
                        albumAdapter.notifyDataSetChanged();
                        Utils.setDynamicHeight(albumsListview,0);

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
                        Utils.setDynamicHeight(relatedArtistsListView,0);
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
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
            }
            else
            {
                //Offset for all other Layout layout ) { }
       /*Returns * the number of rows which should be  applied *         indent returned by getLeadingMargin (true)
        * Note:* Indent only applies to N lines of the first paragraph.*/

                return 0;
            }
        }

        @Override
        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                      int top, int baseline, int bottom, CharSequence text,
                                      int start, int end, boolean first, Layout layout) {}
        @Override
        public int getLeadingMarginLineCount() {
            return lines;
        }
    };


}
