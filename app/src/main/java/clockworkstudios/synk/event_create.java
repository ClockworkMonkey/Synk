package clockworkstudios.synk;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DateTimeKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
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

public class event_create extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public static final String PREFS_USERNAME_KEY = "__USERNAME__";
    public String logged_in_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);

        logged_in_user = "";

        TimePicker time = (TimePicker) findViewById(R.id.timePicker);
        time.setIs24HourView(true);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(event_create.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button create = (Button) findViewById((R.id.btn_create));
        create.setEnabled(true);
    }

    public String checkDigit(int number)
    {
        return number<=9?"0"+number:String.valueOf(number);
    }

    public void OnClickCreateNewEvent(View v)
    {

        EditText edit_title=(EditText) findViewById(R.id.pop_up_edit_title);
        EditText edit_desc=(EditText) findViewById(R.id.pop_up_edit_desc);
        EditText edit_loc=(EditText) findViewById(R.id.pop_up_edit_location);

        TimePicker time = (TimePicker) findViewById(R.id.timePicker);
        DatePicker date = (DatePicker) findViewById(R.id.datePicker);

        Button create = (Button) findViewById((R.id.btn_create));
        create.setEnabled(false);
        int month = date.getMonth();
        int day = date.getDayOfMonth();
        int year = date.getYear();
        int hour = time.getCurrentHour();
        int minute = time.getCurrentMinute();
        String datetime=checkDigit(month) + "/" + checkDigit(day) + "/" + year + " " + checkDigit(hour) + ":" + checkDigit(minute);

        String title=edit_title.getText().toString();
        String desc=edit_desc.getText().toString();
        String locatione=edit_loc.getText().toString();

        if (title.length() <= 60 && desc.length() <= 140) {
            (new AsyncGreateEvent()).execute(logged_in_user, title, desc, locatione, datetime);

        } else {
            Toast.makeText(event_create.this, "Error, title or description too long", Toast.LENGTH_SHORT).show();
        }
    }

    private class AsyncGreateEvent extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(event_create.this);
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
