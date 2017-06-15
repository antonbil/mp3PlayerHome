package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public     class NewAlbumsActivityElectronic extends NewAlbumsActivity {
    public static NewAlbumsActivityElectronic getInstance(){
        return instance;
    }

    FillListviewWithValues fv;
    private static NewAlbumsActivityElectronic instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        instance=this;
        fv=MainActivity.getInstance().fillListviewWithValues;
        }
            @Override
    protected void onDestroy() {
                try {
                    fv.finish();
                }catch(Exception e){}
        super.onDestroy();

    }

    protected String url;

    public void setUrl(){
        url="http://www.spotifynewmusic.com/tagwall3.php?ans=electronic";
    }
    @Override
    public void processAlbum(NewAlbum album) {
       if (!getfv().processAlbum(album))
           super.processAlbum(album);
    }

    @Override
    public void AddAlbumToPlaylist(String uri){
        NewAlbum album=new NewAlbum(uri,"","");
        if (!getfv().processAlbum(album))
            super.processAlbum(album);
    }


        @Override
    protected void doAction(String s) {
        Log.v("samba","pl3:"+s);
        this.finish();
            getfv().executeUrl(s);
    }
    @Override
    public void generateList(ArrayList<NewAlbum> newAlbums) {
        getfv().generateList(newAlbums);
        getfv().addMenuItems(menuItemsArray);

    }
    @Override
    protected String getText(){
        String text = getfv().getText();
        if (text.length()>0)
        text = " " + text + " ";
        return text;
    }

    public FillListviewWithValues getfv() {
        if (fv==null)
            return MainActivity.getInstance().fillListviewWithValues; else
        return fv;
    }
}

