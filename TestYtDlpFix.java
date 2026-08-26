import java.io.*;

public class TestYtDlpFix {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--extractor-args", "youtube:player_client=android",
            "-f", "bestaudio",
            "--fragment-retries", "10",
            "--print", "title",
            "--no-download",
            "ytsearch1:morgenshtern lady gaga"
        );
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println("OUT: " + line);
        }
        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = er.readLine()) != null) {
            System.out.println("ERR: " + line);
        }
    }
}