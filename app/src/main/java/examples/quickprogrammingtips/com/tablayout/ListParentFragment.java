package examples.quickprogrammingtips.com.tablayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import examples.quickprogrammingtips.com.tablayout.model.CustomComparator;
import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.HistoryListview;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import mpc.DatabaseCommand;
import mpc.MPCDatabaseListener;

/**
 * Created by anton on 29-1-16.
 * Display listview with contents of samba or mpd
 */
public  class ListParentFragment extends Fragment implements SambaInterface, MPCDatabaseListener {
    int listViewId = R.id.listViewFiles;
    int fragment_list = R.layout.fragment_list;
    protected FileListAdapter fileListAdapter;
    ArrayList<File> files = new ArrayList<>();NetworkShare networkShare;Logic logic;
    ArrayList<String>filesToCheck=new ArrayList<>();
    ListParentFragment listParentFragment;
    ListView fileListView;
    private int listViewPosition=0;

    private static Parcelable mListViewScrollPos = null;
    int level=0;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the ListView position
        if (mListViewScrollPos != null) {
            fileListView.onRestoreInstanceState(mListViewScrollPos);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the ListView position
        mListViewScrollPos = fileListView.onSaveInstanceState();
    }
    public ListParentFragment() {
        super();
    }
    @SuppressLint("ValidFragment")
    public ListParentFragment(int listViewId, int fragment_list) {
        super();
        this.listViewId=listViewId;
        this.fragment_list=fragment_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logic =((MainActivity)getActivity()).getLogic();
        listParentFragment=this;
        // Inflate the layout for this fragment

        View view = inflater.inflate(fragment_list, container, false);

        fileListView = (android.widget.ListView) view.findViewById(listViewId);
        fileListAdapter = new FileListAdapter(getActivity(),this, files);
        fileListView.setAdapter(fileListAdapter);

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.v("DBFragment", "DB fragment onstart");//chdb
    }

    @Override
    public void sambaCallCompleted(ArrayList<File> files1a, ArrayList<File> filesMp3, String id) {
        if (Objects.equals(id, "Download")) {
            ArrayList<String> files=new ArrayList<>();
            String dirname="artist-album";
            for (File f:filesMp3){
                if (f instanceof Mp3File)
                {
                    Mp3File mp=(Mp3File)f;
                    dirname=String.format("%s-%s" ,mp.getMpcSong().artist,mp.getMpcSong().album);
                }
                String filename = f.getPath() + "/" + f.getFname();
                files.add(filename);

            }
            NetworkShare.copyFile(files,dirname);
            return;
        }
        if (!isAdded()){
            MainActivity.panicMessage("ListFragment is detached from Activity");
            return;
        }

        //
        int count=0;
        for (File f:files1a){
            if (f instanceof Mp3File)count++;
        }
        if (count==0){
            for (File f:filesMp3){
                files1a.add(f);
            }
        }

        if (Objects.equals(id, getString(R.string.select_filelist))) {
            sortAndDisplay(files1a);
        }
    }

    public void sortAndDisplay(ArrayList<File> files1a) {
        try {
            try{
                files1a = logic.sort(files1a);
            } catch (Exception e) {
                DebugLog.log("error");
                Log.v("samba",Log.getStackTraceString(e));
            }

            final ArrayList<File> files1 = files1a;

            listParentFragment.getActivity().runOnUiThread(() -> {
                files.clear();
                files.addAll(files1);
                //hier checken voor eventuele toevoegingen aan beschrijvingen
                for (File f : files) {
                    if (f instanceof Mp3File) {
                        Mp3File mp = (Mp3File) f;
                        if (!mp.isFromMpd()) {
                            filesToCheck.add(mp.getTitle());
                        }
                    }
                }
                fileListAdapter.notifyDataSetChanged();
            });
            //workaround; the following line does not work:fileListView.setSelection(listViewPosition);
            fileListView.post(() -> {

                if (listViewPosition == 0)
                    fileListView.setSelection(listViewPosition);
                listViewPosition = -1;
            });
            if (filesToCheck.size() > 0) {
                String fname = filesToCheck.remove(0).trim().replace("'", "\'");
                new DatabaseCommand(MainActivity.getInstance().getLogic().getMpc(), "find title \"" + fname + "\"", listParentFragment, false, true).run();
            }

        } catch (Exception e) {
            DebugLog.log("error");
            Log.v("samba",Log.getStackTraceString(e));
        }
    }

