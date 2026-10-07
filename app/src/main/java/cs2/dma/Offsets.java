package cs2.dma;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Offsets {
    public static void updateOffsets() {
        try {
            JSONObject offsets = fetchJson("https://raw.githubusercontent.com/a2x/cs2-dumper/main/output/offsets.json");
            JSONObject client  = fetchJson("https://raw.githubusercontent.com/a2x/cs2-dumper/main/output/client_dll.json");

            JSONObject clientDll     = offsets.getJSONObject("client.dll");
            JSONObject matchmaking   = offsets.getJSONObject("matchmaking.dll");
            JSONObject clientClasses = client.getJSONObject("client.dll").getJSONObject("classes");

            JSONObject newOffsets = new JSONObject();
            newOffsets.put("dwLocalPlayerPawn",    String.format("0x%X", clientDll.getLongValue("dwLocalPlayerPawn")));
            newOffsets.put("dwEntityList",         String.format("0x%X", clientDll.getLongValue("dwEntityList")));
            newOffsets.put("dwGameTypes",          String.format("0x%X", matchmaking.getLongValue("dwGameTypes")));
            newOffsets.put("dwGlobalVars",         String.format("0x%X", clientDll.getLongValue("dwGlobalVars")));
            newOffsets.put("m_iHealth",            String.format("0x%X", clientClasses.getJSONObject("C_BaseEntity").getJSONObject("fields").getLongValue("m_iHealth")));
            newOffsets.put("m_iPawnArmor",         String.format("0x%X", clientClasses.getJSONObject("CCSPlayerController").getJSONObject("fields").getLongValue("m_iPawnArmor")));
            newOffsets.put("m_lifeState",          String.format("0x%X", clientClasses.getJSONObject("C_BaseEntity").getJSONObject("fields").getLongValue("m_lifeState")));
            newOffsets.put("m_angEyeAngles",       String.format("0x%X", clientClasses.getJSONObject("C_CSPlayerPawn").getJSONObject("fields").getLongValue("m_angEyeAngles")));
            newOffsets.put("m_iTeamNum",           String.format("0x%X", clientClasses.getJSONObject("C_BaseEntity").getJSONObject("fields").getLongValue("m_iTeamNum")));
            newOffsets.put("m_hPlayerPawn",        String.format("0x%X", clientClasses.getJSONObject("CCSPlayerController").getJSONObject("fields").getLongValue("m_hPlayerPawn")));
            newOffsets.put("m_vOldOrigin",         String.format("0x%X", clientClasses.getJSONObject("C_BasePlayerPawn").getJSONObject("fields").getLongValue("m_vOldOrigin")));
            newOffsets.put("m_iCompTeammateColor", String.format("0x%X", clientClasses.getJSONObject("CCSPlayerController").getJSONObject("fields").getLongValue("m_iCompTeammateColor")));

            // compare with local file, overwrite if different
            String newJson = newOffsets.toJSONString();
            String currentJson = "";
            try { currentJson = Files.readString(Path.of("offsets.json")); } catch (Exception ignored) {}

            if (!currentJson.equals(newJson)) {
                Files.writeString(Path.of("offsets.json"), newJson);
                System.out.println("[*] Offsets updated.");
            } else {
                System.out.println("[*] Offsets are already up to date.");
            }

        } catch (Exception e) {
            System.out.println("[-] Failed to load offsets: " + e.getMessage());
            System.exit(1);
        }
    }

    private static JSONObject fetchJson(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        Scanner s = new Scanner(c.getInputStream()).useDelimiter("\\A");
        return JSON.parseObject(s.hasNext() ? s.next() : "");
    }
}