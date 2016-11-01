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

