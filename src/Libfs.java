import java.nio.MappedByteBuffer;

public class Libfs implements FileSystem {

    /*
     * Basic operations on files (inodes)
     */

    static public void debug_message(final String tag, final String fmt, Object... args) {
        String str = String.format(tag + ": " + fmt, args);
        System.err.println(str);
    }

    static private void derror(String fmt, Object... args) {
        debug_message("ERROR", fmt, args);
    }

    static private void dwarn(String fmt, Object... args) {
        debug_message("WARNING", fmt, args);
    }

    public static void error(String fmt, Object... args) {
        String str = String.format(fmt, args);
        System.err.println(str);
    }

    // inode of the root directory
    static int root_inode_number = 1;
    // inode
    static InodeType root_inode = new InodeType();


    // returns the pointer to the inum-th dinode structure
//    dinode iget(ImageType img, int inum) {
//        if (0 < inum && inum < SBLK(img).ninodes)
//            return (inode_t)img[IBLOCK(inum, SBLKS(img))] + inum % IPB;
//        derror("iget: %u: invalid inode number", inum);
//        return null;
//    }
}
