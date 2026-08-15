package gitlet;

/** 包含分支的name和commit */
public class Branch {
    private String name;
    private String CommitID;

    public Branch(String name, String commitID) {
        this.name = name;
        this.CommitID = commitID;
    }

}
