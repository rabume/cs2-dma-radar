package cs2.dma.tuil;

import vmm.IVmmProcess;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class MemoryTool {
    private IVmmProcess process;

    public MemoryTool(IVmmProcess process) {
        this.process = process;
    }

    public long getModuleAddress(String moduleName) {
        try {
            return process.moduleGet(moduleName, true).getVaBase();
        } catch (Exception e) {
            System.out.println("[-] Failed to get module address for " + moduleName + ": " + e.getMessage());
            return 0;
        }
    }

    public long readAddress(long va, int size) {
        try {
            byte[] data = process.memRead(va, size, 1);
            if (data == null || data.length < size) {
                return 0;
            }
            return longFrom8Bytes(data, 0, true);
        } catch (Exception e) {
            System.out.println("[-] Failed to read address at 0x" + Long.toHexString(va) + ": " + e.getMessage());
            return 0;
        }
    }

    public String readString(long va, int size) {
        try {
            byte[] bstr = process.memRead(va, size, 1);
            if (bstr == null || bstr.length == 0) {
                return "";
            }
            
            int length = 0;
            for (int i = 0; i < bstr.length; i++) {
                if (bstr[i] == 0) {
                    break;
                }
                length++;
            }
            
            byte[] str = new byte[length];
            for (int i = 0; i < length; i++) {
                str[i] = bstr[i];
            }
            
            return new String(str, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("[-] Failed to read string at 0x" + Long.toHexString(va) + ": " + e.getMessage());
            return "";
        }
    }

    public int readInt(long va, int size) {
        try {
            byte[] data = process.memRead(va, size, 1);
            if (data == null || data.length < size) {
                return 0;
            }
            return bytesToIntLittleEndian(data);
        } catch (Exception e) {
            System.out.println("[-] Failed to read int at 0x" + Long.toHexString(va) + ": " + e.getMessage());
            return 0;
        }
    }

    public float readFloat(long va, int size) {
        try {
            byte[] data = process.memRead(va, size, 1);
            if (data == null || data.length < size) {
                return 0.0f;
            }
            return fromByteArray(data);
        } catch (Exception e) {
            System.out.println("[-] Failed to read float at 0x" + Long.toHexString(va) + ": " + e.getMessage());
            return 0.0f;
        }
    }

    public static long longFrom8Bytes(byte[] input, int offset, boolean littleEndian) {
        long value = 0;
        for (int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : (7 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) input[offset + count] << shift);
        }
        return value;
    }

    public static int bytesToIntLittleEndian(byte[] bytes) {
        return bytes[0] & 0xFF |
                (bytes[1] & 0xFF) << 8 |
                (bytes[2] & 0xFF) << 16 |
                (bytes[3] & 0xFF) << 24;
    }

    float fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
    }
}