import java.io.*;

public class TestYtDlpSc {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "-f", "bestaudio",
            "--fragment-retries", "10",
            "-o", "-",
            "scsearch1:morgenshtern lady gaga"
        );
        Process p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }
}