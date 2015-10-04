package dk.ockley.popularmovies.fetchers;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

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

import dk.ockley.popularmovies.MovieKey;
import dk.ockley.popularmovies.adapters.FrontPosterAdapter;
import dk.ockley.popularmovies.models.ParcableMovie;

/**
 * Created by kv on 01/10/15.
 */
public class FetchPopMovies extends AsyncTask<String, Void, String> {
    private static final String LOG_TAG = FetchPopMovies.class.getSimpleName();

    ArrayList<ParcableMovie> topMoviesParcel;
    GridView movieGridView;
    Activity activity;
    private Toast toast;

    public FetchPopMovies(Activity act, GridView gridView) {
        movieGridView = gridView;
        activity = act;
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
            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_BY_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";
            Uri builtURI = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, params[0])
                    .appendQueryParameter(API_KEY_PARAM, MovieKey.KEY)
                    .build();
            URL url = new URL(builtURI.toString());

            //Log.v("POPMOVIE", "Built URI : " + builtURI.toString());

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
            //Log.d("POPMOVIE", "Final string " + moviesJsonStr);
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
        try {
            JSONObject movies = new JSONObject(moviesJSONstr);
            JSONArray jsonArr = movies.getJSONArray("results");

            topMoviesParcel = new ArrayList<>();
            int len = jsonArr.length();
            if ( len > 0) {
                for (int i = 0; i < len; i++) {
                    JSONObject tmpObj = jsonArr.getJSONObject(i);
                    topMoviesParcel.add(new ParcableMovie(tmpObj.getString("id"), tmpObj.getString("original_title"), tmpObj.getString("poster_path"), tmpObj.getString("overview"), (float) tmpObj.getDouble("popularity"), tmpObj.getString("release_date")));
                }
            } else {
                if (toast != null) toast.cancel();
                toast.makeText(activity, "No Movies Found!", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        FrontPosterAdapter adapter = new FrontPosterAdapter(activity, topMoviesParcel);
        movieGridView.setAdapter(adapter);
    }
}
