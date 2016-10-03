package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A login screen that offers login via email/password.
 */
public class EditFavoriteActivity extends AppCompatActivity{


    public static final String SPOTIFY_ALBUM = "spotify:album:";
    // UI references.
    private EditText url;
    private EditText sortkey;
    private EditText description;
    static final int STATIC_RESULT_SELECT =3; //positive > 0 integer.
    ViewGroup vwgroup;
    private ArrayList<RadioButton> radioButtons=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorite);
        url = (EditText) findViewById(R.id.url);
        sortkey = (EditText) findViewById(R.id.sortkey);

        description = (EditText) findViewById(R.id.search);


        Button save = (Button) findViewById(R.id.save_button);
        Button server = (Button) findViewById(R.id.server_button);
        Button sort = (Button) findViewById(R.id.sort_button);

        Bundle extras = getIntent().getExtras();


        String   urlString= extras.getString("url");
        String    descriptionString= extras.getString("description");
        String    categoryString= extras.getString("category");
        String    sortkeyString= extras.getString("sortkey");
        String    imageurl="";
        try{
            imageurl= extras.getString("imageurl");
        }catch (Exception e){}
        String finalimageurl=imageurl;
        vwgroup=((ViewGroup)findViewById(R.id.favorite_radiogroup));

        RadioGroup.LayoutParams rprms;

        for (int i=0;i< Favorite.categoryIdssize();i++){
            RadioButton radioButton = new RadioButton(this);
            String categoryDescription = Favorite.getCategoryDescription(i);
            radioButton.setText(categoryDescription);
            if (categoryString.equals(categoryDescription))
                radioButton.setChecked(true);
            radioButtons.add(radioButton);
            radioButton.setId(i);

            rprms= new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            vwgroup.addView(radioButton,rprms);
        }

        final int    idString= extras.getInt("id");

        sortkey.setText(sortkeyString);
        url.setText(urlString);
        description.setText(descriptionString);

        server.setOnClickListener(v -> {
            String[] desc=description.getText().toString().split("-");
            String url = this.url.getText().toString().replace(Favorite.SPOTIFYALBUM, SPOTIFY_ALBUM);
            String tempfavorite ="";
            for (int i=0;i<radioButtons.size();i++)
                if (radioButtons.get(i).isChecked())
                {
                    //Log.v("samba", "set category to:" + Favorite.categoryIds.get(i));
                    tempfavorite = Favorite.getCategoryDescription(i);
                }

            saveFavoriteToServer(sortkey.getText().toString(), url, tempfavorite, desc[0], desc[1], finalimageurl);
            finish();
        });
        sort.setOnClickListener(v -> {
            String []desc=description.getText().toString().split("-")[0].split(" ");
            if (desc.length>1){
                String sortkeystring=desc[1];
                sortkey.setText(sortkeystring);
            }
        });
        save.setOnClickListener(v -> {
            String tempfavorite = getCategoryDescription();
            Log.v("samba","id:"+idString);
            if (idString<0){
                try{
                    FavoriteRecord fv=new FavoriteRecord(url.getText().toString(),
                            description.getText().toString()+";;"+sortkey.getText().toString(), tempfavorite);
                    long a = fv.save();
                    finish(); //finish the startNewOne activity
                }
                catch (Exception e){}
            }else
            try {
                FavoriteRecord favorite = FavoriteRecord.findById(FavoriteRecord.class, idString);
                favorite.url = url.getText().toString();
                favorite.description = description.getText().toString()+";;"+sortkey.getText().toString();
                favorite.category = tempfavorite;
                favorite.save();
                Intent i = getIntent(); //get the intent that has been called, i.e you did called with startActivityForResult();
                setResult(23, i);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode
                try{
                    SelectFragment.getThis.getFavorites();
                }catch (Exception e){}
                finish(); //finish the startNewOne activity
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();

            }

        });


    }

    private String getCategoryDescription() {
        String tempfavorite ="";
        for (int i=0;i<radioButtons.size();i++)
            if (radioButtons.get(i).isChecked())
            {
                //Log.v("samba", "set category to:" + Favorite.categoryIds.get(i));
                tempfavorite = Favorite.categoryIdsget(i);
            }
        return tempfavorite;
    }

    public static void editFavorite(Activity a, Favorite favorite) {

        if (favorite.getRecord() != null) {
            editFavorite(a,favorite,favorite.getRecord().getId() + 0);
        } else {
            Log.v("samba","getRecord not found");
        }
    }

    public static void saveFavorite(Favorite favorite){
        String url=favorite.getUri();
        if (url.contains(Favorite.SPOTIFYALBUM)){

            //data to store
            String[]names=favorite.getDescription().split("-");
            String categoryDescription = Favorite.getCategoryString(favorite.getRecord().category);
            String artist = names[0];
            String albumname = names[1];
            String sortkey = favorite.getSortkey();

            //call spotify with album-id for album-image
            url=url.replace(Favorite.SPOTIFYALBUM,"");
            final String urlCode=url;
            SpotifyService spotify = new SpotifyApi().getService();

            spotify.getAlbum(url.trim(), new Callback<Album>() {

                /*@Override
                public void success(AlbumsPager albumsPager, Response response) {
                    for (AlbumSimple album : albumsPager.albums.items) {
                        saveFavoriteToServer(sortkey, SPOTIFY_ALBUM +urlCode, categoryDescription, artist, albumname, album.images.get(0).url);
                    }
                    //SearchActivity.getThis.notifyChange();

                }*/

                @Override
                public void success(Album album, Response response) {
                    //save data with album-image
                    saveFavoriteToServer(sortkey, SPOTIFY_ALBUM +urlCode, categoryDescription, artist, albumname, album.images.get(0).url);

                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }
    }

    public static void saveFavoriteToServer(String sortkey, String url, String categoryDescription, String artist, String album, String pictureUrl) {
        String outputurl=String.format("http://192.168.2.8/spotify/data/genre/%s/addlink.php?url=%s&artist=%s&artistsort=%s&album=%s&pictureurl=%s",
                categoryDescription,url, URLEncoder.encode(artist.trim()),
                URLEncoder.encode(sortkey.trim()),URLEncoder.encode(album.trim()),URLEncoder.encode(pictureUrl)).replace(" ","%20");
        Log.v("samba",outputurl);
        //url = outputurl;
        try {

            URL obj = new URL(outputurl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            String USER_AGENT = "Mozilla/5.0";
            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + outputurl);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }catch (Exception e){}
    }

    public static void editFavorite(Activity a, Favorite favorite, long l) {
        int id = (int) (l);
        String imageurl="";
        String uri = favorite.getUri();
        String sortkey = favorite.getSortkey();
        String description = favorite.getDescription();
        String categoryString = Favorite.getCategoryString(favorite.getRecord().category);
        editAndSaveFavorite(a, id, imageurl, uri, sortkey, description, categoryString);
    }

    public static void editAndSaveFavorite(Activity a, int id, String imageurl, String uri, String sortkey, String description, String category) {
        Intent intent = new Intent(a, EditFavoriteActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("url", uri);
        intent.putExtra("description", description);
        intent.putExtra("category", category);
        intent.putExtra("sortkey", sortkey);
        intent.putExtra("imageurl", imageurl);
        a.startActivityForResult(intent, STATIC_RESULT_SELECT);
    }
}

