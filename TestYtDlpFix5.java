import java.io.*;

public class TestYtDlpFix5 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--cookies-from-browser", "edge",
            "-f", "bestaudio",
            "--print", "title",
            "--no-download",
            "ytsearch1:morgenshtern lady gaga"
        );
        Process p = pb.start();
        BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = er.readLine()) != null) {
            System.out.println("ERR: " + line);
        }
    }
}