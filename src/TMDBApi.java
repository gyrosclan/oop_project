package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class TMDBApi {
    private static final String API_KEY = "4fb25856788ed88c4703d72dd44549ca";
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    // Search movies 
    public List<Movie> searchMovies(String query) {
        String endpoint = "/search/movie";
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlString = BASE_URL + endpoint + "?api_key=" + API_KEY + "&query=" + encodedQuery;
            return fetchMoviesFromUrl(urlString);
        } catch (Exception e) {
            System.out.println("Error in searchMovies: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // top rated movies
    public List<Movie> getTopRatedMovies() {
        String urlString = BASE_URL + "/movie/top_rated?api_key=" + API_KEY;
        return fetchMoviesFromUrl(urlString);
    }

    // latest movies
    public List<Movie> getNowPlayingMovies() {
        String urlString = BASE_URL + "/movie/now_playing?api_key=" + API_KEY;
        return fetchMoviesFromUrl(urlString);
    }

    //Discover movies with filters: genreId, language and yer
     
    public List<Movie> discoverMovies(int genreId, String language, int year) {
        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/discover/movie?api_key=" + API_KEY);

            if (genreId > 0) {
                urlBuilder.append("&with_genres=").append(genreId);
            }
            if (language != null && !language.trim().isEmpty()) {
                urlBuilder.append("&with_original_language=").append(URLEncoder.encode(language.trim(), "UTF-8"));
            }
            if (year > 0) {
                urlBuilder.append("&primary_release_year=").append(year);
            }

            String urlString = urlBuilder.toString();
            return fetchMoviesFromUrl(urlString);

        } catch (Exception e) {
            System.out.println("Error in discoverMovies: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Helper method to do the HTTP request and parse JSON into Movies
    private List<Movie> fetchMoviesFromUrl(String urlString) {
        List<Movie> movies = new ArrayList<>();
        try {
            URL url = URI.create(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error: Failed to fetch data. Response code: " + responseCode);
                return movies;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseStrBuilder = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                responseStrBuilder.append(line);
            }
            in.close();

            JSONObject responseJson = new JSONObject(responseStrBuilder.toString());
            JSONArray results = responseJson.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);

                int id = obj.getInt("id");
                String title = obj.getString("title");
                String overview = obj.optString("overview", "No overview available");
                String posterPath = obj.optString("poster_path", "");
                String posterUrl = posterPath.isEmpty() ? "" : "https://image.tmdb.org/t/p/w500" + posterPath;
                double rating = obj.optDouble("vote_average", 0.0);
                String releaseDate = obj.optString("release_date", "Unknown");

                Movie movie = new Movie(id, title, overview, posterUrl, rating, releaseDate);
                movies.add(movie);
            }

        } catch (Exception e) {
            System.out.println("Exception during API call: " + e.getMessage());
        }
        return movies;
    }
}
