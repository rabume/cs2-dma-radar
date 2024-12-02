
package cs2.dma.tuil;

import vmm.IVmm;
import vmm.IVmmProcess;

public class GameProcessMonitor {
    private final IVmm vmm;
    private static final String PROCESS_NAME = "cs2.exe";
    private static final long POLL_INTERVAL = 1000; // 1 second

    public GameProcessMonitor(IVmm vmm) {
        this.vmm = vmm;
    }

    public IVmmProcess waitForProcess() {
        System.out.println("[*] Waiting for CS2 process...");
        while (true) {
            IVmmProcess process = findCS2Process();
            if (process != null) {
                System.out.println("[+] CS2 process found!");
                return process;
            }
            try {
                Thread.sleep(POLL_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
    }

    private IVmmProcess findCS2Process() {
        for (IVmmProcess process : vmm.processGetAll()) {
            if (PROCESS_NAME.equals(process.getName())) {
                return process;
            }
        }
        return null;
    }
}