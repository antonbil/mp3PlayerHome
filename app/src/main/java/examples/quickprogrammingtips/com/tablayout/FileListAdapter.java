package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 20-1-16.
 * Adapter to display list of Mpd-files or smb-files
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;

class FileListAdapter extends BaseAdapter {

    private String previousFilePath="";
    private int level=0;

    private static boolean isInteger(String s) {
        return isInteger(s,10);
    }    private static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    private static ArrayList<File> fileArrayList;
    private LayoutInflater mInflater;
    private SambaInterface caller;
    private Context context;

    FileListAdapter(Context selectFragmentContext, SambaInterface caller, ArrayList<File> files) {

        fileArrayList = files;
        this.caller=caller;
        context=selectFragmentContext;

        mInflater = LayoutInflater.from(selectFragmentContext);
    }

    private static int countMatches(String s,String c){
        return s.length() - s.replace(c, "").length();
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
        try{

        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.item_file, parent,false);
            holder = new ViewHolder();
            holder.description = (TextView) convertView.findViewById(R.id.textViewPerformer);
            holder.image=(ImageView)  convertView.findViewById(R.id.thumbnail_playlist);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
            if (level==0)
            holder.image.setVisibility(View.GONE);
            else
                holder.image.setVisibility(View.VISIBLE);
            final View finalView=convertView;
            float density = context.getResources().getDisplayMetrics().density;
            int paddingPixel = 16;
            int paddingDp = (int)(paddingPixel * density);
            finalView.setPadding(paddingDp,paddingDp,0,0);

        try{
            holder.image.setImageResource(R.drawable.common_full_open_on_phone);

            final String fname = fileArrayList.get(position).getFname();

            final String path=fileArrayList.get(position).getPath();
        if (!(fileArrayList.get(position) instanceof Mp3File)) {

            String filePath = (fileArrayList.get(position).getPath() + fileArrayList.get(position).getFname()).replace(" ","%20");
            setImageForItem(position,fname,path,holder, finalView, density, filePath);
            String itemFname = fileArrayList.get(position).getFname();
            if (itemFname.endsWith("/"))
                itemFname=itemFname.substring(0,itemFname.length()-1);
            //holder.description.setText(itemFname);
            String d = splitStringAtFirstDash(itemFname);
            if (level!=0)

                holder.description.setText(d);
            else
                holder.description.setText(itemFname);

        } else {
            String filePath = ((Mp3File)fileArrayList.get(position)).getFile().replace(" ","%20");
            int p=filePath.lastIndexOf("/");
            filePath=filePath.substring(0,p+1);
            setImageForItem(position, fname, path, holder, finalView, density, filePath);
            Mp3File mp3File = (Mp3File) fileArrayList.get(position);
            holder.description.setText(String.format("%s-%s(%s)\n%s-%s", mp3File.getTracknr(), mp3File.getTitle(), mp3File.getTimeNice(), mp3File.getArtist(), mp3File.getAlbum()));
        }

        convertView.setOnLongClickListener(v -> {

            setContextMenu(position, fname, path, v, "");
            return true;
        });

                convertView.setOnClickListener(v -> {

                    if (!(fileArrayList.get(position) instanceof Mp3File))
                        caller.newSambaCall(path + fname, context.getString(R.string.select_filelist));
                    else {
                        final Mp3File mp=(Mp3File)fileArrayList.get(position);
                        PopupMenu menu = getPopupMenu(v);
                        menu.getMenu().add(R.string.addsong_filelist);
                        menu.show();
                        menu.setOnMenuItemClickListener(item -> {
                            String title = item.getTitle().toString();
                            if (!(title.equals(context.getString(R.string.addsong_filelist)))) {
                                caller.newSambaCall(path, title);
                            } else
                                caller.newSambaCall(mp.getMpcSong().file, title);

                            return true;
                        });

                    }
                });
            } catch (Exception e) {
                Log.v("samba", Log.getStackTraceString(e));
            }
        }catch (Exception e){
            Log.v("samba", Log.getStackTraceString(e));}


