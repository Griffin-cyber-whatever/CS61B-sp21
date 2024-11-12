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
    public Commit commit(String message){
        if (emptyStagingArea()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // overWrite branch would work now because we set HEAD point to that branch by using checkout branchName
        // there might have multiple pointers that point to the same commit,
        // but we will only check which HEAD will point at
        String currentBranch = head.get("HEAD");
        Commit commit = new Commit(message, HEAD);
        head.put(currentBranch, commit.getHash());
        HEAD = commit.getHash();
        // Clear the staging area after a successful commit
        Staging stagingArea = Staging.load();
        stagingArea.clear();
        stagingArea.save();
        save();
        return commit;
    }

    // stage the new version of that file if content in CWD and the latest commit has different
    // otherwise remove it
    public void add(String filename) {
        // get the latest commit in master branch
        Commit currentCommit = Commit.getCommit(HEAD);
        File file = new File(CWD, filename);

        Staging staging = Staging.load();
        if (staging == null) {
            staging = new Staging();
        }

        if (staging.getDeletion().contains(filename)) {
            staging.getDeletion().remove(filename);
            staging.save();
            return;
        }

        String comparisonResult = compare(filename, file, currentCommit.getContent());

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

        if (staging == null) {
            staging = new Staging();
        }
        // if it is currently staged for addition
        if (staging.getAddition().containsKey(filename)) {
            staging.getAddition().remove(filename);
            // If the file is tracked in the current commit
            // stage it for removal and remove the file from the working directory if the user has not already done so
        } else if (n != null && n.containsKey(filename)) {
            staging.getDeletion().add(filename);
            File f = new File(CWD, filename);
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
        if (finded.length() == 0){
            return "Found no commit with that message.";
        } else {
            return finded.toString();
        }
    }

    public String status() {
        StringBuilder status = new StringBuilder();
        Staging staging = Staging.load();
        status.append("=== Branches ===\n");

        String currentBranch = head.get("HEAD");

        // Collect branch names (excluding "HEAD"), sort, and append
        List<String> branches = new ArrayList<>();
        for (String branch : head.keySet()) {
            if (!branch.equals("HEAD")) {
                if (branch.equals(currentBranch)) {
                    branches.add("*" + branch);
                } else {
                    branches.add(branch);
                }
            }
        }
        appendSortedList(status, branches);

        // Append sorted staged files
        status.append("\n=== Staged Files ===\n");
        List<String> additions = new ArrayList<>(staging.getAddition().keySet());
        appendSortedList(status, additions);

        // Append sorted removed files
        status.append("\n=== Removed Files ===\n");
        List<String> deletions = new ArrayList<>(staging.getDeletion());
        appendSortedList(status, deletions);

        // Collect and append sorted modifications not staged for commit
        status.append("\n=== Modifications Not Staged For Commit ===\n");
        HashMap<String, String> future = Commit.SetContent(Commit.getCommit(HEAD));
        List<String> files = plainFilenamesIn(CWD);
        List<String> untracked = new ArrayList<>();
        List<String> modifications = new ArrayList<>();

        for (String file : files) {
            File currentFile = new File(CWD, file);

            // Case 1: File is tracked but deleted in the working directory (not staged for removal)
            if (future.containsKey(file) && !currentFile.exists() && !staging.getDeletion().contains(file)) {
                modifications.add(file + " (deleted)");
            }
            // Case 2: File is modified but not staged
            else if (future.containsKey(file)) {
                String tmp = compare(file, currentFile, future);
                if (tmp != null && tmp.endsWith("(modified)")) {
                    modifications.add(tmp);
                } else if (tmp != null && tmp.endsWith("(untracked)")) {
                    untracked.add(tmp.substring(0, tmp.length() - "(untracked)".length()));
                }
            }
            // Case 3: File is untracked (doesn't exist in the future commit)
            else if (!staging.getAddition().containsKey(file)) {
                untracked.add(file);
            }
        }

        appendSortedList(status, modifications);

        // Append sorted untracked files
        status.append("\n=== Untracked Files ===\n");
        appendSortedList(status, untracked);

        return status.toString();
    }

    private void appendSortedList(StringBuilder sb, List<String> list) {
        Collections.sort(list);
        for (String item : list) {
            sb.append(item).append("\n");
        }
    }

    // overwrite the file in CWD with the corresponding file name in the given commit do not stage it
    // only returns false if that filename didn't exist in that commit
    private boolean overWrite(String filename, String commit) {
        // get the file content in that commit
        Commit previousCommit = Commit.getCommit(commit);
        if (previousCommit == null) {
            System.out.println("No commit with that id exists. ");
            System.exit(0);
        }

        String previousContent = previousCommit.getFileContent(filename);
        if (previousContent == null) {
            System.out.println("File does not exist in that commit. ");
            System.exit(0);
        }
        // overWrite the file in CWD if the file exists, otherwise put it in the CWD
        File file = new File(CWD, filename);
        writeContents(file, previousContent);
        return true;
    }

    public void overWriteWithSameFileName(String filename) {
        boolean tmp = overWrite(filename, HEAD);
    }

    // takes the version of the file with file name in that commit
    public void overWriteWithDifferentFileName(String filename, String commitName) {
        boolean tmp = overWrite(filename,  commitName);
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

    // check actual content here
    private void untrackedFileExists(){
        List<String> files = plainFilenamesIn(CWD);
        if (files == null || files.isEmpty()) {
            return;
        }
        Staging staging = Staging.load();
        Commit currentCommit = Commit.getCommit(HEAD);
        HashMap<String, String> currentContent = currentCommit.getContent();
        for (String file : files) {
            // An untracked file is identified by its filename not being present in the last commit.
            // The file’s name hasn’t been added to the staging area (using git add).
            if ( currentContent == null ||  currentContent.isEmpty() || !(currentContent.containsKey(file) || staging.getAddition().containsKey(file))) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    //  Creates a new branch with the given name, and points it at the current head commit.
    // it will not change which branch that HEAD currently in, checkout [branch name] will deal with it
    public void branch(String newBranch) {
        if (head.containsKey(newBranch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
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
            return;
        }
        overWriteCWD(commitId);

        HEAD = getFullCommitName(commitId);
        String currentBranch = head.get("HEAD");
        head.put(currentBranch, HEAD);

        // clear the staging area
        Staging staging = Staging.load();
        staging.clear();
        save();
    }

    private String getFullCommitName(String commitId) {
        List<String> files = plainFilenamesIn(Commit.commits);
        for (String filename : files) {
            String tmp = filename.substring(0, 6);
            if (filename.equals(commitId) || tmp.equals(commitId)) {
                return filename;
            }
        }
        System.out.println("No such commit exists. error occurs in getFullCommitName.");
        System.exit(0);
        return commitId;
    }

    public void merge(String branch) {
        // Find the split point (the latest common ancestor).
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

        String split = findLCA(head.get("HEAD"), branch, getAncestor(HEAD, branchId));

        // Handle ancestor cases.
        if (split.equals(branchId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.equals(HEAD)) {
            System.out.println("Current branch fast-forwarded.");
            overWriteBranch(branch);
            return;
        }

        // Get content of current, given, and split point commits.
        HashMap<String, String> currentContent = Commit.getCommit(HEAD).getContent();
        HashMap<String, String> branchContent = Commit.getCommit(branchId).getContent();
        HashMap<String, String> splitContent = Commit.getCommit(split).getContent();

        // Traverse through all files from current, given, and split point.
        currentContent = (currentContent != null) ? currentContent : new HashMap<>();
        branchContent = (branchContent != null) ? branchContent : new HashMap<>();
        splitContent = (splitContent != null) ? splitContent : new HashMap<>();

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

            // Requirement 9: File modified differently in the current and given branches (conflict)
            if ( splitBlob != null && (currentBlob != null || branchBlob != null)  && !splitBlob.equals(currentBlob) && !splitBlob.equals(branchBlob)
                && !Objects.equals(currentBlob, branchBlob)) {
                handleConflict(file, currentBlob, branchBlob);
            }
            // Requirement 1: File present only in the given branch, not in split point or current branch
            else if (splitBlob == null && branchBlob != null && currentBlob == null) {
                checkOutAndStage(file, branchBlob);
            }
            // Requirement 2: File modified in the given branch since split point, unmodified in the current branch
            else if (splitBlob != null && currentBlob != null && branchBlob != null && currentBlob.equals(splitBlob) && !splitBlob.equals(branchBlob)) {
                checkOutAndStage(file, branchBlob);
            }
            // Requirement 7: File present at split point, unmodified in the current branch,
            // and absent in the given branch
            // Main issue:
            // it will evaluate as the same as splitBlob != null && currentBlob.equals(splitBlob) && !splitBlob.equals(branchBlob)
            else if (splitBlob != null && currentBlob != null && branchBlob == null && currentBlob.equals(splitBlob)) {
                removeFile(file);
            }

        }
        String mergeLog = String.format("Merged %s into %s.", branch, head.get("HEAD"));
        Commit merged = commit(mergeLog);
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
                "=======\n" +
                branchContent +
                ">>>>>>>\n";

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
            if (n != null) {
                String parent = n.getParent();
                if (parent != null) {
                    queue.add(parent);
                }
            }
        }

        return null; // No common ancestor found (unlikely in Git)
    }

    private String findLCA(String current, String branch, String initialAncestor) {
        int maxDepth = 0;
        String latestCommonAncestor = initialAncestor;

        // Traverse through all branches and find the deepest common ancestor.
        for (String branchName : head.keySet()) {
            if (!branchName.equals(current) && !branchName.equals(branch) && !branchName.equals("HEAD")) {
                String branchHead = head.get(branchName);
                int depth = findDepth(branchHead, initialAncestor);

                // If the depth is greater, update the deepest common ancestor.
                if (depth > maxDepth) {
                    maxDepth = depth;
                    latestCommonAncestor = branchHead;
                }
            }
        }

        return latestCommonAncestor;
    }

    // Helper method to find the depth of a given commit relative to the common ancestor.
    private int findDepth(String givenCommit, String commonAncestor) {
        int depth = -1;
        Commit current = Commit.getCommit(givenCommit);

        while (current != null) {
            if (current.getHash().equals(commonAncestor)) {
                return depth;
            }
            current = Commit.getCommit(current.getParent());
            depth++;
        }

        // If we didn't find the common ancestor in the path, return a large negative value.
        return -1;
    }

    public void log(){
        Commit current = Commit.getCommit(HEAD);
        String tmp = current.log();
        System.out.println(tmp.substring(0, tmp.length()-1));
    }

    public void globalLog(){
        String tmp = Commit.GlobalLog();
        System.out.println(tmp.substring(0, tmp.length()-1));
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
        if (!GITLET_DIR.exists() || !repositoryFile.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
            return null;
        } else {
            return readObject(repositoryFile, Repository.class);
        }
    }
}