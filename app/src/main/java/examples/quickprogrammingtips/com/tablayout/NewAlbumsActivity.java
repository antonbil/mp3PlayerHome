package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class NewAlbumsActivity extends Activity {
    ArrayList<NewAlbum> newAlbums=new ArrayList<>();
    Activity getThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = -100;
        params.height = 1000;
        params.width = 800;
        params.y = -50;

        this.getWindow().setAttributes(params);

        getThis=this;
        setContentView(R.layout.activity_new_albums);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);
        final FloatingActionButton fab = (FloatingActionButton)

                findViewById(R.id.fabspotifylist);
        fab.setOnClickListener(view -> SpotifyActivity.showPlayMenu(this,fab));

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
                    runOnUiThread(() -> {
                        loadingdialog.dismiss();
                        customAdapter.notifyDataSetChanged();

                    });
                }catch(Exception e){
                    loadingdialog.dismiss();

                    Log.v("samba", Log.getStackTraceString(e));}
            }
        };

        task.start();


    }
    public void generateList(ArrayList<NewAlbum> newAlbums){

        try {
            Document doc = Jsoup.connect("http://everynoise.com/spotify_new_albums.html").get();

            Elements trackelements = doc.getElementsByClass("album");
            //ArrayList<String> ids = new ArrayList<String>();
            for (Element element : trackelements) {
                Elements links = element.select("a[href]"); // a with href
                String s = links.get(0).attr("href");

                String artist=element.getElementsByClass("creditartist").text();
                String album=element.getElementsByClass("creditrelease").text().replace("<i>","").replace("</i>","");
                //ids.add(artist+"-"+album);
                newAlbums.add(new NewAlbum(s,artist,album));
            }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }
     public class ListAdapter extends ArrayAdapter<NewAlbum> {
        private Context context;
        ArrayList<NewAlbum> items;

        /*public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }*/

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
                        image.setOnClickListener(v -> {
                            PopupMenu menu = new PopupMenu(v.getContext(), v);

                            menu.setOnMenuItemClickListener(item -> {
                                if (item.getTitle().toString().equals("add album to favorites")) {
                                    SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                }else
                                if (item.getTitle().toString().equals("add album")) {
                                    AddAlbumToPlaylist(position);
                                }else
                                if (item.getTitle().toString().equals("large image")) {
                                    MainActivity.displayLargeImage(getThis, logo);

                                }else
                                if (item.getTitle().toString().equals("wikipedia")) {
                                    MainActivity.startWikipediaPage(items.get(position).artist);
                                }
                                return true;
                            });

                            menu.getMenu().add("add album to favorites");
                            menu.getMenu().add("add album");
                            menu.getMenu().add("large image");
                            menu.getMenu().add("wikipedia");
                            menu.show();
                        });
                    }
                }.execute(p.getImage());

            //}

            rowView.setOnClickListener(v -> processAlbum(items.get(position)));
            image.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(v.getContext(), v);

                basicMenu(position, menu);
                menu.show();
            });
            rowView.setOnLongClickListener(v -> {
                PopupMenu menu = new PopupMenu(v.getContext(), v);

                        basicMenu(position, menu);
                menu.show();
                return true;
            }
            );
                    return rowView;
        }

         private void basicMenu(int position, PopupMenu menu) {
             menu.setOnMenuItemClickListener(item -> {
                 if (item.getTitle().toString().equals("add album")) {
                     AddAlbumToPlaylist(position);
                 }else
                 if (item.getTitle().toString().equals("add album to favorites")) {
                     SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                 }else
                 if (item.getTitle().toString().equals("wikipedia")) {
                     MainActivity.startWikipediaPage(items.get(position).artist);
                 }
                 return true;
             });

             menu.getMenu().add("add album to favorites");
             menu.getMenu().add("add album");
             menu.getMenu().add("wikipedia");
         }

         public void AddAlbumToPlaylist(int position) {
             String uri = items.get(position).url.replace("spotify:album:", "");
             String prefix="spotify:album:";
             SpotifyActivity.AddSpotifyItemToPlaylist(prefix, uri);
             SpotifyActivity.refreshPlaylistFromSpotify(1, SpotifyActivity.getThis.albumAdapter,SpotifyActivity.getThis.getActivity(),SpotifyActivity.albumList,SpotifyActivity.albumTracks);
         }
     }
    public void processAlbum(NewAlbum album){
        SpotifyActivity.artistName=album.artist;
        //Toast.makeText(MainActivity.getThis, "return:"+album.url.replace("spotify:album:",""); Toast.LENGTH_SHORT).show();
        SpotifyActivity.getAlbumtracksFromSpotify(album.url.replace("spotify:album:",""), album.album,this, null, null);

    }
}
