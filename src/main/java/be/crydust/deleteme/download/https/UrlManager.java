package be.crydust.deleteme.download.https;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class UrlManager {

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("UrlManager <url> [output folder]");
                System.err.println("Please provide a URL to fetch.");
                System.exit(-1);
            }
            String url = args[0];
            if (url.trim().isEmpty()) {
                System.err.println("Please provide a URL to fetch.");
                System.exit(-1);
            }
            if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                System.err.println("URL must start with http:// or https://");
                System.exit(-1);
            }
            File cacheFolder = null;
            if (args.length >= 2) {
                String outputFolder = args[1];
                if (outputFolder.trim().isEmpty()) {
                    cacheFolder = new File(".");
                    System.out
                            .println("Output folder (argument 2) not specified. File will be cached to the current folder: "
                                    + cacheFolder.getAbsolutePath());
                } else {
                    cacheFolder = new File(outputFolder);
                    if (!cacheFolder.mkdirs()) {
                        System.err.println("Failed to create output folder " + outputFolder);
                        System.exit(-1);
                    }
                    System.out.println("Files will be cached to folder: " + cacheFolder.getAbsolutePath());
                }
            }
            fetchUrl(url, cacheFolder);
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void fetchUrl(String url, File cacheFolder) throws IOException {
        System.setProperty("https.protocols", "SSLv3");
        URL u = new URL(url);
        HttpURLConnection con = null;
        try {
            System.out.println("Connecting to URL " + u.toString() + "\n");
            if (url.startsWith("https")) {
                con = (HttpsURLConnection) u.openConnection();
            } else {
                con = (HttpURLConnection) u.openConnection();
            }
            int responseCode = con.getResponseCode();
            System.out.println("Response code: " + responseCode + "\n");
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = con.getHeaderField("Content-Disposition");
                String contentType = con.getContentType();
                int contentLength = con.getContentLength();
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    if (url.contains("/")) {
                        fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
                    } else {
                        fileName = "index.html";
                    }
                }
                StringBuilder b = new StringBuilder("HTTP response information:\n");
                b.append("\tContent-Type: ").append(contentType).append("\n");
                b.append("\tContent-Disposition: ").append(disposition).append("\n");
                b.append("\tContent-Length: ").append(contentLength).append("\n");
                b.append("\tFile Name: ").append(fileName).append("\n");
                System.out.println(b + "\n");
                // opens input stream from the HTTP connection
                InputStream inputStream = con.getInputStream();
                // opens an output stream to save into memory (for writing string)
                // ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                // opens an output stream to save into file
                File cacheFile = new File(cacheFolder, fileName);
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                System.out.print("Reading from connection");
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    System.out.print(".");
                }
                System.out.println("done\n");
                outputStream.close();
                inputStream.close();
                // print URL data as a string to the console
                // System.out.println("Downloaded " + outputStream.size() + " bytes of data.\n");
                // System.out.println("Downloaded data:\n" + new String(outputStream.toByteArray(), "UTF-8"));
                System.out.println("Downloaded " + cacheFile.length() + " bytes of data.\n");
                System.out.println("Data cached to " + cacheFile.getAbsolutePath());
            } else {
                System.out.println("Server response not " + HttpURLConnection.HTTP_OK + ". HTTP code: " + responseCode);
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
