// img_t型に相当

public class ImageType {
    byte[] block = new byte[Main.BSIZE];

    void set(byte[] buffer, int startIndex) {
        System.arraycopy(buffer, startIndex, block, 0, Main.BSIZE);
    }
}
