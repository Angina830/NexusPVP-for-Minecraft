package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;

import java.io.InputStream;
public class TestStream4 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder ytdlpPb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "-f", "bestaudio",
            "--no-playlist",
            "--no-warnings",
            "-o", "-",
            "ytsearch1:Bankodyu Белоснежка"
        );
        ProcessBuilder ffmpegPb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\ffmpeg.exe",
            "-i", "pipe:0",
            "-f", "mp3",
            "-acodec", "libmp3lame",
            "pipe:1"
        );
        
        Process ytdlp = ytdlpPb.start();
        Process ffmpeg = ffmpegPb.start();
        
        // Pipe ytdlp stdout to ffmpeg stdin
        new Thread(() -> {
            try {
                InputStream is = ytdlp.getInputStream();
                java.io.OutputStream os = ffmpeg.getOutputStream();
                byte[] buf = new byte[4096];
                int read;
                while ((read = is.read(buf)) != -1) {
                    os.write(buf, 0, read);
                }
                os.close();
            } catch (Exception e) {}
        }).start();
        
        // Discard ffmpeg stderr
        new Thread(() -> {
            try {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(ffmpeg.getErrorStream()));
                while (br.readLine() != null) {}
            } catch (Exception e) {}
        }).start();
        
        long start = System.currentTimeMillis();
        InputStream is = ffmpeg.getInputStream();
        int b1 = is.read();
        System.out.println("First byte received after " + (System.currentTimeMillis() - start) + "ms: " + Integer.toHexString(b1));
        
        ytdlp.destroyForcibly();
        ffmpeg.destroyForcibly();
    }
}