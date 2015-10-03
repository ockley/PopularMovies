package dk.ockley.popularmovies.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kv on 13/08/15.
 */
public class ParcableMovie implements Parcelable {
    private static final String KEY_TITLE_NAME = "title_name";
    private static final String KEY_POSTER_IMAGE = "poster_image";
    private static final String KEY_SYNOPSIS = "synopsis";
    private static final String KEY_USER_RATING = "user_rating";
    private static final String KEY_RELEASE_DATE = "release_date";
    private static final String KEY_ID = "id";

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getPosterImage() {
        return "http://image.tmdb.org/t/p/w185" + posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Float getUserRating() {
        return userRating;
    }

    public void setUserRating(Float userRating) {
        this.userRating = userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getID() {
        return ID;
    }

    private String ID;
    private String titleName;
    private String posterImage;
    private String synopsis;
    private Float userRating;
    private String releaseDate;

    public ParcableMovie(String ID, String titleName, String posterImage, String synopsis, Float userRating, String releaseDate) {
        this.ID = ID;
        this.titleName = titleName;
        this.posterImage = posterImage;
        this.synopsis = synopsis;
        this.userRating = userRating;
        this. releaseDate = releaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ID, this.ID);
        bundle.putString(KEY_TITLE_NAME, this.titleName);
        bundle.putString(KEY_POSTER_IMAGE, this.posterImage);
        bundle.putString(KEY_SYNOPSIS, this.synopsis);
        bundle.putFloat(KEY_USER_RATING, this.userRating);
        bundle.putString(KEY_RELEASE_DATE, this.releaseDate);
        dest.writeBundle(bundle);
    }
    public static final Creator<ParcableMovie> CREATOR = new Creator<ParcableMovie>() {
        @Override
        public ParcableMovie createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();
            return new ParcableMovie(bundle.getString(KEY_ID), bundle.getString(KEY_TITLE_NAME), bundle.getString(KEY_POSTER_IMAGE), bundle.getString(KEY_SYNOPSIS), bundle.getFloat(KEY_USER_RATING), bundle.getString(KEY_RELEASE_DATE));
        }

        @Override
        public ParcableMovie[] newArray(int size) {
            return new ParcableMovie[size];
        }
    };

}
