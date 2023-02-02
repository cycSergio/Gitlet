package gitlet;

import java.util.Objects;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author cyc
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.initCommand();
                break;
            case "add":
                String addFile = args[1];
                Repository.addCommand(addFile);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                // Usage: java gitlet.Main commit [message]
                // Note that there is no "-m" as real git command.
                if (args.length == 1 || Objects.equals(args[1], "")) {
                    Utils.message("Please enter a commit message.");
                }
                Repository.commitCommand(args[1]);
                break;
            case "checkout":
                // TODO: handle the 'checkout [branch name]' command
                // Usage1: java gitlet.Main checkout -- [filename]
                String fst = args[1];
                if (fst.equals("--")) {
                    String filename = args[2];
                    Repository.checkout(filename);
                } else if (args.length == 4) {
                    // Usage2: java gitlet.Main checkout [commit id] -- [filename]
                    String targetCommit = args[1];
                    String targetFilename = args[3];
                    Repository.checkout(targetCommit, targetFilename);
                } else if (args.length == 2) {
                    // Usage3: java gitlet.Main checkout [branch name]
                    Repository.checkoutBranch(args[1]);
                }
                break;
            case "log":
                // Usage: java gitlet.Main log
                Repository.logCommand();
                break;
            case "rm":
                // Usage: java gitlet.Main rm [file name]
                Repository.rm(args[1]);
                break;
            case "global-log":
                // Usage: java gitlet.Main global-log
                Repository.globalLog();
                break;
            case "find":
                // Usage: java gitlet.Main find [commit message]
                Repository.find(args[1]);
                break;
            case "branch":
                // Usage: java gitlet.Main branch [branch name]
                Repository.branch(args[1]);
                break;
            case "status":
                // Usage: java gitlet.Main status
                Repository.status();
                break;


        }
    }
}
