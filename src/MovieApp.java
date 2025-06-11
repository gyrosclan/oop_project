package src;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException; // For SwingWorker

public class MovieApp extends JFrame {
    private TMDBApi api;
    private DefaultListModel<Movie> listModel;
    private JList<Movie> movieJList;
    private JTextPane detailsArea; 
    private JProgressBar progressBar; //loading indication
    private JLabel statusLabel; // status messages
    private JLabel posterLabel;

    private JComboBox<String> genreCombo;
    private JComboBox<String> languageCombo;
    private JComboBox<Integer> yearCombo;
    private JButton filterButton;

    private JTextField searchField;
    private JButton homeButton;
    private JButton searchButton;
    private JButton topRatedButton;

    // Genre mapping (Name to ID)
    private static final Map<String, Integer> GENRE_MAP = new LinkedHashMap<>();
    // Language list (name to code)
    private static final Map<String, String> LANGUAGE_MAP = new LinkedHashMap<>();

    static {
        // genre map
        GENRE_MAP.put("All", 0);
        GENRE_MAP.put("Action", 28);
        GENRE_MAP.put("Comedy", 35);
        GENRE_MAP.put("Drama", 18);
        GENRE_MAP.put("Horror", 27);
        GENRE_MAP.put("Science Fiction", 878);

        // language map
        LANGUAGE_MAP.put("All", "");
        LANGUAGE_MAP.put("English", "en");
        LANGUAGE_MAP.put("French", "fr");
        LANGUAGE_MAP.put("Spanish", "es");
        LANGUAGE_MAP.put("German", "de");
        LANGUAGE_MAP.put("Japanese", "ja");
        // Add more as desired
    }

    public MovieApp() {
        super("Movie Explorer");
        api = new TMDBApi();
        setFlatLaf(); // Setting FlatLaf before initializing components
        initComponents();
        // Initial load of latest movies
        executeMovieTask(this::loadLatestMoviesAsync, "Loading latest movies...");
    }

    private void setFlatLaf() {
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf: " + e);
            // Fallback to system default if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException ex) {
                System.err.println("Failed to initialize System L&F: " + ex);
            }
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // center to the screen

        // Using BorderLayout for the main frame
        setLayout(new BorderLayout(15, 15)); // Increased gaps between regions for a better look

        // Add an empty border to the content pane for overall window padding
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // top panel to hold nav/Search and filter panel's
        JPanel topPanel = new JPanel(new BorderLayout()); // Using BorderLayout to stack nav/search and filter

        // Navigation/Search Panel
        JPanel navSearchPanel = new JPanel(new GridBagLayout()); // GridBag for flexible layout
        navSearchPanel.setBorder(new EmptyBorder(0, 0, 10, 0)); // Padding at bottom of this sub-panelcuz we are adding another panel at the bottom
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding between each components in the nav/Search panel
        gbc.fill = GridBagConstraints.HORIZONTAL; // Components fill their grid cells horizontally

        // Home Button
        homeButton = createIconButton("Home", "icons/home.png");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Anchor to the left
        navSearchPanel.add(homeButton, gbc);

        // Top Rated Button
        topRatedButton = createIconButton("Top Rated", "icons/star.png");
        gbc.gridx = 1;
        gbc.gridy = 0;
        navSearchPanel.add(topRatedButton, gbc);

        // Search Field
        searchField = new JTextField(25);
        searchField.putClientProperty("JTextField.placeholderText", "Search movies by title...");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // search field get's the extra space
        navSearchPanel.add(searchField, gbc);

        // Search Button
        searchButton = createIconButton("Search", "icons/search.png");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.0; // search button Don't get extra space
        navSearchPanel.add(searchButton, gbc);

        topPanel.add(navSearchPanel, BorderLayout.NORTH); // Add nav/search to the top of topPanel

