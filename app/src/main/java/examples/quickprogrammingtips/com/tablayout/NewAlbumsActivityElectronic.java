package examples.quickprogrammingtips.com.tablayout;

import java.util.ArrayList;

public     class NewAlbumsActivityElectronic extends NewAlbumsActivity {
    protected String url;

    public void setUrl(){
        url="http://www.spotifynewmusic.com/tagwall3.php?ans=electronic";
    }

    @Override
    protected void doAction(String s) {
        this.finish();
        SpotifyFragment.getThis.fillListviewWithValues.executeUrl(s);
    }
    @Override
    public void generateList(ArrayList<NewAlbum> newAlbums) {
            SpotifyFragment.getThis.fillListviewWithValues.generateList(newAlbums);
        SpotifyFragment.getThis.fillListviewWithValues.addMenuItems(menuItemsArray);

    }
    @Override
    protected String getText(){
        String text = SpotifyFragment.getThis.fillListviewWithValues.getText();
        if (text.length()>0)
        text = " " + text + " ";
        return text;
    }

}

