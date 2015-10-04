package dk.ockley.popularmovies;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import dk.ockley.popularmovies.models.ParcableMovie;

public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callbacks{

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    public static final String MOVIE_DETAIL_EXTRA = "movie_detail_extra";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                    .commit();
            }

        } else {
            mTwoPane = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        MovieDetailFragment df = (MovieDetailFragment) getFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
    }

    // Callback for handling click from list
    @Override
    public void onMovieSelected(ParcableMovie movie) {

        if (mTwoPane) {
            // Test for tablet layout and put movie info in parcel.
            Bundle bundle = new Bundle();
            bundle.putParcelable(MOVIE_DETAIL_EXTRA, movie);

            //Create detail fragment and set bundle as argument.
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail, fragment, DETAILFRAGMENT_TAG)
                    .commit();

        } else {
            // We are using the phone, so call the detail activity
            // and place the movie parcel as an extra.
            Intent i = new Intent(this, MovieDetail.class);
            i.putExtra(MainActivityFragment.EXTRA_MOVIE, movie);
            startActivity(i);
        }
    }
}
