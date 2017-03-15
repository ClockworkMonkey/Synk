package clockworkstudios.synk;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static clockworkstudios.synk.events.READ_TIMEOUT;
import static clockworkstudios.synk.events.event_util;
import static clockworkstudios.synk.sched.CONNECTION_TIMEOUT;

public class event_view extends AppCompatActivity {

    private ArrayList<String> attendee_list;
    private ArrayList<String> attendee_list_just_names;
    private ArrayList<String> attendee_list_usernames;
    public static final String PREFS_USERNAME_KEY = "__USERNAME__";
    public String logged_in_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        attendee_list = new ArrayList<String>();
        attendee_list_just_names = new ArrayList<String>();
        attendee_list_usernames = new ArrayList<String>();
        logged_in_user = "";

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(event_view.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        (new event_view.AsyncGetEventAttendees()).execute(event_util.event_id);

        TextView txtView = (TextView) findViewById(R.id.pop_up_event_title2);
        txtView.setText(event_util.title);
        TextView txtView2 = (TextView) findViewById(R.id.pop_up_event_desc2);
        txtView2.setText(event_util.desc);
        TextView txtView_sched = (TextView) findViewById(R.id.pop_up_event_location2);
        txtView_sched.setText(event_util.location);
        TextView txtView4 = (TextView) findViewById(R.id.pop_up_event_datetime2);
        txtView4.setText(event_util.date_time);

        (new event_view.AsyncIsManager()).execute(logged_in_user, event_util.event_id);

    }

