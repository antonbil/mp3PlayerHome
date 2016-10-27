package examples.quickprogrammingtips.com.tablayout.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import examples.quickprogrammingtips.com.tablayout.SpotifyFragment;

/**
 * Created by anton on 23-1-16.
 */
public class Favorite {
    public static final String SMBPREFIX = "smb://";
    public static final String SPOTIFYPLAYLISTPREFIX = "https://open.spotify.com/user/";
    public static final String SPOTIFYALBUM = "spotifyalbum://";
    public static final String NEW_LINKS = "New Links";
    public static  String NEWALBUM = "2nd edition";
    public static final String SPOTIFYPRIVATEPLAYLIST = "spotify://";
    /*
    two arraylists for managing genres of albums.
    categoryDescriptions: contains the genre-descriptions
    categoryIds: contains the ids for the genre-description that are stored
    todo: clumsy programming. lists are generated again and again. Can be turned into singleton to generate lists only once.
     */
    private static ArrayList<String> categoryIds=new ArrayList<>(Arrays.asList(NEWALBUM,"3","4","5","6","7","8","9","10","11"));
    private static ArrayList<String> categoryDescriptions;//=new ArrayList<>(Arrays.asList("New Links","Classical","Symphonic Rock","Electronic","Soul","Singer/Songwriter","Alternative","Rock","Ambient","Various"));
    public static String getCategoryDescription(int i){
        generateLists();
        String r="";
        r=categoryDescriptions.get(i);
        //r = PreferenceManager.getDefaultSharedPreferences(MainActivity.getThis).getString("category"+(i+1), categoryDescriptions.get(i));

        return r;
    }
    public static String getCategoryString(String s){
        return getCategoryDescription(Favorite.getCategoryNr(s));
    }

    public static int categoryIdssize(){
        generateLists();
        return categoryIds.size();
    }

    public static String categoryIdsget(int i){
        generateLists();
        return categoryIds.get(i);
    }

    public static String getCategoryId(String desc) {//
        generateLists();
        String ret="";
        for (int i = 0; i < Favorite.categoryDescriptions.size(); i++)
            if (desc.equals(Favorite.categoryDescriptions.get(i))) {
                ret = Favorite.categoryIds.get(i);
                Log.v("samba", "add" + Favorite.NEWALBUM);
            }
        return ret;
    }

    public static int getCategoryNr(String desc) {//
        generateLists();
        int ret=0;
        for (int i = 0; i < Favorite.categoryIds.size(); i++)
            if (desc.equals(Favorite.categoryIds.get(i))) {
                ret = i;
                Log.v("samba", "add" + Favorite.NEWALBUM);
            }
        return ret;
    }

    public static void generateLists() {
        Favorite.categoryDescriptions=new ArrayList<>(Arrays.asList(NEW_LINKS));//categoryIds
        for (int j = 0; j< SpotifyFragment.CATEGORY_IDS.size(); j++)Favorite.categoryDescriptions.add(SpotifyFragment.CATEGORY_IDS.get(j));
        for (int j = Favorite.categoryIds.size(); j< Favorite.categoryDescriptions.size(); j++)Favorite.categoryIds.add("2"+j);
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

    public String getCategoryField() {
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
