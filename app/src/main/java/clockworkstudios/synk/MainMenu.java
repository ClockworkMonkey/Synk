package clockworkstudios.synk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class MainMenu extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public static final String PREFS_USERNAME_KEY = "__USERNAME__";

    public char free_unicode = '\u25AF';
    public char busy_unicode = '\u25AE';

    private obj_friend friend_util;
    private ArrayList<obj_friend> friend_list;
    private static int RESULT_LOAD_IMG = 1;

    public String logged_in_user;

    ImageView imgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //on load, get current status from database

        logged_in_user = "none";

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainMenu.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
            }

        imgView = (ImageView) findViewById(R.id.profile_pic_box);

        final Button button = (Button) findViewById(R.id.btn_add_friend);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    add_friend_click();
                }
            });
        }

        final Button button2;
        button2 = (Button) findViewById(R.id.btn_refresh_friends);
        if (button2 != null) {
            button2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    update_friend_statusse_click();
                }
            });
        }

        final Switch switch_button = (Switch) findViewById(R.id.busy_free_switch);

        // Set a checked change listener for switch button
        if (switch_button != null) {
            switch_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        switch_button.setText("Available");
                        (new AsyncUpdateStatus()).execute("1", logged_in_user);
                    } else {
                        switch_button.setText("Not Available");
                        (new AsyncUpdateStatus()).execute("0", logged_in_user);
                    }
                }
            });
        }

        //onclick event for setting profile image
        final ImageView imageview_profile= (ImageView) findViewById(R.id.profile_pic_box);
        if (imageview_profile != null) {
            imageview_profile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 23){
                    // Here, thisActivity is the current activity
                        if (ContextCompat.checkSelfPermission(MainMenu.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainMenu.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                                // Show an expanation to the user *asynchronously* -- don't block
                                // this thread waiting for the user's response! After the user
                                // sees the explanation, try again to request the permission.

                            } else {

                                // No explanation needed, we can request the permission.

                                ActivityCompat.requestPermissions(MainMenu.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        1);

                                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                                // app-defined int constant. The callback method gets the
                                // result of the request.
                            }
                        }
                    }
                    // select new image
                    // Create intent to Open Image applications like Gallery, Google Photos
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // Start the Intent
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                   //NOTE: Image processing and cropping is done in the child activity
                }
            });
        }

        final ListView friendlist = (ListView) findViewById(R.id.friendlist);

        //onclick for listview items

        if (friendlist != null)
        {
            friendlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    String item = ((TextView)view).getText().toString();
                    friend_util = friend_list.get(position);

                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    String curr_day = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

                    (new AsyncGetProfilePicture_util()).execute(friend_util.username);
                    (new AsyncGetSchedDay_util()).execute(friend_util.username, curr_day);
                    //getcalender

                    //format and create dialog box
                    String title_String;

                    Dialog dialog = new Dialog(MainMenu.this);
                    dialog.setContentView(R.layout.friend_view);
                    ImageView imgView=(ImageView)dialog.findViewById(R.id.pop_up_img);
                    //set image to imgView
                    if (friend_util.picture != null)
                    {
                        imgView.setImageBitmap(friend_util.picture);
                    }
                    TextView txtView = (TextView)dialog.findViewById(R.id.pop_up_text);
                    txtView.setText(friend_util.prefs);
                    TextView txtView2 = (TextView)dialog.findViewById(R.id.pop_up_title);
                    txtView2.setText(item);
                    TextView txtView_sched = (TextView)dialog.findViewById(R.id.pop_up_sched);
                    txtView_sched.setText(friend_util.curr_day_sched);
                    dialog.show();

                }
            });
        }


        (new AsyncGetStatus()).execute(logged_in_user);
        (new AsyncGetPrefs()).execute(logged_in_user);
        (new AsyncGetFriends()).execute(logged_in_user);
        (new AsyncCheckForNewRequests()).execute(logged_in_user);
        (new AsyncGetProfilePicture()).execute(logged_in_user);
    }

    public boolean checkEmail(String email)
    {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void add_friend_click()
    {
        EditText list = (EditText) findViewById(R.id.input_friend_name);

        String to_add = null;
        if (list != null) {
            to_add = list.getText().toString();
        }

        if (!checkEmail(to_add))
        {
            Toast.makeText(MainMenu.this, "Invalid email", Toast.LENGTH_LONG).show();
        }
        else {
            if (to_add != null) {
                if (to_add.equals(logged_in_user))
                {
                    Toast.makeText(MainMenu.this, "You cannot add yourself as a friend", Toast.LENGTH_LONG).show();
                }
                else
                {
                    new AsyncAddFriends().execute(logged_in_user.toLowerCase(), to_add.toLowerCase());
                }
            }

        }
    }

    public void update_friend_statusse_click()
    {
        (new AsyncGetFriends()).execute(logged_in_user);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            imgView.buildDrawingCache();
            final Bitmap old = imgView.getDrawingCache();

            imgView.setImageURI(uri);

            imgView.buildDrawingCache();
            final Bitmap bmp = imgView.getDrawingCache();


            //confirm the new picture
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Update your picture?");
            builder.setMessage("Are you sure?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog
                    // upload it to server
                    (new AsyncUpdateProfilePicture()).execute(bmp);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // Do nothing
                    imgView.setImageBitmap(old);
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();



        } else {
            Toast.makeText(this, "You haven't picked Image",
                    Toast.LENGTH_LONG).show();
        }
    }


    //method to encode a bitmap image as a string so it can be passed to the server
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    //onclick event for preferences
    // make a dialog filled with current preferences to be editer
    // update preferences to database
    //65500 chars
    public void OnClick_update_prefs(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your preferences");

        final TextView current = (TextView) findViewById(R.id.txt_prefs);
        // Set up the input
        final EditText input = new EditText(this);


        if (current != null) {
            input.setText(current.getText().toString());
        }


        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                if (m_Text.length() < 65500) {
                    if (current != null) {
                        current.setText(m_Text);
                    }
                    (new AsyncUpdatePrefs()).execute(logged_in_user, m_Text);
                }
                else
                {
                    Toast.makeText(MainMenu.this, "String too long, max length is 256 characters", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void OnClick_goto_sched(View v)
    {
        Intent intnt = new Intent(MainMenu.this, sched.class);
        startActivity(intnt);
    }

    // onclick event for listview of friends,
    // call GetPreferences, GetImage, GetStatus, GetCalander
    // format and display data in a dialog


    // onclick listener for calander object so that when a date is clicked, display day breakdown

    // update preferences (username, string info)
    private class AsyncUpdatePrefs extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/UpdatePrefs.php");

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
                        .appendQueryParameter("prefs", params[1]);
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
                Toast.makeText(MainMenu.this, "Preferences Updated", Toast.LENGTH_LONG).show();


            } else {

                // If username and password does not match display a error message
                Toast.makeText(MainMenu.this, "Unable to update preferences", Toast.LENGTH_LONG).show();

            }
        }
    }

    // get preferences (username)
    private class AsyncGetPrefs extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/GetPrefs.php");

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

           TextView prefs = (TextView) findViewById(R.id.txt_prefs);

            if (!result.isEmpty())
            {
                if (prefs != null) {
                    prefs.setText(result);
                }

            }
            else
            {
                Toast.makeText(MainMenu.this, "Could not retrieve preferences, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    // update calender (calender object Month[list]<Day[list]<Pair<hour, status>>>)

    // get calender (username)

    // create event (datetime, string location, string details, string title)

    // get event list (events where username is invited

    // get event singular (event name/ID)

    private class AsyncUpdateStatus extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/UpdateStatus.php");

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
                        .appendQueryParameter("status", params[0])
                        .appendQueryParameter("username", params[1]);
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
                Toast.makeText(MainMenu.this, "Status Updated", Toast.LENGTH_LONG).show();


            } else if (result.equalsIgnoreCase("false")) {

                // If username and password does not match display a error message
                Toast.makeText(MainMenu.this, "Unable to update status", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class AsyncUpdateProfilePicture extends AsyncTask<Bitmap, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainMenu.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tUploading picture...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            String uploadImage = getStringImage(bitmap);

            HashMap<String,String> data = new HashMap<>();
            data.put("image", uploadImage);
            data.put("username", logged_in_user);

            //String result = rh.sendPostRequest("http://10.0.2.2/GetPicture.php",data);
            URL url;
            String response = "";
            try {
                url = new URL("http://10.0.2.2/UpdatePicture.php");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : data.entrySet())
                {
                    if (first)
                        first = false;
                    else
                        result.append("&");

                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                writer.write(result.toString());

                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    response = br.readLine();
                } else {
                    response = "Error Registering";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            pdLoading.dismiss();

            if ((result.equalsIgnoreCase("Error Registering"))) {

                Toast.makeText(MainMenu.this, "Unable to update picture", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class AsyncGetStatus extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/GetStatus.php");

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
                if (sw != null) {
                    sw.setChecked(true);
                    sw.setText("Available");
                }


            } else if (result.equalsIgnoreCase("0")) {
                if (sw != null) {
                    sw.setChecked(false);
                    sw.setText("Not Available");
                }
            }
            else if (result.equalsIgnoreCase("failure"))
            {
                Toast.makeText(MainMenu.this, "Could not retrieve status, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncGetProfilePicture extends AsyncTask<String, String, Bitmap> {
        ProgressDialog pdLoading = new ProgressDialog(MainMenu.this);
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
        protected Bitmap doInBackground(String... params) {
            String username = params[0];
            String add = "http://10.0.2.2/GetPicture.php?username="+username;

            Bitmap image = null;
            try {
                url = new URL(add);
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return image;
        }


        @Override
        protected void onPostExecute(Bitmap result) {

            //this method will be running on UI thread

            pdLoading.dismiss();


            if (result != null)
            {
                ImageView img = (ImageView) findViewById(R.id.profile_pic_box);
                if (img != null) {
                    img.setImageBitmap(result);
                }
            }
            else
            {
                Toast.makeText(MainMenu.this, "Could not retrieve image, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncCheckForNewRequests extends AsyncTask<String, String, List<String>> {
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
        protected List<String> doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/CheckForRequests.php");

            } catch (MalformedURLException e) {

                e.printStackTrace();
                return null;
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
                return null;
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    List<String> result = new ArrayList<>();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.add(line);
                    }

                    // Pass data to onPostExecute method, will pass the name of the person requesting
                    return result;

                } else {

                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(List<String> result) {

            //this method will be running on UI thread

            pdLoading.dismiss();


            if (!result.isEmpty()) {

                final List<String> results = result;

                int i = 1;

                for (final String user : result)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);

                    StringBuilder title = new StringBuilder();
                    title.append("Request ");
                    title.append(i);
                    title.append(" of ");
                    title.append(results.size());
                    title.append(".");

                    StringBuilder message = new StringBuilder();
                    message.append(user);
                    message.append(" would like to be your friend");

                    builder.setTitle(title)
                            .setMessage(message);
                    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id) {
                            // add accepted  fried to list
                            (new AsyncConfirmFriend()).execute(user, logged_in_user);
                        }
                    });

                    builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            (new AsyncDenyFriend()).execute(user, logged_in_user);
                        }
                    });

                    AlertDialog dialog = builder.create();

                    dialog.show();
                }



            } else {

                // If username and password does not match display a error message
                Toast.makeText(MainMenu.this, "No new requests", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class AsyncConfirmFriend extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/ConfirmFriend.php");

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
                        .appendQueryParameter("friend", params[0])
                        .appendQueryParameter("username", params[1]);
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

    private class AsyncDenyFriend extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/DenyFriend.php");

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
                        .appendQueryParameter("friend", params[0])
                        .appendQueryParameter("username", params[1]);
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
                url = new URL("http://10.0.2.2/AddFriend.php");

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
                        .appendQueryParameter("to_add", params[1]);
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
                Toast.makeText(MainMenu.this, "Request sent", Toast.LENGTH_LONG).show();


            } else if (result.equalsIgnoreCase("false")) {


                Toast.makeText(MainMenu.this, "A request is already pending", Toast.LENGTH_LONG).show();

            }
            else
            {
                Toast.makeText(MainMenu.this, "User does not exist", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncGetFriends extends AsyncTask<String, String, ArrayList<obj_friend>> {
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
        protected ArrayList<obj_friend> doInBackground(String... params) {
            ArrayList<obj_friend> res = new ArrayList<>();

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/GetFriends.php");

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
                    obj_friend tmp_fr = new obj_friend();

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
                                    tmp_fr = new obj_friend();
                                    tmp_fr.name = line3[0];
                                    tmp_fr.username = line3[2];
                                    tmp_fr.status = Integer.parseInt(line3[1]);
                                    tmp_fr.prefs = line3[3];
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
        protected void onPostExecute(ArrayList<obj_friend> result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if (!result.isEmpty()) {
                ArrayList<String> parsed_results = new ArrayList<>();

                // parse the results we got into an array of single strings,
                // so that itll be compatible with the arrayadapter items
                for (obj_friend current : result)
                {
                    StringBuilder builder = new StringBuilder();

                    builder.append(current.name);

                    if (current.status== 1)
                    {
                        builder.append(" is available!");
                    }
                    else
                    {
                        builder.append(" is NOT available!");
                    }

                    parsed_results.add(builder.toString());

                }

                //parse the array of pairs into a array of single strings to be fed to the array adapter
                ArrayAdapter<String> adapter = new ArrayAdapter<String>((MainMenu.this), android.R.layout.simple_list_item_1, parsed_results);
                ListView list = (ListView) findViewById(R.id.friendlist);
                list.setAdapter(adapter);

                friend_list = result;

            }
            else if (result.isEmpty())
            {
                Toast.makeText(MainMenu.this, "No friends to show", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(MainMenu.this, "Could not retrieve friend list", Toast.LENGTH_LONG).show();
            }
        }
    }

    //util aAsync fucntions, return to variables in stead of directly setting activity objects
    // get preferences (username)
    private class AsyncGetPrefs_util extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/GetPrefs.php");

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


            if (!result.isEmpty()) {
                friend_util.prefs = result;
            }
            else
            {
                Toast.makeText(MainMenu.this, "Could not retrieve preferences, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncGetProfilePicture_util extends AsyncTask<String, String, Bitmap> {
        ProgressDialog pdLoading = new ProgressDialog(MainMenu.this);

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
        protected Bitmap doInBackground(String... params) {
            String username = params[0];
            String add = "http://10.0.2.2/GetPicture.php?username="+username;

            Bitmap image = null;
            try {
                url = new URL(add);
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return image;
        }


        @Override
        protected void onPostExecute(Bitmap result) {

            //this method will be running on UI thread

            pdLoading.dismiss();

            if (result != null)
            {
                friend_util.picture = result;
            }
            else
            {
                Toast.makeText(MainMenu.this, "Could not retrieve image, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }


    private class AsyncGetSchedDay_util extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/GetSchedDay.php");

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
                        .appendQueryParameter("day", params[1]);
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

            if (!result.equals("failure"))
            {
                String[] divided_day;
                String tmp = "";

                tmp = "";
                divided_day = result.split(":");
                divided_day[0].replace(":", "");
                divided_day[0].replace("_", "");
                divided_day[1].replace(":", "");
                divided_day[1].replace("_", "");

                divided_day[1] = divided_day[1].replace('1', busy_unicode);
                divided_day[1] = divided_day[1].replace('0', free_unicode);
                tmp += divided_day[0] + '\n' + divided_day[1];
                friend_util.curr_day_sched = tmp;
            }
        }
    }

    private class AsyncGetStatus_util extends AsyncTask<String, String, String> {
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
                url = new URL("http://10.0.2.2/GetStatus.php");

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



            if (result.equalsIgnoreCase("1") || result.equalsIgnoreCase("0")) {
                friend_util.status = Integer.parseInt(result);
            }
            else if (result.equalsIgnoreCase("failure"))
            {
                Toast.makeText(MainMenu.this, "Could not retrieve status, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class SwitchActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

        Switch mySwitch = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main_menu);


            mySwitch = (Switch) findViewById(R.id.busy_free_switch);
            mySwitch.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                (new AsyncUpdateStatus()).execute("1", logged_in_user);
            } else {
                (new AsyncUpdateStatus()).execute("0", logged_in_user);
            }
        }


    }

    class obj_friend{
        String name;
        String username;
        String prefs;
        String curr_day_sched;
        Bitmap picture;
        int status;
    }
}
