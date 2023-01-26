package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author cyc
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */
    public static final File blobs = join(GITLET_DIR, "blobs");
    public static final File commits = join(GITLET_DIR, "commits");
    public static final File Staging_Area = join(GITLET_DIR, "index");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File master = join(GITLET_DIR, "master");

    /**
     * TODO: create all the rest of the things in the .gitlet that we need
     * TODO: somehow make the initial commit by using some constructor in the Commit Class and serialize it
     * TODO: somehow fail if a Gitlet version-control system exists in the current directory
     */
    public static void initCommand() { // why static? cause I want to be able to call this method, without newing
        // an object of Repository. And "static" enables this.
        if (GITLET_DIR.exists()) { // If a Gitlet already exists, then fail
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir(); // actually create a .git/ directory
        blobs.mkdirs();
        commits.mkdirs();

        HashMap<String, String> StagingIndex = new HashMap<>();
        try {
            Staging_Area.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(Staging_Area, StagingIndex); // I want to make Staging Area a HashMap for indexes.

        Commit initialCommit = new Commit();
        File initialCommitFile = join(commits, initialCommit.getCommitSHA1()); // TODO: style?
        try {
            initialCommitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(initialCommitFile, initialCommit);

        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            master.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeContents(HEAD, initialCommit.getCommitSHA1());
        Utils.writeContents(master, initialCommit.getCommitSHA1());
    }

    /**
     * add a copy of the file to the staging area. Not sure about "a copy of the file"?
     * Anyway, first a corresponding blob should be made
     * then an entry be made into the staging area. An entry consist of the file name
     * and the reference to the blob.
     * TODO: from String "addFile", get the actual file that named addFile
     * TODO: addFile should be transferred to a blob and stored in .git/blobs
     * TODO: add an entry in the index
     */
    public static void addCommand(String addFile) {
        File FileToAdd = join(CWD, addFile); // TODO: not sure, to be confirmed
        HashMap<String, String> curComTrackings = curTrackings();
        HashMap<String, String> curSA = getSA();
        if (!FileToAdd.exists()) {
            if (!curComTrackings.containsKey(addFile)) { // This means it's not the case that
                // the user staged this file for removal because the current commit doesn't hava
                // this key of filename.
                Utils.message("File does not exist."); // This is the failure cases, meaning that
                // the user is trying to stage a nonexistent file for addition.
                return;
            }
            else {
                Index removalIndex = new Index(addFile);
                if (curSA.containsKey(addFile)) {
                    curSA.replace(addFile, removalIndex.getBlobSHA1());
                    return;
                } else {
                    curSA.put(removalIndex.getFileName(), removalIndex.getBlobSHA1()); // key: filename, value: sha1
                }
                writeObject(Staging_Area, curSA);
                return;
            }
        }
        byte[] addFileContents = readContents(FileToAdd); // TODO: or read as String?

        String addFileSha1 = Utils.sha1(stagedFileNameContent(addFile));
        if (curComTrackings.containsKey(addFile) && curComTrackings.get(addFile) == addFileSha1) {
            if (curSA.containsKey(addFile)) {
                curSA.remove(addFile);
            }
            return;
        }  // TODO: this can be written into a helper method later.


        /* Creates a corresponding blob object and serialize it. */
        Blob fileBlob = new Blob(addFileContents);
        File thisBlob = join(blobs, addFileSha1);
        try {
            thisBlob.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(thisBlob, fileBlob);

        /* Creates the corresponding index in the Staging Area. */
        Index thisIndex = new Index(addFile, addFileSha1);
        if (curSA.containsKey(addFile)) {
            curSA.replace(addFile, addFileSha1);
            return;
        } else {
            curSA.put(thisIndex.getFileName(), thisIndex.getBlobSHA1()); // key: filename, value: sha1
        }
        writeObject(Staging_Area, curSA);
    }

    /** A helper method for add.
     *  This method gets the current commit's tracking files, so that I can
     *  compare those references with that the user gives and decide whether
     *  the user's command is valid.
     */
    private static HashMap<String, String> curTrackings() {
        return getCurCommit().getFileToBlob();
    }

    /* A helper method to get the content of the staging files.
       eg: add a.txt
       then this method gets the content of the a.txt from the CWD.
    */
    private static String stagedFileNameContent(String filename) {
        File stageFile = join(CWD, filename);
        String content = readContentsAsString(stageFile);
        return content;
    }

    /** A helper method for add.
     *  Check whether the user's add command is valid.
     *  the first argument is the curTrackings, and the second is the user's operand.
     *  If the result is false,
     */
    private static boolean checkAdd(HashMap<String, String> curTrackings, String operand) {
        String opContent = stagedFileNameContent(operand);
        String opSha1 = Utils.sha1(opContent);
        if (curTrackings.containsKey(operand)) {
            if (curTrackings.get(operand) == opSha1) {
                return false; // meaning that the current working version of the file
                // is identical to the version in the current commit, do not stage it
                // to be added, and remove it from the staging area if it is already there
            }
        }
        HashMap<String, String> curSA = getSA();
        if (curSA.containsKey(operand)) {
            curSA.replace(operand, opSha1); // overwrites SA for the same filename
            return true;
        }
        return true;
    }

    /* A helper method to get the current indexes from the Staging Area. */
    private static HashMap<String, String> getSA() {
        return readObject(Staging_Area, HashMap.class);
    }


    /**
     * The spec says "the commit tree". Is there actually a tree structure?
     * The thing is I have to serialize commits, and the pointers should be
     * Strings or huge memories would be used.
     *  TODO: include everything in the staging area in the next commit
     *  TODO: serialize the new-made commit into .gitlet/commits/
     *  TODO: clear the staging area
     */
    public static void commitCommand(String message) {
        String parSha1 = getCurCommitSha1();
        Commit parCom = getCurCommit();
        HashMap<String, String> curIndexes = getSA();
        HashMap<String, String> curComIndexes = buildIndexes(parCom.getFileToBlob(), curIndexes);
        Commit curCommit = new Commit(message, parSha1, curComIndexes);

        File newCommit = join(commits, curCommit.getCommitSHA1());
        try {
            newCommit.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(newCommit, curCommit);

        // clear the staging area after each commit
        curIndexes.clear();
        writeObject(Staging_Area, curIndexes);
        // move HEAD and master pointers
        String HEADp = readContentsAsString(HEAD);
        String mstp = readContentsAsString(master);
        HEADp = curCommit.getCommitSHA1();
        mstp = curCommit.getCommitSHA1();
        writeContents(HEAD, HEADp);
        writeContents(master, mstp);
    }

    /* A helper method to get the current commit's sha1. */
    private static String getCurCommitSha1() {
        return readContentsAsString(HEAD);
    }

    /* A helper method to get the current commit.*/
    private static Commit getCurCommit() {
        String curComSha1 = getCurCommitSha1();
        File curComPath = join(commits, curComSha1);
        Commit curCom = readObject(curComPath, Commit.class); // Should it be converted into inline variable?
        return curCom;
    }


    /* A helper method for commitCommand to correctly get all the tracked files
     *  from the staging area.
     */  // TODO: how to write comments properly? Style
    private static HashMap<String, String> buildIndexes(HashMap<String, String>parentHM, HashMap<String, String>SA) {
        SA.forEach((filename, sha1) -> {
            File filePath = join(CWD, filename);
            parentHM.putIfAbsent(filename, sha1);
            if (parentHM.containsKey(filename) && (!filePath.exists())) {
                parentHM.remove(filename); // this is the removal case
            } else if (parentHM.containsKey(filename)) {
                parentHM.replace(filename, sha1);
            }
                });
        return parentHM;
    }

    /* A helper method to get a commit by its sha1 value. */
    private static Commit getComBySha1(String sha1) {
        File targetComPath = join(commits, sha1);
        return readObject(targetComPath, Commit.class);
    }

    /* Starting at the current head commit, display information about each
    *  commit backwards along the commit tree until the initial commit,
    *  following the first parent commit links, ignoring any second parents
    *  found in merge commits.
    *  TODO: the merge case is not implemented yet. */
    public static void logCommand() {
        Commit curCommit = getCurCommit();
        while (true) {
            logMessage(curCommit);
            // the ending point
            if (Objects.equals(curCommit.getMessage(), "initial commit")) {
                break;
            }
            String parSha1 = curCommit.getParent();
            curCommit = getComBySha1(parSha1);
        }

    }

    /* A helper method for logCommand. */
    private static void logMessage(Commit curCommit) {
        /* log eg.
           ===
           commit dc4b18a1861b3dfda6b9e9be628f588df6cbf484
           Data: Thu Nov 9 20:00:00 2017 -0800
           A commit message.

         */
        StringBuilder logMes = new StringBuilder();
        logMes.append("===").append("\n");
        String comTime = curCommit.getFormattedTime();
        String comMes = curCommit.getMessage();
        logMes.append("commit ").append(curCommit.getCommitSHA1()).append("\n");
        logMes.append("Date: ").append(comTime).append("\n");
        logMes.append(comMes).append("\n");
        System.out.println(logMes);
        // TODO: merge them into one method?
    }

    /* The first usage of checkout command.
    *  Takes the version of the file as it exists in the head commit and puts
   *  it in the working directory, overwriting the version of the file that's
   *  already there if there is one. The new version of the file is not staged.*/
    public static void checkout(String filename) {
        Commit curCom = getCurCommit();
        String targetSha1 = curCom.getFileToBlob().get(filename);
        Blob targetBlob = readObject(join(blobs, targetSha1), Blob.class);
        byte[] targetContent = targetBlob.getFileContent();
        File targetFile = join(CWD, filename);
        writeContents(targetFile, targetContent);
    }

    /* Takes the version of the file as it exists in the commit with the given
    *  id, and puts it in the working directory, overwriting the version of the
    *  file that's already there if there is one. The new version of the file
    *  is not staged.
    *  // TODO:
    *  //
    *  */
    public static void checkout(String commitId, String filename) {

    }

}