        return convertView;
    }

    @NonNull
    public String splitStringAtFirstDash(String itemFname) {
        String d=itemFname;
        String[] lines=d.split("-");
        if (lines.length>1){
            d=lines[0]+"\n";
            for (int i=1;i<lines.length;i++){
                if (i>1)d+="-";
                d+=lines[i].trim();
            }
        }
        return d;
    }

    private void setContextMenu(int position, String fname, String path, View v, String s) {
        PopupMenu menu = getPopupMenu(v);
        if (fileArrayList.get(position) instanceof Mp3File)
            menu.getMenu().add("info-->");
        if (s.length()>0)
            menu.getMenu().add("Large image");
        menu.show();

        //TODO: merge two onclick-listener together
        menu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Large image"))
                MainActivity.displayLargeImage(MainActivity.getInstance(), s);
            if (title.equals("info-->")){
                PopupMenu menu1 = new PopupMenu(v.getContext(), v);

                menu1.getMenu().add("spotify");
                menu1.getMenu().add("spotify-playlist");
                menu1.getMenu().add("artist-wikipedia");
                menu1.getMenu().add("album-wikipedia");
                menu1.getMenu().add("recommendation");
                menu1.show();
                menu1.setOnMenuItemClickListener(item1 -> {
                    Mp3File mp3File=(Mp3File)fileArrayList.get(position);
                    final MainActivity context1 = MainActivity.getInstance();
                    if (item1.getTitle().toString().equals("recommendation")) {
                        MainActivity.getRecommendation(mp3File.getArtist());
                    }else
                    if (item1.getTitle().toString().equals("spotify")) {
                        context1.callSpotify(mp3File.getArtist());
                    }else
                    if (item1.getTitle().toString().equals("spotify-playlist")) {
                        context1.callSpotifyPlaylist(mp3File.getArtist());
                    }else
                    if (item1.getTitle().toString().equals("artist-wikipedia")) {
                        MainActivity.startWikipediaPage(mp3File.getArtist());
                    }else
                    if (item1.getTitle().toString().equals("album-wikipedia")) {
                        MainActivity.startWikipediaPage(mp3File.getAlbum());
                    }
                    return true;


                });
                return true;
            }
            if (title.equals("Spotify")||title.equals("recommendation")){
                final MainActivity context1 = MainActivity.getInstance();
                try {
                    Mp3File mp3File = (Mp3File) fileArrayList.get(position);
                    if (title.equals("recommendation")) {
                        MainActivity.getRecommendation(mp3File.getArtist());
                    }else
                    context1.callSpotify(mp3File.getArtist());
                } catch (Exception e) {
                    String[] s1 = fname.split("-");
                    String art = s1[0].trim();
                    if (isInteger(art)) art = s1[1].trim();
                    art=art.replace("/", "");
                    if (title.equals("recommendation")) {
                        MainActivity.getRecommendation(art);
                    }else
                    context1.callSpotify(art);
                }

            } else

            if (!(fileArrayList.get(position) instanceof Mp3File)) {
                caller.newSambaCall(path + fname, title);
            } else
                caller.newSambaCall(((Mp3File) fileArrayList.get(position)).getMpcSong().file, title);

            return true;
        });
    }

    private void setImageForItem(int position, String fname, String path, ViewHolder holder, View finalView, float density, String filePath) {
        boolean smb=false;
        String url="";
        if (filePath.startsWith("smb")){
            //DebugLog.log(filePath);
            smb=true;
            url=filePath.replace("smb://192.168.2.8/FamilyLibrary","http://192.168.2.8:8081")+"folder.jpg";
            //return;
        }
        if (previousFilePath.equals(filePath)&&(position>0)){holder.image.setVisibility(View.GONE);return;}
        int m=countMatches(filePath,"/");
        if (m>0&& !smb) {
            url = setFolderPath(filePath);
            //DebugLog.log(url);
        }
        String mUrl=url;
        if (smb ||m>0)

            MainActivity.getInstance().imageLoader.DisplayImage(url, holder.image, bitmap ->
                    MainActivity.getInstance().runOnUiThread(() -> {

                holder.image.setVisibility(View.VISIBLE);
                int paddingPixel1 = 4;
                int paddingDp1 = (int)(paddingPixel1 * density);
                finalView.setPadding(0,paddingDp1,0,0);
                holder.description.setGravity(Gravity.CENTER_VERTICAL);
                holder.image.setOnClickListener(v -> setContextMenu(position, fname, path, holder.image, mUrl));

            }));

        previousFilePath =filePath;
    }

    @NonNull
    static String setFolderPath(String filePath) {
        return "http://192.168.2.8:8081/FamilyMusic/"+filePath+"/folder.jpg";
    }

    @NonNull
    private PopupMenu getPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(v.getContext(), v);

        menu.getMenu().add(context.getString(R.string.add_filelist));
        menu.getMenu().add(context.getString(R.string.addandplay_filelist));
        menu.getMenu().add(context.getString(R.string.replaceandplay_filelist));
        menu.getMenu().add(R.string.addtofavorites_filelist);
        menu.getMenu().add("Download");
        menu.getMenu().add("Spotify");
        menu.getMenu().add("recommendation");
        return menu;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private static class ViewHolder{
        TextView description;
        ImageView image;
    }


}