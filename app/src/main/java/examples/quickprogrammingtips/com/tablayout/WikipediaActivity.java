package examples.quickprogrammingtips.com.tablayout;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.PopupMenu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class WikipediaActivity  extends LeftDrawerPlaylist {

    //private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();

        initLeftDrawerPlaylist(this,this,R.id.newalbumsdrawer_layout,R.id.newalbumsdrawer_list,
                R.id.newalbumsmpddrawer_list,R.id.fabswapplaylist);


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
    }

}
