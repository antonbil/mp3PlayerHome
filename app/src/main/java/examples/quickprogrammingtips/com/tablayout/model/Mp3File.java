package examples.quickprogrammingtips.com.tablayout.model;

/**
 * Created by anton on 23-1-16.
 */

import android.graphics.Bitmap;

import java.util.ArrayList;

import mpc.MPCSong;

public class Mp3File extends File{
    private int time;
    private String file;
    private String title;
    private String album;
    private String artist;
    private int tracknr;
    private MPCSong mpcSong;
    private int year;
    private boolean fromMpd;
    public boolean radio=false;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;

    public Mp3File(String path,ArrayList<String> s) {

        setPath((path));
        this.setTime(0);
        for (int i=0;i<s.size();i++){
            if (s.get(i).startsWith("file:")){
                this.setFile(s.get(i).split("file:")[1].trim());
            }
            if (s.get(i).startsWith("Artist:")){
                this.setArtist(s.get(i).split("Artist:")[1].trim());
            }
            if (s.get(i).startsWith("Album:")){
                this.setAlbum(s.get(i).split("Album:")[1].trim());
            }
            if (s.get(i).startsWith("Title:")){
                String title = s.get(i).split("Title:")[1].trim();
                setFname(title);
                this.setTitle(title);
            }
            if (s.get(i).startsWith("Date:")){
                String s1="";
                try{
                    s1 = s.get(i).split("Date:")[1].trim();
                    this.setYear(Integer.parseInt(s1));
                } catch (Exception e) {
                    try{
                        this.setYear(Integer.parseInt(s1.substring(0, 4)));
                    } catch (Exception e1) {

                        e1.printStackTrace();
                    }
                }
            }
            if (s.get(i).startsWith("Track:")){
                try{
                    this.setTracknr(Integer.parseInt(s.get(i).split("Track:")[1].split("/")[0].trim()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (s.get(i).startsWith("Time:")){
                try{
                    this.setTime(Integer.parseInt(s.get(i).split("Time:")[1].trim()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (this.getTime()<=0){
            this.radio=true;
        }
        this.setMpcSong(new MPCSong(file, 0, artist, title, album, tracknr));

    }
    public Mp3File(){

    }
    public static String removePath(String s){
        return s.split("/home/wieneke/FamilyLibrary/FamilyMusic/")[1];
    }
    public Mp3File(String path,String s){
        setPath(path);
        fromMpd=true;
        String[] hs=s.split("=== ");
        this.setFile(removePath(hs[0]));
        if (hs[0].startsWith("/home/wieneke/FamilyLibrary/FamilyMusic/"))
        for (int i=1;i<hs.length;i++){
            if(hs[i].startsWith("TIT2")){
                try {
                    String title = hs[i].split("TIT2")[1].trim();
                    this.setTitle(title);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(hs[i].startsWith("TALB")){
                try {
                    this.setAlbum(hs[i].split("TALB")[1].trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(hs[i].startsWith("TPE1")){
                try {
                    this.setArtist(hs[i].split("TPE1")[1].trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(hs[i].startsWith("TRCK")){
                try {
                    this.setTracknr(Integer.parseInt(hs[i].split("TRCK")[1].split("/")[0].trim()));
                } catch (Exception e) {
                    this.setTracknr(1);
                }
            }
            if(hs[i].startsWith("TYER")){
                try {
                    this.setYear(Integer.parseInt(hs[i].split("TYER")[1].trim()));
                } catch (Exception e) {
                    this.setYear(2016);
                }
            }
            if(hs[i].startsWith("TIME")){
                try {
                    this.setTime(Integer.parseInt(hs[i].split("TIME")[1].trim()));
                } catch (Exception e) {
                    this.setTime(400);
                }
            }
        }
        this.setMpcSong(new MPCSong(file, 0, artist, title, album, tracknr));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getTracknr() {
        return tracknr;
    }

    public void setTracknr(int tracknr) {
        this.tracknr = tracknr;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public MPCSong getMpcSong() {
        return mpcSong;
    }

    public void setMpcSong(MPCSong mpcSong) {
        this.mpcSong = mpcSong;
    }

    public int getTime() {
        return time;
    }

    public boolean isStartAlbum() {
        return startAlbum;
    }

    public void setStartAlbum(boolean startAlbum) {
        this.startAlbum = startAlbum;
    }

    private boolean startAlbum=false;

    public void setTime(int time) {
        this.time = time;
    }
    public String getTimeNice(){
        return niceTime(time);
    }

    public static String niceTime(int time) {
        return niceString(time);
    }

    public static String niceString(int time) {
        int min=time/60;
        int sec=time-min*60;
        return String.format("%02d:%02d", min, sec);
    }

    public String niceAlbum(){
        return String.format("%s-%s", getArtist(), getAlbum());
    }

    public boolean isFromMpd() {
        return fromMpd;
    }

    public void setFromMpd(boolean fromMpd) {
        this.fromMpd = fromMpd;
    }
}
