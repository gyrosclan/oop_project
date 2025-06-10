package src;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ImageLoader {
    private static final Map<String, ImageIcon> cache = new ConcurrentHashMap<>();

    public static void loadImage(String imageUrl, int width, int height, Consumer<ImageIcon> onSuccess, Runnable onFailure) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            onFailure.run();
            return;
        }

        if (cache.containsKey(imageUrl)) {
            onSuccess.accept(cache.get(imageUrl));
            return;
        }

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = URI.create(imageUrl).toURL();
                BufferedImage img = ImageIO.read(url);
                if (img != null) {
                    Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        cache.put(imageUrl, icon);
                        onSuccess.accept(icon);
                    } else {
                        onFailure.run();
                    }
                } catch (Exception e) {
                    onFailure.run();
                }
            }
        };

        worker.execute();
    }
}
