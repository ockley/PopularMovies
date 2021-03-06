package dk.ockley.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import dk.ockley.popularmovies.adapters.FrontPosterAdapter;
import dk.ockley.popularmovies.contentprovider.FavoritesProvider;
import dk.ockley.popularmovies.data.FavoriteTable;
import dk.ockley.popularmovies.fetchers.FetchPopMovies;
import dk.ockley.popularmovies.models.ParcableMovie;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String INSTANCE_MOVIE = "instane_movie";
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private GridView movieGridView;
    public ArrayList<ParcableMovie> topMoviesParcel;
    private Toast toast;
    private Callbacks mCallbacks;
    private FrontPosterAdapter adapter;

    public MainActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState == null) {
            //getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Swith between all action bar settings
        int id = item.getItemId();
                FetchPopMovies fetchPopMovies = new FetchPopMovies(getActivity(), movieGridView, topMoviesParcel);
        switch (id) {
            case (R.id.action_popular):
                fetchPopMovies.execute("popularity.desc");
                break;
            case (R.id.action_user_rating):
                     fetchPopMovies.execute("vote_average.desc");
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
        movieGridView = (GridView) v.findViewById(R.id.popular_movies_gridview);

        // Handle click and set a callback function
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallbacks.onMovieSelected((ParcableMovie) movieGridView.getItemAtPosition(position));
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MOVIE)) {
            //Something was saved
            Log.v(LOG_TAG, "IM IN!!");
            try {
                topMoviesParcel = savedInstanceState.getParcelableArrayList(INSTANCE_MOVIE);
                adapter = new FrontPosterAdapter(getActivity(), topMoviesParcel);
                movieGridView.setAdapter(adapter);
            } catch (Error e) {

            }
        } else {
            // Hook op grid view and fetch data
            FetchPopMovies fetchPopMovies = new FetchPopMovies(getActivity(), movieGridView, topMoviesParcel);
            fetchPopMovies.execute("popularity.desc");
        }
        return v;
    }

    // Remember to save the outState in case of destruction
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int count = movieGridView.getCount();
        ArrayList<ParcableMovie> outList = new ArrayList<>();
        for(int i=0; i<count; ++i)
        {
            outList.add((ParcableMovie) movieGridView.getItemAtPosition(i));
        }
        outState.putParcelableArrayList(INSTANCE_MOVIE, outList);
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
        String[] projection = Utils.getProjection();

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

        adapter = new FrontPosterAdapter(getActivity(), topMoviesParcel);
        movieGridView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "LOADER RESET");

    }


    public interface Callbacks {

        void onMovieSelected(ParcableMovie movie);
    }
}
