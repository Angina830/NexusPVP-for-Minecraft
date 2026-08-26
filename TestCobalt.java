import java.io.*;
import java.net.*;

public class TestCobalt {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://api.cobalt.tools/api/json");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        String postData = "{\"url\":\"https://www.youtube.com/watch?v=dQw4w9WgXcQ\",\"isAudioOnly\":true}";
        try (OutputStream os = con.getOutputStream()) {
            os.write(postData.getBytes());
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        System.out.println(content.toString());
    }
}