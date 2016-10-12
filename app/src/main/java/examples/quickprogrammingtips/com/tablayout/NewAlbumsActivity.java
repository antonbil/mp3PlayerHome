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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;

public class NewAlbumsActivity extends Activity  {
    ArrayList<NewAlbum> newAlbums=new ArrayList<>();
    static Activity getThis;
    public ListAdapter customAdapter;
    private LeftDrawerPlaylist leftDrawerPlaylist;
    private ProgressDialog loadingdialog;

    @Override
    protected void onStop() {
        leftDrawerPlaylist.onStop();
        MainActivity.getThis.runOnUiThread(() -> {
            //SpotifyFragment.getThis.albumAdapter.setDisplayCurrentTrack(true);
            try{
                SpotifyFragment.getThis.albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
            });


        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
        super.onCreate(savedInstanceState);
            //Log.v("samba","1a");
            //Log.v("samba","5a");
        getThis=this;
            Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this,
                    MainActivity.class));
        setContentView(R.layout.activity_new_albums);
            //Log.v("samba","2a");
        final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabspotifylist);
        fab.setOnClickListener(view -> SpotifyFragment.showPlayMenu(this));
            ((ImageView) findViewById(R.id.thumbnail_top)).setOnClickListener(view -> SpotifyFragment.showPlayMenu(this));
            //Log.v("samba","3a");
            ArrayList<String> menuItemsArray = new ArrayList<String>(
                    Arrays.asList("Settings",
                            "sep","Play-Dialog","sep","Close" ));
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
                            MainActivity.getThis.doSettings();
                            break;
                        case "Play-Dialog":
                            SpotifyFragment.showPlayMenu(getThis);
                            break;
                        case "Close":
                            getThis.finish();
                            break;
                    }
                }
            };
            leftDrawerPlaylist.setMenu(menuItemsArray);

            loadingdialog = ProgressDialog.show(this,"","Loading, please wait",true);
         customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
        Thread task = new Thread()
        {
            @Override
            public void run()
            {
                //Log.v("samba","7a");
                yourListView.setAdapter(customAdapter);
                //Log.v("samba","8a");
                try{
                    //Log.v("samba","9a");
                    runOnUiThread(() -> {
                        try{
                            generateList(newAlbums);
                            loadingdialog.dismiss();
                            //Log.v("samba","10a");
                            customAdapter.notifyDataSetChanged();
                            //Log.v("samba","11a");
                    }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}

                    });
                }catch(Exception e){
                    Log.v("samba", Log.getStackTraceString(e));}
            }
        };

            //Log.v("samba","6a");
        task.start();
    }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
    }

    private void setRightDrawer() {
    }

    public void generateList(ArrayList<NewAlbum> newAlbums){

        try {
            Document doc = Jsoup.connect("http://everynoise.com/spotify_new_albums.html").get();

            Elements trackelements = doc.getElementsByClass("album");
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
                                    SpotifyFragment.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                }else
                                if (item.getTitle().toString().equals("add album")) {
                                    AddAlbumToPlaylist(position);
                                }else
                                if (item.getTitle().toString().equals("large image")) {
                                    MainActivity.displayLargeImage(getThis, logo);

                                }else
                                if (item.getTitle().toString().equals("play")) {
                                    SpotifyFragment.showPlayMenu(getThis);
                                }else
                                if (item.getTitle().toString().equals("wikipedia")) {
                                    MainActivity.startWikipediaPage(items.get(position).artist);
                                }else
                                if (item.getTitle().toString().equals("finish")) {
                                    SpotifyFragment.categoriesMenu.dismiss();
                                    finish();
                                }
                                return true;
                            });

                            menu.getMenu().add("add album to favorites");
                            menu.getMenu().add("add album");
                            menu.getMenu().add("large image");
                            menu.getMenu().add("wikipedia");
                            menu.getMenu().add("play");//
                            menu.getMenu().add("finish");//SpotifyFragment.showPlayMenu(this,fab)
                            menu.show();
                        });
                    }
                }.execute(p.getImage());

            //}

            rowView.setOnClickListener(v -> {processAlbum(items.get(position));});
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

         private PopupMenu basicMenu(int position, PopupMenu menu) {
             menu.setOnMenuItemClickListener(item -> {
                 if (!SpotifyFragment.getThis.fillListviewWithValues.processChoice(item.getTitle().toString(),this,items,position))
                 if (item.getTitle().toString().equals("add album")) {
                     AddAlbumToPlaylist(position);
                 }else
                 if (item.getTitle().toString().equals("add album to favorites")) {
                     SpotifyFragment.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                 }else
                 if (item.getTitle().toString().equals("wikipedia")) {
                     MainActivity.startWikipediaPage(items.get(position).artist);
                 }
                 return true;
             });

             ArrayList<String>choices=SpotifyFragment.getThis.fillListviewWithValues.getChoices();
             for (String s:choices){
                 menu.getMenu().add(s);
             }
             menu.getMenu().add("add album to favorites");
             menu.getMenu().add("add album");
             menu.getMenu().add("wikipedia");
             return menu;
         }

         public void AddAlbumToPlaylist(int position) {
             String uri = items.get(position).url.replace("spotify:album:", "");
             String prefix="spotify:album:";
             SpotifyFragment.AddSpotifyItemToPlaylist(prefix, uri);
             SpotifyFragment.refreshPlaylistFromSpotify(1, SpotifyFragment.getThis.albumAdapter, SpotifyFragment.getThis.getActivity(), SpotifyFragment.albumList, SpotifyFragment.albumTracks);
         }
     }
    public void processAlbum(NewAlbum album){
        SpotifyFragment.artistName=album.artist;
        //Toast.makeText(MainActivity.getThis, "return:"+album.url.replace("spotify:album:",""); Toast.LENGTH_SHORT).show();
        SpotifyFragment.getAlbumtracksFromSpotify(album.url.replace("spotify:album:",""), album.album,this, null, null);

    }

}
