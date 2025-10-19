package cs2.dma.main;

import cs2.dma.entry.PlayerInfo;
import cs2.dma.tuil.MemoryTool;

import java.util.*;
import java.io.FileReader;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class PlayerAddressUpdateThread extends Thread {
    private int index;
    private PlayerInfo playerInfo;
    private boolean isKnowMap;
    private float levelHeight = 250;
    private MemoryTool memoryTool;
    private long EntityList;
    private long clientAddress;

    private long LocalPlayerController;

    private long dwEntityList;

    // Offsets
    private static long m_iHealth = 0x0;
    private static long m_iPawnArmor = 0x0;
    private static long m_lifeState = 0x0;
    private static long m_angEyeAngles = 0x0;
    private static long m_iTeamNum = 0x0;
    private static long m_hPlayerPawn = 0x0;
    private static long m_vOldOrigin = 0x0; // PlayerPosition X //+ 0x4 Y //+ 0x8 Z
    private static long m_iCompTeammateColor = 0x0;

    static {
        try {
            System.out.println("[+] Read offsets.json file");

            FileReader reader = new FileReader("offsets.json");
            char[] buf = new char[1024];
            int len = 0;

            StringBuilder sb = new StringBuilder();
            while ((len = reader.read(buf)) != -1) {
                sb.append(buf, 0, len);
            }

            String json = sb.toString();

            reader.close();

            DefaultJSONParser parser = new DefaultJSONParser(json);
            Map<String, String> map = parser.parseObject(Map.class, Long.class);

            m_iHealth += Long.parseLong(map.get("m_iHealth").replace("0x", ""), 16);
            m_iPawnArmor += Long.parseLong(map.get("m_iPawnArmor").replace("0x", ""), 16);
            m_lifeState += Long.parseLong(map.get("m_lifeState").replace("0x", ""), 16);
            m_angEyeAngles += Long.parseLong(map.get("m_angEyeAngles").replace("0x", ""), 16);
            m_iTeamNum += Long.parseLong(map.get("m_iTeamNum").replace("0x", ""), 16);
            m_hPlayerPawn += Long.parseLong(map.get("m_hPlayerPawn").replace("0x", ""), 16);
            m_vOldOrigin += Long.parseLong(map.get("m_vOldOrigin").replace("0x", ""), 16);
            m_iCompTeammateColor += Long.parseLong(map.get("m_iCompTeammateColor").replace("0x", ""), 16);

            System.out.println("[+] m_iHealth: " + m_iHealth);
            System.out.println("[+] m_iPawnArmor: " + m_iPawnArmor);
            System.out.println("[+] m_lifeState: " + m_lifeState);
            System.out.println("[+] m_angEyeAngles: " + m_angEyeAngles);
            System.out.println("[+] m_iTeamNum: " + m_iTeamNum);
            System.out.println("[+] m_hPlayerPawn: " + m_hPlayerPawn);
            System.out.println("[+] m_vOldOrigin: " + m_vOldOrigin);
            System.out.println("[+] m_iCompTeammateColor: " + m_iCompTeammateColor);

            parser.close();
        } catch (Exception e) {
            System.out.println("[-] Failed to read offsets.json file: " + e.getMessage());
            System.exit(1);
        }
    }

    public void setKnowMap(boolean knowMap) {
        isKnowMap = knowMap;
    }

    public void setLocalPlayerController(long localPlayerController) {
        LocalPlayerController = localPlayerController;
    }

    public void setEntityList(long entityList) {
        EntityList = entityList;
    }

    public void setClientAddress(long clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setDwEntityList(long dwEntityList) {
        this.dwEntityList = dwEntityList;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMemoryTool(MemoryTool memoryTool) {
        this.memoryTool = memoryTool;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    @Override
    public void run() {
        long EntityAddress = memoryTool.readAddress(EntityList + (index + 1) * 0x70, 8);
        if (EntityAddress == 0)
            return;
        long EntityPawnListEntry = memoryTool.readAddress(clientAddress + dwEntityList, 8);
        if (EntityPawnListEntry == 0)
            return;
        long Pawn = memoryTool.readAddress(EntityAddress + m_hPlayerPawn, 8);
        if (Pawn == 0)
            return;

        // color teammates
        int compTeammateColor = memoryTool.readInt(EntityAddress + m_iCompTeammateColor, 4);

        EntityPawnListEntry = memoryTool.readAddress(EntityPawnListEntry + 0x10 + 8 * ((Pawn & 0x7FFF) >> 9), 8);
        Pawn = memoryTool.readAddress(EntityPawnListEntry + 0x70 * (Pawn & 0x1FF), 8);
        if (Pawn == 0)
            return;
        float localPlayerZ = memoryTool.readFloat(LocalPlayerController + m_vOldOrigin + 0x8, 4);
        if (isKnowMap) {
            int teamId = memoryTool.readInt(EntityAddress + m_iTeamNum, 4);
            float playerZ = memoryTool.readFloat(Pawn + m_vOldOrigin + 0x8, 4);
            float levelDv = playerZ - localPlayerZ;
            levelDv = (levelDv < 0) ? -levelDv : levelDv;
            playerInfo = new PlayerInfo(
                    EntityAddress,
                    Pawn,
                    teamId,
                    memoryTool.readInt(Pawn + m_iHealth, 4),
                    memoryTool.readInt(Pawn + m_iPawnArmor, 4),
                    memoryTool.readInt(Pawn + m_lifeState, 4) == 256,
                    LocalPlayerController == Pawn,
                    memoryTool.readInt(LocalPlayerController + m_iTeamNum, 4) != teamId,
                    memoryTool.readFloat(Pawn + m_vOldOrigin + 0x4, 4),
                    memoryTool.readFloat(Pawn + m_vOldOrigin, 4),
                    playerZ,
                    90 - memoryTool.readFloat(Pawn + m_angEyeAngles + 0x4, 8),
                    levelDv < levelHeight,
                    compTeammateColor);
        } else {
            float angle = memoryTool.readFloat(LocalPlayerController + m_angEyeAngles + 0x4, 8) - 90;
            int teamId = memoryTool.readInt(EntityAddress + m_iTeamNum, 4);
            float pX = memoryTool.readFloat(Pawn + m_vOldOrigin + 0x4, 4);
            float pY = memoryTool.readFloat(Pawn + m_vOldOrigin, 4);
            float newX = pX * (float) Math.cos(Math.toRadians(angle)) - pY * (float) Math.sin(Math.toRadians(angle));
            float newY = pX * (float) Math.sin(Math.toRadians(angle)) + pY * (float) Math.cos(Math.toRadians(angle));

            float playerZ = memoryTool.readFloat(Pawn + m_vOldOrigin + 0x8, 4);
            float levelDv = playerZ - localPlayerZ;
            levelDv = (levelDv < 0) ? -levelDv : levelDv;

            playerInfo = new PlayerInfo(
                    EntityAddress,
                    Pawn,
                    teamId,
                    memoryTool.readInt(Pawn + m_iHealth, 4),
                    memoryTool.readInt(Pawn + m_iPawnArmor, 4),
                    memoryTool.readInt(Pawn + m_lifeState, 4) == 256,
                    LocalPlayerController == Pawn,
                    memoryTool.readInt(LocalPlayerController + m_iTeamNum, 4) != teamId,
                    newX,
                    newY,
                    playerZ,
                    90 - memoryTool.readFloat(Pawn + m_angEyeAngles + 0x4, 8) + angle,
                    levelDv < levelHeight,
                    compTeammateColor);
        }
    }

}
