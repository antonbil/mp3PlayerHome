package examples.quickprogrammingtips.com.tablayout.model;

import java.util.ArrayList;

/**
 * Created by anton on 8-1-16.
 * Album contains Songs, and is an Album of a Performer
 */
public class Album {
    //fields
    private static ArrayList<Album> albums;
    private Performer performer;
    private String title;

    public Album(){
    }

    /**
     *
     * @param performer: performer of album
     * @param title: title of album
     */
    public Album(Performer performer,String title){
        this.performer=performer;
        this.title=title;
    }
    public static ArrayList<String> listAlbums(){
        ArrayList<String> returnValue=new ArrayList<>();
        for (int i=0;i<albums.size();i++){
            Album album=albums.get(i);
            String s=album.getPerformer().getName()+"-"+album.getTitle();
            returnValue.add(s);
        }
        return returnValue;
    }
    public static void setAlbums(ArrayList<Album> albumsparameter){
        albums=albumsparameter;
    }
    public Performer getPerformer() {
        return performer;
    }

    public void setPerformer(Performer performer) {
        this.performer = performer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
