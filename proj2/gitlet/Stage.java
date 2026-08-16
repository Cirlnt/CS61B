package gitlet;

import java.util.Map;
import java.util.Set;

public class Stage {
    private String baseTreeId;  //存储上一个commit的旧tree的id
    private Map<String, String> addedFiles;  //存放本次新增 / 修改的「文件名→blob 哈希」
    private Set<String> removedFiles;  //存放待删除的文件名

    public Stage(String baseTreeId, Map<String, String> addedFiles, Set<String> removedFiles) {
        this.baseTreeId = baseTreeId;
        this.addedFiles = addedFiles;
        this.removedFiles = removedFiles;
    }
}
