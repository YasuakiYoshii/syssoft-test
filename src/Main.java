import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;


public class Main implements FileSystem {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    public void readFolder( File dir ) {

        File[] files = dir.listFiles();
        if( files == null )
            return;
        java.util.Arrays.sort(files, Comparator.comparing(File::getName));
        for( File file : files ) {
            if( !file.exists() )
                continue;
            else if( file.isDirectory() )
                readFolder( file );
            else if( file.isFile() )
                execute( file );
        }
    }

    // 指定されたinode番号の名前を取り出す
    public String findFileName(ImageType[] img, dinode[] dinodes, int inum) {
        for(int i=0; i < 8*(57-31); i++) {
            int file_num = dinodes[i].size / 16;
            for (int j = 0; j < 13; j++) {
                if (dinodes[i].addrs[j] != 0) {
                    if (j != NDIRECT) {
                        if (dinodes[i].type == T_DIR) { // direct 参照先のディレクトリのリンク確認
                            for (int k = 0; k < BSIZE / 16; k++) {
                                Byte16 file = new Byte16();
                                dirent tmpdirent = new dirent();
                                file.set(img[dinodes[i].addrs[j]].block, k * 16);
                                file.setNum();
                                tmpdirent.set(file);
                                if (file_num == 0) {
                                    break;
                                } else {
                                    if(tmpdirent.inum == inum){
                                        return tmpdirent.name;
                                    }
                                    file_num--;
                                }
                            }
                        }
                    }

                    /* indirect */
                    if (j == NDIRECT && dinodes[i].addrs[j] != 0) {
                        for (int k = 0; k < 128; k++) {
                            if (dinodes[i].indirection[k] != 0) {
                                if (dinodes[i].type == T_DIR) { // direct 参照先のディレクトリのリンク確認
                                    for (int l = 0; l < BSIZE / 16; l++) {
                                        Byte16 file = new Byte16();
                                        dirent tmpdirent = new dirent();
                                        file.set(img[dinodes[i].indirection[k]].block, l * 16);
                                        file.setNum();
                                        tmpdirent.set(file);
                                        if (file_num == 0) {
                                            break;
                                        } else {
                                            if(tmpdirent.inum == inum){
                                                return tmpdirent.name;
                                            }
                                            file_num--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // 指定されたinodeのinode番号を返す
    public int geti(dinode dinode, dinode[] dinodes) {
        for (int i=0; i < 8*26; i++) {
            if(dinode == dinodes[i]) {
                return i;
            }
        }
        return -1;
    }

    public void showfiles(ImageType[] img, dinode dinode, int i, dinode[] dinodes) {
        int file_num = dinode.size / 16;
        for (int j = 0; j < 13; j++) {
            /* direct */
            if (dinode.type == T_DIR) { // direct 参照先のディレクトリのリンク確認
                if (dinode.addrs[j] != 0) {
                    if (j != NDIRECT) {
                        for (int k = 0; k < BSIZE / 16; k++) {
                            Byte16 file = new Byte16();
                            dirent tmpdirent = new dirent();
                            file.set(img[dinode.addrs[j]].block, k * 16);
                            file.setNum();
                            tmpdirent.set(file);
                            if (file_num == 0) {
                                continue;
                            } else if(!tmpdirent.name.equals("")){
                                if(dinodes[tmpdirent.inum].type == T_DIR)
                                    System.out.println(tmpdirent.name + " " + dinodes[tmpdirent.inum].type + " "
                                            + tmpdirent.inum + " " + dinodes[tmpdirent.inum].size + " "
                                            + dinodes[tmpdirent.inum].nlink + " " + dinodes[tmpdirent.inum].parentInode);
                                file_num--;
                            }
                        }
                    }
                }
            }

            /* indirect */
            if (dinode.type == T_DIR) { // direct 参照先のディレクトリのリンク確認
                if (j == NDIRECT && dinode.addrs[j] != 0) {
                    for (int k = 0; k < 128; k++) {
                        if (dinode.indirection[k] != 0) {
                            for (int l = 0; l < BSIZE / 16; l++) {
                                Byte16 file = new Byte16();
                                dirent tmpdirent = new dirent();
                                file.set(img[dinode.indirection[k]].block, l * 16);
                                file.setNum();
                                tmpdirent.set(file);
                                if (file_num == 0) {
                                    continue;
                                } else if(!tmpdirent.name.equals("")){
                                    if(dinodes[tmpdirent.inum].type == T_DIR)
                                        System.out.println(tmpdirent.name + " " + dinodes[tmpdirent.inum].type + " "
                                                + tmpdirent.inum + " " + dinodes[tmpdirent.inum].size + " "
                                                + dinodes[tmpdirent.inum].nlink + " " + dinodes[tmpdirent.inum].parentInode);
                                    file_num--;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(dinode.type != T_DIR && dinode.type != 0 && findFileName(img, dinodes, i) != null) {
            System.out.println(findFileName(img, dinodes, i) + " " + dinode.type + " " + i + " " + dinode.size
                    + " " + dinode.nlink + " " + dinode.parentInode);
        }
    }

    public void execute( File img_file ) {
        // ここにやりたい処理を書く
        System.out.println("------------------------------ " + img_file.getPath() + " --------------------------------");
//        File img_file = new File(file);
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
//                if(i == 32)
//                System.out.println(Arrays.toString(img[i].block));
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
                String[] tmp = new String[8];
                for(int i=0; i<8; i++){
                    tmp[i] = String.format("%d", (b & 0x80)/0x80);
                    b <<= 1;
                }
                for(int i=7; i >= 0; i--)
                    val += tmp[i];
            }
//            System.out.println(val);
            char[] bitmap = val.toCharArray();
            char[] copybitmap = val.toCharArray();
//            System.out.println(Arrays.toString(bitmap));

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
            /*                                          および                                           */
            /*                                    ディレクトリ情報の抽出                                   */
            /********************************************************************************************/

            int[] inode_num = new int[26*8]; // 各inodeの参照数
            int inodeError = 0;
            int bitmapError = 0;
            int dirError = 0;
            int bcount;
            int file_num;
            for(int i=0; i < 8*(57-31); i++) {
                file_num = dinodes[i].size/16;
                bcount = 0;
                for(int j=0; j < 13; j++) {
                    /* direct */
                    if(dinodes[i].addrs[j] != 0) {
                        if(j!=NDIRECT){
                            if(dinodes[i].type == T_DIR) { // direct 参照先のディレクトリのリンク確認
                                for (int k=0; k < BSIZE/16; k++) { // nlinkのカウント
                                    Byte16 file = new Byte16();
                                    dirent tmpdirent = new dirent();
                                    file.set(img[dinodes[i].addrs[j]].block, k*16);
                                    file.setNum();
                                    tmpdirent.set(file);
                                    if(file_num == 0 || tmpdirent.name.equals("")) {
                                        continue;
                                    } else {
                                        if(!tmpdirent.name.equals(".")) { // "."についてはカウントしない
                                            inode_num[tmpdirent.inum] += 1;
                                        }
                                        dinodes[i].children.add(dinodes[tmpdirent.inum]); // 子の登録
                                        dinodes[tmpdirent.inum].parentInode = i; // 親の登録
                                        if(i==1 && tmpdirent.name.equals("..") && tmpdirent.inum !=1) { // ルートディレクトリにおける".."
                                            System.out.println("ルートディレクトリの\"..\"が自分自身を指していません(inode番号:" +
                                                    + tmpdirent.inum + "を指しています)");
                                            dirError++;
                                        }
                                        if(i != 0 && dinodes[tmpdirent.inum].type == 0) {
                                            System.out.println("ディレクトリが参照しているのは正しいinode番号ではありません(inode番号:"
                                                     + tmpdirent.inum + "は未使用)");
                                            dirError++;
                                        }
                                        file_num--;
                                    }
                                }
                            }
                            bcount++;
                        }
                        if(copybitmap[dinodes[i].addrs[j]] == '1'){ // １つ目発見で0にする
                            copybitmap[dinodes[i].addrs[j]] ='0';
                        } else if(copybitmap[dinodes[i].addrs[j]] == '0') { // 参照しているのに0なら
                            if(bitmap[dinodes[i].addrs[j]] == '0') {
                                System.out.println("未使用ブロックを参照しています(inode番号:" + i
                                        + " 直接参照番号:" + j + " ブロック番号:" + dinodes[i].addrs[j] + ")");
                            } else {
                                System.out.println("１つのデータブロックに複数の参照があります(ブロック番号:"
                                        + dinodes[i].addrs[j] + ")");
                            }
                            inodeError++;
                            bitmapError++;
                        }
                    }

                    /* indirect */
                    if(j==NDIRECT && dinodes[i].addrs[j] != 0) {
                        for(int k=0; k < 128; k++) {
                            if(dinodes[i].indirection[k] != 0) {
                                if(dinodes[i].type == T_DIR) { // direct 参照先のディレクトリのリンク確認
                                    for (int l=0; l < BSIZE/16; l++) {
                                        Byte16 file = new Byte16();
                                        dirent tmpdirent = new dirent();
                                        file.set(img[dinodes[i].indirection[k]].block, l*16);
                                        file.setNum();
                                        tmpdirent.set(file);
                                        if(file_num == 0 || tmpdirent.name.equals("")) {
                                            continue;
                                        } else {
                                            if(!tmpdirent.name.equals(".")) { // "."についてはカウントしない
                                                inode_num[tmpdirent.inum] += 1;
                                            }
                                            dinodes[i].children.add(dinodes[tmpdirent.inum]); // 子の登録
                                            dinodes[tmpdirent.inum].parentInode = i; // 親の登録
                                            if(i==1 && tmpdirent.name.equals("..") && tmpdirent.inum !=1) { // ルートディレクトリにおける".."
                                                System.out.println("ルートディレクトリの\"..\"が自分自身を指していません(inode番号:" +
                                                        + tmpdirent.inum + "を指しています)");
                                                dirError++;
                                            }
                                            if(i != 0 && dinodes[tmpdirent.inum].type == 0) {
                                                System.out.println("ディレクトリが参照しているのは正しいinode番号ではありません(inode番号:"
                                                        + tmpdirent.inum + "は未使用)");
                                                dirError++;
                                            }
                                            file_num--;
                                        }
                                    }
                                }
                                bcount++;
                                if(copybitmap[dinodes[i].indirection[k]] == '1') { // １つ目発見で0にする
                                    copybitmap[dinodes[i].indirection[k]] = '0';
                                } else if(copybitmap[dinodes[i].indirection[k]] == '0') { // 参照しているのに0なら
                                    if(bitmap[dinodes[i].indirection[k]] == '0') {
                                        System.out.println("未使用ブロックを参照しています(inode番号:" + i
                                                + " 間接参照番号:" + k + " ブロック番号:" + dinodes[i].indirection[k] + ")");
                                    } else {
                                        System.out.println("１つのデータブロックに複数の参照があります(ブロック番号:"
                                                + dinodes[i].indirection[k] + ")");
                                    }
                                    inodeError++;
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
                    System.out.println("参照データブロック数が正しくありません(inode番号:" + i
                            + " 参照数:" + bcount + " size/BSIZE:" + truebcount + ")");
                    inodeError++;
                }
            }


            /* bitmapが1なのに参照されていない */
            for(int i=59; i < 1000; i++) {
                if(copybitmap[i] == '1') {
                    System.out.println("ビットマップでは使用済みのブロックが参照されていません(BLOCK番号:" + i + ")");
                    bitmapError++;
                }
            }



            /********************************************************************************************/
            /*                                 inodeに関する一貫性の確認                                   */
            /********************************************************************************************/

            for(int i=0; i < 8*(57-31); i++) {
                if(dinodes[i].type == T_DEV) { // 正しいファイルタイプか
                    if(dinodes[i].major == 0 || dinodes[i].minor == 0){
                        System.out.println("正しいファイルタイプではありません(inode番号:" + i + ")");
                        inodeError++;
                    }
                }

                // nlinkが各ディレクトリからの正しい総参照数に待っているか(0番目のinode(先頭)は除く)
                if(dinodes[i].type != 0 && inode_num[i] != dinodes[i].nlink) {
                    System.out.println("nlinkの値と各ディレクトリからの総参照数が一致しません(inode番号:"
                            + i + " nlink:" + dinodes[i].nlink + " 総参照数:" + inode_num[i] + ")");
                    System.out.println("\".\"による自分自身の参照がカウントされている可能性があります");
                    inodeError++;
                    dirError++;
                }
            }

            System.out.println();

            if(bitmapError == 0) {
                System.out.println("blockの使用状況に関する一貫性:OK");
            } else {
                System.out.println("blockの使用状況に関する一貫性:ERROR (" + bitmapError + " errors)");
            }


            if(inodeError == 0) {
                System.out.println("inodeに関する一貫性:OK");
            } else {
                System.out.println("inodeに関する一貫性:ERROR (" + inodeError + " errors)");
            }

            if(dirError == 0) {
                System.out.println("ディレクトリに関する一貫性:OK");
            } else {
                System.out.println("ディレクトリに関する一貫性:ERROR (" + dirError + " errors)");
            }

            for(int i=0; i < 8*26; i++) {
                showfiles(img, dinodes[i], i, dinodes);
            }

            // 各dinode(ディレクトリタイプのみ)の子ファイルリスト
//            for(int i=0; i < 8*26; i++) {
//                if(dinodes[i].children.size() > 0) {
//                    System.out.print("inode:" + i);
//                    for (dinode dinode : dinodes[i].children) {
//                        System.out.print(" " + findFileName(img, dinodes, geti(dinode, dinodes)));
//                    }
//                    System.out.println();
//                }
//            }


            img_fd.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(EXIT_FAILURE);
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println();
    }

    public static void main(String args[]) {
        if (args.length <= 0) {
            System.err.println("ERROR: input file name.");
            System.exit(EXIT_FAILURE);
        }

        new Main().readFolder( new File(args[0]) );

    }
}
