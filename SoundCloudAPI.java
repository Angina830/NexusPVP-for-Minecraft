package com.nexuspvp.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SoundCloudAPI {
    private static String clientId = null;

    public static String getClientId() throws Exception {
        if (clientId != null) return clientId;
        String html = get("https://soundcloud.com/discover");
        Pattern p = Pattern.compile("<script crossorigin src=\"(https://a-v2\\.sndcdn\\.com/assets/[^\"]+\\.js)\"></script>");
        Matcher m = p.matcher(html);
        while (m.find()) {
            String jsUrl = m.group(1);
            String js = get(jsUrl);
            Pattern p2 = Pattern.compile("client_id:\"([a-zA-Z0-9]{32})\"");
            Matcher m2 = p2.matcher(js);
            if (m2.find()) {
                clientId = m2.group(1);
                return clientId;
            }
        }
        throw new Exception("Could not find client_id");
    }

    public static List<String> getPlaylistTracks(String playlistUrl) throws Exception {
        String cid = getClientId();
        String resolveUrl = "https://api-v2.soundcloud.com/resolve?url=" + playlistUrl + "&client_id=" + cid;
        String json = get(resolveUrl);
        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
        
        List<String> streamUrls = new ArrayList<>();
        
        if (root.has("tracks")) {
            JsonArray tracks = root.getAsJsonArray("tracks");
            for (JsonElement t : tracks) {
                JsonObject track = t.getAsJsonObject();
                if (track.has("media")) {
                    JsonObject media = track.getAsJsonObject("media");
                    JsonArray transcodings = media.getAsJsonArray("transcodings");
                    for (JsonElement tr : transcodings) {
                        JsonObject trans = tr.getAsJsonObject();
                        String format = trans.getAsJsonObject("format").get("protocol").getAsString();
                        String mime = trans.getAsJsonObject("format").get("mime_type").getAsString();
                        if (format.equals("progressive") && mime.contains("audio/mpeg")) {
                            String url = trans.get("url").getAsString() + "?client_id=" + cid;
                            String streamJson = get(url);
                            JsonObject streamObj = new JsonParser().parse(streamJson).getAsJsonObject();
                            streamUrls.add(streamObj.get("url").getAsString());
                            break;
                        }
                    }
                }
            }
        }
        return streamUrls;
    }

    private static String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return response.toString();
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(getPlaylistTracks("https://soundcloud.com/monstercat/sets/monstercat-uncaged-vol-1"));
    }
}
