package dk.ockley.popularmovies;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import dk.ockley.popularmovies.contentprovider.FavoritesProvider;
import dk.ockley.popularmovies.data.FavoriteTable;
import dk.ockley.popularmovies.fetchers.FetchReviews;
import dk.ockley.popularmovies.fetchers.FetchTrailers;
import dk.ockley.popularmovies.models.ParcableMovie;
import dk.ockley.popularmovies.models.Review;
import dk.ockley.popularmovies.models.Trailer;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {
    private static final String FAVORITE_PREFS = "dk.ockley.popularmovies.favorites";
    private static final String INSTANCE_MOVIE = "instance_movie";
    private static final String LOG_TAG = "PopMov";
    private ImageView movieImage;
    private TextView movieTitle;
    private TextView movieReleaseDate;
    private RatingBar userRating;
    private TextView movieSynopsis;
    private ToggleButton favButton;
    private ListView trailerListView;
    private TextView reviewsTextView;
    private ParcableMovie movieInfo;
    ArrayList<Trailer> trailers;
    ArrayList<Review> reviews;
    Toast toast;

    public MovieDetailFragment() {}

    //Setup menu


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.action_item_share);
        menuItem.setVisible(true);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
       if (!trailers.isEmpty()) {
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createYouTubeIntent(trailers.get(0)));
            } else {
                Log.d(LOG_TAG, "Share is null!");
            }
       }
    }

    private Intent createYouTubeIntent(Trailer trailer) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,"Hey, take a look at " + trailer.getTitle() + "\n" + Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
        return shareIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Set retain to true to save data on rotation
        setRetainInstance(true);
        setHasOptionsMenu(true);

        View v =  inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MOVIE)) {
            //Something was saved
            try {
                movieInfo = savedInstanceState.getParcelable(INSTANCE_MOVIE);
            } catch (Error e) {

            }
        } else {
            // Not the best hack, but I needed something in movie info
            movieInfo = new ParcableMovie("-1", "","","",(float)0, "");
        }

        // Look for arguments in case of two pane layout
        Bundle bundle = getArguments();
        if (bundle != null) {
            movieInfo = bundle.getParcelable(MainActivity.MOVIE_DETAIL_EXTRA);
        }

        // Look for intent in case of single pane layout
        Intent i = getActivity().getIntent();
        if (i != null && i.hasExtra(MainActivityFragment.EXTRA_MOVIE)) {
            movieInfo = i.getParcelableExtra(MainActivityFragment.EXTRA_MOVIE);
        }

        //Hook up View items
        movieImage = (ImageView) v.findViewById(R.id.movie_poster);
        movieTitle = (TextView) v.findViewById(R.id.movie_title);
        movieReleaseDate = (TextView) v.findViewById(R.id.movie_release_date);
        userRating = (RatingBar) v.findViewById(R.id.movie_rating_bar);
        movieSynopsis = (TextView) v.findViewById(R.id.movie_synopsis);
        favButton = (ToggleButton) v.findViewById(R.id.favorite_toggleButton);
        reviewsTextView = (TextView) v.findViewById(R.id.reviewsTextView);
        trailerListView = (ListView) v.findViewById(R.id.trailerListView);

        //Fetch trailers
        trailers = new ArrayList<>();
        trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Trailer trailer = trailers.get(position);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey())));
            }
        });

        //Fetch trailers and reviews (if movie info is not my manual -1
        if (movieInfo.getID() != "-1") {
            FetchTrailers fetchTrailers = new FetchTrailers(getActivity(), trailers, trailerListView);
            fetchTrailers.execute(String.valueOf(movieInfo.getID()));

            FetchReviews fetchReviews = new FetchReviews(getActivity(), reviewsTextView);
            fetchReviews.execute(String.valueOf(movieInfo.getID()));

            //Fill view elements with movie info
            Picasso.with(getActivity()).load(movieInfo.getPosterImage()).into(movieImage);
            movieTitle.setText(movieInfo.getTitleName());
            movieReleaseDate.setText(movieInfo.getReleaseDate());
            Float d = ((movieInfo.getUserRating()*5) /100);
            userRating.setRating(d);
            movieSynopsis.setText(movieInfo.getSynopsis());
        }

        //Handle favorite position
        if (isFavorite()) {
            Log.d(LOG_TAG, "Sat til TRUE");
            favButton.setChecked(true);
        }  else {
            Log.d(LOG_TAG, "Sat til FALSK");
            favButton.setChecked(false);
        }

        //Handle favorites button
        favButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ContentValues cValues = new ContentValues();
                    cValues.put(FavoriteTable.COLUMN_ID, movieInfo.getID());
                    cValues.put(FavoriteTable.COLUMN_TITLE, movieInfo.getTitleName());
                    cValues.put(FavoriteTable.COLUMN_IMAGE_PATH, movieInfo.getPosterImage());
                    cValues.put(FavoriteTable.COLUMN_RELEASE_DATE, movieInfo.getReleaseDate());
                    cValues.put(FavoriteTable.COLUMN_SYNOPSIS, movieInfo.getSynopsis());
                    cValues.put(FavoriteTable.COLUMN_USER_RATING, movieInfo.getUserRating());
                    Uri uri = getActivity().getContentResolver().insert(FavoritesProvider.CONTENT_URI, cValues);
                } else {

                    String[] projection = Utils.getProjection();
                    String selectionClause = FavoriteTable.COLUMN_ID + " = ?";
                    String[] selectionArgs = {""};
                    selectionArgs[0] = String.valueOf(movieInfo.getID());

                    Cursor cursor = getActivity().getContentResolver().query(
                            FavoritesProvider.CONTENT_URI,
                            projection,
                            selectionClause,
                            selectionArgs,
                            "");

                    if(null == cursor) {

                    } else if (cursor.getCount() < 1) {

                    } else {
                        cursor.moveToFirst();
                        String id = cursor.getString(cursor.getColumnIndex(FavoriteTable.COLUMN_ID));
                        Uri uri = Uri.parse(FavoritesProvider.CONTENT_URI + "/" + id);
                        getActivity().getContentResolver().delete(uri,
                                null,
                                null);
                    }
                }

            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INSTANCE_MOVIE, movieInfo);
    }

    private boolean isFavorite() {
        String[] projection = Utils.getProjection();
        String selectionClause = FavoriteTable.COLUMN_ID + " = ?";
        String[] selectionArgs = {""};
        selectionArgs[0] = String.valueOf(movieInfo.getID());

        Cursor cursor = getActivity().getContentResolver().query(
                FavoritesProvider.CONTENT_URI,
                projection,
                selectionClause,
                selectionArgs,
                "");
        if (cursor.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
