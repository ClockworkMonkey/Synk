package clockworkstudios.synk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.ArrayList;

import static android.R.attr.defaultValue;
import static android.R.attr.key;

public class MainMenu extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public static final String PREFS_USERNAME_KEY = "__USERNAME__";
    private ListView lv;
    ArrayList<String> listAdapter;
    public Utills utis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //on load, get current status from database

        String logged_in_user = "none";

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainMenu.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
            }

        (new AsyncGetFriends()).execute(logged_in_user);
    }

    public void add_friend_click(View v)
    {
        EditText list = (EditText) findViewById(R.id.input_friend_name);

        String to_add = list.getText().toString();

        if (!utis.checkEmail(to_add))
        {
            Toast.makeText(MainMenu.this, "Invalid email", Toast.LENGTH_LONG).show();

        }
        else {
            // Initialize  AsyncLogin() class with email and password
            new MainMenu.AsyncAddFriends().execute(to_add);
        }
    }

    private class AsyncAddFriends extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainMenu.this);
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
                url = new URL("http://synk-app.com/AddFriend.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
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
                        .appendQueryParameter("username", params[0]);
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
                // TODO Auto-generated catch block
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
            Toast tst;

            if (result.equalsIgnoreCase("true")) {
                Toast.makeText(MainMenu.this, "Request sent", Toast.LENGTH_LONG).show();


            } else if (result.equalsIgnoreCase("false")) {

                // If username and password does not match display a error message
                Toast.makeText(MainMenu.this, "User does not exist", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class AsyncGetFriends extends AsyncTask<String, String, ArrayList<String>> {
        ProgressDialog pdLoading = new ProgressDialog(MainMenu.this);
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
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> res = new ArrayList<String>();

            try {

                // Enter URL address where your php file resides
                url = new URL("http://synk-app.com/GetFriends.php");

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return res;
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
                        .appendQueryParameter("username", params[0]);

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
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return res;
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
                        res.add(line);
                    }

                    // Pass data to onPostExecute method
                    return res;

                } else {
                    return res;
                }

            } catch (IOException e) {
                e.printStackTrace();

                return res;
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {

            //this method will be running on UI thread

            pdLoading.dismiss();
            Toast tst;

            if (!result.isEmpty()) {
                /* Here launching another activity when login successful. If you persist login state
                use sharedPreferences of Android. and logout button to clear sharedPreferences.
                 */
                ArrayAdapter<String> adapter = new ArrayAdapter<String>((MainMenu.this), android.R.layout.simple_list_item_1, result);
                ListView list = (ListView) findViewById(R.id.friendlist);
                list.setAdapter(adapter);


            } else {

                // If username and password does not match display a error message
                Toast.makeText(MainMenu.this, "Could not retrieve friend list", Toast.LENGTH_LONG).show();

            }
        }
    }

    // Triggers when LOGIN Button clicked
    public void change_status(View arg0) {

       //get new status

        // push it to website

        //error if cant connect
    }
}
