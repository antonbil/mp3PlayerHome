package examples.quickprogrammingtips.com.tablayout.model;

/**
 * Created by anton on 8-1-16.
 * Song is part of an Album
 */
public class Song {
    //fields for Song
    private int length;
    private String title;
    private Album album;

    public Song() {
    }

    /**
     * new constructor van Song
     * @param title: title of song
     */
    public Song(String title) {
        this.title=title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
