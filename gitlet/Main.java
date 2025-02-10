package gitlet;


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Formatter;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Izaac Salvador Ruiz
 */
public class Main {



    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }

        switch (args[0]) {
        case "init":
            initHelper(args);
            break;
        case "add":
            validateNumArgs("add", args, 2);
            addFile(Utils.join(CWD, args[1]));
            break;
        case "commit":
            validateNumArgs("commit", args, 2);
            saveCommit(args[1]);
            break;
        case "log" :
            validateNumArgs("log", args, 1);
            log();
            break;
        case "global-log":
            validateNumArgs("global-log", args, 1);
            globalLog();
            break;
        case "branch":
            branchHelper(args);
            break;
        case "rm-branch":
            rmBranchHelper(args);
            break;
        case "checkout":
            checkoutHelper(args);
            break;
        case "find":
            validateNumArgs("find", args, 2);
            find(args[1]);
            break;
        case "rm":
            validateNumArgs("rm", args, 2);
            remove(Utils.join(CWD, args[1]));
            break;
        case "reset":
            validateNumArgs("reset", args, 2);
            reset(args[1]);
            break;
        case "status":
            status();
            break;
        case "merge":
            merge(Utils.readObject(Utils.join(REFS, args[1]), Commit.class));
            break;
        default:
            exitWithError("No command with that name exists.");
        }
        return;
    }

    /** Creates and persists a branch from arguments given in ARGS. */
    public static void branchHelper(String... args) throws IOException {
        validateNumArgs("branch", args, 2);
        File newBranch = Utils.join(REFS, args[1]);
        if (newBranch.exists()) {
            exitWithError("A branch with that name already exists.");
        }
        Utils.writeObject(newBranch, currComm());
        newBranch.createNewFile();
    }

    /** Process the ARGS needed to process the "checkout" command. */
    public static void checkoutHelper(String... args) throws IOException {
        if (args.length <= 3) {
            if (args[1].equals("--")) {
                validateNumArgs("checkout", args, 3);
                checkoutFile(Utils.join(REFS,
                                Utils.readContentsAsString(CURR_BRANCH)),
                        args[2]);
            } else {
                validateNumArgs("checkout", args, 2);
                checkoutBranch(Utils.join(REFS, args[1]));
            }

        } else if (args[2].equals("--")) {
            validateNumArgs("checkout", args, 4);
            checkoutFile(Utils.join(COMMITS, args[1]), args[3]);
        } else {
            exitWithError("Incorrect operands.");
        }
    }

    /** Helps process the ARGS needed to initialize the Gitlet
     * repository. */
    public static void initHelper(String... args) throws IOException {
        if (GITLET_FOLDER.exists()) {
            exitWithError("A Gitlet version-control "
                    + "system already exists in the current directory.");
        }
        validateNumArgs("init", args, 1);
        setUpPersistence();
        File master = Utils.join(REFS, "master");
        master.createNewFile();
        Utils.writeContents(CURR_BRANCH, "master");
        Commit newCom = saveCommit("initial commit");
        Utils.writeObject(master, newCom);
    }

    /** Sets up all the files needed to set up the Gitlet directory. */
    public static void setUpPersistence() throws IOException {
        GITLET_FOLDER.mkdir();
        COMMITS.mkdir();
        REFS.mkdir();
        STAGING_FOLDER.mkdir();
        CURR_BRANCH.createNewFile();
        OBJECTS.mkdir();
        STAGING_HASH.createNewFile();
        Utils.writeObject(STAGING_HASH, new LinkedHashMap<File, String>());
        STAGING_REMOVE.mkdir();

    }

    /** Creates a Commit object out of the specified MSG
     *  and persists it into COMMITS.
     *  @return Commit */
    public static Commit saveCommit(String msg) throws IOException {
        gitletExists();
        File head = Utils.join(REFS, Utils.readContentsAsString(CURR_BRANCH));
        Commit newCom = new Commit(msg, head, new LinkedHashMap<>());

        if (msg.isBlank()) {
            exitWithError("Please enter a commit message.");
        }
        if (msg.equals("initial commit")) {
            Utils.writeObject(head, newCom);
        } else if (STAGING_FOLDER.list().length == 0
            && STAGING_REMOVE.list().length == 0) {
            exitWithError("No changes added to the commit.");
        } else {
            LinkedHashMap<File, String> currContents = currComm().getContents();

            for (File file : STAGING_FOLDER.listFiles()) {
                Blob currBlob = Utils.readObject(file, Blob.class);
                File blobFile = Utils.join(OBJECTS, currBlob.getHash());

                currContents.put(currBlob.getParentFile(),
                        currBlob.getHash());

                blobFile.createNewFile();
                Utils.writeObject(blobFile, currBlob);
                file.delete();

            }
            for (String fileName: Utils.plainFilenamesIn(STAGING_REMOVE)) {
                currContents.remove(Utils.join(CWD, fileName));
            }
            newCom = new Commit(msg, head, currContents);
        }

        File comFile = Utils.join(COMMITS, newCom.getHash());
        comFile.createNewFile();
        Utils.writeObject(comFile, newCom);
        Utils.writeObject(head, newCom);
        return newCom;
    }

    /** Helps process the ARGS of the rm command. */
    public static void rmBranchHelper(String... args) {
        validateNumArgs("rm-branch", args, 2);
        File branch = Utils.join(REFS, args[1]);
        if (!branch.exists()) {
            exitWithError("A branch with that name does not exist.");
        }
        if (branch.getName().equals(
                Utils.readContentsAsString(CURR_BRANCH))) {
            exitWithError("Cannot remove the current branch.");
        }
        branch.delete();
    }

    /** Creates a copy of the given FILE and adds it to the staging area. */
    public static void addFile(File file) throws IOException {
        gitletExists();
        if (!file.exists()) {
            exitWithError("File does not exist.");
        }
        LinkedHashMap<File, String> hash =
                Utils.readObject(STAGING_HASH, LinkedHashMap.class);
        Blob newBlob = new Blob(file);
        Commit currCommit = currComm();
        String commitBlob = currCommit.getContents().get(file);

        if (commitBlob == null || !commitBlob.equals(newBlob.getHash())) {
            File blobFile = Utils.join(STAGING_FOLDER, newBlob.getHash());
            Utils.writeObject(blobFile, newBlob);
            blobFile.createNewFile();
            hash.put(file, newBlob.getHash());
        } else {
            Utils.join(STAGING_FOLDER, commitBlob).delete();
            hash.remove(file);
            Utils.join(STAGING_REMOVE, file.getName());
        }
        Utils.writeObject(STAGING_HASH, hash);
    }

    /** Prints all Commit objects made in the Gitlet directory. */
    public static void globalLog() {
        gitletExists();
        String str = "";
        for (String fileName : Utils.plainFilenamesIn(COMMITS)) {
            Commit currComm = Utils.readObject(Utils.join(COMMITS,
                    fileName), Commit.class);
            str += currComm.toString();
        }
        System.out.println(str);
    }

    /** Presents a string layout of all the Commit objects in a given branch. */
    public static void log() {
        gitletExists();
        Commit startingCommit = currComm();
        String str = "";
        while (startingCommit.getParent() != null) {
            str += startingCommit.toString();
            startingCommit = Utils.readObject(Utils.join(COMMITS,
                    startingCommit.getParent()), Commit.class);
        }
        str += startingCommit;
        System.out.println(str);
    }

    /** Replaces or creates FILE specified in the Commit
     * referenced by COMMITFILE. */
    public static void checkoutFile(File commitFile, String file) {
        gitletExists();
        if (!commitFile.exists()) {
            exitWithError("No commit with that id exists.");
        }
        Commit currCom = Utils.readObject(commitFile, Commit.class);
        File cwdFile = Utils.join(CWD, file);
        Blob fileBlob = Utils.readObject(Utils.join(OBJECTS,
                currCom.getContents().get(cwdFile)), Blob.class);
        if (fileBlob == null) {
            exitWithError("File does not exist in that commit.");
        }
        Utils.writeContents(cwdFile, fileBlob.toString());
    }

    /** Reverts the CWD to the contents to the Commit of the given BRANCH. */
    public static void checkoutBranch(File branch) throws IOException {
        gitletExists();
        if (!branch.exists()) {
            exitWithError("No such branch exists.");
        }
        if (branch.getName().equals(Utils.readContentsAsString(CURR_BRANCH))) {
            exitWithError("No need to checkout the current branch");
        }
        Commit head = Utils.readObject(branch, Commit.class);

        Utils.writeObject(STAGING_HASH, new LinkedHashMap<File, Blob>());
        for (File file: STAGING_FOLDER.listFiles()) {
            file.delete();
        }
        for (File file: STAGING_REMOVE.listFiles()) {
            file.delete();
        }

        for (String strFile : Utils.plainFilenamesIn(CWD)) {
            Utils.join(CWD, strFile).delete();
        }
        for (String strBlob : head.getContents().values()) {
            Blob blob = Utils.readObject(
                    Utils.join(OBJECTS, strBlob), Blob.class);
            File blobFile = Utils.join(CWD, blob.parentFileName());
            Utils.writeContents(blobFile, blob.toString());
            blobFile.createNewFile();
        }
        Utils.writeContents(CURR_BRANCH, branch.getName());

    }

    /** Prints the Commit object associated with the given MSG. */
    public static void find(String msg) {
        gitletExists();
        String commitIds = "";
        for (String fileName : Utils.plainFilenamesIn(COMMITS)) {
            Commit currCom = Utils.readObject(Utils.join
                    (COMMITS, fileName), Commit.class);
            if (currCom.getMessage().equals(msg)) {
                commitIds += currCom.getHash() + "\n";
            }
        }
        if (commitIds.length() == 0) {
            exitWithError("Found no commit with that message.");
        }
        System.out.println(commitIds);
    }

    /** Removes FILE from being tracked in the staging area. */
    public static void remove(File file) throws IOException {
        gitletExists();
        LinkedHashMap<File, String> stage =
                Utils.readObject(STAGING_HASH, LinkedHashMap.class);
        Commit current = currComm();
        if (!stage.containsKey(file)
                && !current.getContents().containsKey(file)) {
            exitWithError("No reason to remove the file.");
        }

        Utils.join(STAGING_FOLDER, stage.get(file)).delete();
        stage.remove(file);
        Utils.writeObject(STAGING_HASH, stage);

        File newFile = Utils.join(STAGING_REMOVE, file.getName());
        newFile.createNewFile();
        File cwdFile = Utils.join(CWD, file.getName());
        if (cwdFile.exists()) {
            cwdFile.delete();
        }
    }

    /** Reverts current Commit to the Commit object labeled by ID. */
    public static void reset(String id) {
        gitletExists();
        File commFile = Utils.join(COMMITS, id);
        if (!commFile.exists()) {
            exitWithError("No commit with that id exists.");
        }
        Commit commit = Utils.readObject(commFile, Commit.class);

        if (CWD.listFiles().length != commit.getContents().size()) {
            exitWithError("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        for (String strFile : Utils.plainFilenamesIn(CWD)) {
            File file = Utils.join(CWD, strFile);
            String possBlob = commit.getContents().get(file);
            if (possBlob.equals(null)) {
                file.delete();
            } else {
                Blob fileBlob = Utils.readObject(
                        Utils.join(OBJECTS, possBlob), Blob.class);
                Utils.writeContents(file, fileBlob.toString());
            }
        }
    }

    /** Provides a status layout of the current Gitlet folder. */
    public static void status() {
        gitletExists();
        Formatter strFrm = new Formatter();
        String str = "";
        String head = Utils.readContentsAsString(CURR_BRANCH);
        str += "=== Branches ===\n";
        for (String file : Utils.plainFilenamesIn(REFS)) {
            if (file.equals(head)) {
                str += strFrm.format("*%1$s\n", file);
            } else {
                str += file + "\n";
            }

        }
        str += "\n=== Staged Files ===\n";
        ArrayList<String> files = new ArrayList<>();

        if (!Utils.plainFilenamesIn(STAGING_FOLDER).isEmpty()) {
            for (String blobFile : Utils.plainFilenamesIn(STAGING_FOLDER)) {
                Blob blob = Utils.readObject(Utils.join(
                        STAGING_FOLDER, blobFile), Blob.class);
                files.add(blob.parentFileName());
            }
            String[] filename = files.toArray(new String[0]);
            Arrays.sort(filename);
            for (int i = 0; i < filename.length; i++) {
                str += filename[i] + "\n";
            }

        }
        str += "\n=== Removed Files ===\n";
        if (!Utils.plainFilenamesIn(STAGING_REMOVE).isEmpty()) {
            for (String fileName : Utils.plainFilenamesIn(STAGING_REMOVE)) {
                str += fileName + "\n";
            }
        }
        str += "\n=== Modifications Not Staged For Commit "
                + "===\n\n=== Untracked Files ===\n";
        System.out.println(str);
    }

    /** Merges two commits, either on top of OTHER or head
     * branch, based on conditions. */
    public static void merge(Commit other) {
        gitletExists();
        Commit currComm = currComm();
        Commit splitPoint = splitPoint(currComm, other);
    }

    /** Finds the split point between two Commit objects, HEAD and OTHER.
     * @return Commit */
    public static Commit splitPoint(Commit head, Commit other) {
        ArrayDeque<String> headBranch = new ArrayDeque<>();
        HashSet<String> otherBranch = new HashSet<>();
        String splitPoint = "";

        while (head.getParent() != null) {
            headBranch.add(head.getHash());
            head = Utils.readObject(Utils.join(COMMITS,
                    head.getParent()), Commit.class);
        }
        headBranch.add(head.getHash());

        while (other.getParent() != null) {
            otherBranch.add(other.getHash());
            other = Utils.readObject(Utils.join(COMMITS,
                    other.getParent()), Commit.class);
        }
        otherBranch.add(other.getHash());

        while (!headBranch.isEmpty()) {
            String id = headBranch.pop();
            Commit currComm = Utils.readObject(
                    Utils.join(COMMITS, id), Commit.class);
            if (otherBranch.contains(id)
                    || currComm.getMergeParent() != null) {
                splitPoint = id;
                break;
            }
        }
        return Utils.readObject(Utils.join(COMMITS, splitPoint), Commit.class);
    }

    /** Returns the latest commit from the current branch.
     * @return Commit */
    public static Commit currComm() {
        File head = Utils.join(REFS, Utils.readContentsAsString(CURR_BRANCH));
        return Utils.readObject(head, Commit.class);
    }

    /** Handles failure cases, printing MESSAGE before exiting. */
    public static void exitWithError(String message) {
        System.out.println(message);
        System.exit(0);
    }

    /** Checks format of given arguments in Main, making
     * sure that the given CMD contains ARGS the length of N. */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            exitWithError("Incorrect operands.");
        }
    }

    /** Checks if the Gitlet directory exists. */
    public static void gitletExists() {
        if (!GITLET_FOLDER.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
    }

    /** Path to CWD. */
    static final File CWD = new File(".");

    /** Gitlet Folder. */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /** Commit Folder. */
    static final File COMMITS = Utils.join(GITLET_FOLDER, ".commits");

    /** Folder keeping track of branch pointers. */
    static final File REFS = Utils.join(GITLET_FOLDER, ".refs");

    /** Folder to keep track of the current branch. */
    static final File CURR_BRANCH = Utils.join(GITLET_FOLDER, ".branch");

    /** Folder that holds all blob objects. */
    static final File OBJECTS = Utils.join(GITLET_FOLDER, ".objects");

    /** Folder holding all blobs waiting to be staged. */
    static final File STAGING_FOLDER = Utils.join(GITLET_FOLDER, ".staging");

    /** Keeps track of the LinkedHashMap that keeps track of staging. */
    static final File STAGING_HASH = Utils.join(GITLET_FOLDER, ".stage");

    /** Files to remove. */
    static final File STAGING_REMOVE = Utils.join(GITLET_FOLDER, ".remove");
}