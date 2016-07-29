package examples.quickprogrammingtips.com.tablayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
    AppCompatActivity getThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getThis=this;
        setContentView(R.layout.activity_new_albums);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);
        final FloatingActionButton fab = (FloatingActionButton)

                findViewById(R.id.fabspotifylist);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SpotifyActivity.showPlayMenu(getThis,fab);
            }
        });

        final ListAdapter customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
        final ProgressDialog loadingdialog;
        loadingdialog = ProgressDialog.show(this,
                "","Loading, please wait",true);
        Thread task = new Thread()
        {
            @Override
            public void run()
            {
                yourListView.setAdapter(customAdapter);
                try{
                generateList(newAlbums);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            loadingdialog.dismiss();
                            customAdapter.notifyDataSetChanged();

                        }
                    });
                }catch(Exception e){
                    loadingdialog.dismiss();

                    Log.v("samba", Log.getStackTraceString(e));}
            }
        };

        task.start();


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

            //if (p != null) {
                TextView tt1 = (TextView) rowView.findViewById(R.id.artistname);
                TextView tt2 = (TextView) rowView.findViewById(R.id.albumname);
                final ImageView image = (ImageView) rowView.findViewById(R.id.spotifylistimageView1);

                if (tt1 != null) {
                    tt1.setText(p.artist);
                }

                if (tt2 != null) {
                    tt2.setText(p.album);
                }
                if (p.getImage().length()>0)
                new DownLoadImageTask() {
                    @Override
                    public void setImage(final Bitmap logo) {
                        image.setImageBitmap(logo);
                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PopupMenu menu = new PopupMenu(v.getContext(), v);

                                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        if (item.getTitle().toString().equals("add album to favorites")) {
                                            SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                        }
                                        if (item.getTitle().toString().equals("large image")) {
                                            MainActivity.displayLargeImage(getThis, logo);

                                        }
                                        return true;
                                    }
                                });

                                menu.getMenu().add("add album to favorites");//submenu
                                menu.getMenu().add("large image");//submenu
                                menu.show();
                            }
                        });
                    }
                }.execute(p.getImage());

            //}

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processAlbum(items.get(position));

                }
            });
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v);

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().toString().equals("add album to favorites")) {
                                SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                            }
                            return true;
                        }
                    });

                    menu.getMenu().add("add album to favorites");//submenu
                    menu.show();
                }
            });
            rowView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v);

                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().toString().equals("add album to favorites")) {
                                    SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                }
                                return true;
                            }
                        });

                        menu.getMenu().add("add album to favorites");//submenu
                    menu.show();
                    return true;
                }
                                           }
            );
                    return rowView;
        }    }
    public void processAlbum(NewAlbum album){
        SpotifyActivity.getThis.artistName=album.artist;
        //Toast.makeText(MainActivity.getThis, "return:"+album.url.replace("spotify:album:",""); Toast.LENGTH_SHORT).show();
        SpotifyActivity.getThis.getAlbumtracksFromSpotify(album.url.replace("spotify:album:",""), album.album,this, null, null);

    };
}
