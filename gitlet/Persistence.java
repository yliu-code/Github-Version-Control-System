package gitlet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

// Writer referenced from
// https://www.journaldev.com/
// 927/objectoutputstream-java-write-object-file

// Reader referenced from
// https://www.journaldev.com/
// 933/objectinputstream-java-read-object-file

public class Persistence {

    static final String FILEEXT = ".ser";

    public static void writeBlob(String id, Blob blob) {
        try {
            FileOutputStream fo = new FileOutputStream(
                    Control.BLOBSPATH + id + FILEEXT);
            ObjectOutputStream oo = new ObjectOutputStream(fo);
            oo.writeObject(blob);
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Blob readBlob(String id) {
        Blob res = null;
        try {
            FileInputStream fi = new FileInputStream(Control.BLOBSPATH + id + FILEEXT);
            ObjectInputStream oi = new ObjectInputStream(fi);
            res = (Blob) oi.readObject();
            oi.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void writeCommit(String id, Commit commit) {
        try {
            FileOutputStream fo = new FileOutputStream(
                    Control.COMMITSPATH + id + FILEEXT);
            ObjectOutputStream oo = new ObjectOutputStream(fo);
            oo.writeObject(commit);
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Commit readCommit(String id) {
        Commit res = null;
        try {
            FileInputStream fi = new FileInputStream(
                    Control.COMMITSPATH + id + FILEEXT);
            ObjectInputStream oi = new ObjectInputStream(fi);
            res = (Commit) oi.readObject();
            oi.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void writeSet(String id, HashSet<String> set) {
        try {
            FileOutputStream fo = new FileOutputStream(
                    Control.STAGEPATH + id + FILEEXT);
            ObjectOutputStream oo = new ObjectOutputStream(fo);
            oo.writeObject(set);
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static HashSet<String> readSet(String id) {
        HashSet<String> res = new HashSet<>();
        try {
            FileInputStream fi = new FileInputStream(
                    Control.STAGEPATH + id + FILEEXT);
            ObjectInputStream oi = new ObjectInputStream(fi);
            res = (HashSet<String>) oi.readObject();
            oi.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void writeMap(String id, HashMap<String, String> map) {
        try {
            FileOutputStream fo = new FileOutputStream(
                    Control.STAGEPATH + id + FILEEXT);
            ObjectOutputStream oo = new ObjectOutputStream(fo);
            oo.writeObject(map);
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> readMap(String id) {
        HashMap<String, String> res = new HashMap<>();
        try {
            FileInputStream fi = new FileInputStream(
                    Control.STAGEPATH + id + FILEEXT);
            ObjectInputStream oi = new ObjectInputStream(fi);
            res = (HashMap<String, String>) oi.readObject();
            oi.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void deserialize() {
        Path path = Paths.get("./.gitlet/");
        if (Files.exists(path)) {
            Control.head = readCommit("head").getID();
            Control.currbranch = readCommit("head").getBranch();
            Control.stage = readSet("stage");
            Control.removed = readSet("removed");
            Control.allCommits = readMap("allCommits");
            Control.branchHeads = readMap("branchHeads");
        }
    }

    public static void reserialize() {
        writeSet("stage", Control.stage);
        writeSet("removed", Control.removed);
        writeMap("allCommits", Control.allCommits);
        writeMap("branchHeads", Control.branchHeads);
    }

}
