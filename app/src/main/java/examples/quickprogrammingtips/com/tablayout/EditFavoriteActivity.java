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

/**
 * A login screen that offers login via email/password.
 */
public class EditFavoriteActivity extends AppCompatActivity{


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

        Bundle extras = getIntent().getExtras();


        String   urlString= extras.getString("url");
        String    descriptionString= extras.getString("description");
        String    categoryString= extras.getString("category");
        String    sortkeyString= extras.getString("sortkey");
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

        save.setOnClickListener(v -> {
            try {
                FavoriteRecord favorite = FavoriteRecord.findById(FavoriteRecord.class, idString);
                favorite.url = url.getText().toString();
                favorite.description = description.getText().toString()+";;"+sortkey.getText().toString();
                for (int i=0;i<radioButtons.size();i++)
                    if (radioButtons.get(i).isChecked())
                    {
                        //Log.v("samba", "set category to:" + Favorite.categoryIds.get(i));
                        favorite.category =Favorite.categoryIdsget(i);}
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
    public static void editFavorite(Activity a, Favorite favorite) {

        if (favorite.getRecord() != null) {
            editFavorite(a,favorite,favorite.getRecord().getId() + 0);
        } else {
            Log.v("samba","getRecord not found");
        }
    }

    public static void saveFavorite(Favorite favorite){
        //http://192.168.2.8/spotify/electronic/addlink.php?url=spotify:album:0KnrvfmcbQMmXeXgwwMY4W&artist=C.P.E Bach&artistsort=Bach CPE&album=Cello Concertos​
        String url=favorite.getUri();
        if (url.contains("spotifyalbum")){
            url=url.replace("spotifyalbum://","spotify:album:");
            String[]names=favorite.getDescription().split("-");
            int nr=Favorite.getCategoryNr(favorite.getRecord().category);
            String categoryDescription = Favorite.getCategoryString(favorite.getRecord().category);
            String artist = names[0];
            String album = names[1];
            String pictureUrl="";
            String sortkey = favorite.getSortkey();

            saveFavoriteToServer(sortkey, url, categoryDescription, artist, album, pictureUrl);
        }

    }

    public static void saveFavoriteToServer(String sortkey, String url, String categoryDescription, String artist, String album, String pictureUrl) {
        String outputurl=String.format("http://192.168.2.8/spotify/%s/addlink.php?url=%s&artist=%s&artistsort=%s&album=%s&pictureurl=%s",
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
        Intent intent = new Intent(a, EditFavoriteActivity.class);
        intent.putExtra("id", (int) (l));
        intent.putExtra("url", favorite.getUri());
        intent.putExtra("description", favorite.getDescription());
        intent.putExtra("category", Favorite.getCategoryString(favorite.getRecord().category));
        intent.putExtra("sortkey", favorite.getSortkey());
        a.startActivityForResult(intent, STATIC_RESULT_SELECT);
    }
}

