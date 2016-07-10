package examples.quickprogrammingtips.com.tablayout;

import java.util.ArrayList;

public     class NewAlbumsActivityElectronic extends NewAlbumsActivity {
    protected String url;
        /*
        //programmatic way:
//doc.select("div").first();//giving an Element instance
         */

    public void setUrl(){
        url="http://www.spotifynewmusic.com/tagwall3.php?ans=electronic";
    }

    @Override
    public void generateList(ArrayList<NewAlbum> newAlbums) {
            SpotifyActivity.getThis.fillListviewWithValues.generateList(newAlbums);

    }
}

