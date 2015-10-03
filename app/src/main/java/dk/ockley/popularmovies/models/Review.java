package dk.ockley.popularmovies.models;

/**
 * Created by kv on 08/09/15.
 */
public class Review {

    private final String author;
    private final String content;
    private final String url;

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public Review(String author, String content, String url) {
        this.author = author;
        this.content = content;
        this.url = url;
    }
}
