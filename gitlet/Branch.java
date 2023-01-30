package gitlet;
import java.io.File;
import java.io.Serializable;

public class Branch implements Serializable{
    // the name of this branch, e.g. master is the default branch name
    private final String branchName;
    // to which commit this branch is currently pointing to
    private String branchCommitSha1;

    /* The constructor for a default branch -- master.*/
    public Branch(String initialCommitSha1) {
        this.branchName = "master";
        this.branchCommitSha1 = initialCommitSha1;
    }

    public Branch(String branchName, String commitSha1) {
        this.branchName = branchName;
        this.branchCommitSha1 = commitSha1;
    }

    public String getBranchName() {
        return this.branchName;
    }

    public String getBranchCommitSha1() {
        return this.branchCommitSha1;
    }

    // move the branch pointer after a commit
    public void move(String newCommitSha1) {
        this.branchCommitSha1 = newCommitSha1;
    }

    public File getBranchPath() {
        return Utils.join(Repository.BRANCH, this.branchName);
    }

    public void branchWrite() {
        Utils.writeObject(getBranchPath(), this);
    }
}
