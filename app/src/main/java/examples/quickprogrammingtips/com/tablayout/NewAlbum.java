package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 25-6-16.
 */
public class NewAlbum {
    private String image;
    public String url,artist,album;
    public NewAlbum(String url,String artist,String album){
        this(url,artist,album,"");

    }

    public NewAlbum(String url, String artist, String album, String image) {
        this.url=url;
        this.artist=artist;
        this.album=album;
        this.setImage(image);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
