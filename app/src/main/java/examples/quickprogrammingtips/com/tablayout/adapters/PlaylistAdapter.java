package examples.quickprogrammingtips.com.tablayout.adapters;

/**
 * Created by anton on 20-1-16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
            convertView.setOnLongClickListener(v -> {
                try{
                        //Log.v("samba", "Longclick"+fileArrayList.get(pos2).getFname());
                        //Toast.makeText(v.getContext(), "click:" + (String) fileArrayList.get(pos2).getName(), Toast.LENGTH_LONG).show();
                        PopupMenu menu = new PopupMenu(v.getContext(), v);

                        menu.getMenu().add("remove->");//submenu
                menu.getMenu().add("move->");//submenu
                menu.getMenu().add("info->");//submenu
                menu.getMenu().add("transfer->");//submenu
                menu.getMenu().add("spotify");//submenu
                    try{
                        menu.show();
                }catch(Exception e){
                    Log.v("samba", Log.getStackTraceString(e));}
                final View v1 = v;
                        menu.setOnMenuItemClickListener(item -> {
                            if (item.getTitle().toString().equals("spotify")) {
                                MainActivity.getThis.callSpotify(mp3File.getArtist());

                            } else
                            if (item.getTitle().toString().equals("transfer->")) {
                                //submenu
                                PopupMenu menu1 = new PopupMenu(v.getContext(), v);
                                final ArrayList<Server> servers=Server.servers;
                                for (int i = 0; i < servers.size(); i++) {
                                    menu1.getMenu().add(servers.get(i).description);
                                }

                                menu1.show();
                                menu1.setOnMenuItemClickListener(item1 -> {
                                    int position1 =0;
                                    for (int i = 0; i < servers.size(); i++) {
                                        if (item1.getTitle().toString().equals(servers.get(i).description)) position1 =i;
                                    }

                                    caller.newMpdCall(mp3File, position1,"export" );
                                    return true;


                                });
                            } else
                            if (item.getTitle().toString().equals("remove->")) {
                                //submenu
                                PopupMenu menu1 = new PopupMenu(v.getContext(), v);

                                menu1.getMenu().add(R.string.playlist_removeall);
                                menu1.getMenu().add(R.string.playlist_removeabum);
                                menu1.getMenu().add(R.string.playlist_removesong);
                                menu1.getMenu().add(R.string.playlist_removetop);
                                menu1.getMenu().add(R.string.playlist_removebottom);
                                menu1.show();
                                menu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

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
                                PopupMenu menu1 = new PopupMenu(v.getContext(), v);

                                menu1.getMenu().add("info");
                                menu1.getMenu().add("spotify");
                                menu1.getMenu().add("artist-wikipedia");
                                menu1.getMenu().add("album-wikipedia");
                                menu1.show();
                                menu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

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
                                PopupMenu menu1 = new PopupMenu(v.getContext(), v);

                                menu1.getMenu().add(R.string.playlist_movebottom);
                                menu1.getMenu().add(R.string.playlist_down);
                                menu1.show();
                                menu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

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
                        });
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
                return true;
            });

                final String fname = mp3File.getFname();
                final String path= mp3File.getFile();
                //Log.v("test", fname);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentSongInPlaylist=position;
                        //hier positie veranderen!

                        //Log.v("samba", "CLick, play "+fname);
                            //Mp3File mp=(Mp3File) mp3File;
                            //mp.getMpcSong()
                            //Toast t = Toast.makeText(v.getContext(), "play:"+mp.getTitle(), Toast.LENGTH_SHORT);
                        try{
                        //if (caller!=null)

                        caller.newMpdCall(mp3File, position, MainActivity.getThis.getBaseContext().getString(R.string.command_play));
                            MainActivity.getThis.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                           notifyDataSetChanged();
                                        }
                                    }, 1000);
                                }
                            });
                        //Log.v("samba", "CLick, play 2 "+fname);
                            //t.show();
                    }catch(Exception e){
                        Log.v("samba", Log.getStackTraceString(e));}

                        MainActivity.getThis.getLogic().getMpc().play();
                        //Log.v("samba", "CLick, play 3 "+fname);
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