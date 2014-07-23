package com.kekwanu.asynctaskexample2;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity {
    private final String TAG = MainActivity.class.getCanonicalName();
    private TextView numFollowersTextView;
    private TextView numFollowingTextView;
    private TextView numLikesTextView;
    private EditText lowEditText;
    private EditText highEditText;
    private Button submitButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numFollowersTextView = (TextView)findViewById(R.id.num_followers);
        numFollowingTextView = (TextView)findViewById(R.id.num_following);
        numLikesTextView = (TextView) findViewById(R.id.num_likes);
        lowEditText = (EditText)findViewById(R.id.low);
        highEditText = (EditText)findViewById(R.id.high);
        submitButton = (Button)findViewById(R.id.submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               requestData();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestData(){
        Log.i(TAG,"requestData");

        int duration = Toast.LENGTH_SHORT;

        if (lowEditText.getText().toString().equals("")){

            Toast.makeText(this, "low value cannot be empty", duration).show();
        }
        else if (highEditText.getText().toString().equals("")){
            Toast.makeText(this, "high value cannot be empty", duration).show();
        }
        else{

            int low = Integer.parseInt(lowEditText.getText().toString());
            int high = Integer.parseInt(highEditText.getText().toString());

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("request", "test_api");
            params.put("low", low);
            params.put("high", high);
            String json = new GsonBuilder().create().toJson(params, Map.class);

            String stringUrl = "https://hello.fanhour.com/api";
            new NetworkTask().execute(stringUrl, json);

        }

    }

    private void processResults(JSONObject asyncResult){
        Log.i(TAG, "processResults");

        if (asyncResult != null) {
            Log.i(TAG, "processResults - asyncResult is not null...");

            try {
                long num_followers = asyncResult.getLong("num_followers");
                long num_following = asyncResult.getLong("num_following");
                long num_likes = asyncResult.getLong("num_likes");

                Log.i(TAG, "processResults - num_followers is: "+num_followers);
                Log.i(TAG, "processResults - num_following is: "+num_following);

                numFollowersTextView.setText("Followers: "+Long.toString(num_followers));
                numFollowingTextView.setText("Following: "+Long.toString(num_following));
                numLikesTextView.setText("Likes: "+Long.toString(num_likes));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else{
            Log.i(TAG, "processResults - asyncResult is null...");

        }
    }


    //network stuff
    private class NetworkTask extends AsyncTask<String, Void, JSONObject> {
        private final String TAG = NetworkTask.class.getCanonicalName();

        @Override
        protected JSONObject doInBackground(String... params) {
            Log.i(TAG, "doInBackground");

            String result = null;
            JSONObject returnObj = null;

            // params comes from the execute() call: params[0] is the url.
            try {

                String url      = params[0];
                String paramStr = params[1];

                Log.i(TAG, "doInBackground - url is: "+url+" params is: "+paramStr);

                HttpResponse response = (new HttpRequestHelper()).makeRequest(url, paramStr);

                if (response != null){
                    Log.i(TAG, "doInBackground - got follows data");

                    result = EntityUtils.toString(response.getEntity());

                    JSONObject jObject = null;

                    try {
                        jObject         = new JSONObject(result);
                        String msg      = jObject.getString("msg");
                        String request  = jObject.getString("request");

                        if (msg.equals("success")){
                            Log.i(TAG, "doInBackground - call returned success, request is: "+request);

                            returnObj = jObject.getJSONObject("data");

                        }
                        else {
                            Log.i(TAG, "doInBackground - error: "+msg);

                        }

                    }
                    catch (JSONException e) {
                        Log.i(TAG, "doInBackground - catch block, err: "+e.getMessage());

                        e.printStackTrace();
                    }
                }
                else{
                    Log.i(TAG, "doInBackground - follows data is null");
                }
            }
            catch (IOException e) {
                Log.i(TAG, "doInBackground - exception: "+e.getMessage());
            }

            return returnObj;
        }

        @Override
        public void onPreExecute() {
            Log.i(TAG, "onPreExecute");

            progressBar.setVisibility(View.VISIBLE);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONObject asyncResult) {
            Log.i(TAG, "onPostExecute - asyncResult is : "+asyncResult);

            progressBar.setVisibility(View.GONE);

            if (asyncResult != null) {
                processResults(asyncResult);
            }
            else{
                Log.i(TAG, "onPostExecute - asyncResult is : "+asyncResult);
            }

        }
    }

}
