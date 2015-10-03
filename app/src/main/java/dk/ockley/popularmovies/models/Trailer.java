package dk.ockley.popularmovies.models;

/**
 * Created by kv on 08/09/15.
 */
public class Trailer {

    private final String title;
    private final String key;

    public String getTitle() {
        return title;
    }

    public String getKey() {
        return key;
    }

    public Trailer(String title, String key) {
        this.title = title;
        this.key = key;
    }
}
