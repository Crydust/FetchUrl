package be.crydust.fetchurl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class App {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar FetchUrl.jar https://keybase.io");
            System.exit(1);
        }
        String urlToRead = args[0];

        CryptoHack.removeCryptographyRestrictions();

        System.out.printf("response = %s%n", request(urlToRead));
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

    private static void transfer(Reader in, StringBuilder out) throws IOException {
        char[] buffer = new char[4096];
        int charsRead;
        while ((charsRead = in.read(buffer)) != -1) {
            out.append(buffer, 0, charsRead);
        }
    }

}
