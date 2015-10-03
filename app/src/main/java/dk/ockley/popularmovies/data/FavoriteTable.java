package dk.ockley.popularmovies.data;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FavoriteTable {

    // Database table
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_USER_RATING = "user_rating";
    public static final String COLUMN_SYNOPSIS = "synopsis";

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_FAVORITES
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_IMAGE_PATH + " text not null, "
            + COLUMN_RELEASE_DATE + " text not null, "
            + COLUMN_USER_RATING + " real not null, "
            + COLUMN_SYNOPSIS + " text not null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(FavoriteTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(database);
    }
}