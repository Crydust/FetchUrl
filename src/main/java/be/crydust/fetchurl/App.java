package be.crydust.fetchurl;

public class App {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java -jar FetchUrl.jar https://keybase.io");
            System.exit(1);
        }
        String urlToRead = args[0];

        CryptoHack.removeCryptographyRestrictions();
        System.out.printf("response = %s%n", FetchUtil.request(urlToRead));
    }

}
