package gitlet;
import java.io.Serializable;

/** Represents a gitlet blob object.
 * Blobs are the saved contents of tracked files. Since Gitlet
 * saves many versions of files, a single file might correspond
 * to multiple blobs: each being tracked in a different commit.
 *
 */

public class Blob implements Serializable{

    private byte[] fileContent;

    /* the Blob constructor */
    public Blob(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getSHA1() {
        return Utils.sha1((Object) fileContent);
    }

    public byte[] getFileContent() {
        return this.fileContent;
    }
}
