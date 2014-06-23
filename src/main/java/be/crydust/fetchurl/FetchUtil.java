package be.crydust.fetchurl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

public final class FetchUtil {

    private FetchUtil() {
    }

    public static void writeUrlToFile(String urlToRead, String folderToWrite, String fileName) throws MalformedURLException, IOException {
        URL urlIn = new URL(urlToRead);
        File folderOut = new File(folderToWrite);
        if (!(folderOut.exists() || folderOut.mkdirs())) {
            throw new RuntimeException("could not create folder " + folderToWrite);
        }
        File fileOut = new File(folderOut, fileName);
        try (
                InputStream in = new BufferedInputStream(urlIn.openStream());
                OutputStream out = new BufferedOutputStream(new FileOutputStream(fileOut));) {
            transfer(in, out);
        }
    }

    public static void writeUrlToFileNIO(String urlToRead, String folderToWrite, String fileName) throws MalformedURLException, IOException {
        URL urlIn = new URL(urlToRead);
        File folderOut = Paths.get(folderToWrite).toFile();
        if (!(folderOut.exists() || folderOut.mkdirs())) {
            throw new RuntimeException("could not create folder " + folderToWrite);
        }
        Path pathOut = Paths.get(folderToWrite, fileName);
        try (
                ReadableByteChannel in = Channels.newChannel(new BufferedInputStream(urlIn.openStream()));
                WritableByteChannel out = Files.newByteChannel(pathOut, CREATE, WRITE);) {
            transfer(in, out);
        }
    }

    public static Response request(String urlToRead) throws MalformedURLException, IOException {
        HttpURLConnection con = null;
        try {
            URL urlIn = new URL(urlToRead);
            con = (HttpURLConnection) urlIn.openConnection();
            String requestedEncoding = "UTF-8";
            con.setRequestProperty("Accept-Charset", requestedEncoding);
            String responseEncoding = readEncoding(con.getContentEncoding(), con.getContentType(), requestedEncoding);
            try (Reader in = new InputStreamReader(new BufferedInputStream(con.getInputStream()), responseEncoding)) {
                StringBuilder sb = new StringBuilder();
                transfer(in, sb);
                return new Response(con.getResponseCode(), sb.toString());
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static String readEncoding(String contentEncoding, String contentType, String fallback) {
        String encoding = fallback;
        String[] values = contentType.split(";");
        if (contentEncoding != null) {
            encoding = contentEncoding;
        } else {
            for (String value : values) {
                value = value.trim();
                if (value.toLowerCase().startsWith("charset=")) {
                    encoding = value.substring("charset=".length());
                }
            }
        }
        return encoding;
    }

    public static void transfer(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    public static void transfer(ReadableByteChannel in, WritableByteChannel out) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        while (in.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
            buffer.clear();
        }
    }

    public static void transfer(Reader in, StringBuilder out) throws IOException {
        char[] buffer = new char[4096];
        int charsRead;
        while ((charsRead = in.read(buffer)) != -1) {
            out.append(buffer, 0, charsRead);
        }
    }

}
