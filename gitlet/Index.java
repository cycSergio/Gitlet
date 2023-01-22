package gitlet;
import java.io.Serializable;

/** Index is an object that should be used in Staging Area.
 *  Staging Area is a HashMap.
 *  For command "add [filename]", an index in Staging Area will be created.
 *  An index consists of [filename] and the SHA-1 value of the content of
 *  the file.
 * */

public class Index implements Serializable {
    private String fileName;
    private String blobSHA1;

    public Index(String fileName, String blobSHA1) {
        this.fileName = fileName;
        this.blobSHA1 = blobSHA1;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getBlobSHA1() {
        return this.blobSHA1;
    }
}
