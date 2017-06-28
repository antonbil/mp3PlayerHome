package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;

import kaaes.spotify.webapi.android.models.Track;

public class NewAlbumsActivity extends Activity  {
    ArrayList<NewAlbum> newAlbums=new ArrayList<>();
    static Activity getThis;
    public ListAdapter customAdapter;
    private LeftDrawerPlaylist leftDrawerPlaylist;
    private ProgressDialog loadingdialog;
    protected ArrayList<String> menuItemsArray;

    @Override
    protected void onStop() {
        leftDrawerPlaylist.onStop();
        SpotifyPlaylistFragment.notifyList();
        super.onStop();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            getThis = this;
            Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this
            ));
            setContentView(R.layout.activity_new_albums);

            final ListView yourListView = (ListView) findViewById(R.id.newalbums_listview);

            findViewById(R.id.thumbnail_top).setOnClickListener(view -> SpotifyFragment.showPlayMenu(this));
            menuItemsArray = new ArrayList<>(
                    Arrays.asList("Settings",
                            "sep", "Play-Dialog", "sep", "Close"));

            leftDrawerPlaylist = new LeftDrawerPlaylist(this, /*this,*/ R.id.newalbumsdrawer_layout, R.id.newalbumsdrawer_list,
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
                    if (s.startsWith("http")) {
                        doAction(s);
                    } else {
                        switch (s) {
                            case "Settings":
                                MainActivity.getInstance().doSettings();
                                break;
                            case "Play-Dialog":
                                SpotifyFragment.showPlayMenu(getThis);
                                break;
                            case "Close":
                                getThis.finish();
                                break;
                        }
                    }
                }
            };
            leftDrawerPlaylist.setMenu(menuItemsArray);

            loadingdialog = ProgressDialog.show(this, "", "Loading" + getText() + ", please wait", true);
            customAdapter = new ListAdapter(this, R.layout.item_newalbum, newAlbums);
            Thread task = new Thread() {
                @Override
                public void run() {
                    yourListView.setAdapter(customAdapter);
                    try {
                        generateList(newAlbums);
                        runOnUiThread(() -> {
                            try {
                                loadingdialog.dismiss();
                                customAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.v("samba", Log.getStackTraceString(e));
                            }

                        });
                    } catch (Exception e) {
                        Log.v("samba", Log.getStackTraceString(e));
                    }
                }
            };

            task.start();
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    protected String getText(){
        return "";
    }

    protected void doAction(String s) {
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
                newAlbums.add(new NewAlbum(s,artist,album));
            }
        } catch (Exception e) {
            Log.v("samba", Log.getStackTraceString(e));
        }
    }

    public class ListAdapter extends ArrayAdapter<NewAlbum> {
        private Context context;
        ArrayList<NewAlbum> items;
        private int extrawidth=0;

        ListAdapter(Context context, int resource, ArrayList<NewAlbum> items) {
            super(context, resource, items);
            this.items=items;
            this.context = context;
        }

        ListAdapter(Context context, int resource, ArrayList<NewAlbum> items, int extrawidth) {
            super(context, resource, items);
            this.items=items;
            this.context = context;
            this.extrawidth=extrawidth;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.item_newalbum, parent, false);
            NewAlbum p = items.get(position);

            TextView tt1 = (TextView) rowView.findViewById(R.id.artistname);
            TextView tt2 = (TextView) rowView.findViewById(R.id.albumname);
            final ImageView image = (ImageView) rowView.findViewById(R.id.spotifylistimageView1);

            if (tt1 != null) {
                tt1.setText(p.artist);
            }

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int widthscreen = size.x;
            if (tt2 != null) {
                tt2.setText(p.album);
                Rect bounds = new Rect();
                Paint textPaint = tt2.getPaint();
                textPaint.getTextBounds(p.album, 0, p.album.length(), bounds);
                int height = bounds.height();
                int width = bounds.width();

                if (width>widthscreen/2)width=widthscreen/2;
                extrawidth = widthscreen-width-180;
            }
            if (p.getImage()!=null){
                if (p.getImage().length() > 0) {

                    MainActivity.getInstance().imageLoader.DisplayImage(p.getImage(), image, bitmap -> {
                    });

                    extrawidth = extrawidth-120;
                }else image.setVisibility(View.GONE);
            }else image.setVisibility(View.GONE);
            ViewGroup.LayoutParams layPar=tt1.getLayoutParams();
            tt1.setLayoutParams(new LinearLayout.LayoutParams(extrawidth,LinearLayoutCompat.LayoutParams.WRAP_CONTENT));

            rowView.setOnClickListener(v -> processAlbum(items.get(position)));
            rowView.setOnLongClickListener(v -> {
                        if (!isTrack(position)) {
                            PopupMenu menu = new PopupMenu(v.getContext(), v);

                            basicMenu(position, menu);
                            menu.show();
                        }
                        return true;
                    }
            );
            createPopupMenuForClickOnImage(position, p, image,p.url.indexOf("playlist") > 0);
            return rowView;
        }

        public void createPopupMenuForClickOnImage(int position, NewAlbum p, ImageView image, boolean playlist) {
            boolean finalTrack =  isTrack(position);
            image.setOnClickListener(v -> {
                PopupMenu menu = new PopupMenu(v.getContext(), v);

                menu.setOnMenuItemClickListener(item -> {
                    String albumname = items.get(position).album;
                    String uri = items.get(position).url;
                    String[] url = uri.split(":");
                    String albumid=url[url.length-1];
                    String artist = items.get(position).artist;
                    if (isTrack(position)){
                        String[]urilist=items.get(position).url.split(":");
                        Track a = SpotifyFragment.getSpotifyService().getTrack(urilist[urilist.length-1]);
                        albumname=a.album.name;
                        albumid=a.album.id;
                        artist=a.artists.get(0).name;
                        DebugLog.log("album:"+artist+albumid);
                        uri = "spotify:album:"+albumid;
                    }
                    if (item.getTitle().toString().equals("add album to favorites")) {
                        MainActivity.getInstance().fillListviewWithValues.addToFavorites(items.get(position));

                    } else if (item.getTitle().toString().equals("add album")) {
                        NewAlbumsActivity.this.AddAlbumToPlaylist(uri);
                    } else {
                        if (item.getTitle().toString().equals("info album")) {
                            String albumname1 = artist + "-" + albumname;
                            SpotifyFragment.infoAlbum(albumid, albumname1, items.get(position).getImage(), MainActivity.getInstance());
                        } else if (item.getTitle().toString().equals("large image")) {
                            MainActivity.displayLargeImage(getThis, p.getImage());

                        } else if (item.getTitle().toString().equals("play")) {
                            SpotifyFragment.showPlayMenu(getThis);
                        } else if (item.getTitle().toString().equals("wikipedia")) {
                            MainActivity.startWikipediaPage(artist);
                        }    else
                            if (item.getTitle().toString().equals("recommendation")) {
                                MainActivity.getRecommendation(artist);
                            }
                            else if (item.getTitle().toString().equals("finish")) {
                            SpotifyFragment.categoriesMenu.dismiss();
                            finish();
                        }
                    }
                    return true;
                });

                if (!playlist/*&&!finalTrack*/) {
                    menu.getMenu().add("add album");
                    if (!finalTrack)menu.getMenu().add("add album to favorites");
                }
                if (!finalTrack)menu.getMenu().add("info album");
                menu.getMenu().add("play");//
                /*if (!finalTrack)*/menu.getMenu().add("wikipedia");
                if (!finalTrack)menu.getMenu().add("recommendation");
                menu.getMenu().add("large image");
                menu.getMenu().add("finish");
                menu.show();
            });
        }

        private boolean isTrack(int position) {
            String uri = items.get(position).url;
            boolean track=false;
            DebugLog.log("uri:"+uri);
            if (uri.startsWith("spotify:track"))track=true;
            return track;
        }

        private PopupMenu basicMenu(int position, PopupMenu menu) {
             menu.setOnMenuItemClickListener(item -> {
                 if (!MainActivity.getInstance().fillListviewWithValues.processChoice(item.getTitle().toString(),this,items,position))
                 if (item.getTitle().toString().equals("add album")) {
                     AddAlbumToPlaylist(position);
                 }else
                 if (item.getTitle().toString().equals("add album to favorites")) {
                     MainActivity.getInstance().fillListviewWithValues.addToFavorites(items.get(position));

                 }else
                 if (item.getTitle().toString().equals("wikipedia")) {
                     MainActivity.startWikipediaPage(items.get(position).artist);
                 }else
                 if (item.getTitle().toString().equals("recommendation")) {
                     MainActivity.getRecommendation(items.get(position).artist);
                 }else
                 if (item.getTitle().toString().equals("play album on server")) {
                     new SetAndPlayOnServer(getThis){
                         @Override
                         public void atEnd(){
                             processAlbum(items.get(position));
                         }
                     };
                 }
                 return true;
             });

             ArrayList<String>choices= MainActivity.getInstance().fillListviewWithValues.getChoices();
             for (String s:choices){
                 menu.getMenu().add(s);
             }
             menu.getMenu().add("add album to favorites");
             menu.getMenu().add("add album");
             menu.getMenu().add("play album on server");
             menu.getMenu().add("wikipedia");
             return menu;
         }


         void AddAlbumToPlaylist(int position) {//spotify:user:
             String uri = items.get(position).url;
             if (isTrack(position)){
                 String[]urilist=items.get(position).url.split(":");
                 Track a = SpotifyFragment.getSpotifyService().getTrack(urilist[2]);
                 String album=a.album.id;
                 String artist=a.artists.get(0).name;
                 DebugLog.log("album:"+artist+album);
                 uri = "spotify:album:"+album;
             }else {
             }
             NewAlbumsActivity.this.AddAlbumToPlaylist(uri);
         }
     }


    public void AddAlbumToPlaylist(String uri) {
        String prefix=getRightPrefix(uri);
        uri = getRightSpotifyUri(uri);
        SpotifyFragment.AddSpotifyItemToPlaylist(prefix, uri);
    }

    private static String getRightSpotifyUri(String s){
        int p=s.indexOf("playlist");
        if (p>0)
            return s.substring(p+9);
        else
            return s.replace("spotify:album:", "").replace("spotify:user:", "");
    }
    private static String getRightPrefix(String s){
        int p=s.indexOf("playlist");
        if (p>=0)return s.substring(0,p+9);
        else return "spotify:album:";
    }
    public void processAlbum(NewAlbum album){
        String url = album.url;
        Log.v("samba","process:"+ url);
        String[] split = url.split(":");
        if (url.startsWith("spotify:user:")&& split.length==3)
            SpotifyFragment.listPlaylists(split[2]);
        if (url.indexOf("playlist")>0) {
            SpotifyFragment.addPlaylist(this, url);
            //AddAlbumToPlaylist(album.url);
        }
        else {
            Log.v("samba","no playlist");
            SpotifyFragment.artistName = album.artist;
            try {
                SpotifyFragment.getAlbumtracksFromSpotify(getRightSpotifyUri(url), album.album, this);//todo playlist not playing, because playlist must be part of uri!
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
