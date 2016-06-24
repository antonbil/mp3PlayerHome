package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class NewAlbumsActivity extends AppCompatActivity {
    ArrayList<NewAlbum> newAlbums=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_albums);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        generateList(newAlbums);
        ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);

        ListAdapter customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);

        yourListView.setAdapter(customAdapter);
        customAdapter.notifyDataSetChanged();

    }
    public void generateList(ArrayList<NewAlbum> newAlbums){
        Document doc = null;
        try {
            doc = Jsoup.connect("http://everynoise.com/spotify_new_albums.html").get();
        } catch (IOException e) {
            Log.v("samba", Log.getStackTraceString(e));
        }

        Elements trackelements = doc.getElementsByClass("album");;
        //ArrayList<String> ids = new ArrayList<String>();
        for (Element element : trackelements) {
            Elements links = element.select("a[href]"); // a with href
            String s = links.get(0).attr("href");

            String artist=element.getElementsByClass("creditartist").text();
            String album=element.getElementsByClass("creditrelease").text().replace("<i>","").replace("</i>","");
            //ids.add(artist+"-"+album);
            newAlbums.add(new NewAlbum(s,artist,album));
        }
    };
    class NewAlbum {
        private String image;
        public String url,artist,album;
        public NewAlbum(String url,String artist,String album){
            this(url,artist,album,"");

        }

        public NewAlbum(String url, String artist, String album, String image) {
            this.url=url;
            this.artist=artist;
            this.album=album;
            this.setImage(image);
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
    public class ListAdapter extends ArrayAdapter<NewAlbum> {
        private Context context;
        ArrayList<NewAlbum> items;

        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, ArrayList<NewAlbum> items) {
            super(context, resource, items);
            this.items=items;
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_newalbum, parent, false);
            NewAlbum p = items.get(position);

            if (p != null) {
                TextView tt1 = (TextView) rowView.findViewById(R.id.artistname);
                TextView tt2 = (TextView) rowView.findViewById(R.id.albumname);

                if (tt1 != null) {
                    tt1.setText(p.artist);
                }

                if (tt2 != null) {
                    tt2.setText(p.album);
                }

            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processAlbum(items.get(position));

                    /*String url=items.get(position).url;
                    Intent i = getIntent(); //get the intent that has been called, i.e you did called with startActivityForResult();
                    i.putExtra("artist",items.get(position).artist);
                    i.putExtra("album",items.get(position).album);
                    i.putExtra("url",url);
                    //i.putExtras(b);//put some data, in a bundle
                    setResult(441, i);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode
                    finish(); //finish the startNewOne activity*/
                }
            });
            return rowView;
        }    }
    public void processAlbum(NewAlbum album){
        SpotifyActivity.getThis.artistName=album.artist;
        //Toast.makeText(MainActivity.getThis, "return:"+album.url.replace("spotify:album:",""); Toast.LENGTH_SHORT).show();
        SpotifyActivity.getThis.getAlbumtracksFromSpotify(album.url.replace("spotify:album:",""), album.album);

    };
}