    @Override
    public void newSambaCall(String path, String id) {
        //Log.v("samba","hier path in ListParent:"+path);
        if (!isAdded()){
            //MainActivity.panicMessage("ListFragment is detached from Activity");
            return;
        }
        if (id.equals(getString(R.string.addsong_filelist))){
            String message = "add \"" + path + "\"";
            logic.getMpc().sendSingleMessage(message);
        }else {
            if (id.equals("Download")) {

                displayContentOfDir(this,path, id);
            } else
            if (Objects.equals(id, getString(R.string.select_filelist))) {
                HistoryListview hl=new HistoryListview(path, fileListView.getFirstVisiblePosition());
                history().add(hl);
                level++;
                fileListAdapter.setLevel(level);
                DebugLog.log("select:"+level);

                displayContentOfDir(this, path, id);
                listViewPosition=0;
                //fileListView.setSelection(0);
            }
            else  if (Objects.equals(id, getString(R.string.addtofavorites_filelist))){
                String[] paths=path.split("/");
                SpotifyFragment.newFavorite(path, paths[paths.length-1], Favorite.NEWALBUM, "");
            }else {

                if (path.contains("FamilyLibrary"))
                    getContentOfDirAndPlay(path, id);
                else {

                    if (Logic.isMp3File(path)){

                        String[] ss=path.split("/");
                        path=path.substring(0,path.length()-ss[ss.length-1].length());
                    }
                    Logic logic = MainActivity.getInstance().getLogic();
                    int toplay = logic.getToplay(id);
                    boolean clear = logic.isClear(id);
                    ArrayList<String>commands=new ArrayList<>();
                    if (clear)
                        commands.add("clear");
                    path = Logic.removeSlashAtEnd(path);
                    String s = "add \""+path+"\"";

                    commands.add(s);
                    logic.getMpc().enqueCommands(commands);
                    logic.playWithDelay(id, toplay);
                }
            }
        }
    }



    public ArrayList<HistoryListview> history() {
        return logic.getHistory();
    }

    public void displayContentOfDir(SambaInterface si,String path, String id) {
    }

    public void getContentOfDirAndPlay(String path, String id) {
    }

    protected void goBack(ArrayList<HistoryListview >history){
        int position=0;
        level--;
        fileListAdapter.setLevel(level);
        DebugLog.log("back:"+level);

        String newPath="";
        for (int j=1;j<=2;j++) {
            int last = history.size() - 1;
            newPath= history.get(last).path;
            if (j==1) {
                position = history.get(last).position;
                if (last>=1) history.remove(last);
            }
        }

        displayContentOfDir(this, newPath, getString(R.string.select_filelist));
        //restore position. Display it when new data is received
        listViewPosition=position;

    }

    @Override
    public void databaseUpdated() {

    }

    @Override
    public void databaseUpdateProgressChanged(int progress) {

    }

    @Override
    public void connectionFailed(String message) {

    }

    @Override
    public void databaseCallCompleted(ArrayList<File> files) {

    }

    @Override
    public void databaseFindCompleted(ArrayList<File> files1a) {
        for (File f1a:files1a)
            if (f1a instanceof Mp3File) {
                Mp3File found = (Mp3File) f1a;
                for (File f : files) {
                    if (f instanceof Mp3File) {
                        Mp3File mp = (Mp3File) f;
                        if (mp.getTitle().trim().equals(found.getTitle()) && mp.getAlbum().toLowerCase().equals(found.getAlbum().toLowerCase())) {
                            mp.setTime(found.getTime());
                            mp.setArtist(found.getArtist());
                            mp.setTracknr(found.getTracknr());
                            mp.setFromMpd(true);
                            fileListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        if (filesToCheck.size()>0){
            String fname=filesToCheck.remove(0).trim().replace("'","\'");
            new DatabaseCommand(MainActivity.getInstance().getLogic().getMpc(),"find title \""+fname+"\"",listParentFragment,false,true).run();
        } else
            Collections.sort(files, new CustomComparator());
    }
}
