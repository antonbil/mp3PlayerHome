package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import examples.quickprogrammingtips.com.tablayout.adapters.FileListAdapter;
import examples.quickprogrammingtips.com.tablayout.model.CustomComparator;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import examples.quickprogrammingtips.com.tablayout.tools.NetworkShare;
import mpc.DatabaseCommand;
import mpc.MPCDatabaseListener;

/**
 * Created by anton on 29-1-16.
 */
public  class ListParentFragment extends Fragment implements SambaInterface, MPCDatabaseListener {
    protected FileListAdapter fileListAdapter;
    ArrayList<File> files = new ArrayList<>();NetworkShare networkShare;Logic logic;
    ArrayList<String>filesToCheck=new ArrayList<>();
    ListParentFragment listParentFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        logic =((MainActivity)getActivity()).getLogic();
        listParentFragment=this;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        final ListView fileListView = (android.widget.ListView) view.findViewById(R.id.listViewFiles);
        fileListAdapter = new FileListAdapter(getActivity(),this, files);
        fileListView.setAdapter(fileListAdapter);
        //registerForContextMenu(fileListView);

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.v("DBFragment", "DB fragment onstart");//chdb
    }

    @Override
    public void sambaCallCompleted(ArrayList<File> files1a, ArrayList<File> filesMp3, String id) {
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

        if (id==getString(R.string.select_filelist)) {
            sortAndDisplay(files1a);
        } else{

        }
    }

    public void sortAndDisplay(ArrayList<File> files1a) {
        try {
            files1a = logic.sort(files1a);
            //Collections.sort(files1a, new CustomComparator());

            files.clear();
            final ArrayList<File> files1 = files1a;
            getActivity().runOnUiThread(new Runnable() {
                public String albumCheck;

                @Override
                public void run() {
                    files.addAll(files1);
                    //hier checken voor eventuele toevoegingen aan bescchrijvingen
                    for (File f:files)
                        if (f instanceof Mp3File){
                            Mp3File mp=(Mp3File)f;
                            if(!mp.isFromMpd()){
                            //if (false){
                                filesToCheck.add(mp.getTitle());
                                this.albumCheck=mp.getAlbum();
                            }
                        }
                    if (filesToCheck.size()>0){
                        String fname=filesToCheck.remove(0).trim().replace("'", "\'");
                        new DatabaseCommand(MainActivity.getThis.getLogic().getMpc(),"find title \""+fname+"\"",listParentFragment,false,true).run();
                        Log.v("samba", "now search " + fname);
                    }
                    fileListAdapter.notifyDataSetChanged();

                }
            });
        }catch (Exception e){}
    }

    @Override
    public void newSambaCall(String path, String id) {
        Log.v("samba","hier path in ListParent:"+path);
        if (!isAdded()){
            MainActivity.panicMessage("ListFragment is detached from Activity");
            return;
        }
        if (id.equals(getString(R.string.addsong_filelist))){
            String message = "add \"" + path + "\"";
            logic.getMpc().sendSingleMessage(message);
        }else {
            if (id==getString(R.string.select_filelist))
                displayContentOfDir(this,path, id);
            else  if (id==getString(R.string.addtofavorites_filelist)){
                String[] paths=path.split("/");
                FavoriteRecord fv=new FavoriteRecord(path, paths[paths.length-1], "2nd edition");
                fv.save();
            }else {

                if (path.contains("FamilyLibrary"))
                    getContentOfDirAndPlay(path, id);
                else {

                    if (Logic.isMp3File(path)){

                        String[] ss=path.split("/");
                        path=path.substring(0,path.length()-ss[ss.length-1].length());
                    }
                    Logic logic = MainActivity.getThis.getLogic();
                    int toplay = logic.getToplay(id);
                    boolean clear = logic.isClear(id);
                        //logic.getMpc().clearPlaylist();
                    ArrayList<String>commands=new ArrayList<>();
                    if (clear)
                        commands.add("clear");
                    path = Logic.removeSlashAtEnd(path);
                    String s = "add \""+path+"\"";
                    //Log.v("samba","command:"+s);

                    commands.add(s);
                    logic.getMpc().enqueCommands(commands);
                    logic.playWithDelay(id, toplay);
                }
            }
            if (id == getString(R.string.select_filelist)) {

                history().add(path);
            }
        }
    }



    public ArrayList<String> history() {
        return logic.getHistory();
    }

    public void displayContentOfDir(SambaInterface si,String path, String id) {
    }

    public void getContentOfDirAndPlay(String path, String id) {
    }

    protected void goBack(ArrayList<String >history){
        String newPath="";
        for (int j=1;j<=2;j++) {
            int last = history.size() - 1;//chdb
            newPath= history.get(last);//chdb
            if (last>=1) history.remove(last);//chdb
        }
        history.add(newPath);//chdb
        displayContentOfDir(this, newPath, getString(R.string.select_filelist));
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
        for (File f1a:files1a) {

            if (f1a instanceof Mp3File) {
                Mp3File found = (Mp3File) f1a;
                //Log.v("samba", "Found " + found.getTitle() + " and " + found.getAlbum());
                for (File f : files) {
                    Mp3File mp = (Mp3File) f;
                    //Log.v("samba", "compare " + mp.getTitle().trim() +mp.getAlbum()+ " and " + found.getTitle()+found.getAlbum());
                    if (mp.getTitle().trim().equals(found.getTitle()) && mp.getAlbum().toLowerCase().equals(found.getAlbum().toLowerCase())) {
                        //Log.v("samba", "equal " + mp.getTitle() + " and " + found.getAlbum());
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
            //Log.v("samba","now search "+fname);
            new DatabaseCommand(MainActivity.getThis.getLogic().getMpc(),"find title \""+fname+"\"",listParentFragment,false,true).run();
        } else
            Collections.sort(files, new CustomComparator());
    }
}
