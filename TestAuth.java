import java.io.*;
import java.net.*;

public class TestAuth {
    public static void main(String[] args) throws Exception {
        URL url = new URL("https://oauth2.googleapis.com/device/code");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String postData = "client_id=861556708454-d6dlm3lh05idd8npek18k6be8ba3oc68.apps.googleusercontent.com&scope=https://www.googleapis.com/auth/youtube.readonly";
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