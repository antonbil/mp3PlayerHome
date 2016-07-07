package examples.quickprogrammingtips.com.tablayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyService;

public class SearchActivity extends AppCompatActivity {
    //public static String artistName="Abba";
    ArrayList<SearchItem> newAlbums=new ArrayList<>();
    AppCompatActivity getThis;
    SpotifyService spotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getThis=this;
        setContentView(R.layout.activity_new_albums);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);

        final ListAdapter customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
        yourListView.setAdapter(customAdapter);
        final ProgressDialog loadingdialog;
        loadingdialog = ProgressDialog.show(this,
                "","Loading, please wait",true);
        final Handler handler = new Handler();
        generateList(newAlbums);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Thread task = new Thread()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                loadingdialog.dismiss();
                                customAdapter.notifyDataSetChanged();

                            }
                        });            }
                };

                task.start();
            }
        }, 1000);

    }
    public void generateList(ArrayList<SearchItem> newAlbums) {
        SpotifyActivity.getThis.fillListviewWithValues.generateListSearch(newAlbums);

    }
     public class ListAdapter extends ArrayAdapter<SearchItem> {
        private Context context;
        ArrayList<SearchItem> items;

        public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public ListAdapter(Context context, int resource, ArrayList<SearchItem> items) {
            super(context, resource, items);
            this.items=items;
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_newalbum, parent, false);
            SearchItem p = items.get(position);

            //if (p != null) {
                TextView tt1 = (TextView) rowView.findViewById(R.id.artistname);
                TextView tt2 = (TextView) rowView.findViewById(R.id.albumname);
                final ImageView image = (ImageView) rowView.findViewById(R.id.spotifylistimageView1);

            Log.v("samba",p.artist);
                if (tt1 != null) {
                    tt1.setText(p.artist);
                }

                if (tt2 != null) {
                    tt2.setText(p.title);
                }
                if (p.imageid.length()>0)
                new SpotifyActivity.DownLoadImageTask() {
                    @Override
                    public void setImage(final Bitmap logo) {
                        image.setImageBitmap(logo);
                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.displayLargeImage(getThis, logo);
                                /*PopupMenu menu = new PopupMenu(v.getContext(), v);

                                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        if (item.getTitle().toString().equals("add album to favorites")) {
                                            //SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                        }
                                        if (item.getTitle().toString().equals("large image")) {
                                            MainActivity.displayLargeImage(getThis, logo);

                                        }
                                        return true;
                                    }
                                });

                                menu.getMenu().add("add album to favorites");//submenu
                                menu.getMenu().add("large image");//submenu
                                menu.show();*/
                            }
                        });
                    }
                }.execute(p.imageid);

            //}

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processAlbum(items.get(position));

                }
            });
            /*image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v);

                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getTitle().toString().equals("add album to favorites")) {
                                //SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                            }
                            return true;
                        }
                    });

                    menu.getMenu().add("add album to favorites");//submenu
                    menu.show();
                }
            });*/
            /*rowView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v);

                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getTitle().toString().equals("add album to favorites")) {
                                    //SpotifyActivity.getThis.fillListviewWithValues.addToFavorites(items.get(position));

                                }
                                return true;
                            }
                        });

                        menu.getMenu().add("add album to favorites");//submenu
                    menu.show();
                    return true;
                }
                                           }
            );*/
                    return rowView;
        }    }
    public void processAlbum(SearchItem album){
        SpotifyActivity.getThis.fillListviewWithValues.processAlbum(album);
        //SpotifyActivity.getThis.listAlbumsForArtistId(album.id, null, album.artist, new SpotifyApi());
        //SpotifyActivity.getThis.listAlbumsForArtist(album.artist);
        this.finish();
        //Toast.makeText(MainActivity.getThis, "return:"+album.url.replace("spotify:album:",""); Toast.LENGTH_SHORT).show();

    };
}
