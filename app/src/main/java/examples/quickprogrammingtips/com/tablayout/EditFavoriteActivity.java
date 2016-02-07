package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

import examples.quickprogrammingtips.com.tablayout.model.Favorite;
import examples.quickprogrammingtips.com.tablayout.model.FavoriteRecord;

/**
 * A login screen that offers login via email/password.
 */
public class EditFavoriteActivity extends AppCompatActivity{


    // UI references.
    private EditText url;
    private EditText description;
    //private EditText category;
    ViewGroup vwgroup;
    private ArrayList<RadioButton> radioButtons=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorite);
        // Set up the login form.
        url = (EditText) findViewById(R.id.url);

        description = (EditText) findViewById(R.id.search);
        //category = (EditText) findViewById(R.id.category);


        Button save = (Button) findViewById(R.id.save_button);

        Bundle extras = getIntent().getExtras();


        String   urlString= extras.getString("url");
        String    descriptionString= extras.getString("description");
        String    categoryString= extras.getString("category");
        vwgroup=((ViewGroup)findViewById(R.id.favorite_radiogroup));

        RadioGroup.LayoutParams rprms;

        //LinearLayout ll= new LinearLayout(this);
        for (int i=0;i< Favorite.categoryIds.size();i++){
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(Favorite.getCategory(i));
            if (categoryString.equals(Favorite.categoryIds.get(i)))
                radioButton.setChecked(true);
            radioButtons.add(radioButton);
            radioButton.setId(i);

            rprms= new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            vwgroup.addView(radioButton,rprms);
        }
        //vwgroup.addView(ll);

        final int    idString= extras.getInt("id");

        url.setText(urlString);
        description.setText(descriptionString);
        //category.setText(categoryString);

        save.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                try {
                    FavoriteRecord book = FavoriteRecord.findById(FavoriteRecord.class, idString);
                    book.url = url.getText().toString();
                    book.description = description.getText().toString();
                    //book.category = category.getText().toString();
                    for (int i=0;i<radioButtons.size();i++)
                        if (radioButtons.get(i).isChecked())
                        {
                            //Log.v("samba", "set category to:" + Favorite.categoryIds.get(i));
                            book.category =Favorite.categoryIds.get(i);}
                    book.save();
                    Intent i = getIntent(); //get the intent that has been called, i.e you did called with startActivityForResult();
                    //i.putExtras(b);//put some data, in a bundle
                    setResult(Activity.RESULT_OK, i);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode
                    finish(); //finish the startNewOne activity
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();

                }

            }

        });


    }

}

