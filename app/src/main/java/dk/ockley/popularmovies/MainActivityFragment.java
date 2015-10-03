package dk.ockley.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
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

import dk.ockley.popularmovies.adapters.FrontPosterAdapter;
import dk.ockley.popularmovies.contentprovider.FavoritesProvider;
import dk.ockley.popularmovies.data.FavoriteTable;
import dk.ockley.popularmovies.models.ParcableMovie;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String INSTANCE_MOVIE = "instane_movie";
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private GridView movieGridView;
    private ArrayList<ParcableMovie> topMoviesParcel;
    private Toast toast;
    private Callbacks mCallbacks;
    FrontPosterAdapter adapter;
    FrontPosterAdapter favAdapter;

    public MainActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Swith between all action bar settings
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_popular):
                new FetchPopMovies().execute("popularity.desc");
                break;
            case (R.id.action_user_rating):
                new FetchPopMovies().execute("vote_average.desc");
                break;
            case (R.id.action_user_favorites):
                // Get user favorites from database
                getLoaderManager().restartLoader(0, null, this);

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.movie_list, container, false);

        // Hook op grid view and fetch data
        movieGridView = (GridView) v.findViewById(R.id.popular_movies_gridview);
        new FetchPopMovies().execute("popularity.desc");

        // Handle click and set a callback function
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onMovieSelected((ParcableMovie) movieGridView.getItemAtPosition(position));
            }
        });
        return v;
    }

    // Remember to save the outState in case of destruction
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(INSTANCE_MOVIE, topMoviesParcel);
    }

    // Methods to hook up the callback to the activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "CREATE LOADER");
        String[] projection =
                {
                        FavoriteTable.COLUMN_ID,
                        FavoriteTable.COLUMN_TITLE,
                        FavoriteTable.COLUMN_IMAGE_PATH,
                        FavoriteTable.COLUMN_RELEASE_DATE,
                        FavoriteTable.COLUMN_USER_RATING,
                        FavoriteTable.COLUMN_SYNOPSIS,
                };
        CursorLoader cursorLoader = new CursorLoader(getActivity(), FavoritesProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "LOADER FINISHED");
        topMoviesParcel = new ArrayList<>();
        while(data.moveToNext()) {
            topMoviesParcel.add(new ParcableMovie(
                    data.getString(data.getColumnIndex(FavoriteTable.COLUMN_ID)),
                    data.getString(data.getColumnIndex(FavoriteTable.COLUMN_TITLE)),
                    data.getString(data.getColumnIndex(FavoriteTable.COLUMN_IMAGE_PATH)),
                    data.getString(data.getColumnIndex(FavoriteTable.COLUMN_SYNOPSIS)),
                    data.getFloat(data.getColumnIndex(FavoriteTable.COLUMN_USER_RATING)),
                    data.getString(data.getColumnIndex(FavoriteTable.COLUMN_RELEASE_DATE))));
        }

        favAdapter = new FrontPosterAdapter(getActivity(), topMoviesParcel);
        movieGridView.setAdapter(favAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "LOADER RESET");

    }

    public interface Callbacks {

        void onMovieSelected(ParcableMovie movie);
    }

    // Class to fetch data

    class FetchPopMovies extends AsyncTask<String, Void, String> {
        GridView grid;

        public void FetchPopMovies() {
            //grid = (GridView) ctx.findViewById(R.id.popular_movies_gridview);
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
                        .appendQueryParameter(API_KEY_PARAM, "INSERT KEY HERE")
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
                    toast.makeText(getActivity(), "No Movies Found!", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter = new FrontPosterAdapter(getActivity(), topMoviesParcel);
            movieGridView.setAdapter(adapter);
        }
    }

}
