package dk.ockley.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kv on 08/09/15.
 */
public class FavoritesPreference {
    public static final String PREFS_NAME = "dk.ockley.popularmovies";
    public static final String FAVORITES = "Favorite";

    public FavoritesPreference() {
        super();
    }

    public void storeFavorites(Context context, List favorites) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        editor.putString(FAVORITES, jsonFavorites);
        editor.commit();
    }

    public ArrayList loadFavorites(Context context) {
        SharedPreferences settings;
        List favorites;
        settings = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            int[] favoriteMovies = gson.fromJson(jsonFavorites, int[].class);
            favorites = Arrays.asList(favoriteMovies);
            favorites = new ArrayList(favorites);
        } else {
            return null;
        }
        return (ArrayList) favorites;
    }

    public void addFavorite(Context context, int movieID) {
        List favorites = loadFavorites(context);
        if (favorites == null)
            favorites = new ArrayList();
        favorites.add(movieID);
        storeFavorites(context, favorites);
    }

    public void removeFavorite(Context context, int movieID) {
        ArrayList favorites = loadFavorites(context);
        if (favorites != null) {
            favorites.remove(movieID);
            storeFavorites(context, favorites);
        }
    }
}
