package gitlet;

import java.io.File;
import java.io.IOException;

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

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */

    /**
     * TODO: create all the rest of the things in the .gitlet that we need by manipulating files/directories
     * TODO: somehow make the initial commit by using some constructor in the Commit Class and serialize it
     * TODO: somehow fail if a Gitlet version-control system exists in the current directory
     *
     *  .gitlet
     *      initialCommit
     * */
    public static void initCommand() { // TODO: why static? 因为我需要在不需要对象的前提下就能调用这个方法。这是static让我做到的。
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        GITLET_DIR.mkdirs();
        Commit initialCommit = new Commit();
        File initialCommitFile = join(GITLET_DIR, "initialCommit");
        try {
            initialCommitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(initialCommitFile, initialCommit);

    }
}
