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
                break;
            case "log":
                // TODO: handle the 'log' command
                break;

        }
    }
}