        // Separator for visual division and a better look
        topPanel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.CENTER);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5)); // Centered flow layout with larger gaps
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Options")); // Titled border for clear grouping

        genreCombo = new JComboBox<>(GENRE_MAP.keySet().toArray(new String[0]));
        languageCombo = new JComboBox<>(LANGUAGE_MAP.keySet().toArray(new String[0]));

        // Year dropdown (go from current year backwards to 1900 for more option's)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<Integer> yearsList = new ArrayList<>();
        yearsList.add(0); // For "All years" option
        for (int y = currentYear; y >= 1900; y--) { // Iterating backwards
            yearsList.add(y);
        }
        yearCombo = new JComboBox<>(yearsList.toArray(new Integer[0])); // Converting list to array

        filterButton = createIconButton("Apply Filters", "icons/filter.png"); // Button with icon

        // Adding labels and combos to the filter panel
        filterPanel.add(new JLabel("Genre:"));
        filterPanel.add(genreCombo);
        filterPanel.add(new JLabel("Language:"));
        filterPanel.add(languageCombo);
        filterPanel.add(new JLabel("Year:"));
        filterPanel.add(yearCombo);
        filterPanel.add(filterButton);

        topPanel.add(filterPanel, BorderLayout.SOUTH); // Add filter controls to the bottom of topPanel

        add(topPanel, BorderLayout.NORTH); // Add the entire topPanel to the NORTH region of the Jframe

        // Center JList and Details Area
        listModel = new DefaultListModel<>();
        movieJList = new JList<>(listModel);
        movieJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieJList.setCellRenderer(new MovieCellRenderer()); // Set custom made cell renderer
        movieJList.setVisibleRowCount(15); // Suggests initial height for the list

        JScrollPane listScroll = new JScrollPane(movieJList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Movie List")); // Titled border for the list pane

        detailsArea = new JTextPane(); // Use JTextPane to render HTML content
        detailsArea.setContentType("text/html");
        detailsArea.setEditable(false);
        detailsArea.setBorder(new EmptyBorder(10, 10, 10, 10)); // Inner padding for text
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Movie Details")); // Titled border for the details pane
        detailsScroll.setPreferredSize(new Dimension(350, 0));

        posterLabel = new JLabel("No image selected", SwingConstants.CENTER);
        posterLabel.setPreferredSize(new Dimension(300, 450));
        posterLabel.setBorder(BorderFactory.createTitledBorder("Poster"));

        // Right panel: Poster on top, details below
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(10, 10));
        rightPanel.add(posterLabel, BorderLayout.NORTH);
        rightPanel.add(detailsScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, rightPanel); // main content of the app is split in to two using JSplitPane
        splitPane.setResizeWeight(0.6); // 60% for list, 40% for poster+details
        add(splitPane, BorderLayout.CENTER);
        // Adding the split pane to the CENTER region of the frame recall that top panel was added in the NORTH

        // Status Bar Botton of the screen
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT)); // FlowLayout for simple left alignment
        statusBar.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding for the status bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // Continuous animation for "loading"
        progressBar.setVisible(false); // Hidden by default
        statusLabel = new JLabel("Ready.");
        statusBar.add(progressBar);
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH); // Add the status bar to the SOUTH region of the frame avoid anonymous inner class verbosity

        // Event Listeners: We will use lambda expressions to add all 5 listner's 
        homeButton.addActionListener(e -> executeMovieTask(this::loadLatestMoviesAsync, "Loading latest movies..."));
        filterButton.addActionListener(e -> executeMovieTask(this::applyFiltersAsync, "Applying filters..."));
        searchButton.addActionListener(e -> executeMovieTask(this::searchMoviesAsync, "Searching movies..."));
        topRatedButton.addActionListener(e -> executeMovieTask(this::loadTopRatedMoviesAsync, "Loading top rated movies..."));
        // List selection listner for the JList
        movieJList.addListSelectionListener(e -> showDetails(e));
    }

    // Helper to create buttons with icons also rescaling icons if needed
    private JButton createIconButton(String text, String iconPath) {
        JButton button = new JButton(text);
        try {
            // Getting icon resource from classpath
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(iconPath)));
            Image image = icon.getImage();
            Image newimg = image.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(newimg));
        } catch (Exception ex) {
            System.err.println("Icon not found or could not be loaded: " + iconPath + " - " + ex.getMessage());
            // setting A Fallback to text-only button if icon not found
        }
        button.setFocusPainted(false); // Remove border when focused/clicked for cleaner look
        return button;
    }

    // Asynchronous API Calls(Requests) using SwingWorker
    // We will wrap all our API calls with this method which has a SwingWorker to keep the UI responsive.
    // It takes a Runnable (the actual API call function) and a status message.
    private void executeMovieTask(Runnable apiCallRunnable, String statusMessage) {
        progressBar.setVisible(true); // Show progress bar
        statusLabel.setText(statusMessage); // Update status message

        // Disable buttons to prevent multiple simultaneous operations
        setControlsEnabled(false);

        //Inner class for swing worker
        SwingWorker<List<Movie>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Movie> doInBackground() throws Exception {
                // Execute the actual API call function on a background thread
                apiCallRunnable.run();
                return null; // The updateMovieList is called inside the apiCallRunnable, so no return needed here

            }

            @Override
            protected void done() {
                // This method runs on the Event Dispatch Thread (EDT) after doInBackgroud completes
                progressBar.setVisible(false); // Hiding progress bar
                statusLabel.setText("Ready."); // Reset status message
                setControlsEnabled(true); // Re-enabling controls

                try {
                    get(); // This line retrieves the result of doInBackground, and also re-throws any exceptions that occurred in doInBackground If any.
                } catch (InterruptedException | ExecutionException e) {
                    // Handling exceptions from the background task
                    String errorMessage = "Error: "+ (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                    statusLabel.setText(errorMessage);
                    JOptionPane.showMessageDialog(MovieApp.this, errorMessage, "API Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace(); // Logging the full stack trace for debugging
                }
            }
        };
        worker.execute(); // Start the SwingWorker
    }

    // Helper to enable/disable controls during API calls
    private void setControlsEnabled(boolean enabled) {
        homeButton.setEnabled(enabled);
        topRatedButton.setEnabled(enabled);
        searchField.setEnabled(enabled);
        searchButton.setEnabled(enabled);
        genreCombo.setEnabled(enabled);
        languageCombo.setEnabled(enabled);
        yearCombo.setEnabled(enabled);
        filterButton.setEnabled(enabled);
        movieJList.setEnabled(enabled); // Also disable list selection during loading
    }

    // API Call Wrappers functions which are to be called by SwingWorker In ExcuteMovieTask
    private void loadLatestMoviesAsync() {
        List<Movie> movies = api.getNowPlayingMovies(); // API call from TMDBApi.java
        // Update UI on the EDT via invoke later (causing no irresponsiveness)
        SwingUtilities.invokeLater(() -> updateMovieList(movies));
    }

    private void loadTopRatedMoviesAsync() {
        List<Movie> movies = api.getTopRatedMovies(); // API call from TMDBApi.java
        SwingUtilities.invokeLater(() -> updateMovieList(movies));
    }

    private void searchMoviesAsync() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            List<Movie> movies = api.searchMovies(query); // API call from TMDBApi.java
            SwingUtilities.invokeLater(() -> updateMovieList(movies));
        } else {
            // feedback for empty search of a user
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Please enter a search query.");
                updateMovieList(Collections.emptyList()); // Clear list if query is empty
            });
        }
    }

    private void applyFiltersAsync() {
        int genreId = GENRE_MAP.get((String) genreCombo.getSelectedItem());
        String langCode = LANGUAGE_MAP.get((String) languageCombo.getSelectedItem());
        int year = (Integer) yearCombo.getSelectedItem();
        List<Movie> movies = api.discoverMovies(genreId, langCode, year); // API call From TMDBApi.java
        SwingUtilities.invokeLater(() -> updateMovieList(movies));
    }

    // UI Update Methods (for JList and detailsArea(JTextPane))

    // update movie list to be called by any API call
    private void updateMovieList(List<Movie> movies) {
        listModel.clear(); // Clear existing items in the list model
        if (movies != null && !movies.isEmpty()) {
            for (Movie m : movies) {
                listModel.addElement(m); // Adding new movies
            }
            statusLabel.setText("Found " + movies.size() + " movies.");
            movieJList.setSelectedIndex(0); // Selecting the first item by default
        } else {
            // Display a message if no movies found
            statusLabel.setText("No movies found for your criteria.");
        }
        detailsArea.setText(""); // Clear details when list is updated will be updated by the list selection listner
    }

    // Method to display(update) movie details in the JTextPane
    private void showDetails(ListSelectionEvent e) {
        // getValueIsAdjusting() to prevent double-firing events during selection changes which can cause UI unresponsiveness
        if (!e.getValueIsAdjusting()) {
            Movie selected = movieJList.getSelectedValue();
            if (selected != null) {
                // place holder until the image is loaded by ImageLoader
                posterLabel.setText("Loading image...");
                posterLabel.setIcon(null);

                ImageLoader.loadImage(
                        selected.getPosterUrl(),
                        300, 450,
                        icon -> {
                            posterLabel.setIcon(icon);
                            posterLabel.setText(null); // Clear "Loading..." text (the place holder)
                        },
                        () -> {
                            posterLabel.setText("Image not available");
                            posterLabel.setIcon(null);
                        });

                // Using HTML for rich text formatting in JTextPane
                String detailsHtml = "<html>"
                        + "<body style='font-family: sans-serif; padding: 10px;'>" // Basic styling
                        + "<h3 style='color: #4CAF50; margin-bottom: 5px;'>" + selected.getTitle() + "</h3>" // Greenish title
                        + "<p style='margin-top: 0; margin-bottom: 5px;'>" // Smaller margin for paragraphs
                        + "<b>Release Date:</b> "
                        + (selected.getReleaseDate() != null ? selected.getReleaseDate() : "N/A") + "<br>"
                        + "<b>Rating:</b> " + String.format("%.1f", selected.getRating()) + "/10"
                        + "</p>"
                        + "<h4 style='color: #88B04B; margin-top: 10px; margin-bottom: 5px;'>Overview:</h4>" // Subheading for overview
                        + "<p style='text-align: justify;'>"
                        + (selected.getOverview() != null && !selected.getOverview().isEmpty() ? selected.getOverview()
                                : "No overview available.")
                        + "</p>"
                        + "</body></html>";
                detailsArea.setText(detailsHtml);
                detailsArea.setCaretPosition(0); // Scroll to top of details area
            } else {
                detailsArea.setText(""); // Clear details if nothing is selected
            }
        }
    }

    public static void main(String[] args) {
        // Ensuring GUI updates run on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new MovieApp().setVisible(true);
        });
    }
}