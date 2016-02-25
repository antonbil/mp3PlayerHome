package examples.quickprogrammingtips.com.tablayout.adapters;

/**
 * Created by anton on 20-1-16.
 */

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.R;
import examples.quickprogrammingtips.com.tablayout.SambaInterface;
import examples.quickprogrammingtips.com.tablayout.SpotifyActivity;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

public class FileListAdapter extends BaseAdapter {

    private static ArrayList<File> fileArrayList;
    private LayoutInflater mInflater;
    private SambaInterface caller;
    private Context context;

    public FileListAdapter(Context selectFragmentContext, SambaInterface caller,ArrayList<File> files) {

        fileArrayList = files;
        this.caller=caller;
        context=selectFragmentContext;

        mInflater = LayoutInflater.from(selectFragmentContext);
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


    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_file, null);
            holder = new ViewHolder();
            holder.description = (TextView) convertView.findViewById(R.id.textViewPerformer);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try{
            final String fname = fileArrayList.get(position).getFname();
        final String path=fileArrayList.get(position).getPath();
        if (!(fileArrayList.get(position) instanceof Mp3File)) {
            String itemFname = fileArrayList.get(position).getFname();
            if (itemFname.endsWith("/"))
                itemFname=itemFname.substring(0,itemFname.length()-1);
            holder.description.setText(itemFname);
        } else {
            Mp3File mp3File = (Mp3File) fileArrayList.get(position);;
            holder.description.setText(String.format("%s-%s(%s)\n%s-%s", mp3File.getTracknr(), mp3File.getTitle(), mp3File.getTimeNice(), mp3File.getArtist(), mp3File.getAlbum()));
        }
        final int pos2=position;
        convertView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(final View v) {
//                    Log.v("test", fileArrayList.get(pos2).getFname());
                            //Toast.makeText(v.getContext(), "click:" + (String) fileArrayList.get(pos2).getName(), Toast.LENGTH_LONG).show();
                            PopupMenu menu = getPopupMenu(v);
                            if (fileArrayList.get(position) instanceof Mp3File)
                                menu.getMenu().add("info-->");
                            menu.show();
                            final View v1 = v;
                            //TODO: merge two onclick-listener together
                            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    String title = item.getTitle().toString();
                                    if (title.equals("info-->")){
                                        PopupMenu menu = new PopupMenu(v.getContext(), v);

                                        menu.getMenu().add("spotify");
                                        menu.getMenu().add("artist-wikipedia");
                                        menu.getMenu().add("album-wikipedia");
                                        menu.show();
                                        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                Mp3File mp3File=(Mp3File)fileArrayList.get(position);
                                                final MainActivity context = MainActivity.getThis;
                                                if (item.getTitle().toString().equals("spotify")) {
                                                    Intent intent = new Intent(context, SpotifyActivity.class);
                                                    intent.putExtra("artist", mp3File.getArtist());
                                                    context.startActivity(intent);
                                                }else
                                                if (item.getTitle().toString().equals("artist-wikipedia")) {
                                                    context.startWikipediaPage(mp3File.getArtist());
                                                }else
                                                if (item.getTitle().toString().equals("album-wikipedia")) {
                                                    context.startWikipediaPage(mp3File.getAlbum());
                                                }
                                                return true;


                                            }

                                            ;
                                        });
                                        return true;
                                    }

                                    if (!(fileArrayList.get(position) instanceof Mp3File)) {
                                        caller.newSambaCall(path + fname, title);
                                    } else
                                        caller.newSambaCall(((Mp3File) fileArrayList.get(position)).getMpcSong().file, title);
                                    //Toast.makeText(v1.getContext(), "click:" + fileArrayList.get(pos2).getFname() + ":" + title, Toast.LENGTH_LONG).show();

                                    return true;
                                }

                            });
                            return true;
                        }
                    });

//                Log.v("test", fname);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!(fileArrayList.get(position) instanceof Mp3File))
                            caller.newSambaCall(path + fname, context.getString(R.string.select_filelist));
                        else {
                            final Mp3File mp=(Mp3File)fileArrayList.get(position);
                            //mp.getMpcSong()
                            PopupMenu menu = getPopupMenu(v);
                            menu.getMenu().add(R.string.addsong_filelist);
                            menu.show();
                            final View v1 = v;
                            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    String title = item.getTitle().toString();
                                    //Log.v("samba", "title:"+title);
                                    if (!(title.equals(context.getString(R.string.addsong_filelist)))) {
                                        caller.newSambaCall(path, title);
                                    } else
                                        caller.newSambaCall(mp.getMpcSong().file, title);

                                    return true;
                                }

                            });

                        }
                    }
                });
            } catch (Exception e) {
                //successful = false;
                //e.printStackTrace();
            }

        return convertView;
    }

    @NonNull
    public PopupMenu getPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(v.getContext(), v);

        menu.getMenu().add(context.getString(R.string.add_filelist));
        menu.getMenu().add(context.getString(R.string.addandplay_filelist));
        menu.getMenu().add(context.getString(R.string.replaceandplay_filelist));
        menu.getMenu().add(R.string.addtofavorites_filelist);
        menu.getMenu().add("Download");
        return menu;
    }

    static class ViewHolder{
        TextView description;
    }


}