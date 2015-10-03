package dk.ockley.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FavoritesDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = FavoritesDBHelper.class.getSimpleName();

    //Name and version
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 5;

    public FavoritesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Create DB
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "Database created");
        FavoriteTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FavoriteTable.onUpgrade(db, oldVersion, newVersion);

    }
}
