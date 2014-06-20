package be.crydust.deleteme.download.https;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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

public class FetchUrl {

    public static void main(String[] args) throws Exception {
        System.setProperty("https.protocols", "SSLv3");
        if (args.length != 2) {
            System.err.println("Usage: java FetchUrl.jar https://www.google.com ./tmp");
            System.exit(1);
        }
        String urlToRead = args[0];
        String folderToWrite = args[1];
        writeUrlToFile(urlToRead, folderToWrite, "io.bin");
        writeUrlToFileNIO(urlToRead, folderToWrite, "nio.bin");
        System.out.println(fetchUrl(urlToRead));
    }

    public static StatusAndContent fetchUrl(String urlToRead) throws MalformedURLException, IOException {
        return null;
    }

    public static void writeUrlToFile(String urlToRead, String folderToWrite, String fileName) throws MalformedURLException, IOException {
        URL urlIn = new URL(urlToRead);
        File folderOut = new File(folderToWrite);
        if (!(folderOut.exists() || folderOut.mkdirs())) {
            throw new IOException("could not create folder " + folderToWrite);
        }
        File fileOut = new File(folderOut, fileName);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(urlIn.openStream());
            try {
                out = new BufferedOutputStream(new FileOutputStream(fileOut));
                transfer(in, out);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static void writeUrlToFileNIO(String urlToRead, String folderToWrite, String fileName) throws MalformedURLException, IOException {
        URL urlIn = new URL(urlToRead);
        Path pathOut = Paths.get(folderToWrite, fileName);
        try (
                ReadableByteChannel in = Channels.newChannel(new BufferedInputStream(urlIn.openStream()));
                WritableByteChannel out = Files.newByteChannel(pathOut, CREATE, WRITE);) {
            transfer(in, out);
        }
    }

    private static void transfer(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[1024];
        while (true) {
            int bytesRead = in.read(bytes);
            if (bytesRead == -1) {
                break;
            }
            out.write(bytes, 0, bytesRead);
        }
    }

    private static void transfer(ReadableByteChannel in, WritableByteChannel out) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = 0;
        while (bytesRead != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
            buffer.clear();
            bytesRead = in.read(buffer);
        }
    }

    public static class StatusAndContent {

        private final int status;
        private final byte[] content;

        public StatusAndContent(int status, byte[] content) {
            this.status = status;
            this.content = content;
        }

        public int getStatus() {
            return status;
        }

        public byte[] getContent() {
            return content;
        }

        @Override
        public String toString() {
            try {
                return String.format("%d %s", status, new String(content, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
                return "error";
            }
        }

    }
}
