package com.nexuspvp.modules;

import com.nexuspvp.module.Category;
import com.nexuspvp.module.Module;
import com.nexuspvp.setting.ModeSetting;
import com.nexuspvp.setting.NumberSetting;
import com.nexuspvp.setting.BooleanSetting;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.Player;
import net.minecraft.client.MinecraftClient;

import javax.sound.sampled.FloatControl;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Radio extends Module {
    private static Radio instance;
    private ModeSetting station;
    public NumberSetting localVolume;
    public NumberSetting worldVolume;
    public BooleanSetting paused;
    public BooleanSetting broadcast;
    
    private Thread radioThread;
    private Player player;
    private Object audioDevice;
    private boolean playing = false;
    private String lastStation = "MyPlaylist";
    
    private List<File> localFiles = new ArrayList<>();
    private List<String> myPlaylistQueries = new ArrayList<>();
    private int currentTrackIndex = 0;
    
    public static volatile String currentTrackName = "";
    public static volatile String downloadProgress = "";
    
    private long currentTrackStartTime = 0;
    private long lastBroadcastTime = 0;
    
    private ConcurrentHashMap<String, RemotePlayback> remotePlaybacks = new ConcurrentHashMap<>();
    
    public static class RemotePlayback {
        public Thread thread;
        public Process process;
        public Player player;
        public Object device;
        public boolean active = false;
    }

    public Radio() {
        super("Radio", "Plays music using MQTT", Category.VISUAL);
        instance = this;
        
        station = new ModeSetting("Station", "MyPlaylist", "MyPlaylist", "LocalFolder", "ListenOnly");
        localVolume = new NumberSetting("LocalVolume", 50, 0, 100, 1);
        worldVolume = new NumberSetting("WorldVolume", 100, 0, 100, 1);
        paused = new BooleanSetting("Paused", false);
        broadcast = new BooleanSetting("Broadcast", true);
        
        addSetting(station);
        addSetting(localVolume);
        addSetting(worldVolume);
        addSetting(paused);
        addSetting(broadcast);
        
        loadPlaylist();
    }
    
    public static Radio getInstance() {
        return instance;
    }

    public List<String> getPlaylist() {
        if (station.getValue().equals("MyPlaylist")) {
            return myPlaylistQueries;
        } else if (station.getValue().equals("LocalFolder")) {
            List<String> names = new ArrayList<>();
            for (File f : localFiles) names.add(f.getName());
            return names;
        }
        return new ArrayList<>();
    }
    
    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public boolean isActuallyPlaying() {
        return isEnabled() && playing && !paused.getValue();
    }
    
    public void togglePlayPause() {
        if (!this.isEnabled()) {
            this.setEnabled(true);
            this.paused.setValue(false);
        } else {
            paused.setValue(!paused.getValue());
        }
    }
    
    public void playTrack(int index) {
        if (!this.isEnabled()) {
            this.setEnabled(true);
        }
        paused.setValue(false);
        List<String> list = getPlaylist();
        if (index >= 0 && index < list.size()) {
            currentTrackIndex = index;
            startRadioThread();
        }
    }

    public String getCurrentTrackTitle() {
        if (!currentTrackName.isEmpty()) return currentTrackName;
        List<String> list = getPlaylist();
        if (currentTrackIndex >= 0 && currentTrackIndex < list.size()) {
            return list.get(currentTrackIndex);
        }
        return "No Track Loaded";
    }

    public String getStatusText() {
        if (!downloadProgress.isEmpty()) return downloadProgress;
        if (isActuallyPlaying()) return "Playing...";
        if (paused.getValue()) return "Paused";
        return "Idle";
    }

    public boolean isPlaying() {
        return isActuallyPlaying();
    }

    public float getVolume() {
        return localVolume.getFloatValue() / 100.0f;
    }

    public void setVolume(float val) {
        localVolume.setValue((double) (int) (Math.max(0.0f, Math.min(1.0f, val)) * 100));
    }

    public void nextTrack() {
        List<String> list = getPlaylist();
        if (!list.isEmpty()) {
            currentTrackIndex = (currentTrackIndex + 1) % list.size();
            playTrack(currentTrackIndex);
        }
    }

    public boolean isBroadcastEnabled() {
        return broadcast.getValue();
    }

    public void toggleBroadcast() {
        broadcast.setValue(!broadcast.getValue());
    }

    public String getStation() {
        return station.getValue();
    }

    public void cycleStation() {
        station.cycle();
    }

    public float getWorldVolume() {
        return worldVolume.getFloatValue() / 100.0f;
    }

    public void setWorldVolume(float val) {
        worldVolume.setValue((double) (int) (Math.max(0.0f, Math.min(1.0f, val)) * 100));
    }
    
    private void loadPlaylist() {
        File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "playlist.txt");
        
        if (!file.exists()) {
            myPlaylistQueries.add("Bankodyu Белоснежка");
            myPlaylistQueries.add("morgenshtern lady gaga");
            myPlaylistQueries.add("Пошлая Молли Буду твоим пёсиком");
            savePlaylist();
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        myPlaylistQueries.add(line.trim());
                    }
                }
            } catch (Exception e) {}
        }
        
        File musicDir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp/music");
        if (!musicDir.exists()) musicDir.mkdirs();
        File[] files = musicDir.listFiles();
        if (files != null) {
            for (File f : files) if (f.getName().toLowerCase().endsWith(".mp3")) localFiles.add(f);
        }
        Collections.shuffle(localFiles);
    }
    
    private void savePlaylist() {
        File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
        File file = new File(dir, "playlist.txt");
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String q : myPlaylistQueries) {
                pw.println(q);
            }
        } catch (Exception e) {}
    }
    
    public static void addTrack(String track) {
        if (instance != null) {
            instance.myPlaylistQueries.add(track);
            instance.savePlaylist();
        }
    }

    @Override
    public void onEnable() {
        paused.setValue(false);
        lastStation = station.getValue();
        startRadioThread();
    }

    @Override
    public void onDisable() {
        paused.setValue(false);
        stopRadio();
        stopAllRemotePlaybacks();
    }
    
    @Override
    public void onTick() {
        if (!station.getValue().equals(lastStation)) {
            lastStation = station.getValue();
            if (this.isEnabled()) {
                currentTrackIndex = 0;
                startRadioThread();
            }
        }
        
        if (playing && audioDevice != null) {
            updateVolume(audioDevice, localVolume.getFloatValue() / 100.0f);
        }
        
        // Update MQTT topic based on current server
        RadioSyncManager.getInstance().updateTopic();
        
        // Continuous broadcasting of current track and position
        if (playing && !paused.getValue() && currentTrackStartTime > 0 && station.getValue().equals("MyPlaylist") && broadcast.getValue() && MinecraftClient.getInstance().player != null) {
            long now = System.currentTimeMillis();
            if (now - lastBroadcastTime > 3000) {
                lastBroadcastTime = now;
                String trackName = getPlaylist().get(currentTrackIndex);
                String query = "ytsearch1:" + trackName;
                RadioSyncManager.getInstance().broadcastMyTrack(query, currentTrackStartTime, 
                    MinecraftClient.getInstance().player.getX(),
                    MinecraftClient.getInstance().player.getY(),
                    MinecraftClient.getInstance().player.getZ());
            }
        }
        
        // Update 3D Volume for remote tracks
        if (MinecraftClient.getInstance().player != null) {
            double px = MinecraftClient.getInstance().player.getX();
            double py = MinecraftClient.getInstance().player.getY();
            double pz = MinecraftClient.getInstance().player.getZ();
            
            for (String sender : remotePlaybacks.keySet()) {
                RemotePlayback rp = remotePlaybacks.get(sender);
                RadioSyncManager.RemoteTrack track = RadioSyncManager.getInstance().remoteTracks.get(sender);
                
                if (rp != null && rp.active && rp.device != null && track != null) {
                    double dist = Math.sqrt(Math.pow(px - track.x, 2) + Math.pow(py - track.y, 2) + Math.pow(pz - track.z, 2));
                    float maxDist = 50.0f;
                    float baseVol = worldVolume.getFloatValue() / 100.0f;
                    
                    float finalVol = 0.0f;
                    if (dist < maxDist) {
                        finalVol = baseVol * (float)(1.0 - (dist / maxDist));
                    }
                    
                    updateVolume(rp.device, finalVol);
                }
            }
            
            long now = System.currentTimeMillis();
            for (String sender : RadioSyncManager.getInstance().remoteTracks.keySet()) {
                RadioSyncManager.RemoteTrack t = RadioSyncManager.getInstance().remoteTracks.get(sender);
                if (now - t.lastUpdateTime > 15000) {
                    RemotePlayback existingRp = remotePlaybacks.get(sender);
                    if (existingRp == null || !existingRp.active) {
                        RadioSyncManager.getInstance().remoteTracks.remove(sender);
                        stopRemotePlayback(sender);
                    }
                }
            }
        }
    }

    public void skipTrack(int direction) {
        if (!this.isEnabled()) return;
        paused.setValue(false);
        currentTrackIndex += direction;
        startRadioThread();
    }

    private void startRadioThread() {
        stopRadio();
        playing = true;
        Radio.currentTrackName = "";
        
        radioThread = new Thread(() -> {
            try {
                if (station.getValue().equals("LocalFolder")) {
                    playLocalFolder();
                } else if (station.getValue().equals("MyPlaylist")) {
                    playYouTubePlaylist();
                } else {
                    playStream(getUrl());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        radioThread.setDaemon(true);
        radioThread.start();
    }
    
    private void playYouTubePlaylist() {
        if (myPlaylistQueries.isEmpty()) {
            playing = false;
            Radio.currentTrackName = "Playlist is empty!";
            return;
        }
        
        int consecutiveFails = 0;
        
        while (playing && Thread.currentThread() == radioThread) {
            if (myPlaylistQueries.isEmpty()) break;
            if (currentTrackIndex >= myPlaylistQueries.size()) currentTrackIndex = 0;
            if (currentTrackIndex < 0) currentTrackIndex = myPlaylistQueries.size() - 1;
            
            String trackName = myPlaylistQueries.get(currentTrackIndex);
            String query = "ytsearch1:" + trackName;
            Radio.currentTrackName = trackName;
            
            currentTrackStartTime = System.currentTimeMillis();
            lastBroadcastTime = currentTrackStartTime;
            
            // Broadcast start
            if (broadcast.getValue() && MinecraftClient.getInstance().player != null) {
                RadioSyncManager.getInstance().broadcastMyTrack(query, currentTrackStartTime, 
                    MinecraftClient.getInstance().player.getX(),
                    MinecraftClient.getInstance().player.getY(),
                    MinecraftClient.getInstance().player.getZ());
            }
            
            // Background pre-download the NEXT track in queue!
            int nextIdx = (currentTrackIndex + 1) % myPlaylistQueries.size();
            String nextQuery = "ytsearch1:" + myPlaylistQueries.get(nextIdx);
            YtDlpResolver.preloadTrack(nextQuery);
            
            try {
                File mp3File = YtDlpResolver.downloadAndConvertToMp3(query).join();
                
                if (Thread.currentThread() != radioThread) {
                    return;
                }
                
                if (mp3File != null && mp3File.exists()) {
                    consecutiveFails = 0;
                    Radio.downloadProgress = "";
                    playFile(mp3File);
                } else {
                    Radio.currentTrackName = "Failed to download";
                    consecutiveFails++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Radio.currentTrackName = "Error downloading";
                consecutiveFails++;
            }
            
            if (playing) {
                if (consecutiveFails >= 3) {
                    RadioLog.log("Too many download failures (" + consecutiveFails + "), stopping radio.");
                    playing = false;
                    Radio.currentTrackName = "YT Blocked/Error. Radio stopped.";
                    break;
                }
                
                if (consecutiveFails > 0) {
                    try { Thread.sleep(3000); } catch (Exception ignored) {}
                }
                
                if (playing && !paused.getValue()) {
                    currentTrackIndex++;
                }
            }
        }
    }
    
    private void playLocalFolder() {
        if (localFiles.isEmpty()) {
            playing = false;
            Radio.currentTrackName = "No local MP3 files!";
            return;
        }
        
        while (playing && Thread.currentThread() == radioThread) {
            if (currentTrackIndex >= localFiles.size()) currentTrackIndex = 0;
            if (currentTrackIndex < 0) currentTrackIndex = localFiles.size() - 1;
            
            File f = localFiles.get(currentTrackIndex);
            Radio.currentTrackName = f.getName();
            playFile(f);
            
            if (playing && !paused.getValue()) {
                currentTrackIndex++;
            }
        }
    }

    private void playStream(String url) {
        try {
            InputStream is = new java.net.URL(url).openStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            player = new Player(bis, FactoryRegistry.systemRegistry().createAudioDevice());
            audioDevice = getAudioDevice(player);
            
            // Decode first frame and apply volume
            if (playing && player != null && player.play(1)) {
                updateVolume(audioDevice, localVolume.getFloatValue() / 100.0f);
            }
            
            while (playing && player != null && player.play(1)) {
                while (paused.getValue() && playing) {
                    try { Thread.sleep(50); } catch (Exception e) {}
                }
            }
            
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
            playing = false;
            Radio.currentTrackName = "Stream error";
        }
    }

    private void playFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            player = new Player(bis, FactoryRegistry.systemRegistry().createAudioDevice());
            audioDevice = getAudioDevice(player);
            
            Radio.downloadProgress = "";
            
            // Decode first frame to open the line with exact format, then immediately set volume!
            if (playing && player != null && player.play(1)) {
                updateVolume(audioDevice, localVolume.getFloatValue() / 100.0f);
            }
            
            while (playing && player != null && player.play(1)) {
                while (paused.getValue() && playing) {
                    try { Thread.sleep(50); } catch (Exception e) {}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Radio.currentTrackName = "Error playing file";
        }
    }

    private String getUrl() {
        return "";
    }

    private void stopRadio() {
        playing = false;
        currentTrackStartTime = 0;
        if (player != null) {
            try {
                player.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            player = null;
        }
        YtDlpResolver.cancelCurrentDownload();
        if (radioThread != null) {
            radioThread.interrupt();
            radioThread = null;
        }
    }

    private Object getAudioDevice(Player p) {
        try {
            java.lang.reflect.Field field = p.getClass().getDeclaredField("audio");
            field.setAccessible(true);
            return field.get(p);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateVolume(Object device, float volume) {
        if (device == null) return;
        try {
            if (device.getClass().getName().contains("JavaSoundAudioDevice")) {
                java.lang.reflect.Field sourceField = device.getClass().getDeclaredField("source");
                sourceField.setAccessible(true);
                javax.sound.sampled.SourceDataLine source = (javax.sound.sampled.SourceDataLine) sourceField.get(device);
                if (source != null && source.isOpen()) {
                    if (source.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gainControl = (FloatControl) source.getControl(FloatControl.Type.MASTER_GAIN);
                        float min = gainControl.getMinimum();
                        float max = gainControl.getMaximum();
                        
                        float db = (float) (Math.log10(volume > 0 ? volume : 0.0001) * 20.0);
                        if (db < min) db = min;
                        if (db > max) db = max;
                        
                        gainControl.setValue(db);
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public boolean hasActivePlayback(String sender) {
        RemotePlayback rp = remotePlaybacks.get(sender);
        return rp != null && rp.active;
    }

    public void onRemoteTrackStarted(RadioSyncManager.RemoteTrack rt) {
        RadioLog.log("onRemoteTrackStarted called for sender " + rt.sender + " with track: " + rt.trackQuery);
        stopRemotePlayback(rt.sender);
        
        RemotePlayback rp = new RemotePlayback();
        rp.active = true;
        remotePlaybacks.put(rt.sender, rp);
        
        rp.thread = new Thread(() -> {
            Process ytdlpProcess = null;
            Process ffmpegProcess = null;
            try {
                long elapsedMs = System.currentTimeMillis() - rt.startTime;
                if (elapsedMs < 0) elapsedMs = 0;
                
                int elapsedSecs = (int) (elapsedMs / 1000);
                String seekTime = String.format("%02d:%02d:%02d", elapsedSecs / 3600, (elapsedSecs % 3600) / 60, elapsedSecs % 60);
                
                File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
                File ytdlp = new File(dir, "yt-dlp.exe");
                File ffmpeg = new File(dir, "ffmpeg.exe");
                if (!ytdlp.exists() || !ffmpeg.exists()) return;
                
                java.util.List<String> ytdlpCmd = new java.util.ArrayList<>();
                ytdlpCmd.add(ytdlp.getAbsolutePath());
                ytdlpCmd.add("--extractor-args");
                ytdlpCmd.add("youtube:player_client=android");
                
                File cookies = new File(dir, "cookies.txt");
                if (cookies.exists() && cookies.length() > 0) {
                    ytdlpCmd.add("--cookies");
                    ytdlpCmd.add(cookies.getAbsolutePath());
                }
                
                ytdlpCmd.add("-f");
                ytdlpCmd.add("bestaudio/best");
                ytdlpCmd.add("--no-playlist");
                ytdlpCmd.add("--no-warnings");
                ytdlpCmd.add("--retries");
                ytdlpCmd.add("10");
                ytdlpCmd.add("--fragment-retries");
                ytdlpCmd.add("10");
                ytdlpCmd.add("-o");
                ytdlpCmd.add("-");
                ytdlpCmd.add(rt.trackQuery);
                
                ProcessBuilder ytdlpPb = new ProcessBuilder(ytdlpCmd);
                
                java.util.List<String> ffmpegCmd = new java.util.ArrayList<>();
                ffmpegCmd.add(ffmpeg.getAbsolutePath());
                if (elapsedSecs > 0) {
                    ffmpegCmd.add("-ss");
                    ffmpegCmd.add(seekTime);
                }
                ffmpegCmd.add("-i");
                ffmpegCmd.add("pipe:0");
                ffmpegCmd.add("-f");
                ffmpegCmd.add("mp3");
                ffmpegCmd.add("-acodec");
                ffmpegCmd.add("libmp3lame");
                ffmpegCmd.add("pipe:1");
                ProcessBuilder ffmpegPb = new ProcessBuilder(ffmpegCmd);
                
                RadioLog.log("Starting remote stream (seek=" + seekTime + ")...");
                ytdlpProcess = ytdlpPb.start();
                ffmpegProcess = ffmpegPb.start();
                
                rp.process = ffmpegProcess;
                final Process ytdlpProc = ytdlpProcess;
                final Process ffmpegProc = ffmpegProcess;
                
                new Thread(() -> {
                    try {
                        java.io.InputStream is = ytdlpProc.getInputStream();
                        java.io.OutputStream os = ffmpegProc.getOutputStream();
                        byte[] buf = new byte[8192];
                        int read;
                        while ((read = is.read(buf)) != -1) {
                            os.write(buf, 0, read);
                        }
                        os.close();
                    } catch (Exception e) {}
                }).start();
                
                new Thread(() -> {
                    try {
                        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(ffmpegProc.getErrorStream()));
                        while (br.readLine() != null) {}
                    } catch (Exception e) {}
                }).start();
                
                new Thread(() -> {
                    try {
                        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(ytdlpProc.getErrorStream()));
                        while (br.readLine() != null) {}
                    } catch (Exception e) {}
                }).start();
                
                if (!rp.active || Thread.currentThread().isInterrupted()) {
                    ytdlpProcess.destroyForcibly();
                    ffmpegProcess.destroyForcibly();
                    return;
                }
                
                RadioLog.log("Starting player stream...");
                java.io.BufferedInputStream bis = new java.io.BufferedInputStream(ffmpegProcess.getInputStream(), 16384);
                rp.player = new Player(bis, FactoryRegistry.systemRegistry().createAudioDevice());
                rp.device = getAudioDevice(rp.player);
                
                if (!rp.active) {
                    rp.player.close();
                    ytdlpProcess.destroyForcibly();
                    ffmpegProcess.destroyForcibly();
                    return;
                }
                
                if (rp.player.play(1)) {
                    updateVolume(rp.device, worldVolume.getFloatValue() / 100.0f);
                }
                
                while (rp.active && rp.player != null && rp.player.play(1)) {
                    while (paused.getValue() && rp.active) {
                        try { Thread.sleep(50); } catch (Exception e) {}
                    }
                }
                
                ytdlpProcess.destroyForcibly();
                ffmpegProcess.destroyForcibly();
            } catch (Exception e) {
                RadioLog.log("Exception in remote track thread: " + e.getMessage());
            } finally {
                if (ytdlpProcess != null) {
                    try { ytdlpProcess.destroyForcibly(); } catch (Exception e) {}
                }
                if (ffmpegProcess != null) {
                    try { ffmpegProcess.destroyForcibly(); } catch (Exception e) {}
                }
            }
        });
        rp.thread.setDaemon(true);
        rp.thread.start();
    }

    private void stopRemotePlayback(String sender) {
        RemotePlayback rp = remotePlaybacks.get(sender);
        if (rp != null) {
            rp.active = false;
            if (rp.process != null) {
                try { rp.process.destroyForcibly(); } catch (Exception e) {}
            }
            if (rp.player != null) {
                try { rp.player.close(); } catch (Exception e) {}
            }
            if (rp.thread != null) {
                rp.thread.interrupt();
            }
            remotePlaybacks.remove(sender);
        }
    }
    
    private void stopAllRemotePlaybacks() {
        for (String sender : remotePlaybacks.keySet()) {
            stopRemotePlayback(sender);
        }
    }
}