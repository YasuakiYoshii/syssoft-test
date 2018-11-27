public class Byte64 {
    byte[] element = new byte[64];

    void set(byte[] buffer, int startIndex) {
        System.arraycopy(buffer, startIndex, element, 0, 64);
    }
}
