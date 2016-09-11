package examples.quickprogrammingtips.com.tablayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

public abstract class PlanetAdapter extends ArrayAdapter<String> {
    public static boolean longclicked=false;
    private final ArrayList<PlaylistItem> tracksPlaylist;
    private boolean displayCurrentTrack = true;
    int currentItem = -1;
    private boolean albumVisible=false;

    public void setCurrentItem(int i) {
        this.currentItem = i;
    }

    private Context context;
    private Context getThis;
    private int mypos=1;

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
    Bitmap logo1;

    public PlanetAdapter(List<String> planetList, Context ctx, ArrayList<PlaylistItem> tracksPlaylist) {
        super(ctx, R.layout.spotifylist, planetList);
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
        final View convertView2=convertView;
// Now we can fill the layout with the right values
        holder = new ViewHolder();
        holder.pos = (TextView) convertView.findViewById(R.id.number);

        holder.image = (ImageView) convertView.findViewById(R.id.spotifylistimageView);
        holder.name = (TextView) convertView.findViewById(R.id.name);
        holder.time = (TextView) convertView.findViewById(R.id.spotifytime);

        convertView.setTag(holder);

        holder.pos.setVisibility(View.VISIBLE);
        try {
            PlaylistItem t = tracksPlaylist.get(position);
            holder.name.setText(t.text);
            if (t.time>0)
            holder.time.setText(Mp3File.niceTime(t.time));
            else holder.time.setVisibility(View.GONE);
            if(t.pictureVisible) {
                holder.image.setVisibility(View.VISIBLE);
                holder.pos.setVisibility(View.GONE);
                mypos=1;

                //Log.v("samba", "look for:" + t.url);
                new DownLoadImageTask() {

                    @Override
                    public void setImage(final Bitmap logo) {
                        logo1=logo;
                        holder.image.setImageBitmap(logo);
                        holder.image.setOnClickListener(arg0 -> {
                            //MainActivity.displayLargeImage(getThis, logo);
                            longclick( position,  convertView2,logo);
                        });
                    }
                }.execute(t.url);
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
        if (tracksPlaylist.get(position).url.startsWith("http://192.168.2.8:8081")&&!displayCurrentTrack){
            holder.name.setTextColor(Color.YELLOW);
        } else {
            holder.name.setTextColor(textcolor);
            holder.time.setTextColor(textcolor);
        }
        convertView.setOnClickListener(view -> {
            onClickFunc(position);
        });
        convertView.setOnLongClickListener(view -> {
            longclick( position,  convertView2,logo1);
            return false;
        });
        /*OnFlingGestureListener flingListener;
        flingListener = new OnFlingGestureListener() {
            @Override
            public void onRightToLeft() {
                SpotifyFragment.getThis.previousList();
            }

            @Override
            public void onLeftToRight() {
                SpotifyFragment.getThis.nextList();
            }


            @Override
            public void onTapUp() {
                onClickFunc(position);
            }

            @Override
            public void onLongTapUp() {
                PlanetAdapter.longclicked=true;
                longclick( position,  convertView2);

            }

        };*/

        holder.pos.setText("" + (mypos));
        mypos++;
        /*convertView.setOnTouchListener((v, event) -> {
            if (flingListener.onTouch(v, event)) {
                // if gesture detected, ignore other touch events
                return true;
            } //else {
                //Log.v("samba","nofling");

            int action = MotionEventCompat.getActionMasked(event);
            if (action == MotionEvent.ACTION_DOWN) {
                // normal touch events
                onClickFunc(position);
                return false;
            }
            return true;
        });*/


        return convertView;
    }

    private void longclick(int position, View v, Bitmap logo){
        Log.v("samba","ontouch album");
        PopupMenu menu = new PopupMenu(v.getContext(), v);
        if (!isAlbumVisible()) {

            //Toast.makeText(v.getContext(), "click:" + (String) fileArrayList.get(pos2).getName(), Toast.LENGTH_LONG).show();
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String title = item.getTitle().toString();
                    if (title.equals("remove->")) {
                        //submenu
                        PopupMenu menu = new PopupMenu(v.getContext(), v);
                        menu.getMenu().add("remove top");
                        menu.getMenu().add("remove bottom");
                        menu.getMenu().add("remove track");
                        menu.getMenu().add("remove album");

                        menu.show();
                        menu.setOnMenuItemClickListener(item1 -> {
                            String title1 = item1.getTitle().toString();
                            if (title1.equals("remove top")) {
                                removeUp(position);
                            } else if (title1.equals("remove bottom")) {
                                removeDown(position);

                            } else if (title1.equals("remove album")) {
                                removeAlbum(position);
                            } else if (title1.equals("remove track")) {
                                removeTrack(position);
                            }
                            //
                            return true;
                        }


                        );
                    }else if (title.equals("add album")) {
                        addAlbum(position);
                    } else if (title.equals("display artist")) {
                        displayArtist(position);
                    } else if (title.equals("wikipedia")) {
                        displayArtistWikipedia(position);
                    } else if (title.equals("large picture")) {
                        MainActivity.displayLargeImage(getThis, logo);
                    } else if (title.equals("add album to favorites")) {
                        addAlbumToFavoritesTrack(position);
                    }

                    return true;
                }
            });

            menu.getMenu().add("remove->");
            menu.getMenu().add("add album");
            menu.getMenu().add("add album to favorites");
            menu.getMenu().add("display artist");
            menu.getMenu().add("wikipedia");
            menu.getMenu().add("large picture");
        } else {
            menu.setOnMenuItemClickListener(item -> {
                /*if (tracksPlaylist.get(position).url.startsWith("http://192.168.2.8:8081")){
                    Toast.makeText(v.getContext(), "not implemented yet", Toast.LENGTH_LONG).show();
                    return false;
                }*/

                if (item.getTitle().toString().equals("replace and play")) {
                    replaceAndPlayAlbum(position);
                } else if (item.getTitle().toString().equals("add and play")) {
                    addAndPlayAlbum(position);
                } else if (item.getTitle().toString().equals("wikipedia artist")) {
                    albumArtistWikipedia(position);
                } else if (item.getTitle().toString().equals("large picture")) {
                    MainActivity.displayLargeImage(getThis, logo);
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
            });

            menu.getMenu().add("replace and play");//submenu
            menu.getMenu().add("add and play");//submenu
            menu.getMenu().add("add");//submenu
            menu.getMenu().add("wikipedia artist");//submenu
            menu.getMenu().add("add album to favorites");//submenu
            menu.getMenu().add("large picture");

        }
        menu.show();
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
        TextView pos, name,time;
        public ImageView image;
    }
}
