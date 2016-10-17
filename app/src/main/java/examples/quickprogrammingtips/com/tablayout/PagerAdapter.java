package examples.quickprogrammingtips.com.tablayout;

/**
 * Created by anton on 4-9-16.
 */


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class PagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {
    /*
    http://stackoverflow.com/questions/21471712/replacing-fragment-in-viewpager-shows-blank-screen

    Use this,

public class MyPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter
     */
    private final Context context;
    SpotifyFragment spotifyFragment;
    /*public int[] imageResId = {
            R.drawable.play,
            R.drawable.smb,
            R.drawable.ic_sync_black_24dp,
            R.drawable.spotifylist,
            R.drawable.mpd,
            R.drawable.swan1,
            R.drawable.spf
    };*/
    SelectFragment selectFragment;
    SpotifyPlaylistFragment spotifyPlaylistFragment;

    PlaylistsFragment playlistFragment;
    PlayFragment playFragment;
    ListFragment listFragment;
    DBFragment         dbFragment;
    int mNumOfTabs;
    public int tabselected=0;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, Context context) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.context=context;
        //create fragments for tabs in background
        playFragment = new PlayFragment();
        new Thread(() -> {
            selectFragment = new SelectFragment();
            listFragment = new ListFragment();
            Log.d("samba", "Text:9");
            playlistFragment = new PlaylistsFragment();
            dbFragment = new DBFragment();
            try{
            spotifyFragment=new SpotifyFragment();}
            catch (Exception e){Log.v("samba","error spotify create");}
            try{
                spotifyPlaylistFragment = new SpotifyPlaylistFragment();
            }
            catch (Exception e){Log.v("samba","error spotify playlist create");}
            //spotifyPlaylistFragment = new SpotifyPlaylistFragment();

        }).start();

    }

    /*
    http://stackoverflow.com/questions/30892545/tablayout-with-icons
    By default, the tab created by TabLayout sets the textAllCaps property to be true, which prevents ImageSpans from being rendered. You can override this behavior by changing the tabTextAppearance property.

  <style name="MyCustomTabLayout" parent="Widget.Design.TabLayout">
        <item name="tabTextAppearance">@style/MyCustomTextAppearance</item>
  </style>

  <style name="MyCustomTextAppearance" parent="TextAppearance.Design.Tab">
        <item name="textAllCaps">false</item>
  </style>
     */
    /*@Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        // return tabTitles[position];
        Drawable image = context.getResources().getDrawable(imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        SpannableString sb = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return null;
    }*/

    @Override
    public Fragment getItem(int position) {
        //MainActivity.getThis.connectListenersToThumbnail();

        Log.v("samba","pos:"+position);
        tabselected=position;

        switch (position) {
            case 0:
                return playFragment;
            case MainActivity.SMBTAB:
                return listFragment;
            case 5:
                return playlistFragment;
            case MainActivity.MPDTAB:
                return dbFragment;
            case MainActivity.SELECTTAB:
                return selectFragment;
            case MainActivity.SPOTIFYTAB:
                return spotifyFragment;
            case 6:
                return spotifyPlaylistFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}