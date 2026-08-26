import java.io.*;

public class TestCookies {
    public static void main(String[] args) throws Exception {
        File cookies = new File("C:\\Users\\zakur\\AppData\\Roaming\\.minecraft\\nexus_pvp\\cookies.txt");
        if (!cookies.exists()) {
            cookies.createNewFile();
            System.out.println("Created blank cookies.txt");
        } else {
            System.out.println("cookies.txt exists");
        }
    }
}