public class Byte2 {
    byte[] element = new byte[2];
    short num;

    void set(byte[] buffer, int startIndex) {
        System.arraycopy(buffer, startIndex, element, 0, 2);
    }

    void setNum() {
        this.num = (short)((short)element[0] + (short)element[1]*256);
    }
}
