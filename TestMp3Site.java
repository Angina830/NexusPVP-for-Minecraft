import java.io.*;
import java.net.*;

public class TestMp3Site {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://mp3party.net/search?q=morgenshtern");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains(".mp3")) {
                System.out.println(inputLine);
                break;
            }
        }
        in.close();
    }
}