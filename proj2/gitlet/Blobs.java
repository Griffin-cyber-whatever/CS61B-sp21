package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Blobs implements Serializable {
    public static final File blobs = new File(GITLET_DIR, "blobs");
    private final String content;
    private final String blobId;

    public Blobs(String content) {
        this.content = content;
        this.blobId = hash();
    }

    public static String saveContent(String conflictContent) {
        Blobs blobs = new Blobs(conflictContent);
        blobs.save();
        return blobs.blobId;
    }

    private String hash(){
        return sha1(content);
    }

    public String getBlobId() {
        return blobId;
    }

    public void save(){
        writeContents(join(blobs, blobId), content);
    }

    // return content in that file as a string by providing blobId
    // return null if it doesn't exist
    public static String getContent(String blobId) {
        if (blobId == null || blobId.isEmpty()) return null;
        File file = new File(blobs, blobId);
        if (!file.exists()) return null;
        return readContentsAsString(file);
    }
}
