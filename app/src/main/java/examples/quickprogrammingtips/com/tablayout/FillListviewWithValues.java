package examples.quickprogrammingtips.com.tablayout;

import java.util.ArrayList;

/**
 * Created by anton on 25-6-16.
 */
public abstract class FillListviewWithValues {
    public void  generateListSearch(ArrayList<SearchItem> newAlbums){};
    public void  generateList(ArrayList<NewAlbum> newAlbums){};

    public void addToFavorites(NewAlbum newAlbum){};
    public void processAlbum(SearchItem album){}
}
