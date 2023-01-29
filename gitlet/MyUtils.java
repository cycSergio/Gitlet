package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class MyUtils {

    /* A helper method to implement filePath.createNewFile() . */
    static void createFile(File filePath) {
        try {
            filePath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Get the content of a file from CWD. */
    static byte[] getFileContent(String filename) {
        File filePath = Utils.join(Repository.CWD, filename);
        return Utils.readContents(filePath);
    }

    /* Get the content as String of a file from CWD. */
    static String getFileContentAsString(String filename) {
        File filePath = Utils.join(Repository.CWD, filename);
        return Utils.readContentsAsString(filePath);
    }

    /* Create a new file and serialize it by the Utils.writeObject command. */
    static void createAndWriteObject(File filePath, Serializable content) {
        createFile(filePath);
        Utils.writeObject(filePath, content);
    }
}
