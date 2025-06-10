package src;

public class TestTMDB {
    public static void main(String[] args) {
        TMDBApi api = new TMDBApi();
        java.util.List<Movie> movies = api.discoverMovies(18, "en", 2025);

        for (Movie m : movies) {
            System.out.println(m);
            System.out.println("-------------------");
        }
    }
}
