package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.adapters.PlaylistAdapter;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

@SuppressWarnings("ConstantConditions")
abstract class PlanetAdapter extends ArrayAdapter<String> {
    private final ArrayList<PlaylistItem> tracksPlaylist;
    private boolean displayCurrentTrack = true;
    int currentItem = -1;
    private boolean albumVisible=false;
    protected ViewGroup parent;

    void setCurrentItem(int i) {
        this.currentItem = i;
    }

    protected Context context;
    private Context getThis;
    private int mypos=1;

    public abstract void removeUp(int counter);//onClickFunc
    public abstract void removeAll();

    public abstract void onClickFunc(int counter);//onClickFunc

    public abstract void removeDown(int counter);

    public abstract void removeAlbum(int counter);
    public abstract void addAlbumToFavoritesAlbum(int counter);
    public abstract void addAlbumToFavoritesTrack(int counter);

    public abstract void removeTrack(int counter);

    public abstract void displayArtist(int counter);
    public abstract void displayArtistWikipedia(int counter);

    public abstract void replaceAndPlayAlbum(int counter);
    public abstract void addAndPlayAlbum(int counter);
    public abstract void albumArtistWikipedia(int counter);
    public abstract void addAlbum(int counter);
    public abstract void transferPlaylist();
    public abstract void addAlbumNoplay(int counter);

    PlanetAdapter(List<String> planetList, Context ctx, ArrayList<PlaylistItem> tracksPlaylist) {
        super(ctx, R.layout.spotifylist, planetList);
        this.tracksPlaylist=tracksPlaylist;
        this.context = ctx;
        this.getThis=ctx;
    }

    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        try{
        this.parent=parent;
        final ViewHolder holder;

// First let's verify the convertView is not null
        if (convertView == null) {
// This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spotifylist, parent, false);
            holder = new ViewHolder();
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        final View convertView2=convertView;
// Now we can fill the layout with the right values
        holder.pos = (TextView) convertView.findViewById(R.id.number);

        holder.image = (ImageView) convertView.findViewById(R.id.spotifylistimageView);
        holder.name = (TextView) convertView.findViewById(R.id.name);
        holder.time = (TextView) convertView.findViewById(R.id.spotifytime);
        holder.pos.setOnClickListener(view -> longclick( position,  convertView2));

        convertView.setTag(holder);

        holder.pos.setVisibility(View.VISIBLE);
            PlaylistItem t = tracksPlaylist.get(position);
            holder.name.setText(t.text);
        int tracknr=t.trackNumber;
            //Log.v("samba","text:"+t.text);
            if (t.time>0)
            holder.time.setText(Mp3File.niceTime(t.time));
            else holder.time.setVisibility(View.GONE);
        try {
            if(t.pictureVisible) {
                holder.image.setVisibility(View.VISIBLE);
                holder.pos.setVisibility(View.GONE);
                mypos=1;
                String url = t.url.replace(" ", "%20");
                MainActivity.getInstance().imageLoader.DisplayImage(url, holder.image,bitmap -> {
                    //((Activity)context).runOnUiThread(() -> holder.image.setImageBitmap(bitmap));
                    //Log.v("samba", "album:" + t.text);
                });
                holder.image.setOnClickListener(arg0 -> longclick( position,  convertView2));

                //Log.v("samba", "look for:" + replace);
                /*new DownLoadImageTask() {

                    @Override
                    public void setImage(final Bitmap logo) {
                        holder.image.setImageBitmap(logo);
                        holder.image.setOnClickListener(arg0 -> longclick( position,  convertView2));
                    }
                }.execute(t.url);*/
            } else
                holder.image.setVisibility(View.GONE);
        } catch (Exception e) {
            holder.image.setVisibility(View.GONE);
        }

        int textcolor = Color.parseColor("#bebebe");
        if (displayCurrentTrack && (position == currentItem))
            convertView.setBackgroundColor(Color.rgb(40, 40, 40));//
        else if ((position & 1) == 0) {
            convertView.setBackgroundColor(Color.rgb(57, 57, 57));
        } else convertView.setBackgroundColor(Color.rgb(64, 64, 64));
            try{
        if (tracksPlaylist.get(position).url.startsWith("http://192.168.2.8:8081")&&!displayCurrentTrack){
            holder.name.setTextColor(Color.YELLOW);
        } else {
            holder.name.setTextColor(textcolor);
            holder.time.setTextColor(textcolor);
        }
        } catch (Exception e) {
                holder.name.setTextColor(textcolor);
                holder.time.setTextColor(textcolor);
        }
        convertView.setOnClickListener(view -> {
            onClickFunc(position);
            currentItem=position;
            MainActivity.getInstance().runOnUiThread(() -> {
                final Handler handler = new Handler();
                handler.postDelayed(this::notifyDataSetChanged, 1000);
            });

        });
        convertView.setOnLongClickListener(view -> {
            longclick( position,  convertView2);
            return false;
        });

        int pos=mypos;
        if (tracknr>=0)pos=tracknr;
        holder.pos.setText("" + (pos));
        mypos++;
    } catch (Exception e) {
        DebugLog.log("error in adapter");
            Log.v("samba", Log.getStackTraceString(e));
    }

        return convertView;
    }

