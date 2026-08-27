package com.nexuspvp.modules;
import com.nexuspvp.util.Compat;


import net.minecraft.client.MinecraftClient;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RadioLog {
    public static void log(String message) {
        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "nexus_pvp");
            if (!dir.exists()) dir.mkdirs();
            File logFile = new File(dir, "radio_debug.log");
            
            PrintWriter out = new PrintWriter(new FileWriter(logFile, true));
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            out.println("[" + time + "] " + message);
            out.close();
            
            System.out.println("[RadioLog] " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}