package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static gitlet.Utils.*; // TODO: figure out what's import static?
import static gitlet.MyUtils.*;


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
    public static final File BRANCH = join(GITLET_DIR, "Branch_heads");
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    /**
     * Create all the rest of the things in the .gitlet that we need.
     * Make the initial commit by using some constructor in the Commit Class and serialize it.
     * Handle the failure case: if a Gitlet version-control system already exists.
     */
    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdir();
        blobs.mkdirs();
        commits.mkdirs();

        HashMap<String, String> StagingIndex = new HashMap<>();
        MyUtils.createAndWriteObject(Staging_Area, StagingIndex);

        Commit initialCommit = new Commit();
        File initialCommitFile = join(commits, initialCommit.getCommitSHA1());
        MyUtils.createAndWriteObject(initialCommitFile, initialCommit);

        BRANCH.mkdirs();
        Branch default_master = new Branch(initialCommit.getCommitSHA1());
        MyUtils.createAndWriteObject(join(BRANCH, default_master.getBranchName()), default_master);
        MyUtils.createFile(HEAD);
        Utils.writeContents(HEAD, default_master.getBranchName()); // TODO: to be confirmed at Path storage
    }

    /* Stage one file for removal. This is part of the add command function. */
    private static void add4removal(String addFile) {
        File FileToAdd = join(CWD, addFile); // TODO: not sure, to be confirmed
        HashMap<String, String> curComTrackings = getCurTrackings();
        HashMap<String, String> curSA = getSA();
        if (!FileToAdd.exists()) {
            if (!curComTrackings.containsKey(addFile)) {
                Utils.message("File does not exist."); // failure case, meaning that
                // the user is trying to stage a nonexistent file for addition.
            }
            else { // addFile is staged for removal
                Index removalIndex = new Index(addFile);
                curSA.put(removalIndex.getFileName(), removalIndex.getBlobSHA1()); // key: filename, value: sha1
                writeObject(Staging_Area, curSA);
            }
            return;
        }
    }

    /**
     * add a copy of the file to the staging area.
     * TODO: from String "addFile", get the actual file that named addFile
     * TODO: addFile should be transferred to a blob and stored in .git/blobs
     * TODO: add an entry in the index
     */
    public static void addCommand(String addFile) {
        File FileToAdd = join(CWD, addFile); // TODO: not sure, to be confirmed
        HashMap<String, String> curComTrackings = getCurTrackings();
        HashMap<String, String> curSA = getSA();
        if (!FileToAdd.exists()) {
            if (!curComTrackings.containsKey(addFile)) {
                Utils.message("File does not exist."); // failure case, meaning that
                // the user is trying to stage a nonexistent file for addition.
            }
            else { // addFile is staged for removal
                Index removalIndex = new Index(addFile);
                curSA.put(removalIndex.getFileName(), removalIndex.getBlobSHA1()); // key: filename, value: sha1
                writeObject(Staging_Area, curSA);
            }
            return;
        }


        byte[] addFileContents = readContents(FileToAdd); // TODO: or read as String?
        String addFileSha1 = Utils.sha1(MyUtils.getFileContentAsString(addFile));
        // the case that current commit has the same file content as the staged one
        if (curComTrackings.containsKey(addFile) && Objects.equals(curComTrackings.get(addFile), addFileSha1)) {
            curSA.remove(addFile); // If this hashmap doesn't contain this key, it just returns null
            writeObject(Staging_Area, curSA);
            return;
        }

        /* Creates a corresponding blob object and serialize it. */
        Blob fileBlob = new Blob(addFileContents);
        File thisBlob = join(blobs, addFileSha1);
        MyUtils.createAndWriteObject(thisBlob, fileBlob);

        /* Creates the corresponding index in the Staging Area. */
        Index thisIndex = new Index(addFile, addFileSha1);
        curSA.put(thisIndex.getFileName(), thisIndex.getBlobSHA1()); // put method: if curSA.containsKey(addFile),
        // it will act just like curSA.replace(addFile, addFileSha1).
        writeObject(Staging_Area, curSA);
    }

    /** A helper method for add.
     *  This method gets the current commit's tracking files, so that I can
     *  compare those references with that the user gives and decide whether
     *  the user's command is valid.
     */
    private static HashMap<String, String> getCurTrackings() {
        return getCurCommit().getFileToBlob();
    }

    /* A helper method to get the current indexes from the Staging Area. */
    private static HashMap<String, String> getSA() {
        return readObject(Staging_Area, HashMap.class);
    }

    /**
     * The spec says "the commit tree". Is there actually a tree structure?
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
        MyUtils.createAndWriteObject(newCommit, curCommit);

        // clear the staging area after each commit
        curIndexes.clear();
        writeObject(Staging_Area, curIndexes);
        // move HEAD and master pointers
        Branch curBranch = readObject(join(BRANCH, getHEAD()), Branch.class); // TODO: change join
        curBranch.move(curCommit.getCommitSHA1());
        writeObject(join(BRANCH, getHEAD()), curBranch);
    }

    /* A helper method to get the HEAD's content. */
    private static String getHEAD() {
        return readContentsAsString(HEAD);
    }

    /* A helper method to get the current commit's sha1. */
    private static String getCurCommitSha1() {
        String curHead = readContentsAsString(HEAD);
        File curBranchPath = join(BRANCH, curHead);
        return readObject(curBranchPath, Branch.class).getBranchCommitSha1();
    }

    /* A helper method to get the current commit.*/
    private static Commit getCurCommit() {
        String curComSha1 = getCurCommitSha1();
        File curComPath = join(commits, curComSha1);
        return readObject(curComPath, Commit.class);
    }

    /* A helper method for commitCommand to correctly get all the tracked files
     *  from the staging area.
     */
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

    /* Displays information about all commits ever made. The order of the commits
       does not matter.
    */
    // TODO: to be tested
    public static void globalLog() {
        List<String> allCommitsSha1 = plainFilenamesIn(commits);
        assert allCommitsSha1 != null;
        for (String commitSha1: allCommitsSha1) {
            logMessage(getComBySha1(commitSha1));
        }
    }

    /* The first usage of checkout command.
    *  Takes the version of the file as it exists in the head commit and puts
   *  it in the working directory, overwriting the version of the file that's
   *  already there if there is one. The new version of the file is not staged.*/
    public static void checkout(String filename) {
        String targetSha1 = getCurCommit().getFileToBlob().get(filename);
        Blob targetBlob = readObject(join(blobs, targetSha1), Blob.class);
        byte[] targetContent = targetBlob.getFileContent();
        File targetFile = join(CWD, filename);
        writeContents(targetFile, targetContent);
    }

    /* Takes the version of the file as it exists in the commit with the given
    *  id, and puts it in the working directory, overwriting the version of the
    *  file that's already there if there is one. The new version of the file
    *  is not staged.
    *  // TODO: the spec says it should also support abbreviation of commit id,
    *      as in real git. How?
    *  */
    public static void checkout(String commitId, String filename) {
        // TODO: needs refactor, as it's the same as the first checkout command.
        String targetSha1 = getComBySha1(commitId).getFileToBlob().get(filename);
        Blob targetBlob = readObject(join(blobs, targetSha1), Blob.class);
        byte[] targetContent = targetBlob.getFileContent();
        File targetFile = join(CWD, filename);
        writeContents(targetFile, targetContent);
    }

    /* Creates a new branch with the given name, and points it at the current head
     *  commit. This command does not immediately switch to the newly created branch.
     */
    // TODO: to be tested
    public static void branch(String branchName) {
        List<String> allBranches = Utils.plainFilenamesIn(BRANCH);
        assert allBranches != null;
        for (String branch:allBranches) {
            if (branch.equals(branchName)) {
                message("A branch with that name already exists.");
            }
        }
        Branch newBranch = new Branch(branchName, getCurCommitSha1());
        newBranch.branchWrite(); // TODO: to be confirmed
    }

    /* Unstage the file if it is currently staged for addition. If the file is tracked
       in the current commit, stage it for removal and remove the file from the CWD if
       the user has not already done so (do not remove it unless it is tracked in the
       current commit).
     */
    // TODO: to be tested
    public static void rm(String filename) {
        HashMap<String, String> curSA = getSA();
        HashMap<String, String> curCommitTracking = getCurTrackings();
        boolean staged4add = curSA.containsKey(filename) && curSA.get(filename) != null;
        boolean inCurCom = curCommitTracking.containsKey(filename);
        if (staged4add) {
            curSA.remove(filename);
            writeObject(Staging_Area, curSA);
        }
        if (curCommitTracking.containsKey(filename)) {
            if (join(CWD, filename).exists()) {
                restrictedDelete(join(CWD, filename)); // TODO: to be confirmed and refactor
            }
            addCommand(filename);
        }
        if ((!staged4add) && (!inCurCom)) {
            message("No reason to remove the file.");
        }
    }

    /* Prints out the ids of all commits that have the given commit message, one per
       line. If there are multiple such commits, it prints the ids out on separate
       lines.
     */
    // TODO: to be tested
    public static void find(String message) {
        List<String> allCommitsSha1 = plainFilenamesIn(commits);
        if (allCommitsSha1 == null) {
            message("Found no commit with that message.");
        }
        boolean found = false;
        for (String commitSha1: allCommitsSha1) {
            Commit curCommit = getComBySha1(commitSha1);
            if (Objects.equals(curCommit.getMessage(), message)) {
                System.out.println(commitSha1);
                found = true;
            }
        }
        if (!found) {
            message("Found no commit with that message.");
        }
    }

    /* Displays what branches currently exist, and marks the current branch with
    *  a '*'. Also displays what files have been staged for addition or removal.
    *  Entries should be listed in lexicographic order, using the Java
    *  string-comparison order (the asterisk doesn’t count).
    * === Branches ===
    * *master
    * other-branch
    *
    * === Staged Files ===
    * wug.txt
    *
    * === Removed Files ===
    * goodbye.txt
    *
    */
    // TODO: not sure about removed, to be checked
    // TODO: to be tested
    public static void status() {
        // list all branches and specify the active one
        System.out.println("=== Branches ===");
        List<String> allBranches = plainFilenamesIn(BRANCH);
        String activeBranch = getHEAD();
        for (String branch:allBranches) {
            if (Objects.equals(branch, activeBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();
        // then list all the staged files
        System.out.println("=== Staged files ===");
        HashMap<String, String> curSA = getSA(); // TODO: sort this list!
        for (String filename:curSA.keySet()) {
            if (curSA.get(filename) != null) {
                System.out.println(filename);
            }
        }
        System.out.println();

        // then list all the removed files
        System.out.println("=== Removed Files ===");
        for (String filename:curSA.keySet()) {
            if (curSA.get(filename) == null) {
                System.out.println(filename);
            }
        }
        System.out.println();

        // then list modifications not staged
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        // then list untracked files
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }
}
