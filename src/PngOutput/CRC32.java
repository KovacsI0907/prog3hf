package PngOutput;

public final class CRC32 {

    public static int[] crcTable = makeCrcTable();

    private static int[] makeCrcTable()
    {
        int c;
        int[] table = new int[256];

        for (int n = 0; n < 256; n++) {
            c = n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) == 1)
                    c = 0xedb88320 ^ (c >>> 1);
                else
                    c = c >>> 1;
            }
            table[n] = c;
        }

        return table;
    }

    private static int update_crc(int crc, byte[] buf)
    {
        int c = crc;

        for (int n = 0; n < buf.length; n++) {
            c = crcTable[(c ^ buf[n]) & 0xff] ^ (c >>> 8);
        }
        return c;
    }

    public static int crc(byte[] buf)
    {
        return update_crc(0xffffffff, buf) ^ 0xffffffff;
    }

    public static byte[] calculateCRC(byte[] buf) {
        int crc = crc(buf);
        byte[] result = new byte[4];

        result[0] = (byte) ((crc >> 24) & 0xFF);
        result[1] = (byte) ((crc >> 16) & 0xFF);
        result[2] = (byte) ((crc >> 8) & 0xFF);
        result[3] = (byte) (crc & 0xFF);

        return result;
    }

}

/*public final class CRC32 {

    public static long[] crcTable = makeCrcTable();

    private static long[] makeCrcTable() {
        long c;
        long[] table = new long[256];

        for (int n = 0; n < 256; n++) {
            c = n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) == 1)
                    c = 0xedb88320L ^ (c >>> 1);
                else
                    c = c >>> 1;
            }
            table[n] = c;
        }

        return table;
    }

    private static long update_crc(long crc, byte[] buf) {
        long c = crc;

        for (int n = 0; n < buf.length; n++) {
            c = crcTable[(int) ((c ^ buf[n]) & 0xff)] ^ (c >>> 8);
        }
        return c;
    }

    public static long crc(byte[] buf) {
        return update_crc(0xffffffffL, buf) ^ 0xffffffffL;
    }
}*/