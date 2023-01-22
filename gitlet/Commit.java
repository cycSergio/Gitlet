package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  What this Class does at a high level:
 *  Commit saves a snapshot of tracked files in the staging area.
 *  By default, a commit has the same file contents as its parent. Files
 *  staged for addition and removal are the updates to the commit. Of
 *  course, the date (and likely the message) will also different from the
 *  parent.
 *  Files tracked in the current commit may be untracked in the new commit
 *  as a result being staged for removal by the `rm` command.
 *
 *  A commit, will consist of a log message, timestamp, a mapping of file
 *  names to blob references, a parent reference, and (for merges) a second
 *  parent reference.
 *
 *  @author cyc
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The time at which this Commit is created. */
    private Date timestamp;

    /** The mapping of file names to blob references for this commit. */
    private HashMap<String, String> fileToBlob;
    // TODO: fileToBlob or indexes?? 记得要统一命名逻辑鸭，应该让人一看就知道是干嘛的

    /** The parent of this commit. */
    private String parent;




    /* TODO: fill in the rest of this class. */
    /** The constructor for an initial commit. */
    public Commit() { // This is public because I want to new an object of this outside the Commit Class.
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.fileToBlob = new HashMap<>(); // initial commit has an empty tracking HashMap
        this.parent = null; // TODO: 需要给initial commit赋一个null值吗？？
    }

    /* The constructor for general commits. */
    /** how to write this constructor?
     *  TODO: how to link a commit to its parent?
     *  TODO: modify the former constructor for initial commit
     *
     * */
    public Commit(String message, String parent, HashMap<String, String>fileToBlob) { // TODO: how do I receive this message argument from my users?
        this.message = message;
        this.parent = parent;
        this.fileToBlob = fileToBlob;
        // TODO: how to retrieve the Date???
    }

    /** the hashing method for this commit
     *  If two commits have the same SHA1-id, it means they have the same
     *  metadata, the same mapping of names to references, and the same
     *  parent reference.
     *  Including all metadata and references when hashing a commit.
     *  Distinguishing somehow between hashes for commits and hashes for blobs.
     *  A good way of doing this involves a well-thought directory structure
     *  within the .gitlet directory. Another way to do so is to hash in an
     *  extra word for each object the has one value for blobs and another for
     *  commits.
     * */
   public String getCommitSHA1() {
       String sha1 = Utils.sha1(this.message, this.timestamp, this.fileToBlob, this.parent);
       // TODO: which sha1 methond should I use? I have no idea.....
       return sha1;
   }

   public HashMap<String, String> getFileToBlob() {
       return this.fileToBlob;
   }

}
