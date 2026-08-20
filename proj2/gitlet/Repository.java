package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
            rmMap.remove(fileName);
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
        // 从当前 head 指向的提交开始，沿第一父提交反向遍历到初始提交；
        // 忽略合并提交的第二个父提交。
        String branchName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, branchName));
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        while (commit != null) {
            commit.printCommit();
            String parentID = commit.getParentID();
            commit = (parentID == null) ? null
                    : readObject(Utils.join(commits, parentID), Commit.class);
        }
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
    public void checkout2(String shortID,String fileName){
        String commitID = findCommitId(shortID);
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File commitFile = Utils.join(commits, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        TreeMap<String,String> map = researchTree(commit);
        String blobHash = map.get(fileName);
        if (blobHash == null){
            System.out.println("File does not exist in that commit.");
            return;
        }
        byte[] content = readContents(Utils.join(blobs, blobHash));//文件内容
        File file = new File(fileName);                             // 工作目录
        writeContents(file, content);
    }

    /**checkout [分支名]
     * 取出指定分支的 head 提交中的所有文件，放到工作目录中。如果工作目录中已存在同名文件，则直接覆盖。
     * 执行完该命令后，指定的分支将成为当前分支（HEAD）。
     * 当前分支中被跟踪、但在目标分支中不存在的文件会被删除。暂存区会被清空，除非 checkout 的目标分支就是当前分支
     */
    public void checkout3(String branchName){
        String headBranchName = readContentsAsString(HEAD);
        List<String> branchesNames = Utils.plainFilenamesIn(branches); //branch的所有文件名
        if (!branchesNames.contains(branchName)){
            System.out.println("No such branch exists.");
            return;
        }
        if (headBranchName.equals(branchName)){
            System.out.println("No need to checkout the current branch.");
            return;
        }
        String targetCommitID = readContentsAsString(Utils.join(branches, branchName));
        Commit targetCommit = readObject(Utils.join(commits, targetCommitID), Commit.class);
        TreeMap<String,String> targetTree = researchTree(targetCommit); //目标分支跟踪的文件
        TreeMap<String,String> oldMap = researchOldTree(); //当前分支跟踪的文件

        //工作目录里存在「当前分支未跟踪、但目标分支会覆盖」的文件 → 报错退出
        List<String> workFiles = Utils.plainFilenamesIn(CWD);
        for (String name : workFiles){
            if (!oldMap.containsKey(name) && targetTree.containsKey(name)){
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
                new File(fileName).delete();
            }
        }
        writeObject(addstages, new TreeMap<String, String>());
        writeObject(removestages, new TreeMap<String, String>());
        writeContents(HEAD, branchName);
    }

    public void branch(String branchName){
        List<String> branchesNames = Utils.plainFilenamesIn(branches);
        if (branchesNames.contains(branchName)){
            System.out.println("A branch with that name already exists.");
            return;
        }
        String HeadBranchName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, HeadBranchName));
        writeContents(Utils.join(branches, branchName), commitID);
    }

    public void rmBranch(String branchName){
        List<String> branchesNames = Utils.plainFilenamesIn(branches);
        if (!branchesNames.contains(branchName)){
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String HeadBranch = readContentsAsString(HEAD);
        if (HeadBranch.equals(branchName)){
            System.out.println("Cannot remove the current branch.");
            return;
        }
        File branchFile = Utils.join(branches, branchName);
        branchFile.delete();
    }

    //检出指定提交所跟踪的所有文件。删除那些不在该提交中、但被当前跟踪的文件。同时，将当前分支的 head 指针移动到该提交节点。
    public void reset(String shortID){
        String commitID = findCommitId(shortID);
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File commitFile = Utils.join(commits, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        TreeMap<String,String> map = researchTree(commit);//指定提交跟踪的文件
        TreeMap<String,String> oldMap = researchOldTree(); //被当前跟踪的文件
        //如果工作目录中存在一个在当前分支中未被跟踪的文件，而 reset 操作会覆盖它，则报错退出
        for (String fileName : map.keySet()){
            File workFile = new File(fileName);
            if (workFile.exists() && !oldMap.containsKey(fileName)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (String name : oldMap.keySet()){
            if (!map.containsKey(name)){
                new File(name).delete();
            }
        }
        for (String filename : map.keySet()) {
            String blobId = map.get(filename);
            byte[] content = readContents(Utils.join(blobs, blobId));
            writeContents(new File(filename), content);
        }

        String headBranchName = readContentsAsString(HEAD);
        writeContents(Utils.join(branches, headBranchName), commitID);
        writeObject(addstages, new TreeMap<String, String>());
        writeObject(removestages, new TreeMap<String, String>());
    }

    /**  将指定分支的文件合并到当前分支中。 */
    public void merge(String branchName){
        String headBranchName = readContentsAsString(HEAD);

        // 失败检查（顺序遵循规范）
        // 1. 存在已暂存的添加或删除 → You have uncommitted changes.
        TreeMap<String,String> addmap = readObject(addstages, TreeMap.class);
        TreeMap<String,String> removeMap = readObject(removestages, TreeMap.class);
        if (!addmap.isEmpty() || !removeMap.isEmpty()){
            System.out.println("You have uncommitted changes.");
            return;
        }
        // 2. 指定名称的分支不存在
        List<String> branchesNames = Utils.plainFilenamesIn(branches);
        if (!branchesNames.contains(branchName)){
            System.out.println("A branch with that name does not exist.");
            return;
        }
        // 3. 尝试将分支与自身合并
        if (headBranchName.equals(branchName)){
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        // 读取两个 head 的 commit 与分裂点
        String currentHeadID = readContentsAsString(Utils.join(branches, headBranchName));
        String givenHeadID   = readContentsAsString(Utils.join(branches, branchName));
        Commit currentCommit = readObject(Utils.join(commits, currentHeadID), Commit.class);
        Commit givenCommit   = readObject(Utils.join(commits, givenHeadID), Commit.class);
        String splitCommitID = findSplitPoint(currentHeadID, givenHeadID);
        Commit splitCommit = readObject(Utils.join(commits, splitCommitID), Commit.class);

        TreeMap<String,String> currentMap = researchTree(currentCommit);
        TreeMap<String,String> givenMap   = researchTree(givenCommit);
        TreeMap<String,String> splitMap   = researchTree(splitCommit);

        // 4. 未跟踪文件会被合并覆盖 → 报错退出
        List<String> workFiles = Utils.plainFilenamesIn(CWD);
        for (String fileName : workFiles){
            if (!currentMap.containsKey(fileName) && givenMap.containsKey(fileName)){
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
        }

        // 一、确定分裂点：首先需要确定当前分支和指定分支的分裂点。分裂点是当前分支和指定分支头部的最近公共祖先
//        String currentHeadID = readContentsAsString(Utils.join(branches, readContentsAsString(HEAD))); //当前分支
//        String givenHeadID   = readContentsAsString(Utils.join(branches, branchName)); //指定分支
//        String splitCommitID = findSplitPoint(currentHeadID, givenHeadID);
        // 二、特殊情况处理
        //1.如果分裂点与指定分支是同一个提交
        if (splitCommitID.equals(givenHeadID)){
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        //2.如果分裂点与当前分支是同一个提交：效果等同于检出指定分支
        if (splitCommitID.equals(currentHeadID)){
            String currentBranchName = readContentsAsString(HEAD);
            checkoutCommitFiles(givenHeadID);                                    // 检出文件（效果 = "check out the given branch
            writeContents(Utils.join(branches, currentBranchName), givenHeadID); // 当前分支指针前移
            writeObject(addstages, new TreeMap<String,String>());              // 清暂存区
            writeObject(removestages, new TreeMap<String,String>());
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        // 三、文件合并规则
        // 1.仅在指定分支中修改的文件,搬上去了
//        Commit currentCommit = readObject(Utils.join(commits, currentHeadID), Commit.class);//当前分支
//        Commit givenCommit = readObject(Utils.join(commits, givenHeadID), Commit.class);//指定分支
//        Commit splitCommit = readObject(Utils.join(commits, splitCommitID), Commit.class);
//
//        TreeMap<String,String> currentMap = readObject(Utils.join(trees, currentCommit.getTreeID()), TreeMap.class);//当前
//        TreeMap<String,String> givenMap = readObject(Utils.join(trees, givenCommit.getTreeID()), TreeMap.class);//指定
//        TreeMap<String,String> splitMap = readObject(Utils.join(branches, splitCommit.getTreeID()), TreeMap.class);


        TreeMap<String,String> addMap = new TreeMap<>();
        TreeMap<String,String> rmMap = new TreeMap<>();
        boolean hasConflict = false;

        TreeSet<String> allFiles = new TreeSet<>();
        allFiles.addAll(currentMap.keySet());
        allFiles.addAll(givenMap.keySet());
        allFiles.addAll(splitMap.keySet());

        for (String fileName : allFiles){
            String splitBlob = splitMap.get(fileName);     // 文件在分裂点不存在时为 null
            String currentBlob = currentMap.get(fileName); // 文件在当前分支不存在时为 null
            String givenBlob = givenMap.get(fileName);     // 文件在指定分支不存在时为 null
            //用Object避免空指针
            boolean givenChange = !Objects.equals(givenBlob, splitBlob); //指定
            boolean currentChange = !Objects.equals(currentBlob, splitBlob);//指定
            //1.仅在指定分支中修改的文件
            if (givenChange && !currentChange&& givenBlob != null){
                byte[] content = readContents(Utils.join(blobs, givenBlob));
                Utils.writeContents(new File(fileName), content);
                currentMap.put(fileName, givenBlob);
                addMap.put(fileName, givenMap.get(fileName));
            }
            //2.仅在当前分支中修改的文件->currentChange && !givenChange不变
            else if (currentChange && !givenChange){
                continue;
            }
            //3.两个分支以相同方式修改的文件
            else if (currentChange && givenChange && Objects.equals(currentBlob, givenBlob)){
                continue;
            }
            //4.分裂点不存在、仅在当前分支存在的文件
            else if (splitBlob==null && currentBlob!=null && givenBlob ==null){
                continue;
            }
            //5.分裂点不存在、仅在指定分支存在的文件
            else if (splitBlob==null && currentBlob==null && givenBlob !=null){
                byte[] content = readContents(Utils.join(blobs, givenBlob));
                Utils.writeContents(new File(fileName), content);
                currentMap.put(fileName, givenBlob);
                addMap.put(fileName, givenMap.get(fileName));
            }
            //6.分裂点存在、当前分支未修改、指定分支中已删除的文件 -> 应被删除（并取消跟踪）
            else if (splitBlob!=null && !currentChange && givenBlob ==null){
                File workFile = new File(fileName);
                if (workFile.exists()) {
                    workFile.delete();
                }
                currentMap.remove(fileName);
                rmMap.put(fileName, splitBlob);
            }
            //7.分裂点存在、指定分支未修改、当前分支中已删除的文件
            else if (splitBlob!=null && !givenChange && currentBlob ==null){
                continue;
            }
            // 四、冲突处理
            else{
                hasConflict = true;
                String currentContent = (currentBlob == null)
                        ? "" : new String(readContents(Utils.join(blobs, currentBlob)), StandardCharsets.UTF_8);
                String givenContent = (givenBlob == null)
                        ? "" : new String(readContents(Utils.join(blobs, givenBlob)), StandardCharsets.UTF_8);
                String conflictContent = "<<<<<<< HEAD\n"
                        + currentContent + "\n"
                        + "=======\n"
                        + givenContent + "\n"
                        + ">>>>>>>\n";
                byte[] conflictBytes = conflictContent.getBytes(StandardCharsets.UTF_8);
                writeContents(new File(fileName), conflictBytes);
                String conflictBlob = sha1(conflictBytes);
                writeContents(Utils.join(blobs, conflictBlob), conflictBytes);
                currentMap.put(fileName, conflictBlob);
                addMap.put(fileName, conflictBlob);
            }
        }
        // 冲突：暂存结果但不自动提交，让用户解决后手动 commit
        if (hasConflict){
            writeObject(addstages, addMap);
            writeObject(removestages, rmMap);
            System.out.println("Encountered a merge conflict.");
            return;
        }

        // 合并未产生任何更改 → 输出正常提交错误
        if (addMap.isEmpty() && rmMap.isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }

        // 自动提交合并
        String logMessage = "Merged " + branchName + " into " + headBranchName + ".";
        String treeID = treeHash(currentMap);
        writeObject(Utils.join(trees, treeID), currentMap);
        Commit mergeCommit = new Commit(logMessage, new Date(), treeID, currentHeadID);
        mergeCommit.setParent2(givenHeadID);
        String mergeCommitID = mergeCommit.getHash();
        writeObject(Utils.join(commits, mergeCommitID), mergeCommit);
        writeContents(Utils.join(branches, headBranchName), mergeCommitID);
        writeObject(addstages, new TreeMap<String,String>());
        writeObject(removestages, new TreeMap<String,String>());
    }

    /**
     * 这里是获得treeID,HEAD指向当前提交分支，该提交分支指向最新commit的ID，最新commit里面的tree为旧的commit里的tree加上stage里面的
     * 也就是被跟踪的文件
     */
    private TreeMap<String,String> researchOldTree(){
        String branchName = readContentsAsString(HEAD);
        String commitID = readContentsAsString(Utils.join(branches, branchName));
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        return researchTree(commit);
    }

    /** 返回某个 commit 对应的 tree；初始提交 treeID 为 null，返回空 map。 */
    private TreeMap<String,String> researchTree(Commit commit){
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

    private String findCommitId(String shortId) {
        // 如果已经是完整 ID（40位），直接返回
        if (shortId.length() == 40) {
            return shortId;
        }

        List<String> commitIds = Utils.plainFilenamesIn(commits);
        String matched = null;

        for (String commitId : commitIds) {
            if (commitId.startsWith(shortId)) {
                if (matched != null) {
                    // 匹配到多个，不唯一
                    return null;
                }
                matched = commitId;
            }
        }
        return matched;
    }

    /** 把 commitID 对应提交的所有文件检出到工作目录。
     *  只铺文件，不碰 HEAD、不碰分支指针、不清暂存。 */
    private void checkoutCommitFiles(String commitID) {
        Commit commit = readObject(Utils.join(commits, commitID), Commit.class);
        TreeMap<String,String> targetTree = researchTree(commit);
        // 1. 写目标树里的所有文件
        for (Map.Entry<String,String> e : targetTree.entrySet()) {
            writeContents(new File(e.getKey()), readContents(Utils.join(blobs, e.getValue())));
        }
        // 2. 删除「当前分支追踪、但目标树没有」的文件
        TreeMap<String,String> currentTree = researchOldTree();
        for (String f : currentTree.keySet()) {
            if (!targetTree.containsKey(f)) {
                new File(f).delete();
            }
        }
    }


    /** 返回 commit 的所有父提交 id（merge 提交有两个父，普通提交一个）。 */
    private List<String> parentsOf(Commit c) {
        List<String> parents = new ArrayList<>();
        if (c.getParentID() != null) {
            parents.add(c.getParentID());
        }
        if (c.getSecondParentID() != null) {
            parents.add(c.getSecondParentID());
        }
        return parents;
    }

    /** 从 headCommitID 出发 BFS，返回 map：commit id -> 到 head 的最短距离。 */
    private Map<String,Integer> bfsDistances(String headCommitID) {
        Map<String,Integer> dist = new HashMap<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        dist.put(headCommitID, 0);
        queue.add(headCommitID);
        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            Commit c = readObject(Utils.join(commits, id), Commit.class);
            for (String parent : parentsOf(c)) {
                if (!dist.containsKey(parent)) {        // 没访问过 = 第一次到达 = 最短
                    dist.put(parent, dist.get(id) + 1);
                    queue.add(parent);
                }
            }
        }
        return dist;
    }

    /** 找 currentHead 和 givenHead 的分裂点（最近公共祖先）。 */
    private String findSplitPoint(String currentHeadID, String givenHeadID) {
        Map<String,Integer> distA = bfsDistances(currentHeadID);
        Map<String,Integer> distB = bfsDistances(givenHeadID);
        String split = null;
        int best = Integer.MAX_VALUE;
        for (String id : distA.keySet()) {
            if (distB.containsKey(id)) {                // 共同祖先
                int sum = distA.get(id) + distB.get(id);
                if (sum < best) {
                    best = sum;
                    split = id;
                }
            }
        }
        return split;
    }






}


