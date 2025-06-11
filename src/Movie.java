package src;
public class Movie {
    // Fields made private for encapsulation
    private int id;
    private String title;
    private String overview;
    private String posterUrl;
    private double rating;
    private String releaseDate;

    // Constructor
    public Movie(int id, String title, String overview, String posterUrl, double rating, String releaseDate) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    // Display method testing and debugging purpose
    @Override
    public String toString() {
        return "Movie: " + title + " (" + releaseDate + ")\n" +
               "Rating: " + rating + "\n" +
               "Overview: " + overview + "\n" +
               "Poster URL: " + posterUrl + "\n";
    }
}
