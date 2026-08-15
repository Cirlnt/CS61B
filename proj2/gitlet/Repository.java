package gitlet;

import java.util.Date;
import java.io.File;
import java.io.IOException;

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
    public final File addStages = Utils.join(stages, "addstages");
    public final File removeStages = Utils.join(stages, "removestages");

    /**
     * 定义需要创建的目录结构
     */
    private static final String[][] SUB_DIRS = {
            {"objects", "commits"},
            {"objects", "blobs"},
            {"objects", "trees"},
            {"stages", "addstages"},
            {"stages", "removestages"},
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
            String commitID = sha1(serialize(commit));
            writeObject(Utils.join(commits, commitID), commit); //将本次commit写入对应文件
            writeContents(HEAD, "master");
            writeContents(Utils.join(branches, "master"), commitID);
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

    }

    //这里是获得treeID,HEAD指向当前提交分支，该提交分支指向最新commit，最新commit里面的tree为旧的commit里的tree加上stage里面的
}
