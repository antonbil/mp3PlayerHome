package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 4-9-16.
 */


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class PagerAdapter extends FragmentStatePagerAdapter {
    SelectFragment selectFragment;
    PlaylistsFragment playlistFragment;
    PlayFragment playFragment;
    int mNumOfTabs;
    ListFragment listFragment;
    DBFragment         dbFragment;
    public int tabselected=0;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        //create fragments for tabs in background
        playFragment = new PlayFragment();
        new Thread(() -> {
            listFragment = new ListFragment();
            //Log.d("samba", "Text:9");
            playlistFragment = new PlaylistsFragment();
            selectFragment = new SelectFragment();
            dbFragment = new DBFragment();
        }).start();

    }

    @Override
    public Fragment getItem(int position) {

        Log.v("samba","pos:"+position);
        tabselected=position;

        switch (position) {
            case 0:
                return playFragment;
            case 1:
                return listFragment;
            case 2:
                return playlistFragment;
            case 3:
                return dbFragment;
            case 4:
                return selectFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}