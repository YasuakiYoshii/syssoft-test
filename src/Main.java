import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Main implements FileSystem {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    public static void main(String args[]) {
        if (args.length <= 0) {
            System.err.println("ERROR: input file name.");
            System.exit(EXIT_FAILURE);
        }

        File img_file = new File(args[0]);
        long img_size = img_file.length();


        /*img_t img = mmap(null, img_size, PROT_READ | PROT_WRITE,
                MAP_SHARED, img_fd, 0);*/
        try {
            // open + mmap
            RandomAccessFile img_fd = new RandomAccessFile(img_file, "r");
            MappedByteBuffer img_all = img_fd.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, img_size);

            // img(512000byte) to array size of 512byte
            ImageType[] img = new ImageType[(int)img_size/BSIZE];
            byte[] img_buffer = new byte[img_all.remaining()];
            img_all.get(img_buffer);

            //copy from img_all to img
            for(int i = 0; i < (int)img_size/BSIZE; i++) {
                img[i] = new ImageType();
                img[i].set(img_buffer, i*BSIZE);
                if(i == 32)
                System.out.println(Arrays.toString(img[i].block));
            }


            /********************************************************************************************/
            /*                          スーパーブロックの抽出および一貫性確認           　　　               */
            /********************************************************************************************/
            Byte4[] elements = new Byte4[7];
            for(int i = 0; i < 7; i++) {
                elements[i] = new Byte4();
                elements[i].set(img[1].block, i*4);
                elements[i].setNum();
            }


            SuperBlock sb = new SuperBlock();
            sb.set(elements);
            sb.check();

            /********************************************************************************************/
            /*                                ビットマップブロックの抽出                                    */
            /********************************************************************************************/

            String val = "";
            for(byte b : img[58].block){
                for(int i=0; i<8; i++){
                    val += String.format("%d", (b & 0x80)/0x80);
                    b <<= 1;
                }
            }
//            System.out.println(val);
            char[] bitmap = val.toCharArray();
            char[] copybitmap = val.toCharArray();
            System.out.println(Arrays.toString(bitmap));

            /********************************************************************************************/
            /*                                  inodeブロック情報の抽出                                   */
            /********************************************************************************************/
            dinode[] dinodes = new dinode[8*(57-31)];
            Byte64[] inodes = new Byte64[8*(57-31)];
            for(int j=32; j < 58; j++) { // each block
                for(int i = 0; i < 8; i++) { // each dinode
                    inodes[i+8*(j-32)] = new Byte64();
                    inodes[i+8*(j-32)].set(img[j].block, i*64);

                    Byte2[] elements1 = new Byte2[4];
                    for(int k = 0; k < 4; k++) {
                        elements1[k] = new Byte2();
                        elements1[k].set(inodes[i+8*(j-32)].element, k*2);
                        elements1[k].setNum();
                    }
                    Byte4[] elements2 = new Byte4[14];
                    for(int k = 0; k < 14; k++) {
                        elements2[k] = new Byte4();
                        elements2[k].set(inodes[i+8*(j-32)].element, 8+k*4);
                        elements2[k].setNum();
                    }

                    dinodes[i+8*(j-32)] = new dinode();
                    dinodes[i+8*(j-32)].set(img, elements1, elements2);
                }
            }

            /********************************************************************************************/
            /*                            ブロックの使用状況に関する一貫性の確認                             */
            /********************************************************************************************/

            int inodeError = 0;
            int bitmapError = 0;
            int bcount;
            for(int i=0; i < 8*(57-31); i++) {
                bcount = 0;
                for(int j=0; j < 13; j++) {
                    /* direct */
                    if(dinodes[i].addrs[j] != 0) {
                        if(j!=NDIRECT){
                            bcount++;
                        }
                        if(copybitmap[dinodes[i].addrs[j]] == '1'){ // １つ目発見で0にする
                            copybitmap[dinodes[i].addrs[j]] ='0';
                        } else if(copybitmap[dinodes[i].addrs[j]] == '0') { // 参照しているのに0なら
                            bitmapError++;
                        }
                    }

                    /* indirect */
                    if(j==NDIRECT && dinodes[i].addrs[j] != 0) {
                        for(int k=0; k < 128; k++) {
                            if(dinodes[i].indirection[k] != 0) {
                                bcount++;
                                if(copybitmap[dinodes[i].indirection[k]] == '1') { // １つ目発見で0にする
                                    copybitmap[dinodes[i].indirection[k]] = '0';
                                } else if(copybitmap[dinodes[i].indirection[k]] == '0') { // 参照しているのに0なら
                                    bitmapError++;
                                }
                            }
                        }
                    }
                }

                /* ファイルサイズ/BSIZE=必要なブロック数(=参照ブロック数) */
                int truebcount;
                if(dinodes[i].size != 0) {
                    truebcount = (dinodes[i].size - 1)/BSIZE + 1;
                } else {
                    truebcount = 0;
                }
                if(bcount != truebcount) {
                    System.out.print(truebcount + ",");
                    System.out.print(dinodes[i].size + ",");
                    System.out.println(i);
                    inodeError++;
                }
            }


            /* bitmapが1なのに参照されていない */
            for(int i=59; i < 1000; i++) {
                if(copybitmap[i] == '1') {
                    System.out.println(Arrays.toString(img[i].block));
                    bitmapError++;
                }
            }

            if(bitmapError == 0) {
                System.out.println("bitmapblock:OK");
            } else {
                System.out.println("bitmapblock:ERROR (" + bitmapError + " errors)");
            }

//            for (int i=0; i < 1000; i++) {
//                if(bitmap[i] != '1') {
//                    System.out.print(i + ",");
//                }
//            }

            /********************************************************************************************/
            /*                                 inodeに関する一貫性の確認                                   */
            /********************************************************************************************/

            for(int i=0; i < 8*(57-31); i++) {
                if(dinodes[i].type == T_DIR) {
                    if(dinodes[i].major == 0 && dinodes[i].minor == 0){
                        System.out.println(dinodes[1].type + "," + dinodes[1].minor);
                        inodeError++;
                    }
                }
//                System.out.print(dinodes[i].nlink+",");
            }

            if(inodeError == 0) {
                System.out.println("inodeblock:OK");
            } else {
                System.out.println("inodeblock:ERROR (" + inodeError + " errors)");
            }



            img_fd.close();

            System.exit(EXIT_SUCCESS);
        } catch (IOException e) {
//            perror(img_file);
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
    }
}
