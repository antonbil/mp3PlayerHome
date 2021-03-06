package examples.quickprogrammingtips.com.tablayout.tools;

/**
 * Created by anton on 10-1-16.
 */
/**
 * @author Kushal Paudyal
 * Create on 2012-10-12
 * Last Modified On 2012-10-12
 * www.sanjaal.com/java, www.icodejava.com
 *
 * JCIFS is an Open Source client library that implements the CIFS/SMB networking protocol in 100% Java.
 * CIFS is the standard file sharing protocol on the Microsoft Windows platform
 * Visit their website at: jcifs.samba.org
 *
 * Uses: jcifs-1.1.11.jar
 *
 */
//example MPD: https://github.com/Ichimonji10/impedimenta/blob/master/Android/JeremysJamminJukebox/trunk/src/net/JeremyAudet/JeremysJamminJukebox

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.MainActivity;
import examples.quickprogrammingtips.com.tablayout.SambaInterface;
import examples.quickprogrammingtips.com.tablayout.model.File;
import examples.quickprogrammingtips.com.tablayout.model.Logic;
import examples.quickprogrammingtips.com.tablayout.model.Mp3File;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import mpc.DatabaseCommand;
import mpc.MPCDatabaseListener;
import mpc.MPCSong;

public class NetworkShare  implements MPCDatabaseListener{
    static final String USER_NAME = "wieneke";
    static final String PASSWORD = "wieneke";

    public static final int DEFAULT_PORT = 6600;

    static final String NETWORK_FOLDER = "smb://192.168.2.8/FamilyLibrary/favorites/m/";///Paul McCartney/1973 - Band On The Run/";
    private String albumToSearch;
    private boolean waitForCallBack;
    private SambaInterface caller;
    private NetworkShare networkShare;
    private String id;
    private ArrayList<File> filesMp3;
    private ArrayList<File> files;

