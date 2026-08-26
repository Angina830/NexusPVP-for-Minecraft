import java.io.*;
import java.net.*;

public class TestInstances2 {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://api.invidious.io/instances.json?sort_by=health");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        System.out.println(content.toString().substring(0, 1000));
    }
}