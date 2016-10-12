package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class WebActivity  extends Activity {
    private ListView drawerListRight;

    //private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        Log.v("samba", "in web-activity");

        new LeftDrawerPlaylist(this, /*this,*/ R.id.newalbumsdrawer_layout, R.id.newalbumsdrawer_list,
                R.id.newalbumsmpddrawer_list, R.id.fabswapplaylist) {
            @Override
            public void performTouchEvent(MotionEvent event){
                drawerListRight.onTouchEvent(event);
            }
            @Override
            public void performClickOnRightDrawer(){
                drawerListRight.performClick();
            }

            @Override
            protected void doMenuAction(int position) {

            }
        };
        drawerListRight = (ListView) findViewById(R.id.DrawerListRight);
        String[] osArray = { "Android", "iOS", "Windows", "OS X", "Linux" };
        ArrayAdapter<String> drawerListRightAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        drawerListRight.setAdapter(drawerListRightAdapter);
        drawerListRight.setOnItemClickListener((parent, view, position, id) -> {
            Log.v("samba","Select"+position);
        });

        final String   searchString= extras.getString("url");
        /*try {
            getActionBar().setTitle("URL: " + searchString);
        }catch (Exception e){}
        try {
            getSupportActionBar().setTitle("URL "+searchString);  // provide compatibility to all the versions
        }catch (Exception e){}*/
        final WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(searchString);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(view.getContext(), view);

                menu.getMenu().add("Album");
                menu.getMenu().add("Band");
                menu.getMenu().add("Artist");
                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        webView.loadUrl("https://en.m.wikipedia.org/wiki/" + searchString + " (" + item.getTitle().toString()+")");
                        return false;
                    }
                });

                }
            });*/
    }

}
