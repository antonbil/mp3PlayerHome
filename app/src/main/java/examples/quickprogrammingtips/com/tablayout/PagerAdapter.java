package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 4-9-16.
 */


        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    ListFragment listFragment;
    DBFragment         dbFragment;
    public int tabselected;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        listFragment = new ListFragment();
        dbFragment = new DBFragment();

    }

    @Override
    public Fragment getItem(int position) {
        tabselected=position;

        switch (position) {
            case 0:
                PlayFragment tab1 = new PlayFragment();
                return tab1;
            case 1:
                return listFragment;
            case 2:
                return dbFragment;
            case 3:
                PlaylistsFragment         playlistFragment = new PlaylistsFragment();
                return playlistFragment;
            case 4:
                SelectFragment         selectFragment = new SelectFragment();
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