    private void longclick(int position, View v){
        String imageUrl=tracksPlaylist.get(position).url;
        PopupMenu menu = new PopupMenu(v.getContext(), v);
        if (!isAlbumVisible()) {

            menu.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("remove->")) {
                    //submenu
                    android.widget.PopupMenu menu1 = PlaylistAdapter.createRemovePopupMenu(v);
                    menu1.setOnMenuItemClickListener(item1 -> {
                        String title1 = item1.getTitle().toString();
                        /*
                                menu1.getMenu().add(R.string.playlist_removeall);
        menu1.getMenu().add(R.string.playlist_removeabum);
        menu1.getMenu().add(R.string.playlist_removesong);
        menu1.getMenu().add(R.string.playlist_removetop);
        menu1.getMenu().add(R.string.playlist_removebottom
                         */
                        if (title1.equals(MainActivity.getInstance().getString(R.string.playlist_removeall)))
                            removeAll(); else
                        if (title1.equals(MainActivity.getInstance().getString(R.string.playlist_removesong)))
                            removeTrack(position); else
                        if (title1.equals(MainActivity.getInstance().getString(R.string.playlist_removeabum)))
                            removeAlbum(position); else
                        if (title1.equals(MainActivity.getInstance().getString(R.string.playlist_removetop)))
                            removeUp(position); else
                        if (title1.equals(MainActivity.getInstance().getString(R.string.playlist_removebottom)))
                            removeDown(position);;
                        return true;
                    });
                }else if (title.equals("add album")) {
                    addAlbum(position);
                } else if (title.equals("display artist")) {
                    displayArtist(position);
                } else if (title.equals("wikipedia")) {
                    displayArtistWikipedia(position);
                } else if (title.equals("large picture")) {
                    MainActivity.displayLargeImage(getThis, imageUrl);
                } else if (item.getTitle().toString().equals("-->transfer")) {
                    //Log.v("samba","transfer planetadapter");
                    transferPlaylist();
                } else if (title.equals("add album to favorites")) {
                    addAlbumToFavoritesTrack(position);
                }

                return true;
            });

            menu.getMenu().add("remove->");
            menu.getMenu().add("add album");
            menu.getMenu().add("add album to favorites");
            menu.getMenu().add("display artist");
            menu.getMenu().add("wikipedia");
            menu.getMenu().add("-->transfer");
            menu.getMenu().add("large image");
        } else {
            menu.setOnMenuItemClickListener(item -> {

                String title1 = item.getTitle().toString();
                switch (title1) {
                    case "replace and play":
                        replaceAndPlayAlbum(position);
                        break;
                    case "add and play":
                        addAndPlayAlbum(position);
                        break;
                    case "wikipedia artist":
                        albumArtistWikipedia(position);
                        break;
                    case "large image":
                        MainActivity.displayLargeImage(getThis, imageUrl);
                        break;
                    case "add":
                        addAlbumNoplay(position);
                        break;
                    case "add album to favorites":
                        //tracksPlaylist.get(position).url
                        addAlbumToFavoritesAlbum(position);
                        break;
                }

                return true;
            });

            menu.getMenu().add("replace and play");//submenu
            menu.getMenu().add("add and play");//submenu
            menu.getMenu().add("add");//submenu
            menu.getMenu().add("add album to favorites");//submenu
            menu.getMenu().add("wikipedia artist");//submenu
            menu.getMenu().add("large image");

        }
        menu.show();
    }
    void setDisplayCurrentTrack(boolean displayCurrentTrack) {
        this.displayCurrentTrack = displayCurrentTrack;
    }

    private boolean isAlbumVisible() {
        return albumVisible;
    }

    void setAlbumVisible(boolean albumVisible) {
        this.albumVisible = albumVisible;
    }

    private class ViewHolder {
        TextView pos, name,time;
        public ImageView image;
    }
}
