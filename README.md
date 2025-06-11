# 🎬 Movie Explorer App

A modern Java Swing GUI application that fetches and displays movies from the [TMDB (The Movie Database)](https://www.themoviedb.org/) API. Users can search, filter, and browse popular, top-rated, and latest movies in an elegant and responsive desktop app.

---

## 📌 Features

- 🔍 **Search movies** by title.
- 📂 **Filter movies** (Popular, Top Rated, Latest).
- 🎨 **Custom-styled list** using `ListCellRenderer` with posters, titles, and ratings.
- 📄 **Details pane** with full overview and poster.
- ⚡ **Non-blocking API calls** using `SwingWorker`.

---

## 🛠️ Technologies Used

- **Java SE 8+**
- **Java Swing**
- **TMDB API**
- **SwingWorker** for background threading
- **Custom `ListCellRenderer`** for dynamic UI
- **FlatLaf** for modern dark/light themes
- **JSON Parsing** via `org.json` library

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 8 or higher
- Internet connection (for API and image loading)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/movie-explorer.git
   cd movie-explorer
   ```

2. Open the project in your favorite IDE (e.g., IntelliJ, NetBeans, VS Code with Java extension).

3. Add your **TMDB API key** in the API URL string in `MovieApp.java` or use a config file.

4. Run the `MovieApp.java` class to launch the application.

---

## 📂 Project Structure

```
.
├── MovieApp.java           # Main class with GUI setup and event handlers
├── MovieFetcher.java       # Handles API calls and JSON parsing
├── MovieRenderer.java      # Custom JList cell renderer with images and text
├── Movie.java              # POJO representing movie data
├── utils/                  # Utility classes (if any)
└── README.md
```

---

## 🧠 Highlights & Unusual Concepts

### 1. 🧵 SwingWorker
- Used in `fetchAndDisplayMovies()` to fetch movie data in the background.
- Keeps the GUI responsive while networking is done asynchronously.

### 2. 🌐 TMDB API Fetching
- Raw HTTP GET requests made with `HttpURLConnection`.
- JSON parsed using `org.json` to extract movie details.
- Error handling included for robustness.

### 3. 🖼️ Custom ListCellRenderer
- The `MovieRenderer` class overrides how each `JList` item is displayed.
- Poster images are dynamically loaded from TMDB.
- HTML used in `JLabel` for formatting text and layout.

---

## 🖼️ UI Preview

> Add screenshots or a demo gif here showing the search, filter, and detail pane.
![Uploading project_demo.gif…]()


---


## 🙌 Acknowledgements

- [TMDB API](https://www.themoviedb.org/documentation/api) for movie data.
- [FlatLaf](https://www.formdev.com/flatlaf/) for modern Swing UI themes.
