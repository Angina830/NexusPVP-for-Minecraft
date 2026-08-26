import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestScrape {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://soundcloud.com/discover");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) response.append(line);
            in.close();
            
            String html = response.toString();
            Pattern p = Pattern.compile("<script crossorigin src=\"(https://a-v2\\.sndcdn\\.com/assets/[^\"]+\\.js)\"></script>");
            Matcher m = p.matcher(html);
            while (m.find()) {
                String jsUrl = m.group(1);
                URL urlJs = new URL(jsUrl);
                HttpURLConnection connJs = (HttpURLConnection) urlJs.openConnection();
                connJs.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                BufferedReader inJs = new BufferedReader(new InputStreamReader(connJs.getInputStream()));
                StringBuilder resJs = new StringBuilder();
                String lineJs;
                while ((lineJs = inJs.readLine()) != null) resJs.append(lineJs);
                inJs.close();
                
                String js = resJs.toString();
                Pattern p2 = Pattern.compile("client_id:\"([a-zA-Z0-9]{32})\"");
                Matcher m2 = p2.matcher(js);
                if (m2.find()) {
                    System.out.println("FOUND CLIENT ID: " + m2.group(1));
                    return;
                }
            }
            System.out.println("Not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
