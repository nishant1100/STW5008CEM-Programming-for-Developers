import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ImageDownloaderApplication extends JFrame {

    private ExecutorService executorService;
    private static final String DOWNLOAD_DIRECTORY = "./downloaded_file/";
    private List<Future<?>> downloadTasks;
    private Map<Future<?>, DownloadInfo> downloadInfoMap;
    private JTextField inputarea;
    private JProgressBar progressindicator;
    private JButton cancel_btn;
    private JButton download_btn;
    private JButton pause_btn;
    private JButton resume_btn;
    private JLabel statusLabel;
    private JLabel textImage;

    public ImageDownloaderApplication() {
        initComponents();
        executorService = Executors.newFixedThreadPool(5);
        downloadTasks = new CopyOnWriteArrayList<>();
        downloadInfoMap = new ConcurrentHashMap<>();

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Image Downloader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        textImage = new JLabel("IMAGE DOWNLOADER", JLabel.CENTER);
        textImage.setFont(new Font("Algerian", Font.BOLD, 24));
        add(textImage, BorderLayout.NORTH);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inputarea = new JTextField(40);
        panel.add(inputarea);

        download_btn = new JButton("Download", new ImageIcon("download.png"));
        download_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String urlsText = inputarea.getText();
                String[] urls = urlsText.split("[,\\s]+");
                for (String url : urls) {
                    if (!url.isEmpty()) {
                        downloadImage(url);
                    }
                }
            }
        });
        panel.add(download_btn);

        pause_btn = new JButton("Pause", new ImageIcon("pause.png"));
        pause_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseDownloads();
            }
        });
        panel.add(pause_btn);

        resume_btn = new JButton("Resume", new ImageIcon("resume.png"));
        resume_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeDownloads();
            }
        });
        panel.add(resume_btn);

        cancel_btn = new JButton("Cancel", new ImageIcon("cancel.png"));
        cancel_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelDownloads();
            }
        });
        panel.add(cancel_btn);

        progressindicator = new JProgressBar();
        progressindicator.setStringPainted(true);
        panel.add(progressindicator);

        statusLabel = new JLabel("Download status will be displayed here", JLabel.CENTER);
        panel.add(statusLabel);

        add(panel, BorderLayout.CENTER);

        pack();
    }

    private void downloadImage(String urlString) {
        Runnable downloadTask = new Runnable() {
            @Override
            public void run() {
                DownloadInfo downloadInfo = downloadInfoMap.get(Thread.currentThread());
                int progress = downloadInfo != null ? downloadInfo.getProgress() : 0;
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                    if (progress > 0) {
                        connection.setRequestProperty("Range", "bytes=" + progress + "-");
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
                        int contentLength = connection.getContentLength();
                        InputStream inputStream = connection.getInputStream();
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            progress += bytesRead;
                            int currentProgress = (int) ((progress / (double) contentLength) * 100);
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    progressindicator.setValue(currentProgress);
                                }
                            });

                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException("Download interrupted");
                            }

                            Thread.sleep(50);
                        }

                        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
                        saveImage(outputStream.toByteArray(), fileName);

                        inputStream.close();
                        outputStream.close();

                        updateStatusLabel("Download completed: " + fileName);
                    } else {
                        throw new IOException("Failed to download image. Response code: " + responseCode);
                    }
                } catch (IOException | InterruptedException e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    if (!(e instanceof InterruptedException)) {
                        e.printStackTrace();
                    }

                    updateStatusLabel("Error downloading image: " + e.getMessage());
                }
            }
        };

        Future<?> task = executorService.submit(downloadTask);
        downloadTasks.add(task);
        downloadInfoMap.put(task, new DownloadInfo(urlString, 0));
    }

    private void saveImage(byte[] imageData, String fileName) {
        File directory = new File(DOWNLOAD_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fullPath = DOWNLOAD_DIRECTORY + fileName;

        try {
            FileOutputStream outputStream = new FileOutputStream(fullPath);
            outputStream.write(imageData);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resumeDownloads() {
        for (Future<?> task : downloadTasks) {
            if (task.isCancelled()) {
                DownloadInfo downloadInfo = downloadInfoMap.get(task);
                if (downloadInfo != null) {
                    downloadImage(downloadInfo.getUrl());
                }
            }
        }
    }

    private void pauseDownloads() {
        for (Future<?> task : downloadTasks) {
            if (!task.isDone() && !task.isCancelled()) {
                task.cancel(true);
            }
        }
    }

    private void cancelDownloads() {
        for (Future<?> task : downloadTasks) {
            task.cancel(true);
        }
        progressindicator.setValue(0);
    }

    private class DownloadInfo {
        private String url;
        private int progress;

        public DownloadInfo(String url, int progress) {
            this.url = url;
            this.progress = progress;
        }

        public String getUrl() {
            return url;
        }

        public int getProgress() {
            return progress;
        }
    }

    private void updateStatusLabel(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText(message);
            }
        });
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ImageDownloaderApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ImageDownloaderApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ImageDownloaderApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ImageDownloaderApplication.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ImageDownloaderApplication().setVisible(true);
            }
        });
    }
}
