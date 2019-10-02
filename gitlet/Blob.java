package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    private String id;
    private String filename;
    private File file;
    private byte[] content;

    public Blob(String filename) {
        this.filename = filename;
        this.file = new File(Control.WDPATH + filename);
        this.content = Utils.readContents(file);
        this.id = generateID();
    }

    private String generateID() {
        return Utils.sha1(this.filename, this.content);
    }

    public String getID() {
        return id;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }
}
