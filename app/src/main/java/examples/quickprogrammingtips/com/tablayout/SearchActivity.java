package examples.quickprogrammingtips.com.tablayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A login screen that offers login via email/password.
 */
public class SearchActivity extends AppCompatActivity{

    // UI references.

    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // Set up the login form.


        searchEditText = (EditText) findViewById(R.id.search);


        Button save = (Button) findViewById(R.id.save_search_button);

        Bundle extras = getIntent().getExtras();

        //String    descriptionString= extras.getString("searchEditText");

        //searchEditText.setText(descriptionString);

        save.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                try {
//                    FavoriteRecord book = FavoriteRecord.findById(FavoriteRecord.class, idString);
//
//                    book.description = searchEditText.getText().toString();
//                    book.save();
                    Intent i = getIntent(); //get the intent that has been called, i.e you did called with startActivityForResult();
                    i.putExtra("search_string", searchEditText.getText().toString());//put some data, in a bundle
                    setResult(Activity.RESULT_OK, i);  //now you can use Activity.RESULT_OK, its irrelevant whats the resultCode
                    finish(); //finish the startNewOne activity
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error!", Toast.LENGTH_SHORT).show();

                }

            }

        });


    }

}

