package clockworkstudios.synk;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
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
import java.util.ArrayList;
import java.util.List;

import static clockworkstudios.synk.MainMenu.PREFS_USERNAME_KEY;
import static clockworkstudios.synk.R.array.day_names;
import static clockworkstudios.synk.R.array.day_values;

public class sched extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    ListView sched_lv = null;
    String[] day_name = null;
    final ArrayList<Pair<String, String>> Schedule = new ArrayList<>();
    public String logged_in_user;
    public char free_unicode = '\u25AF';
    public char busy_unicode = '\u25AE';

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sched);

        sched_lv = (ListView) findViewById(R.id.sched_list);

        day_name = getResources().getStringArray(R.array.day_names);

        //default value for Schedule, in case of connection failure
        String default_sched = "111111110000000011111111";
        for (int f = 0; f < day_name.length; f++)
        {
            Schedule.add(new Pair<>(day_name[f], default_sched));
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(sched.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Resources res = getResources();
        sched_lv.setAdapter(new ArrayAdapter<String>(this, R.layout.sched_listview, res.getStringArray(R.array.day_values)));

        if (sched_lv != null)
        {
            sched_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position,
                                        long id) {
                    String item = ((TextView)view).getText().toString();

                    final String[] times = getResources().getStringArray(R.array.time_values);
                    final ArrayList<Pair<String, String>> sched_array = new ArrayList<Pair<String, String>>();
                    final boolean changed = false;
                    boolean[] checked_pos = new boolean[times.length];


                    //on item click, translate current string to check/unchecked boxes

                    item = item.replace(" ", "");
                    final String[] tmp = item.split("\n");
                    final String day = tmp[0];

                    for (int i = 0; i < tmp[1].length(); i++)
                    {
                        if (tmp[1].charAt(i) == busy_unicode)
                        {
                            sched_array.add(new Pair<>(times[i], "1"));
                            checked_pos[i] = false;
                        }
                        else
                        {
                            sched_array.add(new Pair<>(times[i], "0"));
                            checked_pos[i] = true;
                        }
                    }

                    // and allow users to select times free, then translate back
                    // where we will store or remove selected items
                    final ArrayList<Integer> mSelectedItems = new ArrayList<>();

                    AlertDialog.Builder builder = new AlertDialog.Builder(sched.this);

                    // set the dialog title
                    builder.setTitle("Check the box of each hour you are free")
                            // specify the list array, the items to be selected by default (null for none),
                            // and the listener through which to receive call backs when items are selected
                            // R.array.choices were set in the resources res/values/strings.xml
                            .setMultiChoiceItems(R.array.time_values, checked_pos, new DialogInterface.OnMultiChoiceClickListener() {
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
                                        selectedIndex += i + ", ";
                                    }


                                    String re_built = "";
                                    for(int k = 0; k < times.length; k++)
                                    {
                                        if (mSelectedItems.contains(k))
                                        {
                                            sched_array.set(k, new Pair<String, String>(sched_array.get(k).first, "0"));
                                        }
                                        if (sched_array.get(k).second == "1")
                                        {
                                            re_built += "1";
                                        }
                                        else
                                        {
                                            re_built += "0";
                                        }
                                    }

                                    for (int c = 0; c < Schedule.size(); c++)
                                    {
                                        if (Schedule.get(c).first.equals(day))
                                        {
                                            Schedule.set(c, new Pair<>(day, re_built));
                                        }
                                    }

                                    re_built.replace('1', busy_unicode);
                                    re_built.replace('0', free_unicode);

                                    tmp[0] += "\n " + re_built;
                                    ((TextView) view).setText(tmp[0]);

                                }
                            })

                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    // removes the AlertDialog in the screen
                                }
                            })
                            .show();
                }
            });
        }
    }

    public void OnClick_update_sched(View v)
    {
        sched_lv = (ListView) findViewById(R.id.sched_list);

        String[] day_names = getResources().getStringArray(R.array.day_names);
        for (int i = 0; i < day_names.length; i++)
        {
            (new AsyncUpdateSched_day()).execute(logged_in_user, day_names[i], Schedule.get(i).second);
        }

    }

    private class AsyncUpdateSched_day extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(sched.this);
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
                url = new URL("http://10.0.2.2/UpdateScheduleDay.php");

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
                        .appendQueryParameter("day", params[1])
                        .appendQueryParameter("sched", params[2]);
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

            if (result.equalsIgnoreCase("true")) {


            } else if (result.equalsIgnoreCase("false")) {

                // If username and password does not match display a error message
                Toast.makeText(sched.this, "Unable to update Schedule", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class AsyncGetSched extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(sched.this);
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
                url = new URL("http://10.0.2.2/GetSched.php");

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

            //Toast.makeText(MainMenu.this, "Status synced from server.", Toast.LENGTH_LONG).show();

            Switch sw = (Switch)findViewById(R.id.busy_free_switch);

            if (result.equalsIgnoreCase("1")) {
            }
        }
    }
}
