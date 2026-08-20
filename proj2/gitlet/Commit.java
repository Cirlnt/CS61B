package gitlet;


import java.io.Serializable;
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
     * 该提交的提交信息。时间戳，tree，父指针
     */
    private String message;
    private Date timestamp;
    private String treeID;    // 指向的树ID，初始提交为 null
    private String parentID;  // 父提交ID，初始提交为 null
    private String parentID2;

    // 默认为初始提交，message 为 initial commit
    public Commit(String message, Date timestamp, String treeID, String parentID) {
        this.message = message;
        this.timestamp = timestamp;
        this.treeID = treeID;
        this.parentID = parentID;
    }

    // 设置第二个父提交
    public void setParent2(String parent2) {
        this.parentID2 = parent2;
    }

    public String getSecondParentID() {
        return parentID2;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append("\n");
        sb.append(message).append("\n");
        sb.append(parentID).append("\n");
        sb.append(treeID).append("\n");
        return sb.toString();
    }

    public String getHash() {
        return sha1(toString());
    }

    public String getTreeID() {
        return treeID;
    }

    public String getParentID() {
        return parentID;
    }

    public void printCommit() {
        Formatter fmt = new Formatter(Locale.ENGLISH);
        fmt.format("%1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz", timestamp);
        String s = fmt.toString();
        System.out.println("===");
        System.out.println("commit" + " " + getHash());
        if (parentID2 != null) {
            System.out.println("Merge: " + parentID.substring(0, 7) + " "
                    + parentID2.substring(0, 7));
        }
        System.out.println("Date:" + " " + s);
        System.out.println(message);
        System.out.println();
    }

    public String getMessage() {
        return message;
    }

}
