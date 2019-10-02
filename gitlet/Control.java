package gitlet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Control {
    // latest commit id
    static String head = null;
    // branch name of latest commit id
    static String currbranch = null;
    // keeps track of file name added to staging area
    static HashSet<String> stage = new HashSet<>();
    // track removed file name
    static HashSet<String> removed = new HashSet<>();
    // map commit id to branch name
    static HashMap<String, String> allCommits = new HashMap<>();
    // map branch name to commit id of branch head
    static HashMap<String, String> branchHeads = new HashMap<>();
    // path of working directory
    static final String WDPATH =
            System.getProperty("user.dir") + File.separator;
    // path of gitlet
    static final String GITLETPATH = WDPATH + ".gitlet/";
    // path of stage
    static final String STAGEPATH = GITLETPATH + "stage/";
    // path of commits
    static final String COMMITSPATH = GITLETPATH + "commits/";
    // path of blobs
    static final String BLOBSPATH = GITLETPATH + "blobs/";
    // line separator in log
    static final String LINESEPARATOR = "===";

    // create .gitlet repo, initial commit, master branch
    public static void init() {
        boolean gitletInit = new File(GITLETPATH).mkdirs();
        boolean stageInit = new File(STAGEPATH).mkdirs();
        boolean commitsInit = new File(COMMITSPATH).mkdirs();
        boolean blobsInit = new File(BLOBSPATH).mkdirs();

        if (!gitletInit || !stageInit || !commitsInit || !blobsInit) {
            System.out.println("A gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }

        Commit commitInit = new Commit("initial commit");

        head = commitInit.getID();
        currbranch = commitInit.getBranch();
        allCommits.put(head, currbranch);
        branchHeads.put(currbranch, head);

        Persistence.writeCommit("head", commitInit);
        Persistence.writeCommit(commitInit.getID(), commitInit);
        Persistence.writeMap("allCommits", allCommits);
        Persistence.writeMap("branchHeads", branchHeads);
    }

    // add file to hashset stage
    public static void add(String fileName) {
        File currFile = new File(WDPATH + fileName);
        if (currFile.exists() && !currFile.isDirectory()) {
            Commit currCommit = Persistence.readCommit("head");
            Blob currBlob = new Blob(fileName);
            removed.remove(fileName);
            if (!currCommit.getTree().containsValue(currBlob.getID())) {
                stage.add(fileName);
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    // create new commit with parent as previous commit
    // add staged file to tree, initialize new commit
    public static void commit(String log) {
        if (stage.size() <= 0 && removed.size() <= 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (log.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit currCommit = Persistence.readCommit("head");
        Commit newCommit = new Commit(log, currCommit);
        for (String fileName : stage) {
            Blob currBlob = new Blob(fileName);
            newCommit.addBlob(fileName, currBlob);
        }
        for (String fileName : removed) {
            newCommit.rmBlob(fileName);
        }

        head = newCommit.getID();
        currbranch = newCommit.getBranch();
        allCommits.put(head, currbranch);
        branchHeads.put(currbranch, head);

        Persistence.writeCommit("head", newCommit);
        Persistence.writeCommit(newCommit.getID(), newCommit);
        Persistence.writeMap("allCommits", allCommits);
        Persistence.writeMap("branchHeads", branchHeads);

        stage.clear();
        removed.clear();
    }

    // remove file from stage, add to Hashset removed
    public static void rm(String fileName) {
        Commit currCommit = Persistence.readCommit("head");
        if (currCommit.containsBlob(fileName)) {
            Utils.restrictedDelete(fileName);
            stage.remove(fileName);
            removed.add(fileName);
        } else if (!currCommit.containsBlob(fileName) && stage.contains(fileName)) {
            stage.remove(fileName);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    // display commit metadata until init commit
    public static void log() {
        Commit currCommit = Persistence.readCommit("head");
        printLog(currCommit);

        while (currCommit.getParent() != null) {
            currCommit = Persistence.readCommit(currCommit.getParent());
            System.out.println();
            printLog(currCommit);
        }
    }

    // display all commit info
    public static void glog() {
        for (String commitID : allCommits.keySet()) {
            Commit currCommit = Persistence.readCommit(commitID);
            printLog(currCommit);
            System.out.println();
        }
    }

    // print id of commit with given msg
    public static void find(String msg) {
        boolean printed = false;
        for (String commitID : allCommits.keySet()) {
            Commit toBeProcessedCommit = Persistence.readCommit(commitID);
            if (toBeProcessedCommit.getLogMessage().equals(msg)) {
                printed = true;
                System.out.println(toBeProcessedCommit.getID());
            }
        }
        if (!printed) {
            System.out.println("Found no commit with that message");
        }
    }

    // display all cur branch, staged, rm, not stage, untrack
    public static void status() {
        //Print sorted branches
        System.out.println(LINESEPARATOR + " Branches " + LINESEPARATOR);
        ArrayList<String> sortedBranches = new ArrayList<>(branchHeads.keySet());
        Collections.sort(sortedBranches);
        for (String branch : sortedBranches) {
            if (branch.equals(currbranch)) {
                branch = "*" + branch;
            }
            System.out.println(branch);
        }
        System.out.println();

        //Print sorted staged files
        System.out.println(LINESEPARATOR + " Staged Files " + LINESEPARATOR);
        List<String> sortedStagedFiles = new ArrayList<>(stage);
        Collections.sort(sortedStagedFiles);
        for (String filename : sortedStagedFiles) {
            System.out.println(filename);
        }
        System.out.println();


        //Print sorted removed files
        System.out.println(LINESEPARATOR + " Removed Files " + LINESEPARATOR);
        List<String> sortedRemovedFiles = new ArrayList<>(removed);
        Collections.sort(sortedRemovedFiles);
        for (String filename : sortedRemovedFiles) {
            System.out.println(filename);
        }

        //Optional
        System.out.println();
        System.out.println(LINESEPARATOR + " Modifications Not Staged For Commit " + LINESEPARATOR);
        System.out.println();
        System.out.println(LINESEPARATOR + " Untracked Files " + LINESEPARATOR);
    }

    // place file from head in wd, overwrite, add to untracked
    public static void checkoutF(String filename) {
        Commit currCommit = Persistence.readCommit("head");
        if (!currCommit.containsBlob(filename)) {
            System.out.println("File does not exist in that commit");
            System.exit(0);
        } else {
            Blob currBlob = Persistence.readBlob(currCommit.getBlob(filename));
            Utils.writeContents(currBlob.getFile(), currBlob.getContent());
        }
    }

    // place file from commit in wd, overwrite, etc. ...
    public static void checkoutC(String id, String filename) {
        Commit currCommit = findAbbreviatedCommit(id);
        if (currCommit != null) {
            if (!currCommit.containsBlob(filename)) {
                System.out.println("File does not exist in that commit");
                System.exit(0);
            } else {
                Blob currBlob = Persistence.readBlob(currCommit.getBlob(filename));
                Utils.writeContents(currBlob.getFile(), currBlob.getContent());
            }
        }
    }

    // place all file from branch in wd, branch is head, clear sa
    public static void checkoutB(String branchname) {
        if (!branchHeads.containsKey(branchname)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        } else if (currbranch.equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        } else {
            Commit currCommit = Persistence.readCommit("head");
            Commit newCommit = Persistence.readCommit(branchHeads.get(branchname));
            checkUntracked(newCommit);

            for (String blob : newCommit.getTree().values()) {
                Blob currBlob = Persistence.readBlob(blob);
                Utils.writeContents(currBlob.getFile(), currBlob.getContent());
            }
            for (String filename : currCommit.getTree().keySet()) {
                if (!newCommit.getTree().containsKey(filename)) {
                    Utils.restrictedDelete(filename);
                }
            }

            newCommit.setBranch(branchname);
            Persistence.writeCommit("head", newCommit);
            stage.clear();
        }
    }

    // create branch, point branch to head node
    public static void branch(String branchname) {
        if (branchHeads.containsKey(branchname)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            branchHeads.put(branchname, head);
            allCommits.put(head, branchname);
        }
    }

    // delete branch with name from branches
    public static void rmBranch(String branchname) {
        if (!branchHeads.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (currbranch.equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            //?: Do I need to delete branchnames in hashmap allCommits?
            branchHeads.remove(branchname);
        }
    }

    // checkout commit with id, move head to commit
    public static void reset(String id) {
        Commit currCommit = findAbbreviatedCommit(id);
        Commit headCommit = Persistence.readCommit("head");
        if (currCommit != null) {
            checkUntracked(currCommit);
            for (String filename : currCommit.getTree().keySet()) {
                if (!currCommit.containsBlob(filename)) {
                    Utils.restrictedDelete(filename);
                } else {
                    Blob currBlob = Persistence.readBlob(currCommit.getBlob(filename));
                    Utils.writeContents(currBlob.getFile(), currBlob.getContent());
                }
            }

            List<String> fileInWd = Utils.plainFilenamesIn(WDPATH);
            if (fileInWd != null) {
                for (String filename : fileInWd) {
                    if (headCommit.containsBlob(filename) && !currCommit.containsBlob(filename)) {
                        Utils.restrictedDelete(filename);
                    }
                }
            }
        }
        head = currCommit.getID();
        currbranch = currCommit.getBranch();
        branchHeads.put(currbranch, currCommit.getID());

        stage.clear();
        removed.clear();

        Persistence.writeCommit("head", currCommit);
    }

    // change overload files from next branch to new branch}
    public static void merge(String givenbranch) {
        if (!stage.isEmpty() || !removed.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        } else if (!branchHeads.keySet().contains(givenbranch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (currbranch.equals(givenbranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit splitCommit = findSplit(currbranch, givenbranch);
        if (splitCommit.getID().equals(branchHeads.get(givenbranch))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitCommit.getID().equals(branchHeads.get(currbranch))) {
            branchHeads.put(currbranch, branchHeads.get(givenbranch));
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else {
            boolean existConflictedFiles = false;
            Commit currCommit = Persistence.readCommit(branchHeads.get(currbranch));
            Commit givenCommit = Persistence.readCommit(branchHeads.get(givenbranch));

            checkUntracked(givenCommit);

            for (String filename : givenCommit.getTree().keySet()) {
                if (splitCommit.containsBlob(filename)
                        && currCommit.containsBlob(filename)
                        && splitCommit.getBlob(filename)
                        .equals(currCommit.getBlob(filename))
                        && splitCommit.getBlob(filename)
                        .equals(givenCommit.getBlob(filename))) {
                    Blob givenBlob = Persistence.readBlob(givenCommit.getBlob(filename));
                    Utils.writeContents(givenBlob.getFile(), givenBlob.getContent());
                    stage.add(filename);

                } else if (!splitCommit.containsBlob(filename)) {
                    Blob givenBlob = Persistence.readBlob(givenCommit.getBlob(filename));
                    Utils.writeContents(givenBlob.getFile(), givenBlob.getContent());
                    stage.add(filename);

                }
            }

            for (String filename : splitCommit.getTree().keySet()) {
                if (currCommit.containsBlob(filename)
                        && currCommit.getBlob(filename)
                        .equals(splitCommit.getBlob(filename))
                        && !givenCommit.containsBlob(filename)) {
                    Utils.restrictedDelete(filename);
                    stage.remove(filename);
                    removed.add(filename);
                } else if (currCommit.containsBlob(filename)
                        && !currCommit.getBlob(filename)
                        .equals(givenCommit.getBlob(filename))) {
                    existConflictedFiles = true;
                    mergeConflict(currCommit, givenCommit, filename);
                }
            }

            if (!existConflictedFiles) {
                String mergeLog = "Merged " + currbranch + " with " + givenbranch + ".";
                mergeCommit(mergeLog, givenCommit);

            } else {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    private static void mergeConflict(Commit currCommit, Commit givenCommit, String filename) {
        File conflictFile = Persistence.readBlob(currCommit.getBlob(filename)).getFile();

        byte[] beforeCurrent = "<<<<<<< HEAD\n".getBytes();
        byte[] fileSep = "=======\n".getBytes();
        byte[] afterGiven = ">>>>>>>\n".getBytes();

        byte[] currContent = Persistence.readBlob(currCommit.getBlob(filename)).getContent();
        byte[] givenContent = new byte[0];

        if (givenCommit.getBlob(filename) != null) {
            givenContent = Persistence.readBlob(givenCommit.getBlob(filename)).getContent();
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(beforeCurrent);
            outputStream.write(currContent);
            outputStream.write(fileSep);
            outputStream.write(givenContent);
            outputStream.write(afterGiven);

            byte[] conflictFileContent = outputStream.toByteArray();
            Utils.writeContents(conflictFile, conflictFileContent);

        } catch (IOException e) {
            System.out.println("File Conflict Error");
        }
    }

    private static void mergeCommit(String log, Commit currCommit) {
        if (stage.size() <= 0 && removed.size() <= 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (log.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = new Commit(log, currCommit);
        for (String fileName : stage) {
            Blob currBlob = new Blob(fileName);
            newCommit.addBlob(fileName, currBlob);
        }
        for (String fileName : removed) {
            newCommit.rmBlob(fileName);
        }

        newCommit.setBranch(currbranch);
        newCommit.setParent(head);

        head = newCommit.getID();
        allCommits.put(head, currbranch);
        branchHeads.put(currbranch, head);

        Persistence.writeCommit("head", newCommit);
        Persistence.writeCommit(newCommit.getID(), newCommit);
        Persistence.writeMap("allCommits", allCommits);
        Persistence.writeMap("branchHeads", branchHeads);

        stage.clear();
        removed.clear();
    }


    private static Commit findSplit(String branch1, String branch2) {
        HashSet<String> allBranch1 = new HashSet<>();
        Commit branch1Commit = Persistence.readCommit(branchHeads.get(branch1));
        while (branch1Commit.getParent() != null) {
            allBranch1.add(branch1Commit.getID());
            branch1Commit = Persistence.readCommit(branch1Commit.getParent());
        }

        Commit branch2Commit = Persistence.readCommit(branchHeads.get(branch2));
        while (branch2Commit != null || branch2Commit.getParent() != null) {
            if (allBranch1.contains(branch2Commit.getID())) {
                return branch2Commit;
            } else {
                branch2Commit = Persistence.readCommit(branch2Commit.getParent());
            }
        }
        return null;
    }

    public static void listwd() {
        List<String> fileInWd = Utils.plainFilenamesIn(WDPATH);
        if (fileInWd != null) {
            for (String filename : fileInWd) {
                System.out.println(filename);
            }
        }
    }

    private static void checkUntracked(Commit commit) {

        HashSet<String> untracked = new HashSet<>();

        Commit currCommit = Persistence.readCommit("head");
        Commit prevCommit = null;

        if (currCommit.getParent() != null) {
            prevCommit = Persistence.readCommit(currCommit.getParent());
        }
        List<String> fileInWd = Utils.plainFilenamesIn(WDPATH);
        if (fileInWd != null) {
            for (String filename : fileInWd) {
                if (!stage.contains(filename) || !removed.contains(filename)) {
                    if (!currCommit.containsBlob(filename)
                            && (prevCommit == null || !prevCommit.containsBlob(filename))) {
                        untracked.add(filename);
                    }
                }

            }
        }

        for (String filename : commit.getTree().keySet()) {
            if (untracked.contains(filename)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it or add it first.");
                System.exit(0);
            }
        }
    }

    private static Commit findAbbreviatedCommit(String abbId) {
        File commitDirectory = new File(COMMITSPATH);
        FilenameFilter beginsWithId = new FilenameFilter() {
            public boolean accept(File directory, String filename) {
                return filename.startsWith(abbId);
            }
        };
        File[] commitFile = commitDirectory.listFiles(beginsWithId);
        if (commitFile == null || commitFile.length <= 0) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            String filename = commitFile[0].getName();
            return Persistence.readCommit(filename.substring(0, filename.lastIndexOf('.')));
        }
        return null;
    }

    private static void printLog(Commit currCommit) {
        System.out.println(LINESEPARATOR);
        System.out.println("Commit " + currCommit.getID());
        System.out.println(currCommit.getTimeStamp());
        System.out.println(currCommit.getLogMessage());
    }
}
