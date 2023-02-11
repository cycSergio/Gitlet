package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*; // TODO: figure out what's import static?


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
    public static final File BLOBS = join(GITLET_DIR, "blobs");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File STAGING_AREA = join(GITLET_DIR, "index");
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
        BLOBS.mkdirs();
        COMMITS.mkdirs();

        HashMap<String, String> stagingIndex = new HashMap<>();
        MyUtils.createAndWriteObject(STAGING_AREA, stagingIndex);

        Commit initialCommit = new Commit();
        File initialCommitFile = join(COMMITS, initialCommit.getCommitSHA1());
        MyUtils.createAndWriteObject(initialCommitFile, initialCommit);

        BRANCH.mkdirs();
        Branch defaultMaster = new Branch(initialCommit.getCommitSHA1());
        MyUtils.createAndWriteObject(join(BRANCH, defaultMaster.getBranchName()), defaultMaster);
        MyUtils.createFile(HEAD);
        Utils.writeContents(HEAD, defaultMaster.getBranchName()); // TODO: to be confirmed at Path storage
    }

    /* Stage one file for removal. This is part of the add command function. */
    private static void add4removal(String addFile) {
        File fileToAdd = join(CWD, addFile); // TODO: not sure, to be confirmed
        HashMap<String, String> curComTrackings = getCurTrackings();
        HashMap<String, String> curSA = getSA();
        if (!fileToAdd.exists()) {
            if (!curComTrackings.containsKey(addFile)) {
                Utils.message("File does not exist."); // failure case, meaning that
                // the user is trying to stage a nonexistent file for addition.
            }
            else { // addFile is staged for removal
                Index removalIndex = new Index(addFile);
                curSA.put(removalIndex.getFileName(), removalIndex.getBlobSHA1()); // key: filename, value: sha1
                writeObject(STAGING_AREA, curSA);
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
        File fileToAdd = join(CWD, addFile); // TODO: not sure, to be confirmed
        HashMap<String, String> curComTrackings = getCurTrackings();
        HashMap<String, String> curSA = getSA();
        if (!fileToAdd.exists()) {
            if (!curComTrackings.containsKey(addFile)) {
                Utils.message("File does not exist."); // failure case, meaning that
                // the user is trying to stage a nonexistent file for addition.
            }
            else { // addFile is staged for removal
                Index removalIndex = new Index(addFile);
                curSA.put(removalIndex.getFileName(), removalIndex.getBlobSHA1()); // key: filename, value: sha1
                writeObject(STAGING_AREA, curSA);
            }
            return;
        }


        byte[] addFileContents = readContents(fileToAdd); // TODO: or read as String?
        String addFileSha1 = Utils.sha1(MyUtils.getFileContentAsString(addFile));
        // the case that current commit has the same file content as the staged one
        if (curComTrackings.containsKey(addFile) && Objects.equals(curComTrackings.get(addFile), addFileSha1)) {
            curSA.remove(addFile); // If this hashmap doesn't contain this key, it just returns null
            writeObject(STAGING_AREA, curSA);
            return;
        }

        /* Creates a corresponding blob object and serialize it. */
        Blob fileBlob = new Blob(addFileContents);
        File thisBlob = join(BLOBS, addFileSha1);
        MyUtils.createAndWriteObject(thisBlob, fileBlob);

        /* Creates the corresponding index in the Staging Area. */
        Index thisIndex = new Index(addFile, addFileSha1);
        curSA.put(thisIndex.getFileName(), thisIndex.getBlobSHA1()); // put method: if curSA.containsKey(addFile),
        // it will act just like curSA.replace(addFile, addFileSha1).
        writeObject(STAGING_AREA, curSA);
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
        return readObject(STAGING_AREA, HashMap.class);
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
        if (curIndexes.isEmpty()) {
            message("No changes added to the commit.");
        }
        HashMap<String, String> curComIndexes = buildIndexes(parCom.getFileToBlob(), curIndexes);
        Commit curCommit = new Commit(message, parSha1, curComIndexes);

        File newCommit = join(COMMITS, curCommit.getCommitSHA1());
        MyUtils.createAndWriteObject(newCommit, curCommit);

        // clear the staging area after each commit
        curIndexes.clear();
        writeObject(STAGING_AREA, curIndexes);
        // move HEAD and master pointers
        Branch curBranch = readObject(join(BRANCH, getHEAD()), Branch.class); // TODO: change join
        curBranch.move(curCommit.getCommitSHA1());
        writeObject(join(BRANCH, getHEAD()), curBranch);
    }

    /** A particular method for merge commit cause it has two parents. */
    private static void mergeCommit(String message, String secondParent) {
        String parSha1 = getCurCommitSha1();
        Commit parCom = getCurCommit();
        HashMap<String, String> curIndexes = getSA();
        if (curIndexes.isEmpty()) {
            message("No changes added to the commit.");
        }
        HashMap<String, String> curComIndexes = buildIndexes(parCom.getFileToBlob(), curIndexes);
        Commit curCommit = new Commit(message, parSha1, secondParent, curComIndexes);

        File newCommit = join(COMMITS, curCommit.getCommitSHA1());
        MyUtils.createAndWriteObject(newCommit, curCommit);

        // clear the staging area after each commit
        curIndexes.clear();
        writeObject(STAGING_AREA, curIndexes);
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
        File curComPath = join(COMMITS, curComSha1);
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

    private static Commit getInitialCommit() {
        List<String> allCommmitsSha1 = plainFilenamesIn(COMMITS);
        Commit curCommit;
        for (String commitSha1: allCommmitsSha1) {
            curCommit = getComBySha1(commitSha1);
            if (Objects.equals(curCommit.getMessage(), "initial commit")) {
                return curCommit;
            }
        }
        return null;
    }

    /* A helper method to get a commit by its sha1 value. */
    private static Commit getComBySha1(String sha1) {
        if (Objects.equals(sha1, "0")) {
            return getInitialCommit();
        }
        File targetComPath = join(COMMITS, sha1);
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
            String parSha1 = curCommit.getFirstParent();
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
        if (curCommit.sizeOfParent() == 2) {
            logMes.append("Merge: ").append(curCommit.getShortFirstParent());
            logMes.append(" ").append(curCommit.getShortSecondParent());
        }
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
    public static void globalLog() {
        List<String> allCommitsSha1 = plainFilenamesIn(COMMITS);
        assert allCommitsSha1 != null;
        for (String commitSha1: allCommitsSha1) {
            logMessage(getComBySha1(commitSha1));
        }
    }

    /* The first usage of checkout command.
       Takes the version of the file as it exists in the head commit and puts
       it in the working directory, overwriting the version of the file that's
       already there if there is one. The new version of the file is not staged.

       From current commit get the sha1 of [filename], by this sha1 get the
       corresponding content from the blob named this [sha1].
    */
    public static void checkout(String filename) {
        HashMap<String, String> curTracking = getCurTrackings();
        if (!curTracking.containsKey(filename)) {
            message("File does not exist in that commit.");
            return;
        }
        String targetSha1 = curTracking.get(filename);
        File targetFile = join(CWD, filename);
        writeContents(targetFile, getFileContentBySha1(targetSha1));
    }

    /* A helper method to get content from the blob that named [targetSha1]
    *  If no such blob file exists, just returns an empty byte[] for the
    *  purpose of making merge conflict work out fine. */
    private static byte[] getFileContentBySha1(String targetSha1) {
        if (targetSha1 == null) {
            byte[] empty = {};
            return empty;
        }
        File targetPath = join(BLOBS, targetSha1);
        Blob targetBlob = readObject(targetPath, Blob.class);
        byte[] targetContnet = targetBlob.getFileContent();
        return targetContnet;
    }

    /** Return false if the commit named [commitId] does not exists. */
    private static Boolean checkIfCommitExists(String commitId) {
        if (join(COMMITS, commitId).exists()) {
            return true;
        } else {
            message("No commit with that id exists.");
            return false;
        }
    }

    /** The second usage of checkout command.
     *  Takes the version of the file as it exists in the commit with the given
     *  id, and puts it in the working directory, overwriting the version of the
     *  file that's already there if there is one. The new version of the file
     *  is not staged.
     *  // TODO: the spec says it should also support abbreviation of commit id,
     *      as in real git. How?
     *  */
    public static void checkout(String commitId, String filename) {
        // TODO: needs refactor, as it's the same as the first checkout command.
        if (commitId.length() < 40) { // the user uses a short version of commit id
            commitId = convertShortID2full(commitId);
        }
        if (!checkIfCommitExists(commitId) || commitId == null) {
            return;
        }
        HashMap<String, String> targetTracking = getComBySha1(commitId).getFileToBlob();
        if (!targetTracking.containsKey(filename)) {
            message("File does not exist in that commit.");
            return;
        }
        String targetSha1 = targetTracking.get(filename);
        File targetFile = join(CWD, filename);
        writeContents(targetFile, getFileContentBySha1(targetSha1));
    }

    /** Returns the full version of the short version of the commit id.
     *  If that commit id does not exist in COMMITS, just returns null. */
    private static String convertShortID2full(String shortId) {
        int len = shortId.length();
        List<String> allCommitIds = plainFilenamesIn(COMMITS);
        for (String commitId: allCommitIds) {
            if (commitId.substring(0, len).equals(shortId)) {
                return commitId;
            }
        }
        return null;
    }

    /** Return true if there are untracked files. */
    private static boolean checkUntrackedFiles() {
        // If a file in CWD is not tracked in the current Commit/SA, then it's untracked.
        List<String> allCWDfiles = plainFilenamesIn(CWD);
        HashMap<String, String> curTracking = getCurTrackings();
        HashMap<String, String> curSA = getSA();
        for (String CWDfile:allCWDfiles) {
            if (!curTracking.containsKey(CWDfile) && !curSA.containsKey(CWDfile)) {
                message("There is an untracked file in the way; delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    /** A helper method to checks out all files from a certain commit
     *  and overwrites the current CWD files.
     */
    private static void overwriteCWDbyCertainCommit(String commitId) {
        List<String> allCWDfiles = plainFilenamesIn(CWD);
        Commit targetCom = getComBySha1(commitId);
        HashMap<String, String> targetTracking = targetCom.getFileToBlob();
        String targetSha1;
//        Blob targetBlob;;
        for (String CWDfile:allCWDfiles) {
            if (!targetTracking.containsKey(CWDfile)) {
                restrictedDelete(join(CWD, CWDfile));
            }
        }
        for (String trackingFile: targetTracking.keySet()) {
            targetSha1 = targetTracking.get(trackingFile);
            //targetBlob = readObject(join(blobs, targetSha1), Blob.class);
            writeContents(join(CWD, trackingFile), getFileContentBySha1(targetSha1));
        }
    }

    /** The third usage of checkout command.
    *  Takes all files in the commit at the head of the given branch, and puts them
    *  in the working directory, overwriting the versions of the files that are
    *  already there if they exist.
    *  Also, at the end of this command, the given branch will now be considered
    *  the current branch(HEAD). Any files that are tracked in the current (old)
    *  branch but are not present in the checked-out branch(new) are deleted.
    *  The staging area is cleared, unless the checked-out branch is the current
    *  branch.
    *  */
    public static void checkoutBranch(String branchname) {
        if (!join(BRANCH, branchname).exists()) {
            message("No such branch exists.");
            return;
        }
        String curHead = getHEAD();
        if (curHead.equals(branchname)) {
            message("No need to checkout the current branch.");
            return;
        }
        if (checkUntrackedFiles()) {
            return;
        }

        String commitId = readObject(join(BRANCH, branchname), Branch.class).getBranchCommitSha1();
        overwriteCWDbyCertainCommit(commitId);

        writeContents(HEAD, branchname);
        HashMap<String, String> curSA = getSA();
        curSA.clear();
        writeObject(STAGING_AREA, curSA);
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
    public static void rm(String filename) {
        HashMap<String, String> curSA = getSA();
        HashMap<String, String> curCommitTracking = getCurTrackings();
        boolean staged4add = curSA.containsKey(filename) && curSA.get(filename) != null;
        boolean inCurCom = curCommitTracking.containsKey(filename);
        if (staged4add) {
            curSA.remove(filename);
            writeObject(STAGING_AREA, curSA);
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
    public static void find(String message) {
        List<String> allCommitsSha1 = plainFilenamesIn(COMMITS);
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
    *
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
    public static void status() {
        // check failure cases
        if (!GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            return;
        }

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
        // then list all the staged files -- files that are staged for addition
        System.out.println("=== Staged Files ===");
        HashMap<String, String> curSA = getSA(); // TODO: sort this list!
        for (String filename:curSA.keySet()) {
            if (curSA.get(filename) != null) {
                System.out.println(filename);
            }
        }
        System.out.println();

        // then list all the removed files -- files that are staged for removal
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
//        List<String> allCWDfiles = plainFilenamesIn(CWD);
//        Set<String>  trackings = getCurTrackings().keySet();
//        Set<String> SA = curSA.keySet();
//        trackings.addAll(SA);
//        assert allCWDfiles != null;
//        for (String filename:allCWDfiles) {
//            if (!getCurTrackings().containsKey(filename)) {
//                System.out.println(filename);
//            }
//        }
        System.out.println();
    }

    private static Boolean checkBranchExists(String branchName) {
        return join(BRANCH, branchName).exists();
    }

    /* Deletes the branch with the given name. This only means to delete the pointer
    *  associated with the branch; it does not mean to delete all commits that
    *  were created under the branch, or anything like that. */
    public static void rmBranch(String branchname) {
        if (!checkBranchExists(branchname)) {
            message("A branch with that name does not exist.");
            return;
        }
        if (Objects.equals(branchname, getHEAD())) {
            message("Cannot remove the current branch.");
            return;
        }
        join(BRANCH, branchname).delete();
    }

    /** A helper method to get the current Branch object.    */
    private static Branch getCurBranch() {
        return readObject(join(BRANCH, getHEAD()), Branch.class);
    }

    private static Branch getBranchByName(String branchName) {
        return readObject(join(BRANCH, branchName), Branch.class);
    }

    private static String getBranchHeadId(String branchName) {
        return getBranchByName(branchName).getBranchCommitSha1();
    }

    /** Checks out all the files tracked by the given commit.
     *  Also moves the current branch's head to that commit node.
     *  The staging area is clear. This command is essentially
     *  `checkout` of an arbitrary commit that also changes the
     *  current branch head. */
    public static void reset(String commitId) {
        if (checkUntrackedFiles() || !checkIfCommitExists(commitId)) {
            return;
        }
        overwriteCWDbyCertainCommit(commitId);
        Branch curBranch = getCurBranch();
        curBranch.move(commitId);
        writeObject(join(BRANCH, curBranch.getBranchName()), curBranch); // TODO: tend to forget write!!!!!!
        HashMap<String, String> curSA = getSA();
        curSA.clear();
        writeObject(STAGING_AREA, curSA);
    }

    /** Merges files from the given branch into the current branch.
     *  TODO: how to identify the split point?
     *  TODO: encode the merge rules to determine the merge result
     *  TODO: deal with merge conflict */
    public static void merge(String branchName) {
        if (!getSA().isEmpty()) {
            message("You have uncommitted changes.");
            return;
        }
        if (!checkBranchExists(branchName)) {
            message("A branch with that name does not exist.");
            return;
        }
        if (getHEAD().equals(branchName)) {
            message("Cannot merge a branch with itself.");
            return;
        }
        if (checkUntrackedFiles()) {
            return;
        }
        Commit head = getCurCommit();
        Commit other = getComBySha1(getBranchHeadId(branchName));
        Commit split = getSplitPoint(head, other);
        if (Objects.equals(split.getCommitSHA1(), other.getCommitSHA1())) {
            message("Given branch is an ancestor of the current branch.");
            return;
        }
        if (Objects.equals(head.getCommitSHA1(), split.getCommitSHA1())) {
            checkoutBranch(branchName);
            message("Current branch fast-forwarded.");
            return;
        }
        Boolean hasConflict = mergeByrules(split.getFileToBlob(),head.getFileToBlob(), other.getFileToBlob(), branchName);
        mergeCommit("Merged " + branchName + " " + "into " + getHEAD() + ".", other.getCommitSHA1());

//        Commit mergedCommit = getCurCommit();
//        mergedCommit.addSecondParent(other.getCommitSHA1()); // TODO: think about this!!!!!!
//        writeObject(join(COMMITS, mergedCommit.getCommitSHA1()), mergedCommit);
    }

    /** Returns the split point, which is the latest common ancestor
     *  of the current and given branch heads. */
    private static Commit getSplitPoint(Commit head, Commit other) {
        Queue<Commit> commitsQueue = new LinkedList<>();
        commitsQueue.add(head);
        commitsQueue.add(other);
        HashSet<String> checkedCommitIds = new HashSet<>();
        Commit curCheck;
        Commit curCheckParent;
        String curCheckParentId;
        while (true) {
            curCheck = commitsQueue.poll();
            checkedCommitIds.add(curCheck.getCommitSHA1());
            if (Objects.equals(curCheck.getMessage(), "initial commit")) {
                return curCheck; // it's already the initial commit node
            }
            curCheckParentId = (curCheck.sizeOfParent() == 1) ? curCheck.getFirstParent() : curCheck.getSecondParent();
            //curCheckParentId = curCheck.getFirstParent();
            curCheckParent = getComBySha1(curCheckParentId);
            if (checkedCommitIds.contains(curCheckParentId)) {
                return curCheckParent;
            }
            commitsQueue.add(curCheckParent);
            checkedCommitIds.add(curCheckParentId);
        }
    }

    /** Returns true if there is a merge conflict! */
    private static Boolean mergeByrules(HashMap<String, String> split,
                                     HashMap<String, String> head,
                                     HashMap<String, String> other,
                                     String branchName) {
        HashSet<String> checkedFiles = new HashSet<>();
        String headSha1;
        String splitSha1;
        String otherSha1;
        Boolean hasConflict = false;
        for (String filename: head.keySet()) {
            headSha1 = head.get(filename);
            splitSha1 = split.get(filename);
            otherSha1 = other.get(filename);
            if (Objects.equals(otherSha1, splitSha1) || Objects.equals(headSha1, otherSha1)) {
                continue;
            } else if (Objects.equals(headSha1, splitSha1) && !Objects.equals(otherSha1, splitSha1)) {
                if (otherSha1 != null) {
                    checkout(getBranchHeadId(branchName), filename);
                    addCommand(filename);
                } else {
                    restrictedDelete(filename);
                    addCommand(filename); // staged for removal
                }
            } else if (!Objects.equals(headSha1, splitSha1) ||
                    (otherSha1 == null && !Objects.equals(headSha1, splitSha1)) ||
                    (!Objects.equals(headSha1, otherSha1) && splitSha1 == null)) {
                stageMergeConflict(filename, headSha1, otherSha1);
                hasConflict = true;
            }
            checkedFiles.add(filename);
        }

        for (String filename: other.keySet()) {
            otherSha1 = other.get(filename);
            splitSha1 = split.get(filename);
            if (checkedFiles.contains(filename)) {
                continue;
            }
            if (splitSha1 == null) {
                checkout(getBranchHeadId(branchName), filename);
                addCommand(filename);
            } else if (splitSha1 != null && splitSha1.equals(otherSha1)) {
                continue;
            } else if (splitSha1 != null && !splitSha1.equals(otherSha1)) {
                stageMergeConflict(filename, null, otherSha1);
            }
            checkedFiles.add(filename);
        }
        return hasConflict;
    }

    private static void stageMergeConflict(String filename, String headSha1, String otherSha1) {
        byte[] headContent = getFileContentBySha1(headSha1);
        byte[] otherContent = getFileContentBySha1(otherSha1);
        File conflictFile = join(CWD, filename);
        writeContents(conflictFile,
                "<<<<<<< HEAD", "\n",
                headContent, "=======", "\n",
                otherContent, ">>>>>>>", "\n");
        addCommand(filename);
        System.out.println("Encountered a merge conflict.");
    }


}
