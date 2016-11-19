package examples.quickprogrammingtips.com.tablayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import kaaes.spotify.webapi.android.SpotifyService;

public class SearchActivity extends AppCompatActivity {
    ArrayList<SearchItem> newAlbums = new ArrayList<>();
    public static SearchActivity getThis;
    SpotifyService spotify;
    private ListAdapter customAdapter;
    private ProgressDialog loadingdialog;
    protected ArrayList<String> menuItemsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getThis = this;
            setContentView(R.layout.activity_new_albums);

            menuItemsArray = new ArrayList<>(
                    Arrays.asList("Settings",
                            "sep", "Close"));
            LeftDrawerPlaylist leftDrawerPlaylist = new LeftDrawerPlaylist(this, /*this,*/ R.id.newalbumsdrawer_layout, R.id.newalbumsdrawer_list,
                    R.id.newalbumsmpddrawer_list, R.id.fabswapplaylist) {

                @Override
                public void performTouchEvent(MotionEvent event) {

                }

                @Override
                public void performClickOnRightDrawer() {

                }

                @Override
                protected void doMenuAction(int position) {
                    String s = menuItemsArray.get(position);
                    switch (s) {
                        case "Settings":
                            MainActivity.getInstance().doSettings();
                            break;
                        case "Close":
                            getThis.finish();
                            break;
                    }
                }
            };
            leftDrawerPlaylist.setMenu(menuItemsArray);


            final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);

            customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
            yourListView.setAdapter(customAdapter);
            loadingdialog = ProgressDialog.show(this,
                    "", "Loading, please wait", true);
            generateList(newAlbums);

        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
            //e.printStackTrace();
        }

    }

    public void notifyChange() {
        Thread task = new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    loadingdialog.dismiss();
                    customAdapter.notifyDataSetChanged();

                });
            }
        };

        task.start();
    }

    public void generateList(ArrayList<SearchItem> newAlbums) {
        MainActivity.getInstance().fillListviewWithValues.generateListSearch(newAlbums);

    }

    public class ListAdapter extends ArrayAdapter<SearchItem> {
        private Context context;
        ArrayList<SearchItem> items;

        /*public ListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }*/

        ListAdapter(Context context, int resource, ArrayList<SearchItem> items) {
            super(context, resource, items);
            this.items = items;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_newalbum, parent, false);
            SearchItem p = items.get(position);

            //if (p != null) {
            TextView tt1 = (TextView) rowView.findViewById(R.id.artistname);
            TextView tt2 = (TextView) rowView.findViewById(R.id.albumname);
            final ImageView image = (ImageView) rowView.findViewById(R.id.spotifylistimageView1);

            //Log.v("samba", p.artist);
            if (tt1 != null) {
                tt1.setText(p.artist);
            }

            if (tt2 != null) {
                tt2.setText(p.title);
            }
            if (p.imageid.length() > 0)
                MainActivity.getInstance().imageLoader.DisplayImage(p.imageid, bitmap -> {
                    getThis.runOnUiThread(() -> {
                        image.setImageBitmap(bitmap);
                        image.setOnClickListener(v -> MainActivity.displayLargeImage(getThis, p.imageid));
                    });
                });

            rowView.setOnClickListener(v -> processAlbum(items.get(position)));

            return rowView;
        }
    }

    public void processAlbum(SearchItem album) {
        MainActivity.getInstance().fillListviewWithValues.processAlbum(album);
        this.finish();
    }
}
