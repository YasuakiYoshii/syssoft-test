public class Byte16 {
    byte[] element = new byte[16];
    int num;
    char[] name = new char[FileSystem.DIRSIZ];

    void set(byte[] buffer, int startIndex) {
        System.arraycopy(buffer, startIndex, element, 0, 16);
    }

    void setNum() {
        this.num = Byte.toUnsignedInt(element[0]) + Byte.toUnsignedInt(element[1]) * 256;
        for(int i=0; i < FileSystem.DIRSIZ; i++) {
            if(element[i+2]!= 0) {
                name[i] = (char)element[i+2];
            }
        }
    }
}
