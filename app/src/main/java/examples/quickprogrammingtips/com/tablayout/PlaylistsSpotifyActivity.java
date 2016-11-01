package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import examples.quickprogrammingtips.com.tablayout.model.Favorite;

public class PlaylistsSpotifyActivity extends Activity {
    private LeftDrawerPlaylist leftDrawerPlaylist;
    private PlaylistsSpotifyActivity getThis;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            getThis =this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);

            try{

                ArrayList<String> menuItemsArray = new ArrayList<>(
                        Arrays.asList("Settings",
                                "sep","Topsify", "Dutch Playlists", "Playlists.net","sep","Play-Dialog","sep","Close","sep"  ));
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
                        if (s.startsWith("http")){//http://playlists.net/
                            webView.loadUrl(s);
                        }else
                        switch (s) {//http://topsify.com/
                            case "Topsify":
                                webView.loadUrl("http://topsify.com");
                                break;
                            case "Dutch Playlists":
                                webView.loadUrl("http://topsify.com/nl");
                                break;
                            case "Playlists.net":
                                webView.loadUrl("http://playlists.net");
                                break;
                            case "Settings":
                                MainActivity.getInstance().doSettings();
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

        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
            String url = "http://playlists.net/charts";
            leftDrawerPlaylist.addItem(url);
            webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //leftDrawerPlaylist.addItem(url);
                DebugLog.log("https://open.spotify.com/"+url.substring(8).replace(":","/"));
                if (url.startsWith("spotify:")){
                    ArrayList<String> choices=new ArrayList<>();
                    choices.add("first 30 tracks");
                    choices.add("entire list");
                    choices.add("add to favorites");
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(getThis);
                    builderSingle.setIcon(R.drawable.common_ic_googleplayservices);
                    builderSingle.setTitle("Play entire playlist?");

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                            getThis,
                            android.R.layout.select_dialog_singlechoice);
                    for (int i = 0; i < choices.size(); i++) {
                        arrayAdapter.add(choices.get(i));
                    }

                    builderSingle.setNegativeButton(
                            "cancel",
                            (dialog, which) -> {
                                dialog.dismiss();
                            });

                    builderSingle.setAdapter(
                            arrayAdapter,
                            (dialog, which) -> {
                                final String title1 = arrayAdapter.getItem(which);
                                if (title1.equals("first 30 tracks")) {
                                    SelectFragment.executeExternalSpotifyPlaylist30Songs(getThis,"https://open.spotify.com/"+url.substring(8).replace(":","/"));
                                } else if (title1.equals("entire list")) {
                                    SelectFragment.executeExternalSpotifyPlaylist(getThis, url);

                                }else if (title1.equals("add to favorites")) {
                                    String[] parts=url.split(":");
                                    SpotifyFragment.addAlbumToFavorites(
                                            Favorite.SPOTIFYPRIVATEPLAYLIST+url, parts[parts.length-1], null);

                                        }
                            });
                    builderSingle.show();

                }else
                if (url.startsWith("https://open.spotify.com"))
                    SelectFragment.executeExternalSpotifyPlaylist30Songs(getThis,url);
                else {
                    leftDrawerPlaylist.addItem(url);
                    view.loadUrl(url);
                }
                return true;
            }
        });

    }catch (Exception e){Log.v("samba",Log.getStackTraceString(e));}
    }
    @Override
    protected void onStop() {
        leftDrawerPlaylist.onStop();
        MainActivity.getInstance().runOnUiThread(() -> {
            try{
                SpotifyFragment.getInstance().albumAdapter.notifyDataSetChanged();
            }catch(Exception e){
                }
        });
        super.onStop();

    }

}
