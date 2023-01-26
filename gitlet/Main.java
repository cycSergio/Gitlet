package gitlet;

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
                String message = args[1];
                Repository.commitCommand(message);
                break;
            case "checkout":
                // TODO: handle the 'checkout -- [file name]' and the 'checkout [commit id] -- [file name]' command
                // TODO: handle the 'checkout [branch name]' command
                // Usage1: java gitlet.Main checkout -- [filename]
                String fst = args[1];
                if (fst.equals("--")) {
                    String filename = args[2];
                    Repository.checkout(filename);
                } else if (args.length != 2) {
                    // Usage2: java gitlet.Main checkout [commit id] -- [filename]
                    String targetCommit = args[1];
                    String targetFilename = args[3];
                    Repository.checkout(targetCommit, targetFilename);
                }
                // Usage3: tbc
                break;
            case "log":
                // Usage: java gitlet.Main log
                Repository.logCommand();
                // TODO: handle the 'log' command
                break;

        }
    }
}
