package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class WikipediaActivity  extends Activity {
    private ListView drawerListRight;
    private LeftDrawerPlaylist leftDrawerPlaylist;

    //private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();

            try{
                leftDrawerPlaylist=new LeftDrawerPlaylist(this, /*this,*/ R.id.newalbumsdrawer_layout, R.id.newalbumsdrawer_list,
                R.id.newalbumsmpddrawer_list, R.id.fabswapplaylist) {
            @Override
            public void performTouchEvent(MotionEvent event){
                drawerListRight.onTouchEvent(event);
            }
            @Override
            public void performClickOnRightDrawer(){
                drawerListRight.performClick();
            }
        };
        drawerListRight = (ListView) findViewById(R.id.DrawerListRight);
        String[] osArray = { "Android", "iOS", "Windows", "OS X", "Linux" };
        ArrayAdapter<String> drawerListRightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        drawerListRight.setAdapter(drawerListRightAdapter);
        drawerListRight.setOnItemClickListener((parent, view, position, id) -> {
            Log.v("samba","Select"+position);
        });

        }catch (Exception e){}
        final String   searchString= extras.getString("searchitem");
        /*try {
            getActionBar().setTitle("Wiki " + searchString);
        }catch (Exception e){}
        try {
            getSupportActionBar().setTitle("Wiki "+searchString);  // provide compatibility to all the versions
        }catch (Exception e){}*/
        final WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://en.m.wikipedia.org/wiki/"+searchString);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ((ImageView) findViewById(R.id.thumbnail_top)).setOnClickListener(view -> {
            PopupMenu menu = new PopupMenu(view.getContext(), view);

            menu.getMenu().add("Album");
            menu.getMenu().add("Band");
            menu.getMenu().add("Artist");
            menu.show();
            menu.setOnMenuItemClickListener(item -> {
                webView.loadUrl("https://en.m.wikipedia.org/wiki/" + searchString + " (" + item.getTitle().toString()+")");
                return false;
            });

            });
    }catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
    }
    @Override
    protected void onStop() {
        leftDrawerPlaylist.onStop();
        MainActivity.getThis.runOnUiThread(() -> {
            //SpotifyFragment.getThis.albumAdapter.setDisplayCurrentTrack(true);
            try{
                SpotifyFragment.getThis.albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                Log.v("samba", Log.getStackTraceString(e));}
        });


        super.onStop();

    }

}
