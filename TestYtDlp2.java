import java.io.*;
public class TestYtDlp2 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--ffmpeg-location", "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\ffmpeg.exe",
            "-x", "--audio-format", "mp3",
            "--newline",
            "--no-playlist",
            "-o", "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\temp_track.mp3",
            "--force-overwrites",
            "ytsearch1:Bankodyu Белоснежка"
        );
        pb.redirectErrorStream(true);
        Process activeProcess = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(activeProcess.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("RAW: " + line);
            if (line.contains("[download]") && line.contains("%")) {
                String progress = line.substring(line.indexOf("[download]") + 10).trim();
                System.out.println("PARSED: Downloading track: " + progress);
            }
        }
        activeProcess.waitFor();
        System.out.println("EXIT CODE: " + activeProcess.exitValue());
    }
}