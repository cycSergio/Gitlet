package gitlet;

import java.io.File;
import java.io.IOException;

public class MyUtils {

    /* A helper method to implement filePath.createNewFile() . */
    static void createFile(File filePath) {
        try {
            filePath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
