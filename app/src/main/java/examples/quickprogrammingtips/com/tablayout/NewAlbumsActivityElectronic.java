package examples.quickprogrammingtips.com.tablayout;

import android.util.Log;

import java.util.ArrayList;

public     class NewAlbumsActivityElectronic extends NewAlbumsActivity {
    protected String url;

    public void setUrl(){
        url="http://www.spotifynewmusic.com/tagwall3.php?ans=electronic";
    }
    @Override
    public void processAlbum(NewAlbum album) {
       if (!MainActivity.getInstance().fillListviewWithValues.processAlbum(album))
           super.processAlbum(album);
    }

    @Override
    public void AddAlbumToPlaylist(String uri){
        NewAlbum album=new NewAlbum(uri,"","");
        if (!MainActivity.getInstance().fillListviewWithValues.processAlbum(album))
            super.processAlbum(album);
    }


        @Override
    protected void doAction(String s) {
        Log.v("samba","pl3:"+s);
        this.finish();
        MainActivity.getInstance().fillListviewWithValues.executeUrl(s);
    }
    @Override
    public void generateList(ArrayList<NewAlbum> newAlbums) {
        MainActivity.getInstance().fillListviewWithValues.generateList(newAlbums);
        MainActivity.getInstance().fillListviewWithValues.addMenuItems(menuItemsArray);

    }
    @Override
    protected String getText(){
        String text = MainActivity.getInstance().fillListviewWithValues.getText();
        if (text.length()>0)
        text = " " + text + " ";
        return text;
    }

}

