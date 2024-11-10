package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 *  The structure of a Capers Repository is as follows:
 *
 */

public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    public static final File commits = new File(Repository.GITLET_DIR, "commits");

    // a mapping of file names to blob references
    private HashMap<String, String> content;
    // file object which assigned with its corresponding file address
    private File commitFile;

    /** The message of this Commit. */
    private final String message;
    private final String timestamp;
    // (for merges) a second parent reference
    private String parent;
    // if it's regular commit secondParent will be set to null
    // it will not be null if the commit is merged from two commits
    private String secondParent;
    private String hashCode;

    // only use this constructor to initialize a repo
    public Commit() {
        if (!empty()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        this.message = "initial commit";
        this.parent = null;
        this.secondParent = null;
        Date now = new Date();
        this.timestamp = String.valueOf((now.getTime()/1000));
        hashCode = sha1(message + timestamp);
        commitFile = new File(commits, hashCode);
        save();
    }

    // check if this directory is not initialized by checking if its log is empty
    private boolean empty(){
        return Repository.repositoryFile.exists();
    }


    // Repository class will keep track on pointers so we don't have to worry about how to find its parent or the latest commit
    // in the branch now
    // we will leave the implementation detail at Repository e.g. update staging area, figure out how pointer
    // The staging area is cleared after a commit.
    // commit will never modify content outside the .git directory
    public Commit(String message, String parent) {
        Commit parentCommit = Commit.getCommit(parent);

        // set metadata
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
        this.parent = parent;
        this.secondParent = null;

        // Set the content based on parent commit and staging area
        content = SetContent(parentCommit);

        // Calculate hash and initialize commit file after setting content
        hash();
        commitFile = new File(commits, hashCode);
        save();
    }

    // return the content for current commit which based on its parent content and the modification with staging area
    // will clear the staging area afterward
    public static HashMap<String, String> SetContent(Commit parentCommit) {
        Staging stagingArea = Staging.load();
        // initialize current content hashmap with its parent and then update it by checking the staging area content
        HashMap<String, String> contentCopy = new HashMap<>(parentCommit.getContent());

        HashMap<String, String> additionalContent = stagingArea.getAddition();
        HashSet<String> removalContent = stagingArea.getDeletion();

        // Update content with additions and handle deletions
        for (String fileName : additionalContent.keySet()) {
            if (!removalContent.contains(fileName)) {
                contentCopy.put(fileName, additionalContent.get(fileName));
            }
        }
        return contentCopy;
    }

    // get commit unique hashcode by provide string with metadata and assign the actual address of current commit
    private void hash() {
        String n = timestamp + message + content.toString();
        hashCode = sha1(n);
    }

    public String getHash(){
        return hashCode;
    }

    public static String getParent(String commit){
        Commit parent = Commit.getCommit(commit);
        return parent.getParent();
    }

    public String getParent(){
        return parent;
    }

    public String getSecondParent(){
        return secondParent;
    }

    public void setSecondParent(String secondParent){
        this.secondParent = secondParent;
    }

    public String getMessage(){
        return message;
    }
    public Commit load(File file){
        return readObject(file, Commit.class);
    }

    // store each commit obj with hashCode as its file name must initialize commitFile first
    public void save(){
        writeObject(commitFile, this);
    }

    public HashMap<String, String> getContent() {
        return content;
    }

    // get commit obj by providing its file name/ hash code
    public static Commit getCommit(String hash){
        File obj = new File(commits, hash);
        if (!obj.exists()){
            return null;
        }
        return readObject(obj, Commit.class);
    }


    // return the content of that file in the current commit
    public String getFileContent(String filename){
        String tmp = null;
        if (content == null){
            return null;
        }
        String hash = content.get(filename);
        if (hash != null){
            tmp = Blobs.getContent(hash);
        }
        return tmp;
    }

    // generate a single block of commit log
    public String writeCommitLog(){
        String n = String.format("===\n" +
                "commit %s\n" +
                "Date %s\n" +
                "%s\n\n",
                hashCode, timestamp, message);
        return n;
    }

    // generate a single block of merge log
    // two hex numbers consist of the first seven digits of the first and second parent's ids
    public String writeMergeLog(){
        String firstParent = this.parent.substring(0, 7);
        String secondParent = this.secondParent.substring(0, 7);
        String n =  String.format("===\n" +
                        "commit %s\n" +
                        "Merge %s %s\n" +
                        "Date %s\n" +
                        "%s\n\n",
                hashCode, firstParent, secondParent, timestamp, message);
        return n;
    }

    public String log(){
        StringBuilder tmp = new StringBuilder();
        Commit i = this;
        while (i != null){
            String secondParent = i.getSecondParent();
            if (secondParent == null){
                tmp.append(i.writeCommitLog());
            } else {
                tmp.append(i.writeMergeLog());
            }
            i = Commit.getCommit(i.getParent());
        }
        return tmp.toString();
    }

    // traverse through the entire commit directory and return the log from all of them
    public static String GlobalLog(){
        StringBuilder tmp = new StringBuilder();
        List<String> commitFiles = plainFilenamesIn(commits);
        for (String filename : commitFiles){
            Commit i = Commit.getCommit(filename);
            String secondParent = i.getSecondParent();
            if (secondParent == null){
                tmp.append(i.writeCommitLog());
            } else {
                tmp.append(i.writeMergeLog());
            }
        }
        return tmp.toString();
    }
}
