package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;

public class WikipediaActivity  extends Activity {
    private LeftDrawerPlaylist leftDrawerPlaylist;
    private WikipediaActivity getThis;
    private WebView webView;
    private String searchString;

    //private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            getThis =this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();

            try{

                ArrayList<String> menuItemsArray = new ArrayList<String>(
                        Arrays.asList("Settings",
                                "sep","Album", "Band", "Artist","sep","Play-Dialog","sep","Close","sep"  ));
                leftDrawerPlaylist=new LeftDrawerPlaylist(this, /*this,*/ R.id.newalbumsdrawer_layout, R.id.newalbumsdrawer_list,
                R.id.newalbumsmpddrawer_list, R.id.fabswapplaylist) {
            @Override
            public void performTouchEvent(MotionEvent event){

            }
            @Override
            public void performClickOnRightDrawer(){

            }

                    @Override
                    protected void doMenuAction(int position) {
                        String s = menuItemsArray.get(position);
                        if (s.startsWith("http")){
                            webView.loadUrl(s);
                        }else
                        switch (s) {
                            case "Settings":
                                MainActivity.getThis.doSettings();
                                break;
                            case "Album":
                            case "Band":
                            case "Artist":
                                webView.loadUrl("https://en.m.wikipedia.org/wiki/" + searchString + " (" + s+")");
                                break;
                            case "Play-Dialog":
                                SpotifyFragment.showPlayMenu(getThis);
                                break;
                            case "Close":
                                getThis.finish();
                                break;
                        }

                    }
                };
                leftDrawerPlaylist.setMenu(menuItemsArray);

        }catch (Exception e){}
        searchString= extras.getString("searchitem");
        /*try {
            getActionBar().setTitle("Wiki " + searchString);
        }catch (Exception e){}
        try {
            getSupportActionBar().setTitle("Wiki "+searchString);  // provide compatibility to all the versions
        }catch (Exception e){}*/
        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://en.m.wikipedia.org/wiki/"+searchString);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                leftDrawerPlaylist.addItem(url);
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
