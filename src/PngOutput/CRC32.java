package PngOutput;

public final class CRC32 {

    private static final int CRC_TABLE_SIZE = 256;
    private static final int CRC_TABLE_MASK = 0xFF;
    private static final long POLYNOMIAL = 0xEDB88320L;

    private static final long[] CRC_TABLE = new long[CRC_TABLE_SIZE];
    private static boolean crcTableComputed = false;

    private CRC32() {
        throw new AssertionError("This class cannot be instantiated");
    }

    private static void makeCRCTable() {
        for (int n = 0; n < CRC_TABLE_SIZE; n++) {
            long c = (long) n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) == 1) {
                    c = POLYNOMIAL ^ (c >>> 1);
                } else {
                    c = c >>> 1;
                }
            }
            CRC_TABLE[n] = c;
        }
        crcTableComputed = true;
    }

    public static byte[] updateCRC(byte[] crc, byte[] buf, int len) {
        long c1 = crc[0] & 0xFF;
        long c2 = crc[1] & 0xFF;
        long c3 = crc[2] & 0xFF;
        long c4 = crc[3] & 0xFF;

        if (!crcTableComputed) {
            makeCRCTable();
        }

        for (int i = 0; i < len; i++) {
            long lookupIndex = (c4 ^ buf[i]) & CRC_TABLE_MASK;
            long temp = CRC_TABLE[(int) lookupIndex];
            c4 = (c3 ^ temp) & 0xFF;
            c3 = (c2 ^ (temp >>> 8)) & 0xFF;
            c2 = (c1 ^ (temp >>> 16)) & 0xFF;
            c1 = (POLYNOMIAL ^ (temp >>> 24)) & 0xFF;
        }

        return new byte[]{(byte) c1, (byte) c2, (byte) c3, (byte) c4};
    }

    public static byte[] calculateCRC(byte[] buf) {
        byte[] crc = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        byte[] result = updateCRC(crc, buf, buf.length);
        return new byte[]{ result[0], result[1], result[2], result[3] };
    }
}