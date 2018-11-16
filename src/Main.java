import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;


public class Main {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;
    static int BSIZE = 512; // block size

    public static void main(String args[]) {
//        Libfs.progname = args[0];
        if (args.length <= 0) {
//            Libfs.error("usage: %s img_file command [arg...]", Libfs.progname);
//            Libfs.error("Commands are:");
//            for (int i = 0; cmd_table[i].name != null; i++)
//                Libfs.error("    %s %s", cmd_table[i].name, cmd_table[i].args);
            System.err.println("ERROR: input file name.");
            System.exit(EXIT_FAILURE);
        }

//        String cmd = args[1];
        File img_file = new File(args[0]);
        long img_size = img_file.length();


        /*img_t img = mmap(null, img_size, PROT_READ | PROT_WRITE,
                MAP_SHARED, img_fd, 0);*/
        try {
            // open + mmap
            RandomAccessFile img_fd = new RandomAccessFile(img_file, "r");
            MappedByteBuffer img_all = img_fd.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, img_size);

            // img(512000byte) to array size of 512byte
            ImageType[] img = new ImageType[1000];
            byte[] img_buffer = new byte[img_all.remaining()];
            img_all.get(img_buffer);

            //copy from img_all to img
            for(int i = 0; i < 1000; i++) {
                img[i] = new ImageType();
                img[i].set(img_buffer, i*BSIZE);
                System.out.println(Arrays.toString(img[i].block));
            }



            // get file information
            BasicFileAttributes attr = null;
            Path img_path = img_file.toPath();
            attr = Files.readAttributes(img_path, BasicFileAttributes.class);
            Object fileKey = attr.fileKey();
            if (fileKey == null) {
                img_fd.close();
                System.exit(EXIT_FAILURE);
            }

            // information of fs.img
            System.out.println("file key : " + fileKey.toString());
            System.out.println("file size: " + img_size + " Byte");
            System.out.println(Byte.toUnsignedInt(img[1].block[0]));

//            Libfs.root_inode = Libfs.iget(img, Libfs.root_inode_number);

            // shift argc and argv to point the first command argument
//            int status = EXIT_FAILURE;
//            if (setjmp(fatal_exception_buf) == 0)
//                status = exec_cmd(img, cmd, Arrays.copyOfRange(args,3,args.length));

            // Java system garbage-collect buffer itself
            // munmap(img, img_size);
            img_fd.close();

            System.exit(EXIT_SUCCESS);
        } catch (IOException e) {
//            perror(img_file);
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }
}
