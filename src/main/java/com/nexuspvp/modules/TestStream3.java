package com.nexuspvp.modules;
import java.io.InputStream;
public class TestStream3 {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--ffmpeg-location", "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\ffmpeg.exe",
            "-x", "--audio-format", "mp3",
            "--no-playlist",
            "--no-warnings",
            "-o", "-",
            "ytsearch1:Bankodyu Белоснежка"
        );
        Process process = pb.start();
        new Thread(() -> {
            try {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
                while (br.readLine() != null) {}
            } catch (Exception e) {}
        }).start();
        
        InputStream is = process.getInputStream();
        int b1 = is.read();
        int b2 = is.read();
        System.out.println("First two bytes: " + Integer.toHexString(b1) + " " + Integer.toHexString(b2));
        process.destroyForcibly();
    }
}