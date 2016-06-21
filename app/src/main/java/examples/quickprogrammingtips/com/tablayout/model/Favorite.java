package examples.quickprogrammingtips.com.tablayout.model;

import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

import examples.quickprogrammingtips.com.tablayout.MainActivity;

/**
 * Created by anton on 23-1-16.
 */
public class Favorite {
    public static final String SMBPREFIX = "smb://";
    public static final String SPOTIFYPLAYLISTPREFIX = "https://open.spotify.com/user/";
    public static final String SPOTIFYALBUM = "spotifyalbum://";
    public static final String NEWALBUM = "2nd edition";
    public static final String SPOTIFYPRIVATEPLAYLIST = "spotify://";
    public static ArrayList<String> categoryIds=new ArrayList<>(Arrays.asList(NEWALBUM,"3","4","5","6","7","8","9","10","11"));
    private static ArrayList<String> categoryDescriptions=new ArrayList<>(Arrays.asList("New Links","Classical","Symphonic Rock","Electronic","Soul","Singer/Songwriter","Alternative","Rock","Ambient","Various"));
    public static String getCategory(int i){
        String r="";
        r = PreferenceManager.getDefaultSharedPreferences(MainActivity.getThis).getString("category"+(i+1), categoryDescriptions.get(i));

        return r;
    }
    private String uri;
    private String description;
    private String category;
    private String sortkey;

    public FavoriteRecord getRecord() {
        return record;
    }

    public void setRecord(FavoriteRecord record) {
        this.record = record;
    }

    public boolean isSambaItem(){
        return uri.startsWith(SMBPREFIX);
    }

    public boolean isSpotifyItem(){
        return uri.startsWith(SPOTIFYPLAYLISTPREFIX)||uri.startsWith(SPOTIFYPRIVATEPLAYLIST)||uri.startsWith(SPOTIFYALBUM);
    }
    private FavoriteRecord record;
    public Favorite(String uri, String description, String category){
        this( uri,  description,  category,"");
    }
    public Favorite(String uri, String description, String category, String sortkey){
        this.uri=uri;
        this.description=description;
        this.category=category;

        this.setSortkey(sortkey);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSortkey() {
        return sortkey;
    }

    public void setSortkey(String sortkey) {
        this.sortkey = sortkey;
    }
}
