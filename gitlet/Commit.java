package gitlet;

import java.io.Serializable;
import java.util.*;
import java.text.SimpleDateFormat;


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
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;

    /** The time at which this Commit is created. */
    private final Date timestamp;

    /** The mapping of file names to blob references for this commit. */
    private final HashMap<String, String> fileToBlob;
    // TODO: fileToBlob or indexes?? you need to uniform the rules

    /** The parent of this commit. */
    private final ArrayList<String> parent = new ArrayList<>();


    /** The constructor for an initial commit. */
    public Commit() {
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.fileToBlob = new HashMap<>(); // initial commit has an empty tracking HashMap
        this.parent.add(0, "0"); // initial commit has no parent, place a "0" here to avoid nullExp
    }

    /* The constructor for general commits. */
    public Commit(String message, String parent, HashMap<String, String> fileToBlob) {
        this.message = message;
        this.parent.add(0, parent); // the first parent
        this.fileToBlob = fileToBlob;
        this.timestamp = new Date(); // this supposed to be the current time
    }

    /** the constructor for merge commit */
    public Commit(String message, String parent, String parent2, HashMap<String, String> fileToBlob) {
        this.message = message;
        this.parent.add(0, parent); // the first parent
        this.parent.add(1, parent2); // the second parent
        this.fileToBlob = fileToBlob;
        this.timestamp = new Date();
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
        String strTrackings = this.fileToBlob.toString();
        String strTime = this.timestamp.toString();
        return Utils.sha1(this.message, this.parent.toString(), strTime, strTrackings);
   } // TODO: remember this!!!!!!! why you fail on merge-parent2!!!!!!!!

     public HashMap<String, String> getFileToBlob() {
       return this.fileToBlob;
   }

     public String getFirstParent() {
       return this.parent.get(0);
   }

    public String getSecondParent() {
        return this.parent.get(1);
   }

   public String getShortFirstParent() {
       return getFirstParent().substring(0, 7);
   }

    public String getShortSecondParent() {
        return getSecondParent().substring(0, 7);
    }

    public void addSecondParent(String secondParentId) {
         this.parent.add(1, secondParentId);
    }

    public int sizeOfParent() {
         return this.parent.size();
    }

    public String getMessage() {
       return this.message;
   }

    public String getFormattedTime() {
       // Thu Nov 9 20:00:00 2017 -0800
        SimpleDateFormat formatTime = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return formatTime.format(this.timestamp);
   }

   /* help checkout command to restore the target file
   *  from the HEAD commit. */
//    public void restoreTargetFile(String filename) {
//       // TODO: tbc, implemented in Repository just for now
//   }

}
