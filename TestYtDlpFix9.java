import java.io.*;

public class TestYtDlpFix9 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--extractor-args", "youtube:player_client=android",
            "-f", "bestaudio",
            "--print", "title",
            "--no-download",
            "https://www.youtube.com/watch?v=zAxu3JXIHQs"
        );
        Process p = pb.start();
        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = er.readLine()) != null) {
            System.out.println("ERR: " + line);
        }
    }
}