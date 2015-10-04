package dk.ockley.popularmovies;

import dk.ockley.popularmovies.data.FavoriteTable;

/**
 * Created by kv on 03/10/15.
 */
public class Utils {

    static String[] getProjection() {
        return new String[]
                {
                        FavoriteTable.COLUMN_ID,
                        FavoriteTable.COLUMN_TITLE,
                        FavoriteTable.COLUMN_IMAGE_PATH,
                        FavoriteTable.COLUMN_RELEASE_DATE,
                        FavoriteTable.COLUMN_USER_RATING,
                        FavoriteTable.COLUMN_SYNOPSIS,

                };

    }
}
