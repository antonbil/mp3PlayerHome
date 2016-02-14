package examples.quickprogrammingtips.com.tablayout.model;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.R;
import examples.quickprogrammingtips.com.tablayout.SambaInterface;
import mpc.MPC;
import mpc.MPCSong;
import mpc.MPCStatus;
import mpc.MusicDatabase;

/**
 * Created by anton on 24-1-16.
 */
public class Logic  implements SambaInterface {
    private MPC mpc;
    public static final int DEFAULT_PORT = 6600;
    private String basePath = "smb://192.168.2.8/FamilyLibrary/TotalMusic/";
    private boolean paused=false;
    private ArrayList<String> history=new ArrayList<>(Arrays.asList(basePath));
    private ArrayList<String> historyMpd=new ArrayList<>(Arrays.asList(""));
    private CopyOnWriteArrayList<Mp3File> playlistFiles = new CopyOnWriteArrayList<>();
    MainActivity activity;
    public MPCStatus mpcStatus;
    public static boolean isMp3File(String name) {
        return name.endsWith(".mp3")|| name.endsWith(".m4a")|| name.endsWith(".flac")|| name.endsWith(".mpc");
    }

    @NonNull
    public static String getUrlFromSongpath(Mp3File currentSong) {
        String file=currentSong.getFile();
        return "http://192.168.2.8:8081/FamilyMusic/"+file.substring(0, file.lastIndexOf("/"))+"/folder.jpg";
    }
    @NonNull
    public static String removeSlashAtEnd(String path) {
        if (path.endsWith("/"))path=path.substring(0,path.length()-1);
        return path;
    }
    public Logic(MainActivity activity){
        openServer(Server.servers.get(Server.getServer(activity)).url);
        getMpc().setMPCListener(activity);
        this.activity=activity;

    }

    public void openServer(String address) {
        MusicDatabase db=new MusicDatabase() {
            @Override
            public void clear() {

            }

            @Override
            public void addSong(MPCSong song) {

            }

            @Override
            public void startTransaction() {

            }

            @Override
            public void setTransactionSuccessful() {

            }

            @Override
            public void endTransaction() {

            }
        };
        //address = "192.168.2.16";
        setMpc(new MPC(address, DEFAULT_PORT, 1000, db));
        mpc.sendSingleMessage("consume 0");
        mpc.sendSingleMessage("random 0");
        mpc.sendSingleMessage("repeat 1");
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<String> history) {
        this.history = history;
    }

    public MPC getMpc() {
        return mpc;
    }

    public void setMpc(MPC mpc) {
        this.mpc = mpc;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean getPaused() {
        return paused;
    }

    public CopyOnWriteArrayList<Mp3File> getPlaylistFiles() {
        return playlistFiles;
    }

    public void setPlaylistFiles(CopyOnWriteArrayList<Mp3File> playlistFiles) {
        this.playlistFiles = playlistFiles;
    }

    @Override
    public void sambaCallCompleted(ArrayList<File> files1a, ArrayList<File> filesMp3, String id) {
        //Log.v("samba","hier1"+files1a.size()+","+filesMp3.size());
        int toplay = getToplay(id);
        boolean clear = isClear(id);
        //todo: code is coded twice, also in ListParentFragment
        int count=0;
        for (File f:files1a){
            if (f instanceof Mp3File)count++;
        }
        if (count==0){
            for (File f:filesMp3){
                files1a.add(f);
            }
        }
        //Log.v("samba","hier2,"+count);

        ArrayList<MPCSong> songs = new ArrayList<>();
        for (File file : files1a) {
            //Log.v("samba",file.getFname());
            if (file instanceof Mp3File) {
                //Log.v("samba", "added");
                songs.add(((Mp3File) file).getMpcSong());
            }
        }

        getMpc().enqueSongs(songs, clear);
        playWithDelay(id, toplay);


    }

    public void commandWithDelay(final String command) {

            new Thread() {

                @Override
                public void run() {
                    try {

                        Thread.sleep(1000);
                        try {
                            getMpc().sendSingleMessage(command);
                        } catch (Exception e) {
                            //mpc.connectionFailed("Connection failed, check settings");
                            //t.stop();
                        }

                    } catch (InterruptedException e) {
                    }
                }
            }.start();

    }
    public void playWithDelay(String id, int toplay) {
        if ((id == activity.getString(R.string.replaceandplay_filelist)) || (id == activity.getString(R.string.addandplay_filelist))) {
            final int play = toplay;
            new Thread() {

                @Override
                public void run() {
                    try {

                        Thread.sleep(1000);
                        try {
                            getMpc().play(play);
                        } catch (Exception e) {
                            //mpc.connectionFailed("Connection failed, check settings");
                            //t.stop();
                        }

                    } catch (InterruptedException e) {
                    }
                }
            }.start();
        }
    }

    public boolean isClear(String id) {
        boolean clear = false;
        if (id == activity.getString(R.string.replaceandplay_filelist)) {
            clear = true;
        }
        return clear;
    }

    public int getToplay(String id) {
        int toplay = 0;
        if (id == activity.getString(R.string.addandplay_filelist)) {
            toplay = getPlaylistFiles().size();
        }
        return toplay;
    }

    @Override
    public void newSambaCall(String path, String id) {

    }

    public ArrayList<String> getHistoryMpd() {
        return historyMpd;
    }

    public void setHistoryMpd(ArrayList<String> historyMpd) {
        this.historyMpd = historyMpd;
    }

    public ArrayList<File> sort(ArrayList<File> files1a) {
        Collections.sort(files1a, new CustomComparator());
        return files1a;
    }
}