    public void OnClick_add_invitees(View v)
    {
        //create dialog, list of friends, checkmarks by eachh, send inviites

        //generate pop-up with friend names and corrosponding checkboxes

        final ArrayList<android.support.v4.util.Pair<String, String>> friend_array = new ArrayList<android.support.v4.util.Pair<String, String>>();
        final ArrayList<String> name_list = new ArrayList<>();
        final boolean changed = false;
        boolean[] checked_pos = new boolean[MainMenu.friend_list.size()];


        //on item click, translate current string to check/unchecked boxes
        for (int i = 0; i < MainMenu.friend_list.size(); i++)
        {
            if (!(attendee_list_just_names.contains(MainMenu.friend_list.get(i).name))) {
                friend_array.add(new android.support.v4.util.Pair<>(MainMenu.friend_list.get(i).name, "0"));
                checked_pos[i] = false;
                name_list.add(MainMenu.friend_list.get(i).name);
            }
        }

        // and allow users to select times free, then translate back
        // where we will store or remove selected items
        final ArrayList<Integer> mSelectedItems = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(event_view.this);

        // set the dialog title
        builder.setTitle("Check the box next to each friend you want to invite")
                // specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive call backs when items are selected
                // R.array.choices were set in the resources res/values/strings.xml
                .setMultiChoiceItems(name_list.toArray(new CharSequence[name_list.size()]), checked_pos, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            // if the user checked the item, add it to the selected items
                            mSelectedItems.add(which);
                        }

                        else if (mSelectedItems.contains(which)) {
                            // else if the item is already in the array, remove it
                            mSelectedItems.remove(Integer.valueOf(which));
                        }
                    }

                })

                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // user clicked OK, so save the mSelectedItems results somewhere
                        // here we are trying to retrieve the selected items indices
                        String selectedIndex = "";
                        for(Integer i : mSelectedItems){
                            (new AsyncInvite()).execute(MainMenu.friend_list.get(i).username, events.event_util.event_id);
                        }

                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // removes the AlertDialog in the screen
                    }
                })
                .show();

        (new event_view.AsyncGetEventAttendees()).execute(event_util.event_id);
    }

    public void OnClick_remove_invitees(View v)
    {
        final ArrayList<android.support.v4.util.Pair<String, String>> friend_array = new ArrayList<android.support.v4.util.Pair<String, String>>();
        final ArrayList<String> name_list = new ArrayList<>();
        final boolean changed = false;
        boolean[] checked_pos = new boolean[MainMenu.friend_list.size()];


        //on item click, translate current string to check/unchecked boxes
        // and allow users to select times free, then translate back
        // where we will store or remove selected items
        final ArrayList<Integer> mSelectedItems = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(event_view.this);

        // set the dialog title
        builder.setTitle("Check the box next to each you want to un-invite")
                // specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive call backs when items are selected
                // R.array.choices were set in the resources res/values/strings.xml
                .setMultiChoiceItems(attendee_list_just_names.toArray(new CharSequence[name_list.size()]), checked_pos, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                        if (isChecked) {
                            // if the user checked the item, add it to the selected items
                            mSelectedItems.add(which);
                        }

                        else if (mSelectedItems.contains(which)) {
                            // else if the item is already in the array, remove it
                            mSelectedItems.remove(Integer.valueOf(which));
                        }
                    }

                })

                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // user clicked OK, so save the mSelectedItems results somewhere
                        // here we are trying to retrieve the selected items indices
                        String selectedIndex = "";
                        for(Integer i : mSelectedItems){
                            (new AsyncRemove()).execute(attendee_list_usernames.get(i), events.event_util.event_id);
                        }

                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // removes the AlertDialog in the screen
                    }
                })
                .show();

        (new event_view.AsyncGetEventAttendees()).execute(event_util.event_id);
    }

    public class obj_event{
        String title;
        String manager;
        String desc;
        String location;
        String date_time;
        String event_id;
    }

    private class AsyncIsManager extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(event_view.this);
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
            String res = "";

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/IsManager.php");

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
                    event_view.obj_event tmp_fr = new event_view.obj_event();

                    //since we are returning an array of pairs(name and status)
                    // and we are only reading one line at a time, we toggle between the tmp variable
                    // and filling the array

                    while ((line = reader.readLine()) != null) {
                        res = line;

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
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();
            Button add = (Button) findViewById(R.id.btn_add);
            Button rem = (Button) findViewById(R.id.btn_remove);

            if (!result.isEmpty()) {
                if (result.equals("manager"))
                {
                    add.setEnabled(true);
                    rem.setEnabled(true);
                }
                else
                {
                    add.setEnabled(false);
                    rem.setEnabled(false);
                }

            }

        }
    }

    private class AsyncInvite extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(event_view.this);
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
            String res = "";

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/InviteToEvent.php");

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
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("eventID", params[1]);

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
                    event_view.obj_event tmp_fr = new event_view.obj_event();

                    //since we are returning an array of pairs(name and status)
                    // and we are only reading one line at a time, we toggle between the tmp variable
                    // and filling the array

                    while ((line = reader.readLine()) != null) {
                        res = line;
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
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

        }
    }

    private class AsyncRemove extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(event_view.this);
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
            String res = "";

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/RemoveFromEvent.php");

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
                        .appendQueryParameter("username", params[0])
                        .appendQueryParameter("eventID", params[1]);

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
                    event_view.obj_event tmp_fr = new event_view.obj_event();

                    //since we are returning an array of pairs(name and status)
                    // and we are only reading one line at a time, we toggle between the tmp variable
                    // and filling the array

                    while ((line = reader.readLine()) != null) {
                        res = line;
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
        protected void onPostExecute(String result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

        }
    }

    private class AsyncGetEventAttendees extends AsyncTask<String, String, ArrayList<String>> {
        ProgressDialog pdLoading = new ProgressDialog(event_view.this);
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
                    event_view.obj_event tmp_fr = new event_view.obj_event();

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
                    //dont add ourselves to the list of attendees, obviously we're oging
                    if (!(tmp[2].equals(logged_in_user))) {
                        attendee_list_just_names.add(tmp[0]);
                        attendee_list_usernames.add(tmp[2]);
                        attendee_list.add(tmp2);
                    }
                }
                if(!attendee_list.isEmpty())
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(event_view.this, android.R.layout.simple_list_item_1, attendee_list);
                    ListView event_friends = (ListView) findViewById(R.id.event_friendlist);
                    event_friends.setAdapter(adapter);
                }

            }

        }
    }
}
