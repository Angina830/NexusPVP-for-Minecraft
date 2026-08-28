package com.nexuspvp.modules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nexuspvp.NexusPVP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class RadioSyncManager {
    private static RadioSyncManager instance;
    private MqttClient client;
    private String currentServerIp = "none";
    private String activeTopic = null;
    private final Gson gson = new Gson();
    
    public ConcurrentHashMap<String, RemoteTrack> remoteTracks = new ConcurrentHashMap<>();
    
    public static class RemoteTrack {
        public String sender;
        public String trackQuery;
        public long startTime;
        public double x, y, z;
        public long lastUpdateTime;
    }
    
    private RadioSyncManager() {
        new Thread(() -> {
            try {
                String clientId = "TopkaRadio_" + System.currentTimeMillis();
                client = new MqttClient("tcp://broker.hivemq.com:1883", clientId, new MemoryPersistence());
                
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(true);
                options.setConnectionTimeout(10);
                
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        currentServerIp = "none"; // force resubscribe on reconnect
                        RadioLog.log("MQTT Connection lost: " + (cause != null ? cause.getMessage() : "unknown"));
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) {
                        try {
                            String json = new String(message.getPayload(), StandardCharsets.UTF_8);
                            RadioLog.log("MQTT message arrived on " + topic + ": " + json);
                            JsonObject obj = gson.fromJson(json, JsonObject.class);
                            
                            String sender = obj.get("sender").getAsString();
                            if (MinecraftClient.getInstance().getSession() != null && sender.equals(MinecraftClient.getInstance().getSession().getUsername())) {
                                return; // ignore self
                            }
                            
                            double x = obj.get("x").getAsDouble();
                            double y = obj.get("y").getAsDouble();
                            double z = obj.get("z").getAsDouble();
                            String track = obj.get("track").getAsString();
                            long startTime = obj.get("startTime").getAsLong();
                            
                            RemoteTrack rt = remoteTracks.get(sender);
                            boolean isNewTrack = (rt == null || !rt.trackQuery.equals(track) || rt.startTime != startTime);
                            boolean isReconnectDuplicate = (rt == null && remoteTracks.get(sender) == null);
                            
                            if (isNewTrack) {
                                // Check if this is just the same track re-appearing after MQTT reconnect
                                boolean sameTrackReappeared = false;
                                Radio radio = NexusPVP.getInstance().getModuleManager().getModule(Radio.class);
                                if (radio != null && radio.isEnabled() && radio.hasActivePlayback(sender)) {
                                    // There's an active playback - check if it's the same track
                                    RemoteTrack oldRt = rt; // rt from before update
                                    if (oldRt == null) {
                                        // rt was removed from remoteTracks (MQTT disconnect cleanup)
                                        // but playback is still going - don't restart
                                        sameTrackReappeared = true;
                                    }
                                    // If oldRt exists but track/startTime differ, it's genuinely new
                                }
                                
                                rt = new RemoteTrack();
                                rt.sender = sender;
                                rt.trackQuery = track;
                                rt.startTime = startTime;
                                remoteTracks.put(sender, rt);
                                
                                if (radio != null && radio.isEnabled()) {
                                    if (sameTrackReappeared) {
                                        RadioLog.log("Same track re-appeared for " + sender + " after reconnect, skipping re-download.");
                                    } else {
                                        RadioLog.log("New remote track from " + sender + ", passing to Radio module...");
                                        radio.onRemoteTrackStarted(rt);
                                    }
                                } else {
                                    RadioLog.log("Radio module is disabled or null, ignoring track from " + sender);
                                }
                            }
                            
                            rt.x = x;
                            rt.y = y;
                            rt.z = z;
                            rt.lastUpdateTime = System.currentTimeMillis();
                            
                        } catch (Exception e) {
                            RadioLog.log("Error parsing MQTT message: " + e.getMessage());
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {}
                });
                
                client.connect(options);
                RadioLog.log("MQTT connected successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                RadioLog.log("MQTT connect failed: " + e.getMessage());
            }
        }).start();
    }
    
    public static RadioSyncManager getInstance() {
        if (instance == null) {
            instance = new RadioSyncManager();
        }
        return instance;
    }
    
    public void updateTopic() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) {
            activeTopic = null;
            currentServerIp = "none";
            return;
        }
        
        String newIp = "singleplayer";
        ServerInfo serverData = mc.getCurrentServerEntry();
        if (serverData != null) {
            newIp = serverData.address;
        }
        
        if (!newIp.equals(currentServerIp)) {
            try {
                if (client != null && client.isConnected()) {
                    if (!currentServerIp.equals("none") && activeTopic != null) {
                        client.unsubscribe(activeTopic);
                        RadioLog.log("Unsubscribed from " + activeTopic);
                    }
                    currentServerIp = newIp;
                    activeTopic = "nexus_pvp/radio/" + currentServerIp.replaceAll("[^a-zA-Z0-9_-]", "_");
                    client.subscribe(activeTopic);
                    RadioLog.log("Subscribed to " + activeTopic);
                }
            } catch (Exception e) {
                RadioLog.log("MQTT subscribe failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void broadcastMyTrack(String trackQuery, long startTime, double x, double y, double z) {
        if (activeTopic == null || client == null || !client.isConnected()) {
            RadioLog.log("Cannot broadcast track: activeTopic=" + activeTopic + ", client.isConnected=" + (client != null ? client.isConnected() : "null"));
            return;
        }
        
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("sender", MinecraftClient.getInstance().getSession().getUsername());
            payload.addProperty("track", trackQuery);
            payload.addProperty("startTime", startTime);
            payload.addProperty("x", x);
            payload.addProperty("y", y);
            payload.addProperty("z", z);
            
            String json = payload.toString();
            MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            message.setQos(0); // Fire and forget
            client.publish(activeTopic, message);
            RadioLog.log("Broadcasted track to " + activeTopic + ": " + json);
        } catch (Exception e) {
            RadioLog.log("MQTT broadcast failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}