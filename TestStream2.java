import javazoom.jl.player.Player;
import javazoom.jl.player.FactoryRegistry;
public class TestStream2 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--ffmpeg-location", "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\ffmpeg.exe",
            "-x", "--audio-format", "mp3",
            "--no-playlist",
            "-o", "-",
            "ytsearch1:Bankodyu Белоснежка"
        );
        pb.redirectError(ProcessBuilder.Redirect.INHERIT); // print stderr to console
        Process process = pb.start();
        
        try {
            Player player = new Player(process.getInputStream(), FactoryRegistry.systemRegistry().createAudioDevice());
            System.out.println("Player created successfully! Playing...");
            player.play(50); // Play 50 frames
            System.out.println("Played 50 frames!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.destroyForcibly();
    }
}