    public void getContent(final SambaInterface caller,String path, final String id){
        this.id=id;
        networkShare=this;
        final String networkPath=path;
        this.caller=caller;

        Thread  thread = new Thread(new Runnable() {
            public ArrayList<File> filesMp3;

            @Override
            public void run() {
                ArrayList<File> files=new ArrayList<>();
                ArrayList<File> filesMp3=new ArrayList<>();
                String mp3FileForDatabaseCommand="";
                try{
                    System.setProperty("jcifs.smb.client.responseTimeout", "1000"); // default: 30000 millisec.
                    System.setProperty("jcifs.smb.client.soTimeout", "1000"); // default: 35000 millisec.
                    NtlmPasswordAuthentication auth = getNtlmPasswordAuthentication();

                    waitForCallBack=false;
                    SmbFile dir = new SmbFile(""+networkPath, auth);
                    for (SmbFile f : dir.listFiles()) {
                        String name = f.getName();
                        String parent = f.getParent();
                        if (f.isDirectory()){
                            //Log.v("samba", f.getURL().getPath());
                            File file=new File(name, parent);
                            files.add(file);
                            //Log.v("samba","a:"+file.toString()+";");
                        } else

                        try
                        {
                            if (Logic.isMp3File(name)){
                                String album="";
                                int p=parent.indexOf("/FamilyMusic/");
                                if (p>0)parent=parent.substring(p+"/FamilyMusic/".length()); else{
                                    String n=name;
                                    while (n.length()>0 && (Character.isDigit(n.toCharArray()[0])) || n.toCharArray()[0]=='-')
                                        n=n.substring(1).trim();
                                    int l1=n.lastIndexOf(".");
                                    n=n.substring(0,l1);
                                    String[] l=parent.split("/");
                                    String last=l[l.length-1];
                                    l=last.split("-");
                                    last=l[l.length-1];
                                    while (last.length()>0 && (Character.isDigit(last.toCharArray()[0])) || last.toCharArray()[0]=='-')
                                        last=last.substring(1).trim();
                                    while (last.length()>0 && (Character.isDigit(last.toCharArray()[last.length()-1])) || last.toCharArray()[last.length()-1]==')' || last.toCharArray()[last.length()-1]=='(')
                                        last=last.substring(0,last.length()-1).trim();
                                    album=last;
                                    //Log.v("samba","find song:"+n+", and album:"+last);
                                    albumToSearch=last;

                                    //split string at /, last is filename, before: split at -, last is album
                                    //remove all spaces and digits at start of string.

                                    if (!waitForCallBack)
                                        mp3FileForDatabaseCommand=n;

                                    parent=parent.replace("smb://192.168.2.8", "/home/wieneke");
                                    waitForCallBack=true;
                                }

                                Mp3File file = new Mp3File();
                                file.setPath(parent);
                                file.setFromMpd(false);
                                file.setFname(name);
                                filesMp3.add(file);
                                String s = String.format("%s%s", parent, name);
                                //Log.v("samba", s);
                                MPCSong mp=new MPCSong(s,0,"", name,"",0);
                                file.setTime(0);
                                file.setAlbum(album);
                                int l=name.lastIndexOf(".");
                                name=name.substring(0,l);
                                //parse name
                                String[] nameParts=name.split("-");
                                if (nameParts.length==2){
                                    file.setTracknr(Integer.parseInt(nameParts[0].trim()));
                                    file.setTitle(nameParts[1].trim());
                                    file.setArtist("");
                                }else
                                if (nameParts.length==3){
                                    file.setTracknr(Integer.parseInt(nameParts[0].trim()));
                                    file.setArtist(nameParts[1]);
                                    file.setTitle(nameParts[2].trim());
                                }else {
                                    file.setTitle(name.trim());
                                    file.setArtist("");
                                }
                                file.setMpcSong(mp);
                                //countmp3++;
                                //file.setTracknr(countmp3);
                            }
                    } catch (Exception exception) {
                        //Log.v("samba", "Errorin");
                        exception.printStackTrace();
                    }
                    }
                    SmbFile smbFile = new SmbFile(networkPath+"mp3info.txt", auth);
                    ArrayList<String> builder;
                    try {
                        builder = readFileContent(smbFile);
                        String dirname="";
                        for (int i=0;i<builder.size();i++){
                            try{
                                //Log.v("samba","log:"+builder.get(i));
                                try{
                                    if (i==0){dirname=Mp3File.removePath(builder.get(0));continue;}
                                } catch (Exception exception) {}
                                Mp3File mp=new Mp3File(networkPath,builder.get(i));
                                if (mp.getTitle()!=null) {
                                    if (!mp.getMpcSong().file.startsWith(dirname)){
                                        //dirname is better than path itself, because , can be removed.
                                        String[] spl=mp.getMpcSong().file.split("/");
                                        mp.getMpcSong().file=dirname+"/"+spl[spl.length-1];
                                    }
                                    waitForCallBack=false;
                                    files.add(mp);
                                }
                            } catch (Exception exception) {
                                //Log.v("samba", "Errorin");
                                exception.printStackTrace();
                            }
                            //Log.v("samba", mp.getArtist() + "-" + mp.getTracknr() + "-" + mp.getTitle() + "(" + mp.getTimeNice() + ")");
                            //songs.add(mp.getMpcSong());
                        }
                    } catch (Exception exception) {
                        //Log.v("samba", "Errorin");
                        exception.printStackTrace();
                    }

                } catch (Exception e) {
                    //successful = false;
                    e.printStackTrace();
                    //Log.v("samba", "Errorout: ");
                }
                networkShare.files=files;
                networkShare.filesMp3=filesMp3;
                //Log.v("samba", "id:"+id);
                if (!waitForCallBack)
                    caller.sambaCallCompleted(files,filesMp3, id);
                else
                    new DatabaseCommand(MainActivity.getInstance().getLogic().getMpc(),"find title \""+mp3FileForDatabaseCommand+"\"",networkShare,false,true).run();
            }
        });
        thread.start();
    }

    @NonNull
    public static NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
        String user = USER_NAME + ":" + PASSWORD;

