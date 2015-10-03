package dk.ockley.popularmovies.contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import dk.ockley.popularmovies.data.FavoriteTable;
import dk.ockley.popularmovies.data.FavoritesDBHelper;


public class FavoritesProvider extends ContentProvider {
    private static final String LOG_TAG = FavoritesProvider.class.getSimpleName();


    public static final String CONTENT_AUTHORITY = "dk.ockley.popularmovies.contentprovider";
    private static final String BASE_PATH = FavoriteTable.TABLE_FAVORITES;

    //Create Content Uri
    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + FavoriteTable.TABLE_FAVORITES);

    //Create Cursor of base type directory
    public static final String CONTENT_DIR_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FavoriteTable.TABLE_FAVORITES;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FavoriteTable.TABLE_FAVORITES;

    public static Uri buildFavoritesUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }


    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoritesDBHelper database;

    private static final int FAVORITES = 100;
    private static final int FAVORITE_WITH_ID = 200;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        //final String authority = CONTENT_AUTHORITY;

        matcher.addURI(CONTENT_AUTHORITY, FavoriteTable.TABLE_FAVORITES, FAVORITES);
        matcher.addURI(CONTENT_AUTHORITY, FavoriteTable.TABLE_FAVORITES + "/#", FAVORITE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "Create database");
        database = new FavoritesDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case FAVORITES: {
                retCursor = database.getReadableDatabase().query(
                        FavoriteTable.TABLE_FAVORITES,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
            }
            return retCursor;
            case FAVORITE_WITH_ID: {
                retCursor = database.getReadableDatabase().query(
                        FavoriteTable.TABLE_FAVORITES,
                        projection,
                        FavoriteTable.COLUMN_ID + " = ? ",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
            }
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
            return retCursor;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITES: {
                return FavoritesProvider.CONTENT_DIR_TYPE;
            }
            case FAVORITE_WITH_ID: {
                return FavoritesProvider.CONTENT_ITEM_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = database.getWritableDatabase();
        Uri returnUri;
        long id = 0;
        //Log.d(LOG_TAG, "Match: " + sUriMatcher.match(uri) + " og  CONTENT_URI: " + CONTENT_URI);
        switch (sUriMatcher.match(uri)) {
            case FAVORITES: {
                Log.d(LOG_TAG, "VALS er: " + values.toString());
                id = db.insert(FavoriteTable.TABLE_FAVORITES, null, values);

                if(id > 0) {
                    returnUri = buildFavoritesUri(id);
                } else {
                    throw new android.database.SQLException("Failed to insert row int: " + uri);
                }
                break;
            }
            case FAVORITE_WITH_ID:
                String _id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "INDSÆTTER: " + id);

            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = database.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch(match){
            case FAVORITES:
                Log.d(LOG_TAG, "SLETTER FAVORITES");
                numDeleted = db.delete(
                        FavoriteTable.TABLE_FAVORITES, selection, selectionArgs);
                // reset _ID
                //db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + FavoriteTable.TABLE_FAVORITES + "'");
                break;
            case FAVORITE_WITH_ID:
                Log.d(LOG_TAG, "SLETTER FAVORITE MED ID: " + ContentUris.parseId(uri));
                numDeleted = db.delete(FavoriteTable.TABLE_FAVORITES,
                        FavoriteTable.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                //db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + FavoriteTable.TABLE_FAVORITES + "'");

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return numDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        final SQLiteDatabase db = database.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch(match){
            case FAVORITES:
                // allows for multiple transactions
                db.beginTransaction();

                // keep track of successful inserts
                int numInserted = 0;
                try{
                    for(ContentValues value : values){
                        if (value == null){
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;
                        try{
                            _id = db.insertOrThrow(FavoriteTable.TABLE_FAVORITES,
                                    null, value);
                        }catch(SQLiteConstraintException e) {
                            Log.w(LOG_TAG, "Attempting to insert " +
                                    value.getAsString(
                                            FavoriteTable.COLUMN_TITLE)
                                    + " but value is already in database.");
                        }
                        if (_id != -1){
                            numInserted++;
                        }
                    }
                    if(numInserted > 0){
                        // If no errors, declare a successful transaction.
                        // database will not populate if this is not called
                        db.setTransactionSuccessful();
                    }
                } finally {
                    // all transactions occur at once
                    db.endTransaction();
                }
                if (numInserted > 0){
                    // if there was successful insertion, notify the content resolver that there
                    // was a change
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return numInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = database.getWritableDatabase();
        int numUpdated = 0;

        if (values == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch(sUriMatcher.match(uri)){
            case FAVORITES:{
                numUpdated = db.update(FavoriteTable.TABLE_FAVORITES,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            case FAVORITE_WITH_ID: {
                numUpdated = db.update(FavoriteTable.TABLE_FAVORITES,
                        values,
                        FavoriteTable.COLUMN_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            }
            default:{
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        if (numUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }
}
