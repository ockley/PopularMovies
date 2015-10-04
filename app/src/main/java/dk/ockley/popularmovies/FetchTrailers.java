package dk.ockley.popularmovies;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import dk.ockley.popularmovies.adapters.TrailerAdapter;
import dk.ockley.popularmovies.models.Trailer;

/**
 * Created by kv on 08/09/15.
 */

public class FetchTrailers extends AsyncTask<String, Void, String> {

    private static final String LOG_TAG = "PopMov";
    ListView lv;
    Activity context;
    ArrayList<Trailer> trailers;

    public FetchTrailers(Activity activity, ArrayList<Trailer> trailers, ListView trailerListView) {
        //lv = (ListView) context.findViewById(R.id.trailerListView);
        this.context = activity;
        this.trailers = trailers;
        this.lv = trailerListView;
    }

    @Override
    protected String doInBackground(String... params) {

        if(params.length == 0) return null;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String BASE_URL = "http://api.themoviedb.org/3/movie/"+params[0]+"/videos?";
            final String API_KEY_PARAM = "api_key";
            Uri builtURI = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, MovieKey.KEY)
                    .build();
            URL url = new URL(builtURI.toString());

            //Log.v(LOG_TAG, "Built URI : " + builtURI.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            moviesJsonStr = buffer.toString();
            //Log.d(LOG_TAG, "Final string " + moviesJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return moviesJsonStr;
    }

    @Override
    protected void onPostExecute(String moviesJSONstr) {
        super.onPostExecute(moviesJSONstr);
        //Log.d(LOG_TAG, "JSONSTR: "+ moviesJSONstr);

        try {
            JSONObject movies = new JSONObject(moviesJSONstr);
            JSONArray jsonArr = movies.getJSONArray("results");
            int len = jsonArr.length();
            //Log.d(LOG_TAG, "Length er " + len);
            if ( len > 0){

                for (int i = 0; i < len; i++) {
                    JSONObject tmpObj = jsonArr.getJSONObject(i);
                    Trailer trailer = new Trailer(tmpObj.getString("name"), tmpObj.getString("key"));
                    trailers.add(trailer);
                }

            }
            else {
//                    if (toast != null) toast.cancel();
//                    toast.makeText(getActivity(), "No Movies Found!", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        TrailerAdapter adapter = new TrailerAdapter(context, trailers);
        //Log.d(LOG_TAG, "ADAPTER: " + adapter);
        this.lv.setAdapter(adapter);
    }
}
