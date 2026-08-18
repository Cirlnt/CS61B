package gitlet;


import java.io.Serializable;
import java.io.File;
import java.util.Date;

import static gitlet.Utils.sha1;

/** Represents a gitlet commit object.
 *  TODO: 最好在这里描述一下这个类（Class）还有什么其他功能。
 *  does at a high level.
 *
 *  @author Cirlnt
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * 该提交的提交信息。时间戳，tree，父指针
     */
    private String message;
    private Date timestamp;
    private String treeID;    // 指向的树ID，初始提交为 null
    private String parentID;  // 父提交ID，初始提交为 null

    //默认为初始提交，message为initial commit
    public Commit(String message, Date timestamp, String treeID, String parentID) {
        this.message = message;
        this.timestamp = timestamp;
        this.treeID = treeID;
        this.parentID = parentID;
        //HEAD指向当前分支eg:master，当前分支指向最新的commit,最新commit里面的tree为旧的commit里的tree加上stage里面的,这里的commit为旧的,因为这个函数叫构造函数

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append("\n");
        sb.append(message).append("\n");
        sb.append(parentID).append("\n");
        sb.append(treeID).append("\n");
        return sb.toString();
    }

    public String getHash(){
        return sha1(toString());
    }


    public String getTreeID() {
        return treeID;
    }
}
