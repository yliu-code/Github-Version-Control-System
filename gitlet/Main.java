package gitlet;

/* Driver class for Gitlet, the tiny stupid version-control system.
   @author
*/
public class Main {
    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String command = args[0];
        Persistence.deserialize();
        switch (command) {
            case "list":
                // my own test. stop looking at me.
                Control.listwd();
                break;
            case "init":
                op0(args);
                Control.init();
                break;
            case "add":
                op1(args);
                Control.add(args[1]);
                break;
            case "commit":
                op1(args);
                Control.commit(args[1]);
                break;
            case "rm":
                op1(args);
                Control.rm(args[1]);
                break;
            case "log":
                op0(args);
                Control.log();
                break;
            case "global-log":
                op0(args);
                Control.glog();
                break;
            case "find":
                op1(args);
                Control.find(args[1]);
                break;
            case "status":
                op0(args);
                Control.status();
                break;
            case "checkout":
                op123(args);
                if (args[1].equals("--")) {
                    Control.checkoutF(args[2]);
                } else if (args.length == 4) {
                    Control.checkoutC(args[1], args[3]);
                } else {
                    Control.checkoutB(args[1]);
                }
                break;
            case "branch":
                op1(args);
                Control.branch(args[1]);
                break;
            case "rm-branch":
                op0(args);
                Control.rmBranch(args[1]);
                break;
            case "reset":
                op1(args);
                Control.reset(args[1]);
                break;
            case "merge":
                op1(args);
                Control.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
        Persistence.reserialize();

    }

    private static void op0(String... args) {
        if (args.length != 1) {
            System.out.println("incorrect operands.");
            System.exit(0);
        }
    }

    private static void op1(String... args) {
        if (args.length != 2) {
            System.out.println("incorrect operands.");
            System.exit(0);
        }
    }

    private static void op123(String... args) {
        if (args.length < 2 || args.length > 4
                || (args.length == 3 && !args[1].equals("--"))
                || (args.length == 4 && !args[2].equals("--"))) {
            System.out.println("incorrect operands.");
            System.exit(0);
        }
    }

}
