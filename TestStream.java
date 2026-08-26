import javazoom.jl.player.Player;
import javazoom.jl.player.FactoryRegistry;
public class TestStream {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--ffmpeg-location", "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\ffmpeg.exe",
            "-x", "--audio-format", "mp3",
            "--no-playlist",
            "-o", "-",
            "ytsearch1:Bankodyu Белоснежка"
        );
        pb.redirectErrorStream(true); // THIS CORRUPTS THE STREAM!
        Process process = pb.start();
        
        try {
            Player player = new Player(process.getInputStream(), FactoryRegistry.systemRegistry().createAudioDevice());
            System.out.println("Player created successfully!");
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.destroyForcibly();
    }
}