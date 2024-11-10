package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 *  .git/ -- top level folder for all persistent data in your lab12 folder
 *      - commits/ -- folder containing all of the preivous commits
 *      - blobs/ -- folder containing all of the previous blob whose file name is their blobId
 *      - staging -- file containg the staging area object
 *      - repository -- file containg the repository object
 */

public class Repository implements Serializable {
    /**
     * <p>
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    // commit tree is immutable
    // Every object–every blob and every commit in our case–has a unique integer id that serves as a reference to the object.
    // keep track on the newest commit of the current branch
    // The head pointer keeps track of where in the linked list we currently are.
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* store Repository object in a file*/
    public static final File repositoryFile = new File(GITLET_DIR, "repository.txt");

    /* Each branch (e.g., main, master, feature) is just a pointer (reference) to the latest commit on that branch.*/
    // HEAD is a special pointer that refers to the current branch
    private HashMap<String, String> head = new HashMap<>();;

    /* we will keep track
    on what branch will HEAD pointer in at hashmap head and the actual reference to commit in here*/
    private String HEAD;

    /**
     * Initializes a new repository with a master branch and an initial commit.
     */
    public Repository() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        Repository.setup();
        Commit init = new Commit();
        head.put("master", init.getHash());
        head.put("HEAD", "master");
        HEAD = init.getHash();
        Staging staging = new Staging();
        staging.save();
        save();
    }

    private static void setup() {
        try {
            GITLET_DIR.mkdir();
            Blobs.blobs.mkdir();
            Commit.commits.mkdir();
            repositoryFile.createNewFile();
            Staging.StagingArea.createNewFile();
        } catch (FileAlreadyExistsException e) {
            // do nothing
        } catch (IOException e) {
            System.err.println("Failed to set up the repository: " + e.getMessage());
            throw new IllegalStateException("Setup failed", e);
        }
    }

    /* add new commit to the given branch
    * If no files have been staged, abort. Print the message No changes added to the commit.*/
    // TODO potential risk if u want to detached HEAD while current implementation works fine for attaching new commit to the
    //  previous commit without changing branch
    public void commit(String message){
        if (emptyStagingArea()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // overWrite branch would work now because we set HEAD point to that branch by using checkout branchName
        // there might have multiple pointers that point to the same commit,
        // but we will only check which HEAD will point at
        String currentBranch = head.get("HEAD");
        Commit commit = new Commit(message, head.get(currentBranch));
        head.put(currentBranch, commit.getHash());
        HEAD = commit.getHash();
        // Clear the staging area after a successful commit
        Staging stagingArea = Staging.load();
        stagingArea.clear();
        stagingArea.save();
        save();
    }

    // stage the new version of that file if content in CWD and the latest commit has different
    // otherwise remove it
    public void add(String filename) {
        // get the latest commit in master branch
        Commit currentCommit = Commit.getCommit(HEAD);
        File file = new File(CWD, filename);

        String comparisonResult = compare(filename, file, currentCommit.getContent());

        Staging staging = Staging.load();
        if (staging == null) {
            staging = new Staging();
        }

        if (comparisonResult != null) {
            Blobs newFileContent = new Blobs(readContentsAsString(file));
            newFileContent.save();
            staging.getAddition().put(filename, newFileContent.getBlobId());
            staging.save();
        } else {
            if (staging.getAddition().containsKey(filename)) {
                staging.getAddition().remove(filename);
                staging.save();
            }
        }
    }

    // remove addition if filename exists in addition else add it to deletion if that file exists in previous file
    public void remove(String filename) {
        Commit tmp = Commit.getCommit(HEAD);
        HashMap<String, String> n = tmp.getContent();
        Staging staging = Staging.load();
        // if it is currently staged for addition
        if (staging.getAddition().containsKey(filename)) {
            staging.getAddition().remove(filename);
            // If the file is tracked in the current commit
            // stage it for removal and remove the file from the working directory if the user has not already done so
        } else if (n.containsKey(filename)) {
            staging.getDeletion().add(filename);
            File f = new File(filename);
            restrictedDelete(f);
        } else {
            // the file is neither staged nor tracked by the head commit
            System.out.println("No reason to remove the file.");
        }
        staging.save();
    }

    /* return null only if the file is identical to the file name in previousCommit*/
    public static String compare(String filename, File file, HashMap<String, String> previousCommit) {
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        if (previousCommit == null) {
            return filename + "(untracked)";
        }

        String currentFileContent = readContentsAsString(file);
        String previousBlobId = previousCommit.get(filename);

        // If the file is not tracked in the previous commit
        if (previousBlobId == null) {
            return filename + "(untracked)";
        }

        String previousFileContent = Blobs.getContent(previousBlobId);

        // If the previous file content is null or the contents differ
        if (previousFileContent == null || !currentFileContent.equals(previousFileContent)) {
            return filename + "(modified)";
        }

        // If the content is identical, return null
        return null;
    }

    public String find(String message){
        StringBuilder finded = new StringBuilder();
        List<String> filenames = plainFilenamesIn(Commit.commits);
        assert filenames != null;
        for (String filename : filenames) {
            Commit tmp = Commit.getCommit(filename);
            assert tmp != null;
            if (tmp.getMessage().equals(message)) {
                finded.append(filename).append("\n");
            }
        }
        return finded.toString();
    }

    public String status(){
        StringBuilder status = new StringBuilder();
        Staging staging = Staging.load();
        status.append("=== Branches ===" + "\n");
        String currentBranch = head.get("HEAD");
        for (String branch : head.keySet()) {
            if (!branch.equals("HEAD")) {
                if (branch.equals(currentBranch)) {
                    status.append("*").append(branch).append("\n");
                } else {
                    status.append(branch).append("\n");
                }
            }
        }

        status.append("\n=== Staged Files ===" + "\n");
        for (String addition : staging.getAddition().keySet()) {
            status.append(addition).append("\n");
        }

        status.append("\n=== Removed Files ===" + "\n");
        for (String deletion : staging.getDeletion()) {
            status.append(deletion).append("\n");
        }

        List<String> untracked = new ArrayList<String>();
        status.append("\n=== Modifications Not Staged For Commit ===" + "\n");
        HashMap<String, String> future = Commit.SetContent(Commit.getCommit(HEAD));
        List<String> files = plainFilenamesIn(CWD);
        for (String file : files) {
            File currentFile = new File(CWD, file);

            // Case 1: File is tracked but deleted in the working directory (not staged for removal)
            // exist in the future but not exist in the CWD
            if (future.containsKey(file) && !currentFile.exists() && !staging.getDeletion().contains(file)) {
                status.append(file).append(" (deleted)\n");
            }
            // Case 2: File is modified but not staged
            else if (future.containsKey(file)) {
                File f = new File(CWD, file);
                String tmp = compare(file, f, future);
                if (tmp != null) {
                    if (tmp.endsWith("(modified)")) {
                        status.append(tmp).append("\n");
                    } else if (tmp.endsWith("(untracked)")) {
                        untracked.add(tmp.substring(0, tmp.length() - "(untracked)".length()));
                    }
                }
            }
            // Case 3: File is untracked (doesn't exist in the future commit)
            // exist in the CWD but not future
            else if (!staging.getAddition().containsKey(file)) {
                untracked.add(file);
            }
        }

        status.append("\n=== Untracked Files ===" + "\n");
        for (String untrackedFile : untracked) {
            status.append(untrackedFile).append("\n");
        }

        status.append("\n");
        return status.toString();
    }

    // overwrite the file in CWD with the corresponding file name in the given commit do not stage it
    private boolean overWrite(String filename, String commitName, String commit) {
        File file = new File(CWD, filename);
        if (!file.exists()) {
            return false;
        }
        // get the previous commits' hash code
        String previousContent = Commit.getCommit(commit).getFileContent(commitName);
        if (previousContent == null) {
            return false;
        }
        writeContents(file, previousContent);
        return true;
    }

    public void overWriteWithSameFileName(String filename) {
        boolean tmp = overWrite(filename, filename, HEAD);
        if (!tmp) {
            System.out.println("File does not exist in that commit. ");
            System.exit(0);
        }
    }

    public void overWriteWithDifferentFileName(String filename, String commitName) {
        boolean tmp = overWrite(filename, commitName, HEAD);
        if (!tmp){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    // set CWD with the latest commit in that branch and set HEAD to that branch
    public void overWriteBranch(String branch) {
        // get the latest commit in that branch
        String branchId = head.get(branch);
        if (branchId == null) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branch.equals(head.get("HEAD"))) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        overWriteCWD(branchId);
        // it will set HEAD to the given branch if HEAD and given branch didn't point to the same branch
        head.put("HEAD", branch);
        HEAD = branchId;
        // clear the staging area
        Staging staging = Staging.load();
        staging.clear();
        save();
    }

    // overwrite CWD with the given commit will exit if CWD have untracked files
    private void overWriteCWD(String commitId) {
        // Get the current commit and the target branch commit
        String currentCommitId = HEAD;
        Commit targetCommit = Commit.getCommit(commitId);

        // Get content maps for both the current and target branch commits
        HashMap<String, String> branchContent = targetCommit.getContent();

        // Check for untracked files in the working directory that would be overwritten
        untrackedFileExists();

        clearDirectory();

        if (branchContent == null){
            return;
        }

        for (String filename : branchContent.keySet()) {
            File currentFile = new File(CWD, filename);
            String fileContent = targetCommit.getFileContent(filename);
            if (fileContent != null) {
                writeContents(currentFile, fileContent);
            }
        }
    }

    private void untrackedFileExists(){
        List<String> files = plainFilenamesIn(CWD);
        Commit currentCommit = Commit.getCommit(HEAD);
        HashMap<String, String> currentContent = currentCommit.getContent();
        for (String file : files) {
            if (!currentContent.containsKey(file)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
    }

    //  Creates a new branch with the given name, and points it at the current head commit.
    public void branch(String newBranch) {
        head.put("HEAD", newBranch);
        head.put(newBranch, HEAD);
        save();
    }

    public void removeBranch(String branch) {
        // TODO it could cause result in work lost if we dont merged it
       if (head.containsKey(branch)) {
           if (branch.equals(head.get("HEAD"))) {
               System.out.println("Cannot remove the current branch.");
               System.exit(0);
           }
           else {
               head.remove(branch);
               save();
           }
       } else {
           System.out.println("branch with that name does not exist.");
           System.exit(0);
       }
    }

    // similar to overWriteBranch but with a given str argument as commitID
    public void reset(String commitId){
        Commit commit = Commit.getCommit(commitId);
        if (commit == null) {
            System.out.println("No commit with that id exists.");
        }
        overWriteCWD(commitId);
        // TODO update the branch of reset HEAD

        HEAD = commitId;
        // clear the staging area
        Staging staging = Staging.load();
        staging.clear();
        save();
    }

    /* base is the current, so condition like
       (1) Modified in current branch, not modified in given
       (2) Modified in both branches, same content
       (3) Only in current branch
       (4) Unmodified in given, removed in current
       will be ignored
       and the conditions we care is following
       (1) Modified in given branch, not modified in current
       (2) Only in given branch
       (3) Unmodified in current, removed in given*/
    public void merge(String branch) {
        // Find the split point (latest common ancestor).
        String split = getAncestor(HEAD, branch);
        String branchId = head.get(branch);

        if (!emptyStagingArea()){
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!head.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        untrackedFileExists();
        if (HEAD.equals(branchId)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        // Handle ancestor cases.
        if (split.equals(branchId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.equals(HEAD)) {
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        // Get content of current, given, and split point commits.
        HashMap<String, String> currentContent = Commit.getCommit(HEAD).getContent();
        HashMap<String, String> branchContent = Commit.getCommit(branchId).getContent();
        HashMap<String, String> splitContent = Commit.getCommit(split).getContent();

        // Traverse through all files from current, given, and split point.
        Set<String> allFiles = new HashSet<>();
            allFiles.addAll(currentContent.keySet());
            allFiles.addAll(branchContent.keySet());
            allFiles.addAll(splitContent.keySet());
        // Handle each file based on its status in the three commits.
        for (String file : allFiles) {
            // each file has its unique blob id, so we don't need to check its content
            String splitBlob = splitContent.getOrDefault(file, null);
            String currentBlob = currentContent.getOrDefault(file, null);
            String branchBlob = branchContent.getOrDefault(file, null);

            if (splitBlob == null && branchBlob != null && currentBlob == null) {
                // (1) File only in given branch (new file)
                checkOutAndStage(file, branchBlob);
            } else if (splitBlob != null && branchBlob != null && splitBlob.equals(branchBlob) && currentBlob == null) {
                // (2) File removed in current, unmodified in given
                removeFile(file);
            } else if (splitBlob != null && !splitBlob.equals(branchBlob) && splitBlob.equals(currentBlob)) {
                // (3) Modified in given branch, not modified in current
                checkOutAndStage(file, branchBlob);
            } else if (splitBlob != null && !splitBlob.equals(currentBlob) && !splitBlob.equals(branchBlob) && !currentBlob.equals(branchBlob)) {
                // (4) Conflict: modified in both branches differently
                handleConflict(file, currentBlob, branchBlob);
            }
        }
        String mergeLog = String.format("Merged %s into %s.", branch, head.get("HEAD"));
        Commit merged = new Commit(mergeLog, HEAD);
        merged.setSecondParent(branchId);
    }

    // Helper method to check out and stage a file
    private void checkOutAndStage(String filename, String blobId) {
        String content = Blobs.getContent(blobId);
        File file = new File(CWD, filename);
        writeContents(file, content);
        Staging staging = Staging.load();
        staging.getAddition().put(filename, blobId);
        staging.save();
    }

    // Helper method to remove a file and untrack it
    private void removeFile(String filename) {
        File file = new File(CWD, filename);
        if (file.exists()) {
            file.delete();
        }
        Staging staging = Staging.load();
        staging.getDeletion().add(filename);
        staging.save();
    }

    private void handleConflict(String filename, String currentBlob, String branchBlob) {
        String currentContent = currentBlob != null ? Blobs.getContent(currentBlob) : "";
        String branchContent = branchBlob != null ? Blobs.getContent(branchBlob) : "";

        String conflictContent = "<<<<<<< HEAD\n" +
                currentContent +
                "\n=======\n" +
                branchContent +
                "\n>>>>>>>\n";

        File file = new File(CWD, filename);
        writeContents(file, conflictContent);

        System.out.println("encountered a merge conflict.");

        // Stage the conflicted file for addition
        Staging staging = Staging.load();
        staging.getAddition().put(filename, Blobs.saveContent(conflictContent));
        staging.save();
    }

    // The time complexity of this solution is O(N) where N
    // is the total number of ancestor commits for the two branches and
    private String getAncestor(String current, String given) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        // Start BFS from both current and given branches
        queue.add(current);
        queue.add(given);

        // Perform BFS until we find a common ancestor
        while (!queue.isEmpty()) {
            String commit = queue.poll();

            if (visited.contains(commit)) {
                return commit;
            }

            visited.add(commit);

            // Enqueue parent commits
            Commit n = Commit.getCommit(commit);
            String parent = n.getParent();
            if (parent != null) {
                queue.add(parent);
            }
        }

        return null; // No common ancestor found (unlikely in Git)
    }

    public void log(){
        Commit current = Commit.getCommit(HEAD);
        System.out.println(current.log());
    }

    public void globalLog(){
        System.out.println(Commit.GlobalLog());
    }

    private boolean emptyStagingArea(){
        Staging staging = Staging.load();
        return staging.getAddition().isEmpty() && staging.getDeletion().isEmpty();
    }

    // clear entire CWD except .git
    private void clearDirectory(){
        List<String> files = plainFilenamesIn(CWD);
        if (files == null){
            return;
        }
        for (String file : files) {
            File f = new File(file);
            if (!file.equals(".git")){
                f.delete();
            }
        }
    }

    public void save(){
        writeObject(repositoryFile, this);
    }

    public static Repository load(){
        return readObject(repositoryFile, Repository.class);
    }
}