package cs2.dma.tuil;

import cs2.dma.entry.PlayerInfo;
import cs2.dma.main.PlayerAddressUpdateThread;
import vmm.IVmm;
import vmm.IVmmProcess;

import java.util.*;
import java.io.FileReader;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class GameDataManager {
    private static long dwLocalPlayerPawn = 0x0;
    private static long dwEntityList = 0x0;
    private static long dwGameTypes = 0x0;
    private static long dwGlobalVars = 0x0;
    private static final long GLOBAL_VARS_MAPNAME_OFFSET = 0x0188;

    static {
        try {
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

            dwLocalPlayerPawn += Long.parseLong(map.get("dwLocalPlayerPawn").replace("0x", ""), 16);
            dwEntityList += Long.parseLong(map.get("dwEntityList").replace("0x", ""), 16);
            dwGameTypes += Long.parseLong(map.get("dwGameTypes").replace("0x", ""), 16);
            dwGlobalVars += Long.parseLong(map.get("dwGlobalVars").replace("0x", ""), 16);

            parser.close();
        } catch (Exception e) {
            System.out.println("[-] Failed to read offsets.json file: " + e.getMessage());
            System.exit(1);
        }
    }

    private String knowMap = "de_ancient,de_dust2,de_inferno,de_mirage,de_nuke,de_overpass,de_train,de_vertigo";
    private static String[] argvMemProcFS = { "", "-device", "FPGA" };

    private static IVmmProcess gameProcess;
    private static MemoryTool memoryTool;

    private static long clientAddress;
    private long EntityList;
    private long LocalPlayerController;
    private long mapNameAddress;
    private static List<PlayerInfo> playerInfoList;

    private String mapName;

    private IVmm vmm;
    private GameProcessMonitor processMonitor;

    private static final long PROCESS_RETRY_DELAY = 5000;

    public boolean initializeVmm() {
        String os = System.getProperty("os.name", "").toLowerCase();
        System.out.println("[*] Detected OS: " + os);
        boolean isLinux = os.contains("linux");
        String baseDir = System.getProperty("user.dir");
        String vmmPath = baseDir + (isLinux ? "/vmm" : "\\vmm");

        String memmapPath = baseDir + (isLinux ? "/memmap.txt" : "\\memmap.txt");
        if (new java.io.File(memmapPath).exists()) {
            System.out.println("[*] Using existing memmap.txt at: " + memmapPath);
            argvMemProcFS = new String[] { "", "-device", "FPGA", "-memmap", "memmap.txt" };
        }

        this.vmm = IVmm.initializeVmm(vmmPath, argvMemProcFS);
        vmm.setConfig(IVmm.VMMDLL_OPT_REFRESH_FREQ_FAST, 1);
        if (vmm.isValid()) {
            this.processMonitor = new GameProcessMonitor(vmm);
            return true;
        }
        return false;
    }

    public IVmm getVmm() {
        return vmm;
    }

    public String getMapName() {
        return mapName;
    }

    public boolean initializeGameData() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                gameProcess = processMonitor.waitForProcess();
                if (gameProcess == null) {
                    return false;
                }

                memoryTool = new MemoryTool(gameProcess);
                if (refreshGameData()) {
                    return true;
                }

                Thread.sleep(PROCESS_RETRY_DELAY);
            } catch (Exception e) {
                System.out.println("[-] Error initializing game data: " + e.getMessage());
                try {
                    Thread.sleep(PROCESS_RETRY_DELAY);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    private boolean refreshGameData() {
        try {
            clientAddress = memoryTool.getModuleAddress("client.dll");
            long globalVarsPtr = memoryTool.readAddress(clientAddress + dwGlobalVars, 8);
            if (globalVarsPtr == 0)
                return false;
            mapNameAddress = globalVarsPtr;
            EntityList = memoryTool.readAddress(clientAddress + dwEntityList, 8);
            EntityList = memoryTool.readAddress(EntityList + 0x10, 8);
            if (EntityList == 0)
                return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void initPlayerInfo() {
        try {
            if (!refreshGameData() || !processMonitor.isCurrentProcessValid()) {
                if (!initializeGameData()) {
                    playerInfoList = new ArrayList<>();
                    return;
                }
            }
            mapName = readCurrentMapName();

            LocalPlayerController = memoryTool.readAddress(clientAddress + dwLocalPlayerPawn, 8);
            if (LocalPlayerController == 0) {
                return;
            }

            List<PlayerInfo> list = new ArrayList<>();
            List<PlayerAddressUpdateThread> pautList = new ArrayList<>();
            boolean isKnowMap = mapName != null && !"".equals(mapName) && knowMap.indexOf(mapName) != -1;

            for (int i = 0; i < 64; i++) {
                PlayerAddressUpdateThread updateThread = new PlayerAddressUpdateThread();
                updateThread.setIndex(i);
                updateThread.setMemoryTool(memoryTool);
                updateThread.setClientAddress(clientAddress);
                updateThread.setEntityList(EntityList);
                updateThread.setDwEntityList(dwEntityList);
                updateThread.setLocalPlayerController(LocalPlayerController);
                updateThread.setKnowMap(isKnowMap);
                updateThread.start();
                pautList.add(updateThread);
            }

            pautList.forEach(pItem -> {
                try {
                    pItem.join();
                    PlayerInfo data = pItem.getPlayerInfo();
                    if (data != null) {
                        list.add(pItem.getPlayerInfo());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            playerInfoList = list;
        } catch (Exception e) {
            System.out.println("[-] Error updating player info: " + e.getMessage());
            playerInfoList = new ArrayList<>();
        }
    }

    public List<PlayerInfo> getPlayerInfoList() {
        return playerInfoList;
    }

    private String readCurrentMapName() {
        String result = attemptReadMapName(mapNameAddress + GLOBAL_VARS_MAPNAME_OFFSET);
        boolean isValid = isValidMapName(result);
        
        return isValid ? result : "undefined";
    }

    private String attemptReadMapName(long mapNamePtrAddress) {
        try {
            // Read the char* pointer at global_vars + 0x0188
            long namePtr = memoryTool.readAddress(mapNamePtrAddress, 8);
            if (namePtr == 0) {
                return "";
            }
            
            // Read string directly from the pointer (try without -2 first)
            String raw = memoryTool.readString(namePtr, 64);
            if (raw == null || raw.isEmpty()) {
                return "";
            }
            
            return raw.trim();
        } catch (Exception e) {
            System.out.println("[-] Error reading map name: " + e.getMessage());
            return "";
        }
    }
    private boolean isValidMapName(String name) {
        if (name == null || name.isEmpty() || "undefined".equals(name)) {
            return false;
        }

        return knowMap.contains(name);
    }

}
