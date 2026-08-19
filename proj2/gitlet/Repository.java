package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Cirlnt
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    //将所有路径创造
    /**
     * 当前工作目录。
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * .gitlet 隐藏目录（用于存储仓库元数据）。
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**
     * objects 里面放commits，trees和blobs
     */
    public final File objects = Utils.join(GITLET_DIR, "objects");
    /**
     * commits文件夹里面放多个commits，blobs文件夹里面放多个blobs，trees文件里面放
     */
    public final File trees = Utils.join(objects, "trees");
    public final File commits = Utils.join(objects, "commits");
    public final File blobs = Utils.join(objects, "blobs");
    /**
     * HEAD储存当前提交的name，随用随取，随取随读，随读随改，随改随存，，branches里面存分支表，每个branch文件的名字为name，里面装id。
     */
    public final File HEAD = Utils.join(GITLET_DIR, "HEAD");
    public final File branches = Utils.join(GITLET_DIR, "branches");
    /**
     * Stages为暂存区，里面分为addStage和removeStage
     */
    public final File stages = Utils.join(GITLET_DIR, "stages");
    public final File addstages = Utils.join(stages, "addstages");
    public final File removestages = Utils.join(stages, "removestages");

    /**
     * 定义需要创建的目录结构
     */
    private static final String[][] SUB_DIRS = {
            {"objects", "commits"},
            {"objects", "blobs"},
            {"objects", "trees"},
//            {"stages", "addstages"},
//            {"stages", "removestages"},
            {"stages"},
            {"branches"}
    };

    /**
     * 如果 .gitlet 及其子目录不存在，则统一创建
     */
    private void setup() {
        if (!GITLET_DIR.exists()) {
            if (!GITLET_DIR.mkdirs()) {
                throw new RuntimeException("Could not create directory " + GITLET_DIR);
            }
        }
        // 循环批量创建子目录
        for (String[] pathParts : SUB_DIRS) {
            File dir = Utils.join(GITLET_DIR, pathParts); // Utils.join 通常支持可变参数
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new RuntimeException("Could not create directory: " + dir);
                }
            }
        }

    }


    public void init() {
        //如果不存在就set
        if (!GITLET_DIR.exists()) {
            setup();
            Commit commit = new Commit("initial commit", new Date(0), null, null);
            String commitID = commit.getHash();
            writeObject(Utils.join(commits, commitID), commit); //将本次commit写入对应文件
            writeContents(HEAD, "master"); //HEAD指向当前分支name
            writeContents(Utils.join(branches, "master"), commitID); //写入branches组里面对于分支master
            writeObject(addstages, new TreeMap<String, String>());
            writeObject(removestages, new TreeMap<String, String>());
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(1);
        }

    }

    public void add(String fileName) {
        if (!GITLET_DIR.exists()) {
            System.out.println("File does not exist.");
            System.exit(1);
        }
        File file = new File(fileName); //这是即将add的文件，如果当前工作版本的文件与当前提交（commit）中的版本完全相同，则不要将其暂存
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(1);
        }
        String hash = fileHash(file);
        TreeMap<String,String> addMap = readObject(addstages,TreeMap.class);  //addstages里面的Map
        TreeMap<String,String> oldMap = researchOldTree();
        TreeMap<String,String> rmMap = readObject(removestages,TreeMap.class);
        if (oldMap.containsKey(fileName) && oldMap.get(fileName).equals(hash)) {
            addMap.remove(fileName);   // 和当前 commit 相同 → 不暂存
            rmMap.remove(fileName);
        } else {
            addMap.put(fileName, hash);
            writeContents(Utils.join(blobs, hash), readContents(file));
        }
        writeObject(addstages, addMap);
        writeObject(removestages, rmMap);

    }

    public void rm(String fileName) {
        int is = 0;
        File file = new File(fileName); //这是即将rm的文件
        //如果该文件当前已被暂存（staged for addition），则取消暂存。
        //如果该文件在当前提交（current commit）中被追踪（tracked），则将其暂存为待删除（stage it for removal）
        //并从工作目录中删除该文件（如果用户尚未手动删除的话）——但前提是该文件必须被当前提交追踪，否则不要删除它。
        TreeMap<String,String> rmMap = readObject(removestages,TreeMap.class);
        TreeMap<String,String> oldMap = researchOldTree();
        TreeMap<String,String> addMap = readObject(addstages,TreeMap.class);
        //String hash = fileHash(file);
        if (addMap.containsKey(fileName)) {
            addMap.remove(fileName);
            is = 1;
        }
        if (oldMap.containsKey(fileName)) {
            rmMap.put(fileName, oldMap.get(fileName)); //put里面的参数是filename, blobHash
            if (file.exists()) {
                file.delete();
            }
            is = 1;
        }
        if (is == 0){
            System.out.println("No reason to remove the file.");
        }
        writeObject(addstages, addMap);
        writeObject(removestages, rmMap);
    }

    public void commit(String Message) {
        String message =  Message;
        TreeMap<String,String> commitOldMap = researchOldTree();
        TreeMap<String,String> addMap = readObject(addstages,TreeMap.class);
        TreeMap<String,String> rmMap  = readObject(removestages, TreeMap.class);
        if (addMap.isEmpty() && rmMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }
        // 在旧树基础上合并新增/修改，并应用删除
        TreeMap<String,String> newTree = new TreeMap<>(commitOldMap);
        newTree.putAll(addMap);
        for (String name : rmMap.keySet()) {
            newTree.remove(name);
        }
        // 用完整新树生成 treeID，并写盘
        String treeID = treeHash(newTree);
        writeObject(Utils.join(trees, treeID), newTree);
        // 创建新提交
        String branchName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, branchName));
        Commit newcommit = new Commit(message, new Date(), treeID, commitID);
        writeObject(Utils.join(commits, newcommit.getHash()), newcommit);
        //提交完成后，暂存区会被清空
        writeObject(addstages, new TreeMap<String, String>());
        writeObject(removestages, new TreeMap<String, String>());
        //commit 命令执行后，新提交会作为新节点加入提交树。
        writeContents(Utils.join(branches, branchName), newcommit.getHash());
        //刚刚创建的提交成为"当前提交"，HEAD 指针现在指向它。之前的 HEAD 提交则成为这个新提交的父提交。
    }

    public void log(){
        //TODO: 从当前 head 指向的提交开始，沿着提交树反向显示每个提交的信息，一直追溯到初始提交。
        //todo：遍历过程中只跟随第一个父提交的链接，忽略合并提交中的第二个父提交。
        //TODO: 关于merge的操作还没写
        //需要commitID，timestamp，message
        String branchName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, branchName));
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        logHelper(commit);
    }

    public void globalLog(){
        List<String> commitIds = Utils.plainFilenamesIn(commits);
        for (String id : commitIds){
            Commit commit = readObject(Utils.join(commits, id), Commit.class);
            commit.printCommit();
        }
    }

    public void find(String message) {
        List<String> commitIds = Utils.plainFilenamesIn(commits);
        for (String id : commitIds){
            Commit commit = readObject(Utils.join(commits, id), Commit.class);
            if (commit.getMessage().equals(message)){
                System.out.println(id);
            }
        }
    }

    public void status(){
        //先写branches
        List<String> branchNames = Utils.plainFilenamesIn(branches);
        String HeadName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, HeadName));
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        System.out.println("=== Branches ===");
        for (String name : branchNames){
            if (name.equals(HeadName)){
                System.out.println("*"+name);
            }
            else{
                System.out.println(name);
            }
        }
        System.out.println();
        //然后写stages
        TreeMap<String,String> addMap = readObject(addstages,TreeMap.class);
        TreeMap<String,String> rmMap = readObject(removestages,TreeMap.class);
        TreeMap<String,String> oldMap = researchOldTree();
        System.out.println("=== Staged Files ===");
        for (String name : addMap.keySet()){
            System.out.println(name);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        for (String name : rmMap.keySet()){
            System.out.println(name);
        }
        System.out.println();
        //处理未暂存的修改
        System.out.println("=== Modifications Not Staged For Commit ===");
        //得到所有文件名——known
        TreeSet<String> known = new TreeSet<>();//或者直接
        known.addAll(oldMap.keySet());
        known.addAll(addMap.keySet());
        known.addAll(rmMap.keySet());
        //或者直接:
//        TreeSet<String> known = allFiles();

        TreeMap<String,String> mods = new TreeMap<>();   // key=文件名, value=modified/deleted，当然也可以放两个ListArray
        for (String f : known) {
            boolean tracked = oldMap.containsKey(f);
            boolean staged  = addMap.containsKey(f);
            boolean stagedRm = rmMap.containsKey(f);
            File file = new File(f);
            String workHash = file.exists() ? fileHash(file) : null;  // 不存在=null

            if (staged) {
                // 情况3：已暂存但工作目录被删
                if (workHash == null) {
                    mods.put(f, "deleted");
                }
                // 情况2：已暂存，但工作目录内容又变了
                else if (!addMap.get(f).equals(workHash)){
                    mods.put(f, "modified");
                }
            }
            else if (tracked && !stagedRm) {
                // 情况4：被追踪、没 rm、但工作目录被删
                if (workHash == null){
                    mods.put(f, "deleted");
                }
                    // 情况1：被追踪、没暂存、内容变了
                else if (!oldMap.get(f).equals(workHash)){
                    mods.put(f, "modified");
                }
            }
        }
        for (Map.Entry<String,String> e : mods.entrySet()) {
            System.out.println(e.getKey() + " (" + e.getValue() + ")");
        }
        System.out.println();
        //未跟踪文件->工作目录所有文件-已知(已跟踪)文件
        System.out.println("=== Untracked Files ===");
        for (String f : Utils.plainFilenamesIn(CWD)) {
            if (!known.contains(f)) {
                System.out.println(f);
            }
        }
        System.out.println();

    }

    /**  checkout -- [文件名]
     * 取出该文件在当前 head 提交中的版本，放到工作目录中。如果工作目录中已存在同名文件，则直接覆盖。新取出的文件不会被暂存。*/
    public void checkout1(String fileName){
        TreeMap<String,String> oldMap = researchOldTree();
        String BlobHash = oldMap.get(fileName); //当前head提交中的版本
        if (BlobHash == null){
            System.out.println("File does not exist in that commit.");
            return;
        }
        byte[] content = readContents(Utils.join(blobs, BlobHash));//文件内容
        File file = new File(fileName);                             // 工作目录
        writeContents(file, content);                               // 覆盖写回，不存在则创建
    }

    /** checkout [提交ID] -- [文件名]
     * 取出该文件在指定提交 ID 对应的提交中的版本，放到工作目录中。如果工作目录中已存在同名文件，则直接覆盖。新取出的文件不会被暂存。
     */
    public void checkout2(String commitID,String fileName){
        List<String> commitIds = Utils.plainFilenamesIn(commits);
        if (!commitIds.contains(commitID)){
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        TreeMap<String,String> Map = readObject(Utils.join(trees, commit.getTreeID()), TreeMap.class);
        String BlobHash = Map.get(fileName);
        if (BlobHash == null){
            System.out.println("File does not exist in that commit.");
            return;
        }
        byte[] content = readContents(Utils.join(blobs, BlobHash));//文件内容
        File file = new File(fileName);                             // 工作目录
        writeContents(file, content);
    }

    /**checkout [分支名]
     * 取出指定分支的 head 提交中的所有文件，放到工作目录中。如果工作目录中已存在同名文件，则直接覆盖。
     * 执行完该命令后，指定的分支将成为当前分支（HEAD）。
     * 当前分支中被跟踪、但在目标分支中不存在的文件会被删除。暂存区会被清空，除非 checkout 的目标分支就是当前分支
     */
    public void checkout3(String branchName){
        TreeSet<String> know = allFiles(); //.gitlet已知文件
        String HeadBranchName = readContentsAsString(HEAD);
        List<String> workFiles = Utils.plainFilenamesIn(CWD); //工作目录所有文件
        String targetcommitID = readContentsAsString(Utils.join(branches, branchName));
        Commit targetCommit = readObject(Utils.join(commits, targetcommitID), Commit.class);
        List<String> branchesNames = Utils.plainFilenamesIn(branches); //branch的所有文件名
        if (!branchesNames.contains(branchName)){
            System.out.println("No such branch exists.");
            return;
        }
        if (HeadBranchName.equals(branchName)){
            System.out.println("No need to checkout the current branch.");
            return;
        }
        TreeMap<String,String> targetTree;  //指定分支的 head 提交中的所有文件,也就是commit的对应tree
        if (targetCommit.getTreeID() == null) {
            targetTree = new TreeMap<>();
        } else {
            targetTree = readObject(Utils.join(trees, targetCommit.getTreeID()), TreeMap.class);
        }
        TreeMap<String,String> oldMap = researchOldTree(); //当前分支跟踪
        TreeMap<String,String> addMap = readObject(addstages, TreeMap.class);
        //工作目录里有一个文件（比如 hello.txt）
        //这个文件没有被当前分支跟踪（即从未被 add 和 commit 过）
        //你要 checkout 的目标分支里恰好也有一个同名文件 hello.txt
        for (String name : workFiles){
            if (!oldMap.containsKey(name)&&(addMap.containsKey(name))&& targetTree.containsKey(name)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (Map.Entry<String,String> e : targetTree.entrySet()){
            File file = new File(e.getKey());
            byte[] content = readContents(Utils.join(blobs, e.getValue()));
            writeContents(file, content);
        }
        for (String fileName : oldMap.keySet()){
            if (!targetTree.containsKey(fileName)){
                File file = new File(fileName);
                file.delete();
            }
        }
        writeObject(addstages, new TreeMap<String, String>());
        writeObject(removestages, new TreeMap<String, String>());
        writeContents(HEAD,branchName);
    }

    /**
     * 这里是获得treeID,HEAD指向当前提交分支，该提交分支指向最新commit的ID，最新commit里面的tree为旧的commit里的tree加上stage里面的
     * 也就是被跟踪的文件
     */
    private TreeMap<String,String> researchOldTree(){
        String branchName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, branchName));
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        if (commit.getTreeID() == null) {
            return new TreeMap<>();
        }
        return readObject(Utils.join(trees, commit.getTreeID()), TreeMap.class);
    }

    private String fileHash(File file){
        byte[] content = readContents(file);
        String hash = sha1(content);
        return hash;
    }

    private String treeHash(TreeMap<String,String> treeMap){
        StringBuilder sb = new StringBuilder();
        for (String key : treeMap.keySet()) {
            sb.append(key).append("=").append(treeMap.get(key));//防止可能出现的 "ab"+"c" 与 "a"+"bc"
        }
        String hash = sha1(sb.toString());
        return hash;
    }

    private void logHelper(Commit commit){
        //先打印当前提交
        commit.printCommit();
        //如果有父提交，继续递归
        String parentID = commit.getParentID();
        if (parentID != null) {
            Commit parentCommit = readObject(Utils.join(commits, parentID), Commit.class);
            logHelper(parentCommit);
        }
    }

    /** 工作目录中文件当前内容的 hash；文件不存在返回 null。 */
    private String workingHash(String fileName) {
        File f = new File(fileName);
        return f.exists() ? fileHash(f) : null;
    }

    /** 判断 fileName 在工作目录里的内容是否和 HEAD 追踪版本不同。 */
    private boolean modifiedSinceHead(String fileName) {
        TreeMap<String, String> headTree = researchOldTree();
        if (!headTree.containsKey(fileName)) {
            return false;              // HEAD 根本没追踪它，谈不上"和 HEAD 不同"
        }
        String headHash = headTree.get(fileName);   // HEAD 里存的 blob hash
        String workHash = workingHash(fileName);     // 工作目录当前内容的 hash
        return !headHash.equals(workHash);          // 内容变了，或文件被删(null != hash)
    }

    /** 获得.gitlet已知文件的所有文件名 */
    private TreeSet<String> allFiles(){
        TreeMap<String,String> addMap = readObject(addstages,TreeMap.class);
        TreeMap<String,String> rmMap = readObject(removestages,TreeMap.class);
        TreeMap<String,String> oldMap = researchOldTree();
        TreeSet<String> known = new TreeSet<>();
        known.addAll(oldMap.keySet());
        known.addAll(addMap.keySet());
        known.addAll(rmMap.keySet());
        return known;
    }




}


