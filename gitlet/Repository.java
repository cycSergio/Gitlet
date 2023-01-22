package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.HashMap;

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
     * <p>
     * .gitlet
     * -- COMMIT_EDITMSG
     * -- HEAD
     * -- config
     * -- description
     * -- index
     * -- hooks/
     * -- info/
     * -- objects/
     * -- logs/
     * -- refs/
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
        Utils.writeContents(Staging_Area, Staging_Area); // I want to make Staging Area a HashMap for indexes.

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
        byte[] addFileContents = readContents(FileToAdd);
        /* Creates a corresponding blob object and serialize it. */
        Blob fileBlob = new Blob(addFileContents);
        String blobSHA1 = fileBlob.getSHA1();
        File thisBlob = join(blobs, blobSHA1);
        try {
            thisBlob.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(thisBlob, fileBlob);

        /* Creates the corresponding index in the Staging Area. */
        Index thisIndex = new Index(addFile, blobSHA1);
        HashMap<String, String> indexHM = Utils.readObject(Staging_Area, HashMap.class); // indexHM == StagingIndex
        indexHM.put(thisIndex.getFileName(), thisIndex.getBlobSHA1());
        writeObject(Staging_Area, indexHM);
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
        String parent = readObject(HEAD, String.class); // TODO: 每个文件是什么数据类型，这些都要写清楚鸭！
        File parFile = join(commits, parent);
        Commit parCom = readObject(parFile, Commit.class);

        HashMap<String, String> curIndexes = readObject(Staging_Area, HashMap.class);
        HashMap<String, String> curComIndexes = getIndexes(parCom.getFileToBlob(), curIndexes);
        Commit curCommit = new Commit(message, parent, curComIndexes);
    }


    /* A helper method for commitCommand to correctly get all the tracked files
     *  from the staging area.
     */  // TODO: 写注释的规范是什么？指格式上而不是内容上。
    private static HashMap<String, String> getIndexes(HashMap<String, String>parentHM, HashMap<String, String>SA) {
        SA.forEach((filename, sha1) -> {
            parentHM.putIfAbsent(filename, sha1);
            if (parentHM.containsKey(filename)) {
                parentHM.replace(filename, sha1);  // TODO: staged for addition/removal is not implemented yet
            }
                });
        return parentHM;
    }

}
