package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;

import java.io.InputStream;
public class TestStream2 {
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
                String line;
                while ((line = br.readLine()) != null) { System.out.println("STDERR: " + line); }
            } catch (Exception e) {}
        }).start();
        
        InputStream is = process.getInputStream();
        int bytesRead = 0;
        byte[] buf = new byte[1024];
        int read;
        while ((read = is.read(buf)) != -1) {
            bytesRead += read;
        }
        process.waitFor();
        System.out.println("Bytes read from stdout: " + bytesRead);
    }
}