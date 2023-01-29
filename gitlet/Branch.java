package gitlet;
import java.io.Serializable;
import java.io.File;

import static gitlet.Utils.*;

public class Branch implements Serializable{

    private String branchName;
    // to which commit this branch is currently pointing to
    private String commitSha1;

    private File branch;

    /* The constructor for a default branch -- master.*/
    public Branch() {
        this.branchName = "master";

    }

    public Branch(String branchName) {
        this.branchName = branchName;
        commitSha1 = Repository.getCurCommitSha1();
    }

}
