package clockworkstudios.synk;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static android.R.attr.defaultValue;
import static android.R.attr.key;
import static android.R.attr.preferenceCategoryStyle;
import static android.R.attr.titleTextAppearance;
import static clockworkstudios.synk.R.drawable.default_img;

public class MainMenu extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public static final String PREFS_USERNAME_KEY = "__USERNAME__";
    public static final int img_width_and_height = 128;

    private ListView lv;
    ArrayList<String> listAdapter;
    public Utills utis;
    public String logged_in_user;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    ImageView imgView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        //on load, get current status from database

        logged_in_user = "none";

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainMenu.this );
        try {
            logged_in_user =  sharedPrefs.getString(PREFS_USERNAME_KEY, logged_in_user);
        } catch (Exception e) {
            e.printStackTrace();
            }

        imgView = (ImageView) findViewById(R.id.profile_pic_box);

        final Button button = (Button) findViewById(R.id.btn_add_friend);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                add_friend_click();
            }
        });

        final Button button2 = (Button) findViewById(R.id.btn_refresh_friends);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                update_friend_statusse_click();
            }
        });

        final Switch switch_button = (Switch) findViewById(R.id.busy_free_switch);

        // Set a checked change listener for switch button
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

        //onclick event for setting profile image
        final ImageView imageview_profile= (ImageView) findViewById(R.id.profile_pic_box);
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

        (new AsyncGetStatus()).execute(logged_in_user);
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

        String to_add = list.getText().toString();

        if (!checkEmail(to_add))
        {
            Toast.makeText(MainMenu.this, "Invalid email", Toast.LENGTH_LONG).show();
        }
        else {
            if (to_add.equals(logged_in_user))
            {
                Toast.makeText(MainMenu.this, "You cannot add yourself as a friend", Toast.LENGTH_LONG).show();
            }
            else
            {
                new MainMenu.AsyncAddFriends().execute(logged_in_user.toLowerCase(), to_add.toLowerCase());
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
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    //onclick event for preferences
    // make a dialog filled with current preferences to be editer
    // update preferences to database

    // onclick event for listview of friends,
    // call GetPreferences, GetImage, GetStatus, GetCalander
    // format and display data in a dialog

    // conclick listener for calander object so that when a date is clicked, display day breakdown

    // update preferences (username, string info)

    // get preferences (username)

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
                Toast.makeText(MainMenu.this, "Status Updated", Toast.LENGTH_LONG).show();


            } else if (result.equalsIgnoreCase("false")) {

                // If username and password does not match display a error message
                Toast.makeText(MainMenu.this, "Unable to update status", Toast.LENGTH_LONG).show();

            }
        }
    }

    private class AsyncUpdateProfilePicture extends AsyncTask<Bitmap, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainMenu.this);
        HttpURLConnection conn;
        URL url = null;

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

            Toast.makeText(MainMenu.this, "Status synced from server.", Toast.LENGTH_LONG).show();

            Switch sw = (Switch)findViewById(R.id.busy_free_switch);

            if (result.equalsIgnoreCase("1")) {
                sw.setChecked(true);
                sw.setText("Available");


            } else if (result.equalsIgnoreCase("0")) {
                sw.setChecked(false);
                sw.setText("Not Available");
            }
            else if (result.equalsIgnoreCase("failure"))
            {
                Toast.makeText(MainMenu.this, "Could not retrieve status, try again later", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncGetProfilePicture extends AsyncTask<String, String, Bitmap> {
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
        protected Bitmap doInBackground(String... params) {
            String username = params[0];
            String add = "http://10.0.2.2/GetPicture.php?username="+username;
            URL url = null;
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

            Switch sw = (Switch)findViewById(R.id.busy_free_switch);

            if (result != null)
            {
                ImageView img = (ImageView) findViewById(R.id.profile_pic_box);
                img.setImageBitmap(result);
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
                // TODO Auto-generated catch block
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
                // TODO Auto-generated catch block
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
                    List<String> result = new ArrayList<String>();
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
            Toast tst;

            if (!result.isEmpty()) {

                final List<String> selected = new ArrayList<String>();
                final List<String> declined = new ArrayList<String>();
                final List<String> results = result;
                final boolean[] itemChecked = new boolean[result.size()];

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


                Toast.makeText(MainMenu.this, "A request is already pending", Toast.LENGTH_LONG).show();

            }
            else
            {
                Toast.makeText(MainMenu.this, "User does not exist", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AsyncGetFriends extends AsyncTask<String, String, ArrayList<Pair<String, String>>> {
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
        protected ArrayList<Pair<String, String>> doInBackground(String... params) {
            ArrayList<Pair<String, String>> res = new ArrayList<Pair<String, String>>();

            try {

                // Enter URL address where your php file resides
                url = new URL("http://10.0.2.2/GetFriends.php");

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
                    String line = "";
                    String[] line2;
                    String[] line3;

                    Pair<String, String> tmp_pair;
                    Integer toggle = 0;

                    // TODO retrieve and store friend pictures as well

                    //since we are returning an array of pairs(name and status)
                    // and we are only reading one line at a time, we toggle between the tmp variable
                    // and filling the array
                    while ((line = reader.readLine()) != null) {
                            line = line.substring(0, line.length()-1);
                            line2 = line.split("/");
                            for(int i = 0; i < line2.length; i++)
                            {
                                line3 = line2[i].split(",");
                                res.add(new Pair<String, String>(line3[0], line3[1]));
                                line3 = new String[line3.length];
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
        protected void onPostExecute(ArrayList<Pair<String, String>> result) {

            //this method will be running on UI thread

            pdLoading.dismiss();
            Toast tst;

            if (!result.isEmpty() && !result.get(0).first.equals("no_friends")) {
                ArrayList<String> parsed_results = new ArrayList<String>();

                // parse the results we got into an array of single strings,
                // so that itll be compatible with the arrayadapter items
                for (Pair<String, String> current : result)
                {
                    StringBuilder builder = new StringBuilder();

                    builder.append(current.first);

                    if (current.second.equals("1"))
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

            }
            else if (!result.isEmpty() && result.get(0).first.equals("no_friends"))
            {
                Toast.makeText(MainMenu.this, "No friends to show", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(MainMenu.this, "Could not retrieve friend list", Toast.LENGTH_LONG).show();
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
}
