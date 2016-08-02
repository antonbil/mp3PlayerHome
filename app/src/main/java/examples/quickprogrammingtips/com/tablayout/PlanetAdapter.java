package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public abstract class PlanetAdapter extends ArrayAdapter<String> {
    //String previousAlbum="";
    private final ArrayList<PlaylistItem> tracksPlaylist;
    private boolean displayCurrentTrack = true;
    int currentItem = -1;
    private boolean albumVisible=false;

    public void setCurrentItem(int i) {
        this.currentItem = i;
    }

    //private List<String> planetList;
    private Context context;
    private AppCompatActivity getThis;

    public abstract void removeUp(int counter);//onClickFunc

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
    public abstract void addAlbumNoplay(int counter);

    public PlanetAdapter(List<String> planetList, AppCompatActivity ctx, ArrayList<PlaylistItem> tracksPlaylist) {
        super(ctx, R.layout.spotifylist, planetList);
        // this.planetList = planetList;
        this.tracksPlaylist=tracksPlaylist;
        this.context = ctx;
        this.getThis=ctx;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

// First let's verify the convertView is not null
        if (convertView == null) {
// This a new view we inflate the new layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.spotifylist, parent, false);
        }
// Now we can fill the layout with the right values
        holder = new ViewHolder();
        holder.pos = (TextView) convertView.findViewById(R.id.number);
        holder.image = (ImageView) convertView.findViewById(R.id.spotifylistimageView);
        holder.name = (TextView) convertView.findViewById(R.id.name);
        //TextView tv = (TextView) convertView.findViewById(R.id.name);
        convertView.setTag(holder);
        //String p = planetList.get(position);
        //if (position==currentItem){
        //tv.setTextColor(Color.BLUE);}
        // else tv.setTextColor(Color.WHITE);
        //holder.name.setText(p);
        //TextView nr = (TextView) convertView.findViewById(R.id.number);
        //if (position==currentItem){
        //    nr.setTextColor(Color.BLUE);}
        //else nr.setTextColor(Color.WHITE);
        holder.pos.setVisibility(View.VISIBLE);
        //if (displayCurrentTrack)
        try {
            PlaylistItem t = tracksPlaylist.get(position);
            holder.name.setText(t.text);
            if(t.pictureVisible) {
                holder.image.setVisibility(View.VISIBLE);
                holder.pos.setVisibility(View.GONE);

                //Log.v("samba", "look for:" + t.url);
                new DownLoadImageTask() {
                    @Override
                    public void setImage(final Bitmap logo) {
                        holder.image.setImageBitmap(logo);
                        holder.image.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                MainActivity.displayLargeImage(getThis, logo);
                            }
                        });
                    }
                }.execute(t.url);
            } else
                holder.image.setVisibility(View.GONE);
        } catch (Exception e) {
            holder.image.setVisibility(View.GONE);
        }
        //else
        //    holder.image.setVisibility(View.GONE);
        if (displayCurrentTrack && (position == currentItem))
            convertView.setBackgroundColor(Color.rgb(40, 40, 40));//
        else if ((position & 1) == 0) {
            convertView.setBackgroundColor(Color.rgb(57, 57, 57));
        } else convertView.setBackgroundColor(Color.rgb(64, 64, 64));

        holder.pos.setText("" + (position + 1));
        convertView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

                                               @Override
                                               public boolean onLongClick(final View v) {
                                                   PopupMenu menu = new PopupMenu(v.getContext(), v);
                                                   if (!isAlbumVisible()) {

                                                       //Toast.makeText(v.getContext(), "click:" + (String) fileArrayList.get(pos2).getName(), Toast.LENGTH_LONG).show();
                                                       menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                                           @Override
                                                           public boolean onMenuItemClick(MenuItem item) {
                                                               if (item.getTitle().toString().equals("remove top")) {
                                                                   removeUp(position);
                                                               } else if (item.getTitle().toString().equals("remove bottom")) {
                                                                   removeDown(position);
                                                               } else if (item.getTitle().toString().equals("remove track")) {
                                                                   removeTrack(position);
                                                               } else if (item.getTitle().toString().equals("add album")) {
                                                                   addAlbum(position);
                                                               } else if (item.getTitle().toString().equals("remove album")) {
                                                                   removeAlbum(position);
                                                               } else if (item.getTitle().toString().equals("display artist")) {
                                                                   displayArtist(position);
                                                               } else if (item.getTitle().toString().equals("wikipedia")) {
                                                                   displayArtistWikipedia(position);
                                                               } else if (item.getTitle().toString().equals("add album to favorites")) {
                                                                   addAlbumToFavoritesTrack(position);
                                                               }

                                                               return true;
                                                           }
                                                       });

                                                       menu.getMenu().add("remove top");//submenu
                                                       menu.getMenu().add("remove bottom");//submenu
                                                       menu.getMenu().add("add album");//submenu
                                                       menu.getMenu().add("add album to favorites");//submenu
                                                       menu.getMenu().add("display artist");//submenu
                                                       menu.getMenu().add("wikipedia");//submenu
                                                       menu.getMenu().add("remove track");//submenu
                                                       menu.getMenu().add("remove album");//submenu
                                                   } else {
                                                       menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                                           @Override
                                                           public boolean onMenuItemClick(MenuItem item) {
                                                               if (tracksPlaylist.get(position).url.startsWith("http://192.168.2.8:8081")){
                                                                   Toast.makeText(v.getContext(), "not implemented yet", Toast.LENGTH_LONG).show();
                                                                   return false;
                                                               }

                                                               if (item.getTitle().toString().equals("replace and play")) {
                                                                   replaceAndPlayAlbum(position);
                                                               } else if (item.getTitle().toString().equals("add and play")) {
                                                                   addAndPlayAlbum(position);
                                                               } else if (item.getTitle().toString().equals("wikipedia artist")) {
                                                                   albumArtistWikipedia(position);
                                                               } else if (item.getTitle().toString().equals("add")) {
                                                                   //Toast.makeText(getThis.getApplicationContext(), "Not implemented yet",
                                                                   //        Toast.LENGTH_SHORT).show();
                                                                   addAlbumNoplay(position);

                                                                   //addAlbum(position);
                                                               }
                                                               else if (item.getTitle().toString().equals("add album to favorites")) {
                                                                   addAlbumToFavoritesAlbum(position);
                                                               }

                                                               return true;
                                                           }
                                                       });

                                                       menu.getMenu().add("replace and play");//submenu
                                                       menu.getMenu().add("add and play");//submenu
                                                       menu.getMenu().add("add");//submenu
                                                       menu.getMenu().add("wikipedia artist");//submenu
                                                       menu.getMenu().add("add album to favorites");//submenu

                                                   }
                                                   menu.show();
                                                   return true;
                                               }
                                           }
        );
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickFunc(position);
            }
        });

        return convertView;
    }

    public boolean isDisplayCurrentTrack() {
        return displayCurrentTrack;
    }

    public void setDisplayCurrentTrack(boolean displayCurrentTrack) {
        //Log.v("samba",""+displayCurrentTrack);
        this.displayCurrentTrack = displayCurrentTrack;
    }

    public boolean isAlbumVisible() {
        return albumVisible;
    }

    public void setAlbumVisible(boolean albumVisible) {
        this.albumVisible = albumVisible;
    }

    class ViewHolder {
        TextView pos, name;
        public ImageView image;
    }
}
