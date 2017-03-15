package clockworkstudios.synk;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.dial;
import static android.R.attr.name;
import static android.R.attr.password;
import static android.R.attr.patternPathData;

public class events extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public static final String PREFS_USERNAME_KEY = "__USERNAME__";
    public String logged_in_user;


    private static ArrayList<events.obj_event> event_list;
    private ArrayList<String> attendee_list;
    static events.obj_event event_util;


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

        final ListView eventList = (ListView) findViewById(R.id.events_list);

        if (eventList!= null) {
            eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    String item = ((TextView) view).getText().toString();
                    event_util = event_list.get(position);
                    Intent intnt = new Intent(events.this, event_view.class);
                    startActivity(intnt);
                }
            });
        }

        (new events.AsyncGetEvents()).execute(logged_in_user);
    }


    //consider converting to open new inent. would be much easier that way to actually use data and time pickers
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


                if (title.length() <= 60 && desc.length() <= 140) {
                    (new AsyncGreateEvent()).execute(logged_in_user, title, desc, locatione, date);
                    dialog.dismiss();
                } else {
                    Toast.makeText(events.this, "Error, title or description too long", Toast.LENGTH_SHORT).show();
                }



            }
        });
        dialog.show();
    }

    public void OnClick_refresh_events(View v)
    {
        (new events.AsyncGetEvents()).execute(logged_in_user);
    }

    private class AsyncGetEvents extends AsyncTask<String, String, ArrayList<events.obj_event>> {
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
        protected ArrayList<events.obj_event> doInBackground(String... params) {
            ArrayList<events.obj_event> res = new ArrayList<>();

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/GetEvents.php");

            } catch (MalformedURLException e) {

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
                    String line = "";
                    String[] line2;
                    String[] line3;

                    Pair<String, String> tmp_pair;
                    Integer toggle = 0;
                    events.obj_event tmp_fr = new events.obj_event();

                    //since we are returning an array of pairs(name and status)
                    // and we are only reading one line at a time, we toggle between the tmp variable
                    // and filling the array

                    while ((line = reader.readLine()) != null) {
                        line = line.substring(0, line.length()-1);
                        line2 = line.split("/");

                        for(int i = 0; i < line2.length; i++)
                        {
                            line3 = line2[i].split(",");
                            if(line3.length >3) {
                                tmp_fr = new events.obj_event();
                                tmp_fr.title = line3[0];
                                tmp_fr.desc = line3[1];
                                tmp_fr.manager = line3[2];
                                tmp_fr.location = line3[3];
                                tmp_fr.date_time = line3[4];
                                tmp_fr.event_id = line3[5];
                                res.add(tmp_fr);
                                line3 = new String[line3.length];
                            }
                        }

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
        protected void onPostExecute(ArrayList<events.obj_event> result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if (!result.isEmpty()) {
                ArrayList<String> parsed_results = new ArrayList<>();

                // parse the results we got into an array of single strings,
                // so that itll be compatible with the arrayadapter items
                for (events.obj_event current : result)
                {
                    StringBuilder builder = new StringBuilder();

                    builder.append(current.title);

                    builder.append("\n");
                    builder.append(current.date_time);

                    parsed_results.add(builder.toString());

                }

                //parse the array of pairs into a array of single strings to be fed to the array adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<String>((events.this), android.R.layout.simple_list_item_1, parsed_results);
                ListView list = (ListView) findViewById(R.id.events_list);
                list.setAdapter(adapter);

                 event_list = result;

            }
            else if (result.isEmpty())
            {
                Toast.makeText(events.this, "No events to show", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(events.this, "Could not retrieve events list", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncGetEventAttendees extends AsyncTask<String, String, ArrayList<String>> {
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
        protected ArrayList<String> doInBackground(String... params) {
            ArrayList<String> res = new ArrayList<>();

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/GetEventAttendees.php");

            } catch (MalformedURLException e) {

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
                        .appendQueryParameter("eventid", params[0]);

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
                    String line = "";
                    String[] line2;
                    String[] line3;

                    Pair<String, String> tmp_pair;
                    Integer toggle = 0;
                    events.obj_event tmp_fr = new events.obj_event();

                    //since we are returning an array of pairs(name and status)
                    // and we are only reading one line at a time, we toggle between the tmp variable
                    // and filling the array

                    while ((line = reader.readLine()) != null) {
                        line = line.substring(0, line.length()-1);
                        line2 = line.split("/");

                        for(int i = 0; i < line2.length; i++)
                        {
                            res.add(line2[i]);
                        }

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

            if (!result.isEmpty()) {
                attendee_list.clear();
                for (String sv : result)
                {
                    String[] tmp = sv.split(",");
                    String tmp2 = "";
                    if (tmp[1].equals("1"))
                    {
                        tmp2 = tmp[0] + " - Confirmed";
                    }
                    else
                    {
                        tmp2 = tmp[0] + " - Unconfirmed";
                    }
                    attendee_list.add(tmp2);
                }

            }

        }
    }

    class obj_event{
        String title;
        String manager;
        String desc;
        String location;
        String date_time;
        String event_id;
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
