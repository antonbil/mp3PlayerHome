package examples.quickprogrammingtips.com.tablayout;

import java.util.ArrayList;

/**
 * Created by anton on 25-6-16.
 * abstract class to be used within anonymous calls
 */
abstract class FillListviewWithValues {
    public void  generateListSearch(ArrayList<SearchItem> newAlbums){}
    public void  generateList(ArrayList<NewAlbum> newAlbums){}
    protected ArrayList<String> getChoices() {
        return new ArrayList<>();
    }


    public void addToFavorites(NewAlbum newAlbum){}
    public void processAlbum(SearchItem album){}

    public boolean processChoice(String s, NewAlbumsActivity.ListAdapter listAdapter, ArrayList<NewAlbum> items, int position){
        return false;
    }
    protected void addMenuItems(ArrayList<String> menuItems){
    }

    public void executeUrl(String s){}

    public String getText() {
        return "";
    }
}
