public class SuperBlock {
    int size;
    int nblocks;
    int ninodes;
    int nlog;
    int logstart;
    int inodestart;
    int bmapstart;

    void set(Byte4[] elements) {
        size = elements[0].num;
        nblocks = elements[1].num;
        ninodes = elements[2].num;
        nlog = elements[3].num;
        logstart = elements[4].num;
        inodestart = elements[5].num;
        bmapstart = elements[6].num;
    }

    void print() {
        System.out.println(size);
        System.out.println(nblocks);
        System.out.println(ninodes);
        System.out.println(nlog);
        System.out.println(logstart);
        System.out.println(inodestart);
        System.out.println(bmapstart);
    }

    int check() {
        int error = 0;
        /* size */
        if(1000 == size) {
            System.out.println("Superblock.size:OK");
        } else {
            System.out.println("Superblock.size:ERROR (" + size + ")");
            error++;
        }

        /* nblocks */
        if(941 == nblocks) {
            System.out.println("Superblock.nblocks:OK");
        } else {
            System.out.println("Superblock.nblocks:ERROR (" + nblocks + ")");
            error++;
        }

        /* ninodes */
        if(200 == ninodes) {
            System.out.println("Superblock.ninodes:OK");
        } else {
            System.out.println("Superblock.ninodes:ERROR (" + ninodes + ")");
            error++;
        }

        /* nlog */
        if(30 == nlog) {
            System.out.println("Superblock.nlog:OK");
        } else {
            System.out.println("Superblock.nlog:ERROR (" + nlog + ")");
            error++;
        }

        /* logstart */
        if(2 == logstart) {
            System.out.println("Superblock.logstart:OK");
        } else {
            System.out.println("Superblock.logstart:ERROR (" + logstart + ")");
            error++;
        }

        /* inodestart */
        if(32 == inodestart) {
            System.out.println("Superblock.inodestart:OK");
        } else {
            System.out.println("Superblock.inodestart:ERROR (" + inodestart + ")");
            error++;
        }

        /* bmapstart */
        if(58 == bmapstart) {
            System.out.println("Superblock.bmapstart:OK");
        } else {
            System.out.println("Superblock.bmapstart:ERROR (" + bmapstart + ")");
            error++;
        }
        return error;
    }
}
