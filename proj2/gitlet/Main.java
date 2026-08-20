package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Cirlnt
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                new Repository().init();
                break;
            case "add":
                String fileName = args[1];
                new Repository().add(fileName);
                break;
            case "rm":
                new Repository().rm(args[1]);
                break;
            case "commit":
                if (args.length < 2 || args[1].isEmpty()){
                    System.out.println("Please enter a commit message.");
                }
                else{
                    String message = args[1];
                    new Repository().commit(message);
                }
                break;
            case "log":
                new Repository().log();
                break;
            case "global-log":
                new Repository().globalLog();
                break;
            case "find":
                new Repository().find(args[1]);
                break;
            case "status":
                new Repository().status();
                break;
            case "checkout":
                if (args.length == 2) {                       // checkout [branch name]
                    new Repository().checkout3(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {   // checkout -- [file]
                    new Repository().checkout1(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {   // checkout [id] -- [file]
                    new Repository().checkout2(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;
            case "branch":
                new Repository().branch(args[1]);
                break;
            case "rm-branch":
                new Repository().rmBranch(args[1]);
                break;
            case "reset":
                new Repository().reset(args[1]);
                break;
            case "merge":
                new Repository().merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
        }
    }
}
