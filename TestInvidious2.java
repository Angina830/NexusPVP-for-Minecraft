import java.io.*;
import java.net.*;

public class TestInvidious2 {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://sh.itjust.works/api/v1/search?q=morgenshtern");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine.substring(0, Math.min(100, inputLine.length())));
            break;
        }
        in.close();
    }
}