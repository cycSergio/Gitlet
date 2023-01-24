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

    /* Constructor for addition. */
    public Index(String fileName, String blobSHA1) {
        this.fileName = fileName;
        this.blobSHA1 = blobSHA1;
    }

    /* Constructor for removal. */
    public Index(String fileName) {
        this.fileName = fileName;
        this.blobSHA1 = null; // TODO: is null a proper value for my purpose??
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getBlobSHA1() {
        return this.blobSHA1;
    }
}
