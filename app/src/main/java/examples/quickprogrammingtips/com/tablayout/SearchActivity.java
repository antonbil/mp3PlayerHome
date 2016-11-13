package examples.quickprogrammingtips.com.tablayout;

import android.app.ProgressDialog;
import android.content.Context;
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
    public static SearchActivity getThis;
    SpotifyService spotify;
    private ListAdapter customAdapter;
    private ProgressDialog loadingdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getThis=this;
        setContentView(R.layout.activity_new_albums);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);

        customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
        yourListView.setAdapter(customAdapter);
        loadingdialog = ProgressDialog.show(this,
                "","Loading, please wait",true);
        final Handler handler = new Handler();
        generateList(newAlbums);
        /*handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyChange();
            }
        }, 1000);*/

    }

    public void notifyChange() {
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

    public void generateList(ArrayList<SearchItem> newAlbums) {
        MainActivity.getInstance().fillListviewWithValues.generateListSearch(newAlbums);

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
                    MainActivity.getInstance().imageLoader.DisplayImage(p.imageid, bitmap -> {
                                getThis.runOnUiThread(() -> {
                                    image.setImageBitmap(bitmap);
                                    image.setOnClickListener(v -> MainActivity.displayLargeImage(getThis, p.imageid));
                                });
                            });
                /*new DownLoadImageTask() {
                    @Override
                    public void setImage(final Bitmap logo) {
                        image.setImageBitmap(logo);
                        image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MainActivity.displayLargeImage(getThis, p.imageid);
                            }
                        });
                    }
                }.execute(p.imageid);*/

            //}

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processAlbum(items.get(position));

                }
            });

                    return rowView;
        }    }
    public void processAlbum(SearchItem album){
        MainActivity.getInstance().fillListviewWithValues.processAlbum(album);
        //SpotifyActivity.getThis.listAlbumsForArtistId(album.id, null, album.artist, new SpotifyApi());
        //SpotifyActivity.getThis.listAlbumsForArtist(album.artist);
        this.finish();
        //Toast.makeText(MainActivity.getThis, "return:"+album.url.replace("spotify:album:",""); Toast.LENGTH_SHORT).show();

    };
}
