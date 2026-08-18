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

        }
    }
}
