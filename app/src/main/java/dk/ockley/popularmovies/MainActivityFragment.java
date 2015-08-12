package dk.ockley.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private GridView popMovieGridView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);


        String[] posterImagesArray = {
                "http://www.impawards.com/2015/posters/hitman_agent_forty_seven_ver6.jpg",
                "http://www.impawards.com/2011/posters/thor_ver3_xlg.jpg",
                "http://www.impawards.com/intl/uk/2015/posters/shaun_the_sheep_ver14.jpg",
                "http://www.impawards.com/2015/posters/mission_impossible__rogue_nation_ver14.jpg",
                "http://www.impawards.com/2015/posters/goosebumps.jpg",
                "http://www.impawards.com/2015/posters/jennys_wedding.jpg",
                "http://www.impawards.com/2016/posters/finest_hours.jpg"
        };
        ArrayList<String> posterImageList = new ArrayList<String>(
                Arrays.asList(posterImagesArray));

        popMovieGridView = (GridView) v.findViewById(R.id.popular_movies_gridview);
        FrontPosterAdapter adapter = new FrontPosterAdapter(getActivity(), posterImageList);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.frontpage_item, R.id.poster_image, posterImageList);
        popMovieGridView.setAdapter(adapter);
        return v;
    }

    class FetchPopMovies extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=b627c3bad1168d44aca4ff3bb78a5e23");

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
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("POPMOVIE", "Error ", e);
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
                        Log.e("POPMOVIE", "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
        }
    }


}

//            //Clear the top tracks list
//            topTracksParcel = new ArrayList<ParcelableTopTracks>();
//            if (tracks != null && tracks.size() > 0) {
//                for (Track track : tracks) {
//                    //Populate the list with parcelable top tracks
//                    if(track.album.images.size() > 0)
//                        topTracksParcel.add(new ParcelableTopTracks(track.name, track.album.name, track.album.images.get(0).url));
//                    else
//                        topTracksParcel.add(new ParcelableTopTracks(track.name, track.album.name, null));
//                }
//            } else {
//                if (toast != null)
//                    toast.cancel();
//                toast.makeText(getActivity(),"No Top Tracks Found!", Toast.LENGTH_SHORT).show();
//            }
//
//            //Populate list view with adapter
//            TopTracksAdapter adapter = new TopTracksAdapter(getActivity(), topTracksParcel);
//            topTracksList.setAdapter(adapter);
