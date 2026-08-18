package gitlet;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Cirlnt
 */
public class Repository {
    /**
     * TODO: add instance variables here.
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

    private void log(){

    }

    /**
     * 这里是获得treeID,HEAD指向当前提交分支，该提交分支指向最新commit的ID，最新commit里面的tree为旧的commit里的tree加上stage里面的
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



}


