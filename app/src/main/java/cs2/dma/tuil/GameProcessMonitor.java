package cs2.dma.tuil;

import vmm.IVmm;
import vmm.IVmmProcess;

public class GameProcessMonitor {
    private final IVmm vmm;
    private static final String PROCESS_NAME = "cs2.exe";
    private static final long POLL_INTERVAL = 5000;
    private static final long ERROR_BACKOFF = 10000; // 10 second backoff on errors
    private IVmmProcess currentProcess;

    public GameProcessMonitor(IVmm vmm) {
        this.vmm = vmm;
    }

    public IVmmProcess waitForProcess() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                IVmmProcess process = findCS2Process();
                if (process != null && isProcessValid(process)) {
                    currentProcess = process;
                    return process;
                }
                Thread.sleep(POLL_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                System.out.println("[-] Error monitoring process: " + e.getMessage());
                try {
                    Thread.sleep(ERROR_BACKOFF);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    public boolean isProcessValid(IVmmProcess process) {
        try {
            return process != null && process.getName().equals(PROCESS_NAME);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCurrentProcessValid() {
        return isProcessValid(currentProcess);
    }

    private IVmmProcess findCS2Process() {
        try {
            for (IVmmProcess process : vmm.processGetAll()) {
                if (PROCESS_NAME.equals(process.getName())) {
                    return process;
                }
            }
        } catch (Exception e) {
            System.out.println("[-] Error finding process: " + e.getMessage());
        }
        return null;
    }
}