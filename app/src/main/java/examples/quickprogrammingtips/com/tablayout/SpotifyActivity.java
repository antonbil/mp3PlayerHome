package examples.quickprogrammingtips.com.tablayout;
/*Last.fm account details
Here are the details of your new API account.
Application name	musicplayer
API key	07e905eaba54f0d626c2fadcb0fe13f6
Shared secret	378d85061b039f1f8ba34075e2eac11c
Registered to	rockingbilly
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class SpotifyActivity extends AppCompatActivity {
    private ArrayList<String>artistList=new ArrayList<>();
    private ArrayList<String>albumIds=new ArrayList<>();
    private ArrayList<String>albumList=new ArrayList<>();
    private TextView MessageView;
    //private ListView albumsListview, relatedArtistsListView;
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
        setContentView(R.layout.activity_spotify);
        final SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();



            Bundle extras = getIntent().getExtras();
            final String   beatles= extras.getString("artist");


            final ListView albumsListview = (ListView)findViewById(R.id.albums_listview);
        final ArrayAdapter<String> albumAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albumList);
        albumsListview.setAdapter(albumAdapter);
        albumsListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String s = albumIds.get(position);
                spotify.getAlbumTracks(s, new Callback<Pager<Track>>() {

                    @Override
                    public void success(Pager<Track> trackPager, Response response) {
                        albumList.clear();
                        for (Track t : trackPager.items) {
                            albumList.add(t.name+String.format("(%s)", Mp3File.niceString(new Double(t.duration_ms / 1000).intValue())));
                        }
                        albumAdapter.notifyDataSetChanged();
                        Utils.setDynamicHeight(albumsListview,0);
                    }



                    @Override
                    public void failure(RetrofitError error) {

                    }
                });

            }
        });
        final ListView relatedArtistsListView = (ListView)findViewById(R.id.relatedartists_listview);

        final ArrayAdapter<String> relatedArtistsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, artistList);
        relatedArtistsListView.setAdapter(relatedArtistsAdapter);
        relatedArtistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String s = artistList.get(position);
                listAlbumsForArtist(api, spotify, s, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
            }
        });

        Utils.setDynamicHeight(albumsListview,0);
        Utils.setDynamicHeight(relatedArtistsListView,0);

        listAlbumsForArtist(api, spotify, beatles, albumsListview, relatedArtistsListView, albumAdapter, relatedArtistsAdapter);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
