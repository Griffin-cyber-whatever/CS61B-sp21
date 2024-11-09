package gitlet;

import java.io.File;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    // If a user inputs a command that doesn’t exist, print the message No command with that name exists. and exit.
    // If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
    /* If a user inputs a command that requires being in an initialized Gitlet working directory ,
        but is not in such a directory, print the message Not in an initialized Gitlet directory. */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        Repository repo = Repository.load();
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                File cwd = new File(System.getProperty("user.dir"));
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
            // add new commit by using java gielet.Main commit "message"
            case "commit":
                if (args.length != 2 || args[1] == null) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                } else {
                    repo.commit(args[1]);
                }
            case "rm":
                if (args.length != 2 || args[1] == null) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                } else {
                    repo.remove(args[1]);
                }
            // TODO
            case "log":
                break;
            case "global-log":
                break;
            case "find":
                if (args.length != 2 || args[1] == null) {
                    System.exit(0);
                } else{
                    String tmp = repo.find(args[1]);
                    if (tmp == null) {
                        System.out.println("Found no commit with that message.");
                    } else {
                        System.out.println(tmp);
                    }
                }
            case "status":
                if (args.length != 1) {
                    System.exit(0);
                } else {
                    repo.status();
                }
            case "checkout":
                // case 1 java gitlet.Main checkout -- [file name]
                // overwrite the file in CWD with the file in commit
                if (args.length == 3 && args[1].equals("--") && args[2] != null) {
                    repo.overWriteWithSameFileName(args[2]);
                }
                // case 2 java gitlet.Main checkout [commit id] -- [file name]
                // overwrite the file with [file name] with the file in [commit id] the commit\
                // commit id could be
                else if (args.length == 4 && args[1] != null && args[2].equals("--") && args[3] != null) {
                    repo.overWriteWithDifferentFileName(args[3], args[1]);
                }
                // case 3 java gitlet.Main checkout [branch name]
                // make the given branch became the current branch
                // overwrite the CWD entirely so that it's identical to that branch
                else if (args.length == 2 && args[1] != null) {
                    repo.overWriteBranch(args[1]);
                }
            case "branch":
                if (args.length != 2 || args[1] == null) {
                    System.exit(0);
                }
                repo.branch(args[1]);
            case "rm-branch":
                // java gitlet.Main rm-branch [branch name]
                // Deletes the branch with the given name. This only means to delete the pointer associated with the branch;
                // dont delete the committed content
                repo.removeBranch(args[1]);
            case "reset":
                // java gitlet.Main reset [commit id]
                //  Checks out all the files tracked by the given commit.
                //  Removes tracked files that are not present in that commit.
                //  Also moves the current branch’s head to that commit node.
                if (args.length != 2 || args[1] == null) {
                    System.exit(0);
                }
                repo.reset(args[1]);
            case "merge":
                if (args.length != 2 || args[1] == null) {
                    System.exit(0);
                }
                repo.merge(args[1]);
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
