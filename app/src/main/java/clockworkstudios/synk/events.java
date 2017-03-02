package clockworkstudios.synk;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.R.attr.name;

public class events extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public static final String PREFS_USERNAME_KEY = "__USERNAME__";
    public String logged_in_user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        //TODO add onclick code for events in the list
        //TODO add initial read of events the user is a part of

        logged_in_user = "";

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(events.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void OnClick_new_event(View v)
    {
        final Dialog dialog = new Dialog(events.this);
        dialog.setContentView(R.layout.event_create_view);
        dialog.setTitle("New Event");

        Button button = (Button) dialog.findViewById(R.id.btn_create);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText edit_title=(EditText)dialog.findViewById(R.id.pop_up_edit_title);
                EditText edit_desc=(EditText)dialog.findViewById(R.id.pop_up_edit_desc);
                EditText edit_loc=(EditText)dialog.findViewById(R.id.pop_up_edit_location);
                EditText edit_date=(EditText)dialog.findViewById(R.id.pop_up_edit_datetime);


                String title=edit_title.getText().toString();
                String desc=edit_desc.getText().toString();
                String locatione=edit_loc.getText().toString();
                String date=edit_date.getText().toString();

                if (title.length() <= 60 && desc.length() <= 140)
                {
                    (new AsyncGreateEvent()).execute(logged_in_user, title, desc, locatione, date);
                    dialog.dismiss();
                }
                else
                {
                    Toast.makeText(events.this, "Error, title or description too long", Toast.LENGTH_SHORT).show();
                }



            }
        });
        dialog.show();
    }

    public void OnClick_refresh_events(View v)
    {

    }

    private class AsyncGreateEvent extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(events.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/CreateEvent.php");

            } catch (MalformedURLException e) {

                e.printStackTrace();
                return "exception";
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("POST");

                // setDoInput and setDoOutput method depict handling of both send and receive
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("title", params[1])
                        .appendQueryParameter("desc", params[2])
                        .appendQueryParameter("place", params[3])
                        .appendQueryParameter("time", params[4]);
                String query = builder.build().getEncodedQuery();

                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

            } catch (IOException e1) {

                e1.printStackTrace();
                return "exception";
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return "exception";
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();


        }
    }
}
