package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

public class Staging implements Serializable {
    // access it by using file accessing
    // file name : BlobId
    private HashMap<String, String> addition;
    private HashSet<String> deletion;

    public static final File StagingArea = Utils.join(Repository.GITLET_DIR, "staging");

    // if u made change with previous commit u will create blob object
    public Staging() {
        addition = new HashMap<>();
        deletion = new HashSet<>();
    }

    public static Staging load() {
        return readObject(StagingArea, Staging.class);
    }

    public void save(){
        writeObject(StagingArea, this);
    }

    public HashMap<String, String> getAddition() {
        return addition;
    }

    public HashSet<String> getDeletion() {
        return deletion;
    }

    public void clear() {
        addition.clear();
        deletion.clear();
    }
}
