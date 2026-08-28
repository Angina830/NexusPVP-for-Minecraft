package com.nexuspvp.modules;

import net.minecraft.client.MinecraftClient;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class YtDlpResolver {

    public static volatile Process activeProcess = null;

    public static void cancelCurrentDownload() {
        if (activeProcess != null && activeProcess.isAlive()) {
            activeProcess.destroyForcibly();
            activeProcess = null;
        }
    }

    private static void downloadFileWithProgress(String urlStr, File dest, String title) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        int fileSize = conn.getContentLength();
        
        try (InputStream in = conn.getInputStream(); FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            int total = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                total += read;
                if (fileSize > 0) {
                    int percent = (int)((total * 100L) / fileSize);
                    Radio.downloadProgress = title + ": " + percent + "% (" + (total / 1024 / 1024) + "MB)";
                } else {
                    Radio.downloadProgress = title + ": " + (total / 1024 / 1024) + "MB";
                }
            }
        }
        Radio.downloadProgress = "";
    }

    public static File getCacheFileForQuery(String query) {
        File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp/cache");
        if (!dir.exists()) dir.mkdirs();
        
        String safeName = query.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (safeName.length() > 30) safeName = safeName.substring(0, 30);
        String hash = Integer.toHexString(query.hashCode());
        return new File(dir, "track_" + safeName + "_" + hash + ".mp3");
    }

    public static void preloadTrack(String query) {
        File cached = getCacheFileForQuery(query);
        if (cached.exists() && cached.length() > 10240) {
            return; // Already cached!
        }
        CompletableFuture.runAsync(() -> {
            try {
                downloadAndConvertToMp3(query).join();
            } catch (Exception ignored) {}
        });
    }

    public static CompletableFuture<File> downloadAndConvertToMp3(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
                if (!dir.exists()) dir.mkdirs();

                File cacheDir = new File(dir, "cache");
                if (!cacheDir.exists()) cacheDir.mkdirs();

                File cachedFile = getCacheFileForQuery(query);

                // 1. If file already exists in persistent cache, return immediately!
                if (cachedFile.exists() && cachedFile.length() > 10240) {
                    Radio.downloadProgress = "";
                    return cachedFile;
                }
                
                File ytdlp = new File(dir, "yt-dlp.exe");
                if (!ytdlp.exists()) {
                    downloadFileWithProgress("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe", ytdlp, "Downloading yt-dlp");
                }
                
                File ffmpeg = new File(dir, "ffmpeg.exe");
                if (!ffmpeg.exists()) {
                    File zipFile = new File(dir, "ffmpeg.zip");
                    downloadFileWithProgress("https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip", zipFile, "Downloading FFmpeg");
                    
                    Radio.downloadProgress = "Extracting FFmpeg...";
                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                        ZipEntry zipEntry = zis.getNextEntry();
                        while (zipEntry != null) {
                            if (zipEntry.getName().endsWith("ffmpeg.exe")) {
                                Files.copy(zis, ffmpeg.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                break;
                            }
                            zipEntry = zis.getNextEntry();
                        }
                    }
                    zipFile.delete();
                    Radio.downloadProgress = "";
                }
                
                Radio.downloadProgress = "Downloading: " + query.replace("ytsearch1:", "");
                
                // Base path without extension for yt-dlp -o template
                String basePath = cachedFile.getAbsolutePath();
                if (basePath.toLowerCase().endsWith(".mp3")) {
                    basePath = basePath.substring(0, basePath.length() - 4);
                }
                String outTemplate = basePath + ".%(ext)s";
                
                java.util.List<String> ytdlpCmd = new java.util.ArrayList<>();
                ytdlpCmd.add(ytdlp.getAbsolutePath());
                ytdlpCmd.add("--ffmpeg-location");
                ytdlpCmd.add(ffmpeg.getAbsolutePath());
                ytdlpCmd.add("--extractor-args");
                ytdlpCmd.add("youtube:player_client=android");
                ytdlpCmd.add("-f");
                ytdlpCmd.add("bestaudio/best");
                
                File cookies = new File(dir, "cookies.txt");
                if (cookies.exists() && cookies.length() > 0) {
                    ytdlpCmd.add("--cookies");
                    ytdlpCmd.add(cookies.getAbsolutePath());
                }
                
                ytdlpCmd.add("-x");
                ytdlpCmd.add("--audio-format");
                ytdlpCmd.add("mp3");
                ytdlpCmd.add("--newline");
                ytdlpCmd.add("--no-playlist");
                ytdlpCmd.add("--retries");
                ytdlpCmd.add("10");
                ytdlpCmd.add("--fragment-retries");
                ytdlpCmd.add("10");
                ytdlpCmd.add("-o");
                ytdlpCmd.add(outTemplate);
                ytdlpCmd.add("--force-overwrites");
                ytdlpCmd.add(query);
                
                ProcessBuilder pb = new ProcessBuilder(ytdlpCmd);
                pb.redirectErrorStream(true);
                activeProcess = pb.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(activeProcess.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("[download]") && line.contains("%")) {
                        String progress = line.substring(line.indexOf("[download]") + 10).trim();
                        Radio.downloadProgress = "Downloading: " + progress;
                    }
                }
                activeProcess.waitFor();
                activeProcess = null;
                Radio.downloadProgress = "";
                
                // Verify output
                if (cachedFile.exists() && cachedFile.length() > 10240) {
                    return cachedFile;
                }
                
                File doubleExt = new File(cachedFile.getAbsolutePath() + ".mp3");
                if (doubleExt.exists() && doubleExt.length() > 10240) {
                    doubleExt.renameTo(cachedFile);
                    return cachedFile;
                }
                
                throw new Exception("File was not created properly by yt-dlp");
                
            } catch (Exception e) {
                Radio.downloadProgress = "";
                return null;
            }
        });
    }
}