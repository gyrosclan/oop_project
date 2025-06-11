package src;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MovieCellRenderer extends JPanel implements ListCellRenderer<Movie> {
    private JLabel titleLabel;
    private JLabel yearAndRatingLabel;
    private JLabel overviewLabel;
    private JLabel posterLabel;

    public MovieCellRenderer() {
        setLayout(new BorderLayout(10, 5));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Poster Label
        posterLabel = new JLabel();
        posterLabel.setPreferredSize(new Dimension(60, 90));
        posterLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        posterLabel.setOpaque(true);
        posterLabel.setBackground(new Color(60, 60, 60));
        posterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        posterLabel.setVerticalAlignment(SwingConstants.CENTER);
        posterLabel.setText("No Image");
        posterLabel.setForeground(Color.GRAY);
        add(posterLabel, BorderLayout.WEST);

        // Text Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        //titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        textPanel.add(titleLabel);

        yearAndRatingLabel = new JLabel();
        yearAndRatingLabel.setFont(yearAndRatingLabel.getFont().deriveFont(Font.PLAIN, 12f));
        yearAndRatingLabel.setForeground(new Color(150, 150, 150));
        textPanel.add(yearAndRatingLabel);

        overviewLabel = new JLabel();
        overviewLabel.setFont(overviewLabel.getFont().deriveFont(Font.ITALIC, 11f));
        overviewLabel.setForeground(new Color(120, 120, 120));
        textPanel.add(overviewLabel);

        add(textPanel, BorderLayout.CENTER);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Movie> list, Movie movie, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            titleLabel.setForeground(list.getSelectionForeground());
            yearAndRatingLabel.setForeground(list.getSelectionForeground().darker());
            overviewLabel.setForeground(list.getSelectionForeground().darker().darker());
            posterLabel.setBorder(BorderFactory.createLineBorder(list.getSelectionForeground(), 2));
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            titleLabel.setForeground(UIManager.getColor("Label.foreground"));
            yearAndRatingLabel.setForeground(new Color(150, 150, 150));
            overviewLabel.setForeground(new Color(120, 120, 120));
            posterLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }

        titleLabel.setText(movie.getTitle());

        String year = (movie.getReleaseDate() != null && movie.getReleaseDate().length() >= 4)
                ? movie.getReleaseDate().substring(0, 4)
                : "N/A";
        yearAndRatingLabel.setText("Year: " + year + " | Rating: " + String.format("%.1f", movie.getRating()));

        String overviewSnippet = movie.getOverview();
        if (overviewSnippet != null && overviewSnippet.length() > 120) {
            overviewSnippet = overviewSnippet.substring(0, 120) + "...";
        }
        overviewLabel.setText(overviewSnippet != null && !overviewSnippet.isEmpty()
                ? overviewSnippet
                : "No overview available.");

        // Using ImageLoader
        String posterPath = movie.getPosterUrl();
        String imageUrl = (posterPath != null && !posterPath.isEmpty())
                ? "https://image.tmdb.org/t/p/w200" + posterPath
                : null;

        posterLabel.setText("Loading...");
        posterLabel.setIcon(null);

        ImageLoader.loadImage(
                imageUrl,
                60, 90,
                icon -> {
                    posterLabel.setText("");
                    posterLabel.setIcon(icon);
                },
                () -> {
                    posterLabel.setText("No Image");
                    posterLabel.setIcon(null);
                }
        );

        return this;
    }
}