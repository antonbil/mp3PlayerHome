package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
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

public class NewAlbumsActivity extends Activity {
    ArrayList<NewAlbum> newAlbums=new ArrayList<>();
    static Activity getThis;
    public ListAdapter customAdapter;

    @Override
    protected void onStop() {
        MainActivity.getThis.runOnUiThread(() -> {
            //SpotifyFragment.getThis.albumAdapter.setDisplayCurrentTrack(true);
            try{
                SpotifyFragment.getThis.albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
            });


        super.onStop();
        Log.v("samba", "stop");

    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = -100;
        params.height = 1000;
        params.width = 800;
        params.y = -50;

        this.getWindow().setAttributes(params);*/

        getThis=this;
        setContentView(R.layout.activity_new_albums);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);
        final FloatingActionButton fab = (FloatingActionButton)

                findViewById(R.id.fabspotifylist);
        fab.setOnClickListener(view -> SpotifyFragment.showPlayMenu(this,fab));

        Log.d("samba", "Text:7");
        customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
        final ProgressDialog loadingdialog;
        loadingdialog = ProgressDialog.show(this,
                "","Loading, please wait",true);
        Log.d("samba", "Text:8");
        Thread task = new Thread()
        {
            @Override
            public void run()
            {
                Log.d("samba", "Text:9");
                yourListView.setAdapter(customAdapter);
                try{
                    Log.d("samba", "Text:10");
                generateList(newAlbums);
                    Log.d("samba", "Text:11");
                    runOnUiThread(() -> {
                        try{
                            Log.d("samba", "Text:12");
                        loadingdialog.dismiss();
                        customAdapter.notifyDataSetChanged();
                            Log.d("samba", "Text:13");
                            try {
                                getDrawerLayout();
                            }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
                    }   catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}

                    });
                    Log.v("samba", "after generateList");
                }catch(Exception e){
                    //loadingdialog.dismiss();

                    Log.v("samba", Log.getStackTraceString(e));}
            }
        };

        task.start();
        Log.v("samba", "task start");


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
                                    SpotifyFragment.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                }else
                                if (item.getTitle().toString().equals("add album")) {
                                    AddAlbumToPlaylist(position);
                                }else
                                if (item.getTitle().toString().equals("large image")) {
                                    MainActivity.displayLargeImage(getThis, logo);

                                }else
                                if (item.getTitle().toString().equals("play")) {
                                    SpotifyFragment.showPlayMenu(getThis,image);
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
    public DrawerLayout getDrawerLayout() {
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.newalbumsdrawer_layout);

        //define how to handle right drawer
        Log.d("samba", "Text:3");


        Log.d("samba", "Text:4");
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.hello_world,
                R.string.hello_world
        ) {
            PlanetAdapter albumAdapter;

            public void onDrawerClosed(View view) {
                albumAdapter = null;
            }

            public void onDrawerOpened(View drawerView) {
                ListView albumsListview = (ListView) findViewById(R.id.newalbumsdrawer_list);
                ArrayList<String> albumList = new ArrayList<>();
                ArrayList<PlaylistItem> albumTracks = new ArrayList<>();
                albumAdapter=MainActivity.getTracksAdapter(mDrawerLayout,albumsListview, albumList, albumTracks);
                albumsListview.setAdapter(albumAdapter);
                SpotifyFragment.checkAddress();
                SpotifyFragment.refreshPlaylistFromSpotify(1, albumAdapter, MainActivity.getThis, albumList, albumTracks);

            }

        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        return mDrawerLayout;
    }

}
