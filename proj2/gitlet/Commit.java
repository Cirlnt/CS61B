package gitlet;


import java.io.Serializable;
import java.io.File;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import static gitlet.Utils.sha1;

/** Represents a gitlet commit object.
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


    public String getParentID() {
        return parentID;
    }

    public void printCommit(){
        Formatter fmt = new Formatter(Locale.ENGLISH);
        fmt.format("%1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz", timestamp);
//        fmt.format("%ta %tb %te %tH:%tM:%tS %tY %tz", timestamp);
        String s = fmt.toString();
        System.out.println("===");
        System.out.println("commit" + " " +getHash());
        System.out.println("Date:" + " " + s);
        System.out.println(message);
        System.out.println();
    }

    public String getMessage(){
        return message;
    }

}