        return new NtlmPasswordAuthentication(user);
    }


    private ArrayList<String> readFileContent(SmbFile sFile) {
        StringBuilder builder=new StringBuilder();
        ArrayList<String> list=new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(sFile)));
        } catch (Exception ex) {

            return list;
        }
        String lineReader = null;
        {
            try {
                while ((lineReader = reader.readLine()) != null) {
                    list.add(lineReader);
                    builder.append(lineReader).append("\n");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {

                }
            }
        }
        return list;
    }

    public static void copyFile(final ArrayList<String> files, final String dirname){

        new AsyncTask<Void, Void, Void>(){

            ProgressDialog dialog1 = MainActivity.getInstance().dialog;
            Handler updateBarHandler = MainActivity.getInstance().updateBarHandler;

            @Override
            protected void onPostExecute(Void result) {
                //todo see if this code is executed already at end of doinbackground
                updateBarHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (dialog1.isShowing())
                            dialog1.dismiss();
                    }
                });
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final ArrayList<SmbFile>smbFiles=new ArrayList<>();
                    NtlmPasswordAuthentication auth = getNtlmPasswordAuthentication();
                    String destinationDirName="New Dir";

                    String originalDir="";
                    for (String originalFilename:files){
                        String[]list=originalFilename.split("/");
                        try {
                            originalFilename=originalFilename.replace("/home/wieneke/FamilyLibrary","smb://192.168.2.8/FamilyLibrary");
                            SmbFile from = new SmbFile(originalFilename, auth);
                            smbFiles.add(from);
                            originalDir = originalFilename.replace(list[list.length - 1], "");
                            destinationDirName = list[list.length - 3];
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    smbFiles.add(new SmbFile(originalDir + "folder.jpg", auth));
                    if (!dirname.startsWith("-"))destinationDirName=dirname;
                    final String destinationDirNameToUse=destinationDirName;
                    updateBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog1=new ProgressDialog(MainActivity.getInstance());
                            dialog1.setTitle("Downloading to " + destinationDirNameToUse);
                            dialog1.setMessage("To:" + destinationDirNameToUse);
                            dialog1.setProgressStyle(dialog1.STYLE_HORIZONTAL);
                            dialog1.setProgress(0);
                            dialog1.setMax(files.size());
                            dialog1.show();
                        }
                    });

                    //create necessary dirs
                    String sdCard = Environment.getExternalStorageDirectory().toString();
                    final String dest=String.format("%s/Music/", sdCard, dirname);
                    String createPath = dest + "pgplay";
                    java.io.File directory = new java.io.File(createPath);
                    directory.mkdirs();
                    createPath += "/"+destinationDirName;
                    directory = new java.io.File(createPath);
                    directory.mkdirs();
                    //copy files
                     for (final SmbFile smbFile: smbFiles) {
                        String destFilename;
                        destFilename = String.format("%s/%s",createPath,smbFile.getName());
                         updateBarHandler.post(new Runnable() {
                             @Override
                             public void run() {
                                 dialog1.setMessage("file:" + smbFile.getName());
                             }
                         });
                         try {
                            FileOutputStream fileOutputStream;
                            InputStream fileInputStream;
                            byte[] buf;
                            int len;
                            fileOutputStream = new FileOutputStream(destFilename);
                            fileInputStream = smbFile.getInputStream();
                            buf = new byte[16 * 1024 * 1024];
                            while ((len = fileInputStream.read(buf)) > 0) {
                                fileOutputStream.write(buf, 0, len);
                            }
                            fileInputStream.close();
                            fileOutputStream.close();

                            updateBarHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    dialog1.incrementProgressBy(1);
                                }
                            });
                        } catch (Exception e) {
                             Log.v("samba", Log.getStackTraceString(e));
                            e.printStackTrace();

                        }
                    }
                    updateBarHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //todo see if this code is executed already at postexecute
                            if (dialog1.isShowing())
                            dialog1.dismiss();
                        }
                    });
                } catch (Exception e) {
                    Log.v("samba", Log.getStackTraceString(e));
                    e.printStackTrace();
                }

                return null;
            }

        }.execute();

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
    public void databaseCallCompleted(ArrayList<File> files1a) {

    }
    @Override
    public void databaseFindCompleted(ArrayList<File> files1a) {
            for (File f:files1a){

                if (f instanceof Mp3File){
                    Mp3File mp=(Mp3File)f;
                    //Log.v("samba","file1a:"+mp.getFile());
                    if (mp.getFile().toLowerCase().contains(this.albumToSearch.toLowerCase())){
                        //found it!
                        String path=mp.getFile();
                        String[] l=path.split("/");
                        String last=l[l.length-1];
                        path=path.substring(0,path.length()-last.length()-1);
                        int i=1;

                        for (File f2:filesMp3){
                            if (f2 instanceof Mp3File){
                                Mp3File mp2=(Mp3File)f2;
                                //mp2.setFname(mp.getFname());
                                mp2.setPath(path);
                                String file = path +"/"+ mp2.getFname();
                                //Log.v("samba",file);
                                mp2.setFile(file);
                                mp2.setAlbum(mp.getAlbum());
                                mp2.setArtist(mp.getArtist());
                                mp2.setTracknr(i);
                                mp2.setTime(2);
                                mp2.setMpcSong(new MPCSong(mp2.getFile(),2,mp2.getArtist(),mp2.getTitle(),mp2.getAlbum(),i));//String file, int time, String artist, String title, String album, int track)
                                i++;
                                //mp2.getMpcSong().file=file;
                            }
                        }
                    }
                }
            }
            caller.sambaCallCompleted(files,this.filesMp3, id);

        }

}
