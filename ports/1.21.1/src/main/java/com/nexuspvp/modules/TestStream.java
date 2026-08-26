package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import java.io.InputStream;
import java.io.File;

public class TestStream {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\yt-dlp.exe",
            "--ffmpeg-location", "C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\ffmpeg.exe",
            "-x", "--audio-format", "mp3",
            "--download-sections", "*00:00-00:05",
            "--no-playlist",
            "--quiet", "--no-warnings",
            "-o", "-",
            "ytsearch1:Bankodyu Белоснежка"
        );
        Process process = pb.start();
        
        // Read stderr to discard
        new Thread(() -> {
            try {
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
                while (br.readLine() != null) {}
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