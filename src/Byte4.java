public class Byte4 {
    byte[] element = new byte[4];
    int num;

    void set(byte[] buffer, int startIndex) {
        System.arraycopy(buffer, startIndex, element, 0, 4);
    }

    void setNum() {
        this.num = Byte.toUnsignedInt(element[0]) + Byte.toUnsignedInt(element[1]) * 256
                + Byte.toUnsignedInt(element[2]) * 256*256 + Byte.toUnsignedInt(element[3]) * 256*256*256;
    }
}
