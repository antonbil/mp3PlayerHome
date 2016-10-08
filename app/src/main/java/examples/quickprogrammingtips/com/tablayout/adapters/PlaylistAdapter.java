package examples.quickprogrammingtips.com.tablayout.adapters;

/**
 * Created by anton on 20-1-16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.MpdInterface;
import examples.quickprogrammingtips.com.tablayout.PlayFragment;
import examples.quickprogrammingtips.com.tablayout.R;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.model.Server;

public class PlaylistAdapter extends BaseAdapter {

    private static CopyOnWriteArrayList<Mp3File> fileArrayList;
    private final PlayFragment selectFragmentContext;
    private LayoutInflater mInflater;
    private MpdInterface caller;
    private int currentSongInPlaylist=-1;

    public PlaylistAdapter(PlayFragment selectFragmentContext, MpdInterface caller, CopyOnWriteArrayList<Mp3File> files, Context context) {

        fileArrayList = files;
        this.caller=caller;

        this.selectFragmentContext = selectFragmentContext;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
    @Override
    public int getItemViewType(int position) {
        try {
            Mp3File mp3File = fileArrayList.get(position);

            return (mp3File.isStartAlbum()) ? 0 : 1;
        }    catch (Exception e){return 0;}
    }

    @Override
    public int getCount() {
        return fileArrayList.size();
    }

    @Override
    public File getItem(int arg0) {
        return fileArrayList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public void setCurrentSong(int currentSong){
        this.currentSongInPlaylist=currentSong;
    }

    public void onGroupItemClick (MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        try{
            ViewHolder holder;
            if (position>=fileArrayList.size()) return convertView;
            final Mp3File mp3File = fileArrayList.get(position);
            int type = getItemViewType(position);
            if(convertView == null){
                if (mp3File.isStartAlbum())
                    convertView = mInflater.inflate(R.layout.itemalbum_playlist, null);
                    else
                convertView = mInflater.inflate(R.layout.item_playlist, null);
                holder = new ViewHolder();
                holder.performer = (TextView) convertView.findViewById(R.id.textViewPerformer);
                holder.title = (TextView) convertView.findViewById(R.id.textViewTitle);
                holder.time = (TextView) convertView.findViewById(R.id.textViewPerformerCountry);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position==currentSongInPlaylist)convertView.setBackgroundColor(Color.rgb(40,40,40));//
            else
            if ( (position & 1) == 0 ) { convertView.setBackgroundColor(Color.rgb(57, 57, 57)); } else convertView.setBackgroundColor(Color.rgb(64, 64, 64));

            if (!mp3File.isStartAlbum()) {
                String albumId = String.format("%s-%s", mp3File.getTracknr(), mp3File.getTitle());

                holder.performer.setText(albumId);
            }
            else {
                ImageView image=(ImageView) (convertView.findViewById(R.id.thumbnail_playlist));
                String niceAlbum = mp3File.niceAlbum();
                Bitmap bm = MainActivity.albumPictures.get(niceAlbum);
                if (MainActivity.albumPictures.containsKey(niceAlbum)&& bm !=null)
                    image.setImageBitmap(bm);
                else {
                    bm=BitmapFactory.decodeResource(convertView.getResources(), R.drawable.pause);
                    image.setImageBitmap(bm);
                }
                Bitmap bm1=bm;
                image.setOnClickListener(arg0 -> MainActivity.displayLargeImage(MainActivity.getThis, bm1));
                holder.performer.setText(String.format("%s-%s", mp3File.getArtist(), mp3File.getAlbum()));
                holder.title.setText(String.format("%s-%s", mp3File.getTracknr(), mp3File.getTitle()));
            }
            holder.time.setText(mp3File.getTimeNice());
            final int pos2=position;
            final View convertView2=convertView;
            convertView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

                @Override
                public boolean onLongClick(final View v) {
                            Log.v("samba", "Longclick"+fileArrayList.get(pos2).getFname());
                            //Toast.makeText(v.getContext(), "click:" + (String) fileArrayList.get(pos2).getName(), Toast.LENGTH_LONG).show();
                            PopupMenu menu = new PopupMenu(v.getContext(), v);

                            menu.getMenu().add("remove->");//submenu
                    menu.getMenu().add("move->");//submenu
                    menu.getMenu().add("info->");//submenu
                    menu.getMenu().add("transfer->");//submenu
                    menu.getMenu().add("spotify");//submenu
                            menu.show();
                            final View v1 = v;
                            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    if (item.getTitle().toString().equals("spotify")) {
                                        MainActivity.getThis.callSpotify(mp3File.getArtist());

                                    } else
                                    if (item.getTitle().toString().equals("transfer->")) {
                                        //submenu
                                        PopupMenu menu = new PopupMenu(v.getContext(), v);
                                        final ArrayList<Server> servers=Server.servers;
                                        for (int i = 0; i < servers.size(); i++) {
                                            menu.getMenu().add(servers.get(i).description);
                                        }

                                        menu.show();
                                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                int position=0;
                                                for (int i = 0; i < servers.size(); i++) {
                                                    if (item.getTitle().toString().equals(servers.get(i).description)) position=i;
                                                }

                                                caller.newMpdCall(mp3File, position,"export" );
                                                return true;


                                            }

                                            ;
                                        });
                                    } else
                                    if (item.getTitle().toString().equals("remove->")) {
                                        //submenu
                                        PopupMenu menu = new PopupMenu(v.getContext(), v);

                                        menu.getMenu().add(R.string.playlist_removeall);
                                        menu.getMenu().add(R.string.playlist_removeabum);
                                        menu.getMenu().add(R.string.playlist_removesong);
                                        menu.getMenu().add(R.string.playlist_removetop);
                                        menu.getMenu().add(R.string.playlist_removebottom);
                                        menu.show();
                                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                //Log.v("samba","remove:"+item.getTitle().toString());
                                                caller.newMpdCall(mp3File, position, item.getTitle().toString());
                                                return true;


                                            }

                                            ;
                                        });
                                    } else
                                    if (item.getTitle().toString().equals("info->")) {
                                        //submenu
                                        PopupMenu menu = new PopupMenu(v.getContext(), v);

                                        menu.getMenu().add("info");
                                        menu.getMenu().add("spotify");
                                        menu.getMenu().add("artist-wikipedia");
                                        menu.getMenu().add("album-wikipedia");
                                        menu.show();
                                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                if (item.getTitle().toString().equals("spotify")) {
                                                    MainActivity.getThis.callSpotify(mp3File.getArtist());
                                                    /*Intent intent = new Intent(selectFragmentContext.getContext(), SpotifyActivity.class);
                                                    intent.putExtra("artist", mp3File.getArtist());

                                                    selectFragmentContext.startActivity(intent);*/
                                                }else
                                                if (item.getTitle().toString().equals("artist-wikipedia")) {
                                                    MainActivity.getThis.startWikipediaPage(mp3File.getArtist());
                                                }else
                                                if (item.getTitle().toString().equals("album-wikipedia")) {
                                                    MainActivity.getThis.startWikipediaPage(mp3File.getAlbum());
                                                }else
                                                    if (item.getTitle().toString().equals("info")) {
                                                        int length=0;
                                                        for (Mp3File mp:fileArrayList){
                                                            length+=mp.getTime();
                                                        }
                                                        final String snack = String.format("Info playlist\nNumber of songs:%s\nTotal time:%s",fileArrayList.size(),Mp3File.niceString(length));
                                                        Snackbar snackbar=Snackbar.make(convertView2, snack, Snackbar.LENGTH_LONG);
                                                        View snackbarView = snackbar.getView();
                                                        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                                                        textView.setMaxLines(5);
                                                        snackbar.show();
                                                    }else
                                                caller.newMpdCall(mp3File, position, item.getTitle().toString());
                                                return true;


                                            }

                                            ;
                                        });
                                    } else
                                    if (item.getTitle().toString().equals("move->")) {
                                        //submenu
                                        PopupMenu menu = new PopupMenu(v.getContext(), v);

                                        menu.getMenu().add(R.string.playlist_movebottom);
                                        menu.getMenu().add(R.string.playlist_down);
                                        menu.show();
                                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                caller.newMpdCall(mp3File, position, item.getTitle().toString());
                                                return true;


                                            }

                                            ;
                                        });
                                    } else
                                    caller.newMpdCall(mp3File, position, item.getTitle().toString());
                                    return true;
                                }

                            });
                    return true;
                }
                    });

                final String fname = mp3File.getFname();
                final String path= mp3File.getFile();
                //Log.v("test", fname);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.v("samba", "CLick, play "+fname);
                            //Mp3File mp=(Mp3File) mp3File;
                            //mp.getMpcSong()
                            //Toast t = Toast.makeText(v.getContext(), "play:"+mp.getTitle(), Toast.LENGTH_SHORT);
                        if (caller!=null)
                        caller.newMpdCall(mp3File, position, selectFragmentContext.getString(R.string.command_play));
                        Log.v("samba", "CLick, play 2 "+fname);
                        MainActivity.getThis.getLogic().getMpc().play();
                        Log.v("samba", "CLick, play 3 "+fname);
                            //t.show();
                    }
                });
            } catch (Exception e) {
                //successful = false;
                e.printStackTrace();
            }

        return convertView;
    }

    static class ViewHolder{
        TextView performer, title,time;
    }


}