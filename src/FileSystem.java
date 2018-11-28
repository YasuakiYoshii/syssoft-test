public interface FileSystem {
    /* fs.h */
    int ROOTINO = 1; // root i-number
    int  BSIZE = 512; // block size

    int NDIRECT = 12;
    int SIZEOFINT = 4;
    int NINDIRECT = (BSIZE / SIZEOFINT);
    int MAXFILE = (NDIRECT + NINDIRECT);

    int BPB = (BSIZE*8);

    short T_DIR = 1; // ディレクトリ
    short T_FILE = 2; // ファイル
    short T_DEV = 3; // デバイス



    int DIRSIZ = 14;

    // size of dinode = 64
    class dinode {
        short type;           // File type
        short major;          // Major device number (T_DEV only)
        short minor;          // Minor device number (T_DEV only)
        short nlink;          // Number of links to inode in file system
        int size;            // Size of file (bytes)
        int[] addrs = new int[NDIRECT+1]; // Data block addresses
        int[] indirection = new int[128];

        void set(ImageType[] img, Byte2[] elements1, Byte4[] elements2) {
            type = elements1[0].num;
            major = elements1[1].num;
            minor = elements1[2].num;
            nlink = elements1[3].num;
            size = elements2[0].num;
            for(int i=0; i < NDIRECT+1; i++) {
                addrs[i] = elements2[i+1].num;
            }
            if(addrs[NDIRECT] != 0) {
                Byte4[] elements = new Byte4[128];
                for(int i = 0; i < 128; i++) {
                    elements[i] = new Byte4();
                    elements[i].set(img[addrs[NDIRECT]].block, i*4);
                    elements[i].setNum();
                    indirection[i] = elements[i].num;
                }
            }
        }
    }

    // size of dirent = 14
    class dirent {
        short inum;
        char[] name = new char[DIRSIZ];
    }
}
